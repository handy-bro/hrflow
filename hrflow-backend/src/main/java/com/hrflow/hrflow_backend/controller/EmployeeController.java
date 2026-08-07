package com.hrflow.hrflow_backend.controller;

import com.hrflow.hrflow_backend.dto.employee.ChangeDepartmentRequest;
import com.hrflow.hrflow_backend.dto.employee.CreateEmployeeRequest;
import com.hrflow.hrflow_backend.dto.employee.EmployeeResponse;
import com.hrflow.hrflow_backend.dto.employee.UpdateEmployeeRequest;
import com.hrflow.hrflow_backend.entity.Employee;
import com.hrflow.hrflow_backend.enums.Role;
import com.hrflow.hrflow_backend.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
@Tag(name = "Employees Endpoints", description = "Endpoints for managing employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    @PostMapping
    @Operation(summary = "Create an employee", description = "Adds a new employee to the system.")
    public ResponseEntity<EmployeeResponse> createEmployee(
            @Valid @RequestBody CreateEmployeeRequest request
    ) {
        return ResponseEntity.ok(employeeService.createEmployee(request));
    }

    @GetMapping
    @Operation(summary = "List employees", description = "Retrieves a paginated list of employees.")
    public ResponseEntity<Page<EmployeeResponse>> listEmployees(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) String position,
            Pageable pageable
    ) {
        return ResponseEntity.ok(employeeService.listEmployees(name, departmentId, position, pageable));
    }

    @PatchMapping("/{id}/department")
    @Operation(summary = "Change department", description = "Updates the department of an employee.")
    public ResponseEntity<Void> changeDepartment(
            @PathVariable Long id,
            @Valid @RequestBody ChangeDepartmentRequest request
    ) {
        employeeService.changeDepartment(id, request);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an employee", description = "Updates the information of an existing employee.")
    public ResponseEntity<EmployeeResponse> updateEmployee(
            @PathVariable Long id,
            @Valid @RequestBody UpdateEmployeeRequest request
    ) {
        return ResponseEntity.ok(employeeService.updateEmployee(id, request, null));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an employee", description = "Deletes an employee by marking them as removed.")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/restore")
    @Operation(summary = "Restore an employee", description = "Restores a previously deleted employee.")
    public ResponseEntity<Void> restoreEmployee(@PathVariable Long id) {
        employeeService.restoreEmployee(id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/archive")
    @Operation(summary = "Archive an employee", description = "Archives an employee.")
    public ResponseEntity<Void> archiveEmployee(@PathVariable Long id) {
        employeeService.archiveEmployee(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/upload-profile-photo")
    @Operation(summary = "Upload profile photo", description = "Uploads a profile photo for the specified employee.")
    public ResponseEntity<Void> uploadProfilePhoto(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        employeeService.uploadPhoto(id, file);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/export/pdf")
    @Operation(summary = "Export employees to PDF", description = "Exports the list of employees to a PDF file.")
    public ResponseEntity<byte[]> exportEmployeesToPdf() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        List<Employee> employees = employeeService.getAllEmployees(); // Assuming a method to fetch all employees
        employeeService.exportEmployeesToPdf(employees, out);
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=employees.pdf")
                .body(out.toByteArray());
    }

    @GetMapping("/export/csv")
    @Operation(summary = "Export employees to CSV", description = "Exports the list of employees to a CSV file.")
    public ResponseEntity<byte[]> exportEmployeesToCsv() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        List<Employee> employees = employeeService.getAllEmployees(); // Assuming a method to fetch all employees
        employeeService.exportEmployeesToCsv(employees, out);
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=employees.csv")
                .body(out.toByteArray());
    }
}
