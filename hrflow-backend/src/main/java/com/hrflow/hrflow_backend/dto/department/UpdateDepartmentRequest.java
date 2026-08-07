package com.hrflow.hrflow_backend.dto.department;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateDepartmentRequest {

    // Name is optional for update
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    // Description is optional for update
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    // Optional: change the manager
    private Long managerId;
}