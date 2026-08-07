package com.hrflow.hrflow_backend.dto.leaves;

import com.hrflow.hrflow_backend.enums.LeaveStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ReviewLeaveRequest(
        @NotNull
        LeaveStatus decision,

        @Size(max = 1000)
        String comment
) {}
