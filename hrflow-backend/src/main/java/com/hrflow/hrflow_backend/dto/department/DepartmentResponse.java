package com.hrflow.hrflow_backend.dto.department;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class DepartmentResponse {

    private Long id;
    private String name;
    private String description;

    // Manager info (simplified)
    private Long managerId;
    private String managerFullName;

    // Number of employees in this department
    private Integer employeeCount;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}