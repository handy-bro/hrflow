package com.hrflow.hrflow_backend.dto.dashboard;

import java.time.LocalDate;

public record ContractExpiryAlertEntry(
        Long employeeId,
        String fullName,
        LocalDate contractEndDate,
        long daysRemaining
) {}
