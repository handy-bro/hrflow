package com.hrflow.hrflow_backend.dto.payslip;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PayslipResponse(
        Long id,
        Long employeeId,
        String employeeName,
        int year,
        int month,
        BigDecimal baseSalary,
        int unjustifiedAbsenceDays,
        BigDecimal dailyRate,
        BigDecimal deductionAmount,
        BigDecimal netSalary,
        String pdfUrl,
        LocalDateTime generatedAt
) {}