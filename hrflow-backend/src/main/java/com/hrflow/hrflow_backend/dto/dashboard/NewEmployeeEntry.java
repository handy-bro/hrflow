package com.hrflow.hrflow_backend.dto.dashboard;

import java.time.LocalDate;

public record NewEmployeeEntry(
        Long employeeId,
        String fullName,
        String photoUrl,
        String position,
        String departmentName,
        LocalDate hireDate
) {}
