package com.hrflow.hrflow_backend.dto.leaves;

import com.hrflow.hrflow_backend.enums.LeaveType;

public record LeaveBalanceResponse(
        LeaveType leaveType,
        int year,
        int allocatedDays,
        int usedDays,
        int remainingDays
) {}
