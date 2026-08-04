package com.hrflow.hrflow_backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "departments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Department name (must be unique)
    @Column(nullable = false, unique = true)
    private String name;

    // Optional description
    private String description;

    // Department manager (an employee)
    @ManyToOne
    @JoinColumn(name = "manager_id")
    private Employee manager;

    // List of employees in this department
    @OneToMany(mappedBy = "department")
    private List<Employee> employees;

    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}