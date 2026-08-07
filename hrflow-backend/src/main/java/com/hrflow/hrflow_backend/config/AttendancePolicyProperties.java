package com.hrflow.hrflow_backend.config;

import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "attendance-policy")
@Validated
public record AttendancePolicyProperties(
        @Min(0) int gracePeriodMinutes
) {}