package com.hrflow.hrflow_backend.service;

import com.hrflow.hrflow_backend.dto.dashboard.*;
import com.hrflow.hrflow_backend.entity.Employee;
import com.hrflow.hrflow_backend.entity.LeaveRequest;
import com.hrflow.hrflow_backend.enums.AttendanceStatus;
import com.hrflow.hrflow_backend.enums.LeaveStatus;
import com.hrflow.hrflow_backend.repository.*;
import com.hrflow.hrflow_backend.storage.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private static final int CONTRACT_EXPIRY_WINDOW_DAYS = 30;
    private static final int LEAVE_URGENT_WAIT_DAYS = 3;

    private final EmployeeRepository employeeRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final StorageService storageService;

    // ==================================================================
    // Main Summary
    // ==================================================================

    @Transactional(readOnly = true)
    public DashboardSummaryResponse getSummary() {
        LocalDate today = LocalDate.now();
        LocalDate lastMonthEnd = today.minusMonths(1).withDayOfMonth(
                today.minusMonths(1).lengthOfMonth());

        long totalActive = employeeRepository.countActiveAsOf(today, LocalDateTime.now());
        long lastMonthActive = employeeRepository.countActiveAsOf(lastMonthEnd, lastMonthEnd.atTime(23, 59));

        double growthPercent = lastMonthActive == 0
                ? 0.0
                : ((double) (totalActive - lastMonthActive) / lastMonthActive) * 100;

        long pendingLeaves = leaveRequestRepository.countByStatus(LeaveStatus.PENDING);
        long urgentLeaves = leaveRequestRepository.findAllPending().stream()
                .filter(this::isUrgent)
                .count();

        YearMonth currentMonth = YearMonth.from(today);
        long newEmployees = employeeRepository.findHiredBetween(
                currentMonth.atDay(1), currentMonth.atEndOfMonth()).size();

        return new DashboardSummaryResponse(
                totalActive, lastMonthActive,
                Math.round(growthPercent * 100.0) / 100.0,
                pendingLeaves, urgentLeaves, newEmployees
        );
    }

    // ==================================================================
    // Department Distribution (Pie Chart)
    // ==================================================================

    @Transactional(readOnly = true)
    public List<DepartmentDistributionEntry> getDepartmentDistribution() {
        return employeeRepository.countByDepartment().stream()
                .map(row -> new DepartmentDistributionEntry(
                        (Long) row[0], (String) row[1], (Long) row[2]))
                .toList();
    }

    // ==================================================================
    // Workforce Evolution Over 12 Months (Line Chart)
    // ==================================================================

    @Transactional(readOnly = true)
    public List<WorkforceTrendEntry> getWorkforceTrend() {
        LocalDate today = LocalDate.now();
        List<WorkforceTrendEntry> trend = new ArrayList<>();

        for (int i = 11; i >= 0; i--) {
            YearMonth month = YearMonth.from(today.minusMonths(i));
            LocalDate monthEnd = month.atEndOfMonth().isAfter(today) ? today : month.atEndOfMonth();
            long activeCount = employeeRepository.countActiveAsOf(monthEnd, monthEnd.atTime(23, 59));
            trend.add(new WorkforceTrendEntry(month.getYear(), month.getMonthValue(), activeCount));
        }

        return trend;
    }

    // ==================================================================
    // Monthly Attendance Rate by Department (Bar Chart)
    // ==================================================================

    @Transactional(readOnly = true)
    public List<DepartmentAttendanceRateEntry> getDepartmentAttendanceRates(int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        List<Object[]> rows = attendanceRecordRepository.countStatusByDepartment(
                yearMonth.atDay(1), yearMonth.atEndOfMonth());

        Map<Long, String> names = new LinkedHashMap<>();
        Map<Long, Map<AttendanceStatus, Long>> counts = new LinkedHashMap<>();

        for (Object[] row : rows) {
            Long deptId = (Long) row[0];
            String deptName = (String) row[1];
            AttendanceStatus status = (AttendanceStatus) row[2];
            Long count = (Long) row[3];

            names.putIfAbsent(deptId, deptName);
            counts.computeIfAbsent(deptId, k -> new EnumMap<>(AttendanceStatus.class))
                    .merge(status, count, Long::sum);
        }

        return names.entrySet().stream()
                .map(entry -> {
                    Long deptId = entry.getKey();
                    Map<AttendanceStatus, Long> statusCounts = counts.getOrDefault(deptId, Map.of());

                    long present = statusCounts.getOrDefault(AttendanceStatus.PRESENT, 0L);
                    long late = statusCounts.getOrDefault(AttendanceStatus.LATE, 0L);
                    long absent = statusCounts.getOrDefault(AttendanceStatus.ABSENT, 0L);
                    long total = present + late + absent;

                    double rate = total == 0 ? 0.0 : ((double) (present + late) / total) * 100;

                    return new DepartmentAttendanceRateEntry(
                            deptId, entry.getValue(), Math.round(rate * 100.0) / 100.0);
                })
                .toList();
    }

    // ==================================================================
    // Alerts (Birthdays, Contracts, Pending Leaves)
    // ==================================================================

    @Transactional(readOnly = true)
    public DashboardAlertsResponse getAlerts() {
        LocalDate today = LocalDate.now();

        List<BirthdayAlertEntry> birthdays = employeeRepository
                .findBirthdaysOn(today.getMonthValue(), today.getDayOfMonth())
                .stream()
                .map(e -> new BirthdayAlertEntry(
                        e.getId(),
                        e.getFirstName() + " " + e.getLastName(),
                        photoUrlOrNull(e),
                        e.getBirthDate()))
                .toList();

        List<ContractExpiryAlertEntry> expiring = employeeRepository
                .findContractsExpiringBetween(today, today.plusDays(CONTRACT_EXPIRY_WINDOW_DAYS))
                .stream()
                .map(e -> new ContractExpiryAlertEntry(
                        e.getId(),
                        e.getFirstName() + " " + e.getLastName(),
                        e.getContractEndDate(),
                        ChronoUnit.DAYS.between(today, e.getContractEndDate())))
                .toList();

        List<PendingLeaveAlertEntry> pendingLeaves = leaveRequestRepository.findAllPending().stream()
                .map(this::toPendingLeaveAlert)
                .toList();

        return new DashboardAlertsResponse(birthdays, expiring, pendingLeaves);
    }

    private PendingLeaveAlertEntry toPendingLeaveAlert(LeaveRequest lr) {
        long daysWaiting = ChronoUnit.DAYS.between(lr.getCreatedAt().toLocalDate(), LocalDate.now());
        return new PendingLeaveAlertEntry(
                lr.getId(),
                lr.getEmployee().getId(),
                lr.getEmployee().getFirstName() + " " + lr.getEmployee().getLastName(),
                lr.getLeaveType(),
                lr.getStartDate(),
                lr.getEndDate(),
                daysWaiting,
                isUrgent(lr)
        );
    }

    private boolean isUrgent(LeaveRequest lr) {
        long daysWaiting = ChronoUnit.DAYS.between(lr.getCreatedAt().toLocalDate(), LocalDate.now());
        long daysUntilStart = ChronoUnit.DAYS.between(LocalDate.now(), lr.getStartDate());
        return daysWaiting > LEAVE_URGENT_WAIT_DAYS || (daysUntilStart >= 0 && daysUntilStart <= LEAVE_URGENT_WAIT_DAYS);
    }

    // ==================================================================
    // New Employees of the Month
    // ==================================================================

    @Transactional(readOnly = true)
    public List<NewEmployeeEntry> getNewEmployeesThisMonth() {
        YearMonth currentMonth = YearMonth.now();

        return employeeRepository.findHiredBetween(currentMonth.atDay(1), currentMonth.atEndOfMonth())
                .stream()
                .map(e -> new NewEmployeeEntry(
                        e.getId(),
                        e.getFirstName() + " " + e.getLastName(),
                        photoUrlOrNull(e),
                        e.getPosition(),
                        e.getDepartment() != null ? e.getDepartment().getName() : null,
                        e.getHireDate()))
                .toList();
    }

    private String photoUrlOrNull(Employee e) {
        return e.getPhotoKey() != null ? storageService.getPublicUrl(e.getPhotoKey()) : null;
    }
}