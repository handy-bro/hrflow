package com.hrflow.hrflow_backend.controller;

import com.hrflow.hrflow_backend.dto.leaves.*;
import com.hrflow.hrflow_backend.entity.User;
import com.hrflow.hrflow_backend.exceptionHandler.employees.EmployeeNotFoundException;
import com.hrflow.hrflow_backend.repository.EmployeeRepository;
import com.hrflow.hrflow_backend.service.LeaveService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.time.LocalDate;
import java.util.List;

@Tag(
        name = "Leave Management",
        description = "APIs for managing leave requests, leave balances, and team leave calendars."
)
@RestController
@RequestMapping("/api/leaves")
@RequiredArgsConstructor
public class LeaveController {

    private final LeaveService leaveService;
    private final EmployeeRepository employeeRepository;

    @Operation(
            summary = "Submit a leave request",
            description = "Allows the authenticated employee to submit a new leave request."
    )
    @PostMapping
    public ResponseEntity<LeaveRequestResponse> submit(
            @Valid @RequestBody CreateLeaveRequest request,
            @AuthenticationPrincipal User currentUser) {
        Long employeeId = currentEmployeeId(currentUser);
        return ResponseEntity.ok(leaveService.submitRequest(employeeId, request));
    }

    @Operation(
            summary = "Review a leave request",
            description = "Allows an administrator or manager to approve or reject a leave request."
    )
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @PatchMapping("/{id}/review")
    public ResponseEntity<LeaveRequestResponse> review(
            @PathVariable("id") Long id,
            @Valid @RequestBody ReviewLeaveRequest request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(leaveService.review(id, request, currentUser));
    }

    @Operation(
            summary = "Cancel a leave request",
            description = "Allows the employee who created the request to cancel it, subject to business rules."
    )
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<LeaveRequestResponse> cancel(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(leaveService.cancel(id, currentUser.getId()));
    }

    @Operation(
            summary = "Get my leave history",
            description = "Returns the authenticated employee's leave request history as a paginated list."
    )
    @GetMapping("/me")
    public ResponseEntity<Page<LeaveRequestResponse>> myHistory(
            @PageableDefault(size = 20, sort = "startDate") Pageable pageable,
            @AuthenticationPrincipal User currentUser) {
        Long employeeId = currentEmployeeId(currentUser);
        return ResponseEntity.ok(leaveService.getHistory(employeeId, pageable));
    }

    @Operation(
            summary = "Get an employee's leave history",
            description = "Returns the leave request history of a specific employee. Accessible to administrators, HR staff, and managers."
    )
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER')")
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<Page<LeaveRequestResponse>> employeeHistory(
            @PathVariable("employeeId") Long employeeId,
            @PageableDefault(size = 20, sort = "startDate") Pageable pageable) {
        return ResponseEntity.ok(leaveService.getHistory(employeeId, pageable));
    }

    @Operation(
            summary = "Get my leave balances",
            description = "Returns the authenticated employee's leave balances for the specified year. If no year is provided, the current year is used."
    )
    @GetMapping("/me/balance")
    public ResponseEntity<List<LeaveBalanceResponse>> myBalance(
            @RequestParam(required = false) Integer year,
            @AuthenticationPrincipal User currentUser) {
        Long employeeId = currentEmployeeId(currentUser);
        int targetYear = year != null ? year : LocalDate.now().getYear();
        return ResponseEntity.ok(leaveService.getBalances(employeeId, targetYear));
    }

    @Operation(
            summary = "Get an employee's leave balances",
            description = "Returns the leave balances of a specific employee. Accessible to administrators, HR staff, and managers."
    )
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER')")
    @GetMapping("/employee/{employeeId}/balance")
    public ResponseEntity<List<LeaveBalanceResponse>> employeeBalance(
            @PathVariable(name = "employeeId") Long employeeId,
            @RequestParam(required = false) Integer year) {
        int targetYear = year != null ? year : LocalDate.now().getYear();
        return ResponseEntity.ok(leaveService.getBalances(employeeId, targetYear));
    }

    @Operation(
            summary = "Get the team leave calendar",
            description = "Returns all approved leave requests for employees within a department during the specified month."
    )
    @GetMapping("/calendar")
    public ResponseEntity<List<CalendarEntryResponse>> teamCalendar(
            @RequestParam(value = "departmentId") Long departmentId,
            @RequestParam(value = "year") int year,
            @RequestParam(name = "month") int month) {
        return ResponseEntity.ok(leaveService.getTeamCalendar(departmentId, year, month));
    }

    private Long currentEmployeeId(User currentUser) {
        return employeeRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new EmployeeNotFoundException("No employee profile linked to this account"))
                .getId();
    }
}