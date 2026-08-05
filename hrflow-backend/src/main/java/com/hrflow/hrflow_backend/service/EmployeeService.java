package com.hrflow.hrflow_backend.service;

import com.hrflow.hrflow_backend.dto.employee.ChangeDepartmentRequest;
import com.hrflow.hrflow_backend.dto.employee.CreateEmployeeRequest;
import com.hrflow.hrflow_backend.dto.employee.EmployeeResponse;
import com.hrflow.hrflow_backend.dto.employee.UpdateEmployeeRequest;
import com.hrflow.hrflow_backend.entity.Department;
import com.hrflow.hrflow_backend.entity.Employee;
import com.hrflow.hrflow_backend.entity.User;
import com.hrflow.hrflow_backend.enums.EmployeeStatus;
import com.hrflow.hrflow_backend.enums.Role;
import com.hrflow.hrflow_backend.exceptionHandler.departments.DepartmentNotFoundException;
import com.hrflow.hrflow_backend.exceptionHandler.auth.EmailAlreadyTakenException;
import com.hrflow.hrflow_backend.exceptionHandler.employees.EmployeeNotFoundException;
import com.hrflow.hrflow_backend.exceptionHandler.departments.SameDepartmentException;
import com.hrflow.hrflow_backend.mapper.EmployeeMapper;
import com.hrflow.hrflow_backend.repository.DepartmentRepository;
import com.hrflow.hrflow_backend.repository.EmployeeRepository;
import com.hrflow.hrflow_backend.repository.UserRepository;
import com.hrflow.hrflow_backend.storage.FileValidator;
import com.hrflow.hrflow_backend.storage.StorageKeyGenerator;
import com.hrflow.hrflow_backend.storage.StorageService;
import com.hrflow.hrflow_backend.utils.EmployeeSpecifications;
import com.hrflow.hrflow_backend.utils.TokenUtil;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.PageSize;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final EmailService emailService;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final AuthService authService;
    private final EmployeeMapper employeeMapper;

    //files
    private final StorageService storageService;
    private final StorageKeyGenerator keyGenerator;
    private final FileValidator fileValidator;

    @Transactional
    public EmployeeResponse createEmployee(CreateEmployeeRequest request) {

        if (userRepository.existsByEmail(request.email())
                || employeeRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyTakenException("Email already used");
        }

        Department department = departmentRepository.findById(request.departmentId())
                .orElseThrow(() -> new DepartmentNotFoundException("Department not found"));

        String rawToken = TokenUtil.generateRawToken();

        User user = authService.createPendingUser(request.email(), Role.EMPLOYEE);

        Employee employee = Employee.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email())
                .phone(request.phone())
                .birthDate(request.birthDate())
                .gender(request.gender())
                .position(request.position())
                .salary(request.salary())
                .hireDate(request.hireDate())
                .department(department)
                .user(user)
                .build();
        employeeRepository.save(employee);

        emailService.sendAccountActivationEmail(user.getEmail(), rawToken);

        return employeeMapper.toResponse(employee);
    }

    public Page<EmployeeResponse> listEmployees(
            String name,
            Long departmentId,
            String position,
            Pageable pageable
    ) {

        Specification<Employee> spec =
                EmployeeSpecifications.nameContains(name)
                        .and(EmployeeSpecifications.hasPosition(position))
                        .and(EmployeeSpecifications.inDepartment(departmentId));

        Page<Employee> page = employeeRepository.findAll(spec, pageable);

        return page.map(employeeMapper::toResponse);
    }

    @Transactional
    public void uploadPhoto(Long employeeId, MultipartFile file) throws IOException {

        fileValidator.validateImage(file);

        Employee employee = employeeRepository.findById(employeeId).orElseThrow(
                () -> new EmployeeNotFoundException("Employee not found")
        );

        // Delete the ancient photo if it exists
        if (employee.getPhotoKey() != null) {
            storageService.delete(employee.getPhotoKey());
        }

        String key = keyGenerator.employeePhotoKey(employeeId, file.getContentType());
        storageService.upload(key, file.getInputStream(), file.getContentType(), file.getSize());

        employee.setPhotoKey(key);
        employeeRepository.save(employee);

        storageService.getPublicUrl(key);
    }

    @Transactional
    public EmployeeResponse updateEmployee(Long id, UpdateEmployeeRequest request, Role callerRole) {

        Employee employee = employeeRepository.findById(id).orElseThrow(
                () -> new EmployeeNotFoundException("Employee not found")
        );

        if (request.firstName() != null) employee.setFirstName(request.firstName());
        if (request.lastName() != null) employee.setLastName(request.lastName());
        if (request.phone() != null) employee.setPhone(request.phone());
        if (request.birthDate() != null) employee.setBirthDate(request.birthDate());
        if (request.gender() != null) employee.setGender(request.gender());
        if (request.position() != null) employee.setPosition(request.position());

        if (request.salary() != null) {
            if (callerRole != Role.ADMIN) {
                throw new AccessDeniedException("Not authorized to modify salary");
            }
            employee.setSalary(request.salary());
        }

        employeeRepository.save(employee);
        return employeeMapper.toResponse(employee);
    }

    @Transactional
    public void archiveEmployee(Long id) {
        Employee employee = employeeRepository.findById(id).orElseThrow(
                () -> new EmployeeNotFoundException("Employee not found")
        );
        employee.setStatus(EmployeeStatus.ARCHIVED);
        employeeRepository.save(employee);
    }

    @Transactional
    public void deleteEmployee(Long id) {
        Employee employee = employeeRepository.findById(id).orElseThrow(
                () -> new EmployeeNotFoundException("Employee not found")
        );
        employee.setDeleted(true);
        employeeRepository.save(employee);
    }

    @Transactional
    public void restoreEmployee(Long id) {
        Employee employee = employeeRepository.findByIdIncludingDeleted(id)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found"));
        employee.setDeleted(false);
        employeeRepository.save(employee);
    }

    @Transactional
    public void changeDepartment(Long employeeId, ChangeDepartmentRequest request) {

        Employee employee = employeeRepository.findById(employeeId).orElseThrow(
                () -> new EmployeeNotFoundException("Employee not found")
        );

        Department newDepartment = departmentRepository.findById(request.departmentId())
                .orElseThrow(() -> new DepartmentNotFoundException("Department not found"));

        Department oldDepartment = employee.getDepartment();

        if (oldDepartment != null && oldDepartment.getId().equals(newDepartment.getId())) {
            throw new SameDepartmentException("Employee is already in this department");
        }

        employee.setDepartment(newDepartment);
        employeeRepository.save(employee);
    }

    public void exportEmployeesToCsv(List<Employee> employees, OutputStream out) throws IOException {
        try (PrintWriter writer = new PrintWriter(out, true, StandardCharsets.UTF_8)) {
            writer.println("ID,First Name,Last Name,Email,Position,Department,Hire Date,Salary");

            for (Employee e : employees) {
                String department = e.getDepartment().getName();
                String line = e.getId() + "," +
                        csvEscape(e.getFirstName()) + "," +
                        csvEscape(e.getLastName()) + "," +
                        csvEscape(e.getEmail()) + "," +
                        csvEscape(e.getPosition()) + "," +
                        csvEscape(department) + "," +
                        e.getHireDate() +
                        e.getSalary();
                writer.println(line);
            }
        }
    }

    private String csvEscape(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    public void exportEmployeesToPdf(List<Employee> employees, OutputStream out) throws DocumentException {
        Document document = new Document(PageSize.A4.rotate());
        PdfWriter.getInstance(document, out);
        document.open();

        PdfPTable table = new PdfPTable(8);
        table.setWidthPercentage(100);

        Stream.of(
                "ID",
                "First Name",
                "Last Name",
                "Email", "Position",
                "Department",
                "Hire Date",
                "Salary"
        ).forEach(table::addCell);

        for (Employee e : employees) {
            String department = e.getDepartment().getName();
            table.addCell(String.valueOf(e.getId()));
            table.addCell(e.getFirstName());
            table.addCell(e.getLastName());
            table.addCell(e.getEmail());
            table.addCell(e.getPosition());
            table.addCell(department);
            table.addCell(String.valueOf(e.getHireDate()));
            table.addCell(e.getSalary().toString());
        }

        document.add(table);
        document.close();
    }

    public List<Employee> getAllEmployees(){
        return employeeRepository.findAll();
    }
}
