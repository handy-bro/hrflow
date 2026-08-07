package com.hrflow.hrflow_backend.repository;

import com.hrflow.hrflow_backend.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long>, JpaSpecificationExecutor<Employee> {

    // Check if email already exists
    boolean existsByEmail(String email);

    @Query(value = "SELECT * FROM employees WHERE id = :id", nativeQuery = true)
    Optional<Employee> findByIdIncludingDeleted(@Param("id") Long id);

    Optional<Employee> findByUserId(Long userId);

    // Dashboard

    @Query("""
    SELECT COUNT(e) FROM Employee e
    WHERE e.hireDate <= :asOfDate
    AND (e.archivedAt IS NULL OR e.archivedAt > :asOfDateTime)
    """)
    long countActiveAsOf(@Param("asOfDate") LocalDate asOfDate, @Param("asOfDateTime") LocalDateTime asOfDateTime);

    @Query("""
    SELECT e.department.id, e.department.name, COUNT(e)
    FROM Employee e
    WHERE e.status = 'ACTIVE'
    GROUP BY e.department.id, e.department.name
    """)
    List<Object[]> countByDepartment();

    @Query("""
    SELECT e FROM Employee e
    WHERE e.status = 'ACTIVE'
    AND FUNCTION('MONTH', e.birthDate) = :month
    AND FUNCTION('DAY', e.birthDate) = :day
    """)
    List<Employee> findBirthdaysOn(@Param("month") int month, @Param("day") int day);

    @Query("""
    SELECT e FROM Employee e
    WHERE e.status = 'ACTIVE'
    AND e.contractEndDate IS NOT NULL
    AND e.contractEndDate BETWEEN :today AND :limit
    ORDER BY e.contractEndDate ASC
    """)
    List<Employee> findContractsExpiringBetween(@Param("today") LocalDate today, @Param("limit") LocalDate limit);

    @Query("""
    SELECT e FROM Employee e
    WHERE e.status = 'ACTIVE'
    AND e.hireDate BETWEEN :monthStart AND :monthEnd
    ORDER BY e.hireDate DESC
    """)
    List<Employee> findHiredBetween(@Param("monthStart") LocalDate monthStart, @Param("monthEnd") LocalDate monthEnd);
}