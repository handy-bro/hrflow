package com.hrflow.hrflow_backend.dto.employee;

import com.hrflow.hrflow_backend.enums.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateEmployeeRequest(
        @NotBlank
        String firstName,

        @NotBlank
        String lastName,

        @NotBlank(message = "Email is required")
        @Email String email,

        String phone,
        LocalDate birthDate,
        Gender gender,
        String position,
        BigDecimal salary,
        LocalDate hireDate,
        Long departmentId
) {}
