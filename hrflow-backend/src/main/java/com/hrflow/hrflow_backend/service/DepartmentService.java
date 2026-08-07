package com.hrflow.hrflow_backend.service;

import com.hrflow.hrflow_backend.dto.department.CreateDepartmentRequest;
import com.hrflow.hrflow_backend.dto.department.DepartmentResponse;
import com.hrflow.hrflow_backend.dto.department.UpdateDepartmentRequest;
import com.hrflow.hrflow_backend.entity.Department;
import com.hrflow.hrflow_backend.entity.Employee;
import com.hrflow.hrflow_backend.repository.DepartmentRepository;
import com.hrflow.hrflow_backend.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;

    /**
     * Create a new department
     * Only ADMIN can perform this action
     */
    public DepartmentResponse createDepartment(CreateDepartmentRequest request) {

        // Check if department name already exists
        if (departmentRepository.existsByName(request.getName())) {
            throw new RuntimeException("Department name already exists: " + request.getName());
        }

        // Build department entity
        Department department = Department.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();

        // Assign manager if provided
        // todo - verify if the manager has the role MANAGER
        if (request.getManagerId() != null) {
            Employee manager = employeeRepository.findById(request.getManagerId())
                    .orElseThrow(() -> new RuntimeException("Manager not found"));
            department.setManager(manager);
        }

        // Save and return response
        Department saved = departmentRepository.save(department);
        return mapToResponse(saved);
    }

    /**
     * Get all departments with employee count
     */
    public List<DepartmentResponse> getAllDepartments() {
        return departmentRepository.findAllWithEmployees()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get department by ID
     */
    public DepartmentResponse getDepartmentById(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Department not found with id: " + id));
        return mapToResponse(department);
    }

    /**
     * Update department information
     */
    public DepartmentResponse updateDepartment(Long id, UpdateDepartmentRequest request) {

        // Find existing department
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Department not found with id: " + id));

        // Update name if provided and different
        if (request.getName() != null && !request.getName().equals(department.getName())) {
            // Check if new name already taken
            if (departmentRepository.existsByName(request.getName())) {
                throw new RuntimeException("Department name already exists: " + request.getName());
            }
            department.setName(request.getName());
        }

        // Update description if provided
        if (request.getDescription() != null) {
            department.setDescription(request.getDescription());
        }

        // Update manager if provided
        if (request.getManagerId() != null) {
            Employee manager = employeeRepository.findById(request.getManagerId())
                    .orElseThrow(() -> new RuntimeException("Manager not found"));
            department.setManager(manager);
        }

        // Save and return updated response
        Department updated = departmentRepository.save(department);
        return mapToResponse(updated);
    }

    /**
     * Delete department (only if no employees assigned)
     */
    public void deleteDepartment(Long id) {

        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Department not found with id: " + id));

        // Cannot delete department with active employees
        if (department.getEmployees() != null && !department.getEmployees().isEmpty()) {
            throw new RuntimeException(
                    "Cannot delete department with active employees. " +
                            "Please reassign employees first."
            );
        }

        departmentRepository.delete(department);
    }

    /**
     * Search departments by keyword
     */
    public List<DepartmentResponse> searchDepartments(String keyword) {
        return departmentRepository.searchByKeyword(keyword)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Map Department entity to DepartmentResponse DTO
     */
    private DepartmentResponse mapToResponse(Department department) {
        return DepartmentResponse.builder()
                .id(department.getId())
                .name(department.getName())
                .description(department.getDescription())
                .managerId(department.getManager() != null
                        ? department.getManager().getId() : null)
                .managerFullName(department.getManager() != null
                        ? department.getManager().getFirstName() + " "
                        + department.getManager().getLastName() : null)
                .employeeCount(department.getEmployees() != null
                        ? department.getEmployees().size() : 0)
                .createdAt(department.getCreatedAt())
                .updatedAt(department.getUpdatedAt())
                .build();
    }
}