package com.hrflow.hrflow_backend.entity;

import com.hrflow.hrflow_backend.enums.PayslipStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "payslips",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {
                        "employee_id",
                        "year",
                        "month",
                        "status"
                }
        )
)
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Payslip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(nullable = false)
    private int year;

    @Column(nullable = false)
    private int month;

    @Column(nullable = false)
    private BigDecimal baseSalary;

    @Column(nullable = false)
    private int unjustifiedAbsenceDays;

    @Column(nullable = false)
    private BigDecimal dailyRate;

    @Column(nullable = false)
    private BigDecimal deductionAmount;

    @Column(nullable = false)
    private BigDecimal netSalary;

    // Generated pdf key
    private String pdfKey;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private PayslipStatus status = PayslipStatus.GENERATED;

    private LocalDateTime generatedAt;

    @PrePersist
    public void prePersist() {
        generatedAt = LocalDateTime.now();
    }
}