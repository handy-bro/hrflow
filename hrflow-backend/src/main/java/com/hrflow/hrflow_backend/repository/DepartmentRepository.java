package com.hrflow.hrflow_backend.repository;

import com.hrflow.hrflow_backend.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DepartmentRepository extends JpaRepository<Department, Long> {

    // Check if department name already exists
    boolean existsByName(String name);

    // Find department by name
    Optional<Department> findByName(String name);

    // Search departments by name (case insensitive)
    @Query("SELECT d FROM Department d WHERE " +
            "LOWER(d.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Department> searchByKeyword(@Param("keyword") String keyword);

    // Find all departments with their employee count
    @Query("SELECT d FROM Department d LEFT JOIN FETCH d.employees")
    List<Department> findAllWithEmployees();
}