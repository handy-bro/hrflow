package com.hrflow.hrflow_backend.repository;

import com.hrflow.hrflow_backend.entity.AttendanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {

    Optional<AttendanceRecord> findByEmployeeIdAndWorkDate(Long employeeId, LocalDate workDate);

    List<AttendanceRecord> findByEmployeeIdAndWorkDateBetweenOrderByWorkDateAsc(
            Long employeeId,
            LocalDate start,
            LocalDate end
    );

    @Query("""
        SELECT ar FROM AttendanceRecord ar
        WHERE ar.employee.department.id = :departmentId
        AND ar.workDate = :date
        """)
    List<AttendanceRecord> findByDepartmentAndDate(
            @Param("departmentId") Long departmentId,
            @Param("date") LocalDate date
    );
}