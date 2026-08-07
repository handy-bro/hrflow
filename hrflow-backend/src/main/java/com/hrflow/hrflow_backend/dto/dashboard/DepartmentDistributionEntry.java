package com.hrflow.hrflow_backend.dto.dashboard;

public record DepartmentDistributionEntry(
        Long departmentId,
        String departmentName,
        long employeeCount
) {}
