package com.hrflow.hrflow_backend.dto.payslip;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record GeneratePayslipRequest(
        @Min(2000)
        int year,

        @Min(1)
        @Max(12)
        int month
) {}
