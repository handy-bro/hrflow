package com.hrflow.hrflow_backend.dto.department;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateDepartmentRequest {

    // Department name is required
    @NotBlank(message = "Department name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    // Description is optional
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    // Optional: assign a manager at creation
    private Long managerId;
}