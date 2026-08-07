package com.hrflow.hrflow_backend.dto.dashboard;

public record WorkforceTrendEntry(
        int year,
        int month,
        long activeEmployees
) {}
