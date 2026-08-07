package com.hrflow.hrflow_backend.dto.dashboard;

public record DepartmentAttendanceRateEntry(
        Long departmentId,
        String departmentName,
        double attendanceRatePercent // (present + late) / (present + late + absent) * 100
) {}