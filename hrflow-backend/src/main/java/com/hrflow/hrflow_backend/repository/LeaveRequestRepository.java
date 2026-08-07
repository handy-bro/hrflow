package com.hrflow.hrflow_backend.repository;

import com.hrflow.hrflow_backend.entity.LeaveRequest;
import com.hrflow.hrflow_backend.enums.LeaveStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

    // Retrieve leave requests for an employee, sorted by descending start date.
    Page<LeaveRequest> findByEmployeeIdOrderByStartDateDesc(Long employeeId, Pageable pageable);

    @Query("""
        SELECT lr FROM LeaveRequest lr
        WHERE lr.employee.id = :employeeId
        AND lr.status IN ('PENDING', 'APPROVED')
        AND lr.startDate <= :endDate
        AND lr.endDate >= :startDate
        """)
    // Checks for overlapping leave requests for a given employee within a date range.
    List<LeaveRequest> findOverlapping(
            @Param("employeeId") Long employeeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("""
        SELECT lr FROM LeaveRequest lr
        WHERE lr.status = 'APPROVED'
        AND lr.employee.department.id = :departmentId
        AND lr.startDate <= :monthEnd
        AND lr.endDate >= :monthStart
        """)
    // Retrieves approved leaves for a specific department within a given date range.
    List<LeaveRequest> findApprovedForDepartmentInRange(
            @Param("departmentId") Long departmentId,
            @Param("monthStart") LocalDate monthStart,
            @Param("monthEnd") LocalDate monthEnd);

    // For dashboard

    @Query("""
    SELECT lr FROM LeaveRequest lr
    WHERE lr.status = 'PENDING'
    ORDER BY lr.createdAt ASC
    """)
    List<LeaveRequest> findAllPending();

    long countByStatus(LeaveStatus status);
}
