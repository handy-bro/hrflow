package com.hrflow.hrflow_backend.dto.leaves;

import com.hrflow.hrflow_backend.enums.LeaveType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateLeaveRequest(
        @NotNull
        LeaveType leaveType,

        @NotNull
        LocalDate startDate,

        @NotNull
        LocalDate endDate,

        @Size(max = 1000)
        String reason
) {}
