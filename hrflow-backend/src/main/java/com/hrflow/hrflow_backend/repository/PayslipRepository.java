package com.hrflow.hrflow_backend.repository;

import com.hrflow.hrflow_backend.entity.Payslip;
import com.hrflow.hrflow_backend.enums.PayslipStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PayslipRepository extends JpaRepository<Payslip, Long> {

    Optional<Payslip> findByEmployeeIdAndYearAndMonthAndStatus(
            Long employeeId, int year, int month, PayslipStatus status);

    List<Payslip> findByEmployeeIdAndStatusOrderByYearDescMonthDesc(Long employeeId, PayslipStatus status);
}
