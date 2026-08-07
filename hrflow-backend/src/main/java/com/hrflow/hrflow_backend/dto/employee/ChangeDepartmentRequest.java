package com.hrflow.hrflow_backend.dto.employee;

import jakarta.validation.constraints.NotNull;

public record ChangeDepartmentRequest(@NotNull Long departmentId) {}
