package com.hrflow.hrflow_backend.utils;

import com.hrflow.hrflow_backend.entity.Employee;
import org.springframework.data.jpa.domain.Specification;

public class EmployeeSpecifications {

    public static Specification<Employee> nameContains(String name) {
        if (name == null || name.isBlank()) return null;
        String pattern = "%" + name.toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("firstName")), pattern),
                cb.like(cb.lower(root.get("lastName")), pattern),
                cb.like(cb.lower(cb.concat(cb.concat(root.get("firstName"), " "), root.get("lastName"))), pattern)
        );
    }

    public static Specification<Employee> hasPosition(String position) {
        if (position == null || position.isBlank()) return null;
        return (root, query, cb) -> cb.equal(cb.lower(root.get("position")), position.toLowerCase());
    }

    public static Specification<Employee> inDepartment(Long departmentId) {
        if (departmentId == null) return null;
        return (root, query, cb) -> cb.equal(root.get("department").get("id"), departmentId);
    }
}
