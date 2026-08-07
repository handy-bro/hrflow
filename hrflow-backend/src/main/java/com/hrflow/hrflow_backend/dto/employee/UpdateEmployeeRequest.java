package com.hrflow.hrflow_backend.dto.employee;

import com.hrflow.hrflow_backend.enums.Gender;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdateEmployeeRequest(
        String firstName,
        String lastName,
        String phone,
        LocalDate birthDate,
        Gender gender,
        String position,
        BigDecimal salary
) {}