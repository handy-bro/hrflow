package com.hrflow.hrflow_backend.dto.dashboard;

import com.hrflow.hrflow_backend.enums.LeaveType;

import java.time.LocalDate;

public record PendingLeaveAlertEntry(
        Long leaveRequestId,
        Long employeeId,
        String employeeName,
        LeaveType leaveType,
        LocalDate startDate,
        LocalDate endDate,
        long daysWaiting,
        boolean urgent // Pending for more than 03 days
) {}
