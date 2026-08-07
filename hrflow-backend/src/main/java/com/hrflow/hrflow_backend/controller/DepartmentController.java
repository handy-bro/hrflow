package com.hrflow.hrflow_backend.controller;

import com.hrflow.hrflow_backend.dto.department.CreateDepartmentRequest;
import com.hrflow.hrflow_backend.dto.department.DepartmentResponse;
import com.hrflow.hrflow_backend.dto.department.UpdateDepartmentRequest;
import com.hrflow.hrflow_backend.service.DepartmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
@Tag(name = "Department Endpoints")
public class DepartmentController {

    private final DepartmentService departmentService;

    /**
     * Create a new department - ADMIN only
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new department")
    public ResponseEntity<DepartmentResponse> createDepartment(
            @Valid @RequestBody CreateDepartmentRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(departmentService.createDepartment(request));
    }

    /**
     * Get all departments - ADMIN and MANAGER
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Get all departments")
    public ResponseEntity<List<DepartmentResponse>> getAllDepartments() {
        return ResponseEntity.ok(departmentService.getAllDepartments());
    }

    /**
     * Get department by ID - ADMIN and MANAGER
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Get department by ID")
    public ResponseEntity<DepartmentResponse> getDepartmentById(
            @PathVariable Long id) {
        return ResponseEntity.ok(departmentService.getDepartmentById(id));
    }

    /**
     * Update department - ADMIN only
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update department")
    public ResponseEntity<DepartmentResponse> updateDepartment(
            @PathVariable Long id,
            @Valid @RequestBody UpdateDepartmentRequest request) {
        return ResponseEntity.ok(departmentService.updateDepartment(id, request));
    }

    /**
     * Delete department - ADMIN only
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete department")
    public ResponseEntity<Void> deleteDepartment(@PathVariable Long id) {
        departmentService.deleteDepartment(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Search departments by keyword - ADMIN and MANAGER
     */
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Search departments by keyword")
    public ResponseEntity<List<DepartmentResponse>> searchDepartments(
            @RequestParam String keyword) {
        return ResponseEntity.ok(departmentService.searchDepartments(keyword));
    }
}