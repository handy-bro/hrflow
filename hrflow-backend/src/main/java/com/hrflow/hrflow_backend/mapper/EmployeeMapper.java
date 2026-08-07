package com.hrflow.hrflow_backend.mapper;

import com.hrflow.hrflow_backend.dto.employee.EmployeeResponse;
import com.hrflow.hrflow_backend.entity.Employee;
import com.hrflow.hrflow_backend.enums.Role;
import org.springframework.stereotype.Component;

@Component
public class EmployeeMapper {

    public EmployeeResponse toResponse(Employee employee) {

        return new EmployeeResponse(
                employee.getId(),
                employee.getFirstName(),
                employee.getLastName(),
                employee.getEmail(),
                employee.getPhone(),
                employee.getBirthDate(),
                employee.getGender(),
                employee.getPhotoKey(),
                employee.getPosition(),
                employee.getSalary(),
                employee.getHireDate(),
                employee.getStatus(),
                employee.getDepartment() != null ? employee.getDepartment().getName() : null
        );
    }
}