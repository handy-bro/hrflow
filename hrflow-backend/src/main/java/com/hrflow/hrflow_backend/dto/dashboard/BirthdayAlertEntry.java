package com.hrflow.hrflow_backend.dto.dashboard;

import java.time.LocalDate;

public record BirthdayAlertEntry(
        Long employeeId,
        String fullName,
        String photoUrl,
        LocalDate birthDate
) {}
