package com.hrflow.hrflow_backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "leave-policy")
@Validated
public record LeavePolicyProperties(
        @Min(0) int annualDays,
        @Min(0) int sickDays,
        @Min(0) int maternityPaternityDays
) {}