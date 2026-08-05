package com.hrflow.hrflow_backend.dto.leaves;

import com.hrflow.hrflow_backend.enums.LeaveStatus;
import com.hrflow.hrflow_backend.enums.LeaveType;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record LeaveRequestResponse(
        Long id,
        Long employeeId,
        String employeeName,
        LeaveType leaveType,
        LocalDate startDate,
        LocalDate endDate,
        int requestedDays,
        String reason,
        LeaveStatus status,
        String managerComment,
        String reviewedByName,
        LocalDateTime reviewedAt,
        LocalDateTime createdAt
) {}
