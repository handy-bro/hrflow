package com.hrflow.hrflow_backend.entity;

import com.hrflow.hrflow_backend.enums.EmployeeStatus;
import com.hrflow.hrflow_backend.enums.Gender;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "employees")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SQLRestriction("deleted = false")
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Manage concurrent updates
    @Version
    private Long version;

    // Personal information
    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(unique = true, nullable = false)
    private String email;

    private String phone;

    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    // Profile photo URL (stored after upload)
    private String photoKey;

    // Professional information
    private String position;

    private BigDecimal salary;

    private LocalDate hireDate;

    // Employee status (active, inactive, suspended)
    @Enumerated(EnumType.STRING)
    private EmployeeStatus status;

    // Soft delete flag
    private boolean deleted = false;

    // Linked department
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    // Linked user account
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    private Employee manager;

    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // For Attendance
    @Column(nullable = false)
    @Builder.Default
    private LocalTime expectedStartTime = LocalTime.of(9, 0);

    @Column(nullable = false)
    @Builder.Default
    private BigDecimal contractualHoursPerDay = BigDecimal.valueOf(8);

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) status = EmployeeStatus.ACTIVE;
        if (hireDate == null) hireDate = LocalDate.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Employee other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}