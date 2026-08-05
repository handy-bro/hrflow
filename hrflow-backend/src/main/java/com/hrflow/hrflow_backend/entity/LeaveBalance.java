package com.hrflow.hrflow_backend.entity;

import com.hrflow.hrflow_backend.enums.LeaveType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "leave_balances",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {
                        "employee_id",
                        "leave_type",
                        "year"
                }
        )
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveBalance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LeaveType leaveType;

    @Column(nullable = false)
    private int year;

    @Column(nullable = false)
    private int allocatedDays;

    @Builder.Default
    @Column(nullable = false)
    private int usedDays = 0;

    public int getRemainingDays() {
        return allocatedDays - usedDays;
    }
}