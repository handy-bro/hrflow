package com.hrflow.hrflow_backend.service;

import com.hrflow.hrflow_backend.config.AttendancePolicyProperties;
import com.hrflow.hrflow_backend.dto.attendance.*;
import com.hrflow.hrflow_backend.entity.AttendanceRecord;
import com.hrflow.hrflow_backend.entity.Employee;
import com.hrflow.hrflow_backend.entity.LeaveRequest;
import com.hrflow.hrflow_backend.enums.AttendanceStatus;
import com.hrflow.hrflow_backend.enums.LeaveStatus;
import com.hrflow.hrflow_backend.exceptionHandler.attendance.AlreadyCheckedInException;
import com.hrflow.hrflow_backend.exceptionHandler.attendance.AlreadyCheckedOutException;
import com.hrflow.hrflow_backend.exceptionHandler.attendance.NotCheckedInException;
import com.hrflow.hrflow_backend.exceptionHandler.employees.EmployeeNotFoundException;
import com.hrflow.hrflow_backend.repository.AttendanceRecordRepository;
import com.hrflow.hrflow_backend.repository.EmployeeRepository;
import com.hrflow.hrflow_backend.repository.LeaveRequestRepository;
import com.hrflow.hrflow_backend.utils.WorkingDaysCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRecordRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final AttendancePolicyProperties policy;

    // ==================================================================
    // Check-in
    // ==================================================================

    @Transactional
    public CheckInResponse checkIn(Long employeeId) {

        Employee employee = getEmployeeOrThrow(employeeId);
        LocalDate today = LocalDate.now();

        attendanceRepository.findByEmployeeIdAndWorkDate(employeeId, today)
                .ifPresent(existing -> {
                    throw new AlreadyCheckedInException("Already checked in today");
                });

        LocalDateTime now = LocalDateTime.now();
        AttendanceStatus status = computeStatus(employee, now);

        AttendanceRecord record = AttendanceRecord.builder()
                .employee(employee)
                .workDate(today)
                .checkInAt(now)
                .status(status)
                .build();

        attendanceRepository.save(record);

        return new CheckInResponse(
                record.getId(),
                record.getWorkDate(),
                record.getCheckInAt(),
                record.getStatus()
        );
    }

    @Transactional
    public CheckOutResponse checkOut(Long employeeId) {

        LocalDate today = LocalDate.now();

        AttendanceRecord record = attendanceRepository.findByEmployeeIdAndWorkDate(employeeId, today)
                .orElseThrow(() -> new NotCheckedInException("You must check in before checking out"));

        if (record.getCheckOutAt() != null) {
            throw new AlreadyCheckedOutException("Already checked out today");
        }

        LocalDateTime now = LocalDateTime.now();
        record.setCheckOutAt(now);
        record.setWorkedMinutes((int) Duration.between(record.getCheckInAt(), now).toMinutes());

        attendanceRepository.save(record);

        return new CheckOutResponse(
                record.getId(),
                record.getWorkDate(),
                record.getCheckInAt(),
                record.getCheckOutAt(),
                record.getWorkedMinutes(),
                record.getStatus());
    }

    private AttendanceStatus computeStatus(Employee employee, LocalDateTime checkInAt) {
        LocalTime latestOnTime = employee.getExpectedStartTime().plusMinutes(policy.gracePeriodMinutes());
        return checkInAt.toLocalTime().isAfter(latestOnTime) ? AttendanceStatus.LATE : AttendanceStatus.PRESENT;
    }

    // ==================================================================
    // Monthly report
    // ==================================================================

    @Transactional(readOnly = true)
    public MonthlyAttendanceReportResponse getMonthlyReport(Long employeeId, int year, int month) {

        Employee employee = getEmployeeOrThrow(employeeId);
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate monthStart = yearMonth.atDay(1);
        LocalDate monthEnd = yearMonth.atEndOfMonth();

        LocalDate effectiveEnd = monthEnd.isAfter(LocalDate.now()) ? LocalDate.now() : monthEnd;

        List<AttendanceRecord> records = attendanceRepository
                .findByEmployeeIdAndWorkDateBetweenOrderByWorkDateAsc(employeeId, monthStart, monthEnd);

        List<LeaveRequest> approvedLeaves = leaveRequestRepository.findOverlapping(employeeId, monthStart, monthEnd)
                .stream()
                .filter(lr -> lr.getStatus() == LeaveStatus.APPROVED)
                .toList();

        List<AttendanceDayResponse> days = buildDailyBreakdown(
                employee, monthStart, effectiveEnd, records, approvedLeaves);

        int presentDays = countByStatus(days, AttendanceStatus.PRESENT);
        int lateDays = countByStatus(days, AttendanceStatus.LATE);
        int absentDays = countByStatus(days, AttendanceStatus.ABSENT);
        int onLeaveDays = countByStatus(days, AttendanceStatus.ON_LEAVE);

        int totalWorkedMinutes = records.stream()
                .filter(r -> r.getWorkedMinutes() != null)
                .mapToInt(AttendanceRecord::getWorkedMinutes)
                .sum();
        BigDecimal workedHours = BigDecimal.valueOf(totalWorkedMinutes)
                .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);

        int expectedWorkingDays = WorkingDaysCalculator.countWorkingDays(monthStart, effectiveEnd) - onLeaveDays;
        BigDecimal contractualHours = employee.getContractualHoursPerDay()
                .multiply(BigDecimal.valueOf(Math.max(expectedWorkingDays, 0)));

        return new MonthlyAttendanceReportResponse(
                employee.getId(),
                employee.getFirstName() + " " + employee.getLastName(),
                year, month,
                contractualHours,
                workedHours,
                workedHours.subtract(contractualHours),
                presentDays,
                lateDays,
                absentDays,
                onLeaveDays,
                days
        );
    }

    private List<AttendanceDayResponse> buildDailyBreakdown(
            Employee employee, LocalDate start, LocalDate end,
            List<AttendanceRecord> records, List<LeaveRequest> approvedLeaves) {

        return start.datesUntil(end.plusDays(1))
                .filter(date -> date.getDayOfWeek() != DayOfWeek.SATURDAY
                        && date.getDayOfWeek() != DayOfWeek.SUNDAY)
                .map(date -> {
                    AttendanceRecord record = records.stream()
                            .filter(r -> r.getWorkDate().equals(date))
                            .findFirst()
                            .orElse(null);

                    if (record != null) {
                        return new AttendanceDayResponse(
                                date, record.getCheckInAt(), record.getCheckOutAt(),
                                record.getWorkedMinutes(), record.getStatus());
                    }

                    boolean onLeave = approvedLeaves.stream()
                            .anyMatch(lr -> !date.isBefore(lr.getStartDate()) && !date.isAfter(lr.getEndDate()));

                    AttendanceStatus status = onLeave ? AttendanceStatus.ON_LEAVE : AttendanceStatus.ABSENT;
                    return new AttendanceDayResponse(date, null, null, null, status);
                })
                .toList();
    }

    private int countByStatus(List<AttendanceDayResponse> days, AttendanceStatus status) {
        return (int) days.stream().filter(d -> d.status() == status).count();
    }

    @Transactional(readOnly = true)
    public List<AttendanceDayResponse> getTodayForDepartment(Long departmentId) {
        return attendanceRepository.findByDepartmentAndDate(departmentId, LocalDate.now()).stream()
                .map(r -> new AttendanceDayResponse(
                        r.getWorkDate(),
                        r.getCheckInAt(),
                        r.getCheckOutAt(),
                        r.getWorkedMinutes(),
                        r.getStatus()))
                .toList();
    }

    private Employee getEmployeeOrThrow(Long employeeId) {
        return employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found"));
    }
}