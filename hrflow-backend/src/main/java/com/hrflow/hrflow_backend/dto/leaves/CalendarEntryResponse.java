package com.hrflow.hrflow_backend.dto.leaves;

import com.hrflow.hrflow_backend.enums.LeaveType;

import java.time.LocalDate;

public record CalendarEntryResponse(
        Long employeeId,
        String employeeName,
        String departmentName,
        LeaveType leaveType,
        LocalDate startDate,
        LocalDate endDate
) {}
