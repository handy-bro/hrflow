package com.hrflow.hrflow_backend.service;

import com.hrflow.hrflow_backend.dto.attendance.MonthlyAttendanceReportResponse;
import com.hrflow.hrflow_backend.dto.payslip.*;
import com.hrflow.hrflow_backend.entity.Employee;
import com.hrflow.hrflow_backend.entity.Payslip;
import com.hrflow.hrflow_backend.entity.User;
import com.hrflow.hrflow_backend.enums.PayslipStatus;
import com.hrflow.hrflow_backend.enums.Role;
import com.hrflow.hrflow_backend.exceptionHandler.*;
import com.hrflow.hrflow_backend.exceptionHandler.employees.EmployeeNotFoundException;
import com.hrflow.hrflow_backend.exceptionHandler.payslip.PayslipAccessDeniedException;
import com.hrflow.hrflow_backend.exceptionHandler.payslip.PayslipAlreadyExistsException;
import com.hrflow.hrflow_backend.exceptionHandler.payslip.PayslipNotFoundException;
import com.hrflow.hrflow_backend.repository.EmployeeRepository;
import com.hrflow.hrflow_backend.repository.PayslipRepository;
import com.hrflow.hrflow_backend.storage.StorageKeyGenerator;
import com.hrflow.hrflow_backend.storage.StorageService;
import com.hrflow.hrflow_backend.utils.WorkingDaysCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PayslipService {

    private final PayslipRepository payslipRepository;
    private final EmployeeRepository employeeRepository;
    private final AttendanceService attendanceService;
    private final PayslipPdfGenerator pdfGenerator;
    private final StorageService storageService;
    private final StorageKeyGenerator keyGenerator;

    @Transactional
    public PayslipResponse generate(Long employeeId, GeneratePayslipRequest request) {

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found"));

        payslipRepository.findByEmployeeIdAndYearAndMonthAndStatus(
                employeeId, request.year(), request.month(), PayslipStatus.GENERATED
        ).ifPresent(existing -> {
            throw new PayslipAlreadyExistsException(
                    "A payslip already exists for %d/%d".formatted(request.month(), request.year()));
        });

        MonthlyAttendanceReportResponse attendance =
                attendanceService.getMonthlyReport(employeeId, request.year(), request.month());

        YearMonth yearMonth = YearMonth.of(request.year(), request.month());
        int workingDaysInMonth = WorkingDaysCalculator.countWorkingDays(
                yearMonth.atDay(1), yearMonth.atEndOfMonth());

        BigDecimal baseSalary = employee.getSalary();
        BigDecimal dailyRate = baseSalary.divide(
                BigDecimal.valueOf(Math.max(workingDaysInMonth, 1)), 2, RoundingMode.HALF_UP);

        int unjustifiedAbsenceDays = attendance.absentDays();
        BigDecimal deductionAmount = dailyRate.multiply(BigDecimal.valueOf(unjustifiedAbsenceDays));
        BigDecimal netSalary = baseSalary.subtract(deductionAmount);

        Payslip payslip = Payslip.builder()
                .employee(employee)
                .year(request.year())
                .month(request.month())
                .baseSalary(baseSalary)
                .unjustifiedAbsenceDays(unjustifiedAbsenceDays)
                .dailyRate(dailyRate)
                .deductionAmount(deductionAmount)
                .netSalary(netSalary)
                .status(PayslipStatus.GENERATED)
                .build();

        payslipRepository.save(payslip);

        byte[] pdfBytes = pdfGenerator.generate(payslip);
        String key = keyGenerator.payslipKey(employeeId, request.year(), request.month());
        storageService.upload(key, new ByteArrayInputStream(pdfBytes), "application/pdf", pdfBytes.length);

        payslip.setPdfKey(key);
        payslipRepository.save(payslip);

        return toResponse(payslip);
    }

    @Transactional
    public PayslipResponse regenerate(Long employeeId, GeneratePayslipRequest request) {
        payslipRepository.findByEmployeeIdAndYearAndMonthAndStatus(
                employeeId, request.year(), request.month(), PayslipStatus.GENERATED
        ).ifPresent(existing -> {
            existing.setStatus(PayslipStatus.ARCHIVED);
            payslipRepository.save(existing);
        });

        return generate(employeeId, request);
    }

    @Transactional(readOnly = true)
    public List<PayslipResponse> getHistory(Long employeeId) {
        return payslipRepository
                .findByEmployeeIdAndStatusOrderByYearDescMonthDesc(employeeId, PayslipStatus.GENERATED)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Payslip getForDownload(Long payslipId, User currentUser) {

        Payslip payslip = payslipRepository.findById(payslipId)
                .orElseThrow(() -> new PayslipNotFoundException("Payslip not found"));

        boolean isPrivileged = currentUser.getRole() == Role.ADMIN;

        boolean isOwner = employeeRepository.findByUserId(currentUser.getId())
                .map(Employee::getId)
                .map(ownEmployeeId -> ownEmployeeId.equals(payslip.getEmployee().getId()))
                .orElse(false);

        if (!isPrivileged && !isOwner) {
            throw new PayslipAccessDeniedException("Not authorized to access this payslip");
        }

        if (payslip.getPdfKey() == null) {
            throw new PayslipNotFoundException("Payslip PDF is not available");
        }

        return payslip;
    }

    private PayslipResponse toResponse(Payslip payslip) {
        return new PayslipResponse(
                payslip.getId(),
                payslip.getEmployee().getId(),
                payslip.getEmployee().getFirstName() + " " + payslip.getEmployee().getLastName(),
                payslip.getYear(),
                payslip.getMonth(),
                payslip.getBaseSalary(),
                payslip.getUnjustifiedAbsenceDays(),
                payslip.getDailyRate(),
                payslip.getDeductionAmount(),
                payslip.getNetSalary(),
                payslip.getPdfKey() != null ? storageService.getPublicUrl(payslip.getPdfKey()) : null,
                payslip.getGeneratedAt()
        );
    }
}