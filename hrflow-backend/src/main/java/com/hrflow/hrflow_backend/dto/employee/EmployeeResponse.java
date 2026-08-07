package com.hrflow.hrflow_backend.dto.employee;

import com.hrflow.hrflow_backend.enums.EmployeeStatus;
import com.hrflow.hrflow_backend.enums.Gender;

import java.math.BigDecimal;
import java.time.LocalDate;

public record EmployeeResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        String phone,
        LocalDate birthDate,
        Gender gender,
        String photoKey,
        String position,
        BigDecimal salary,
        LocalDate hireDate,
        EmployeeStatus status,
        String departmentName
) {}