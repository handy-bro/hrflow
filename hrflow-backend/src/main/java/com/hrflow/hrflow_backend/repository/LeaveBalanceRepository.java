package com.hrflow.hrflow_backend.repository;

import com.hrflow.hrflow_backend.entity.LeaveBalance;
import com.hrflow.hrflow_backend.enums.LeaveType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LeaveBalanceRepository extends JpaRepository<LeaveBalance, Long> {
    Optional<LeaveBalance> findByEmployeeIdAndLeaveTypeAndYear(Long employeeId, LeaveType leaveType, int year);
    List<LeaveBalance> findByEmployeeIdAndYear(Long employeeId, int year);
}
