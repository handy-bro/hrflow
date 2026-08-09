package com.hrflow.hrflow_backend.controller;

import com.hrflow.hrflow_backend.dto.attendance.AttendanceDayResponse;
import com.hrflow.hrflow_backend.dto.attendance.CheckInResponse;
import com.hrflow.hrflow_backend.dto.attendance.CheckOutResponse;
import com.hrflow.hrflow_backend.dto.attendance.MonthlyAttendanceReportResponse;
import com.hrflow.hrflow_backend.entity.User;
import com.hrflow.hrflow_backend.exceptionHandler.employees.EmployeeNotFoundException;
import com.hrflow.hrflow_backend.repository.EmployeeRepository;
import com.hrflow.hrflow_backend.service.AttendanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(
        name = "Attendance",
        description = "Endpoints for managing employee attendance"
)
@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final EmployeeRepository employeeRepository;

    @Operation(
            summary = "Check-in for the current user",
            description = "Records the check-in time for the authenticated user."
    )
    @PostMapping("/check-in")
    public ResponseEntity<CheckInResponse> checkIn(@AuthenticationPrincipal User currentUser) {
        Long employeeId = currentEmployeeId(currentUser);
        return ResponseEntity.ok(attendanceService.checkIn(employeeId));
    }

    @Operation(
            summary = "Check-out for the current user",
            description = "Records the check-out time for the authenticated user."
    )
    @PostMapping("/check-out")
    public ResponseEntity<CheckOutResponse> checkOut(@AuthenticationPrincipal User currentUser) {
        Long employeeId = currentEmployeeId(currentUser);
        return ResponseEntity.ok(attendanceService.checkOut(employeeId));
    }

    @Operation(
            summary = "Get monthly attendance report for the current user",
            description = "Fetches the attendance report for the authenticated user for a specific month and year."
    )
    @GetMapping("/me/report")
    public ResponseEntity<MonthlyAttendanceReportResponse> myReport(
            @RequestParam(name = "year") int year, @RequestParam(name = "month") int month,
            @AuthenticationPrincipal User currentUser) {
        Long employeeId = currentEmployeeId(currentUser);
        return ResponseEntity.ok(attendanceService.getMonthlyReport(employeeId, year, month));
    }

    @Operation(
            summary = "Get monthly attendance report for an employee",
            description = "Fetches the attendance report for a specific employee for a specific month and year. Requires ADMIN or MANAGER role."
    )
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @GetMapping("/employee/{employeeId}/report")
    public ResponseEntity<MonthlyAttendanceReportResponse> employeeReport(
            @PathVariable Long employeeId, @RequestParam int year, @RequestParam int month) {
        return ResponseEntity.ok(attendanceService.getMonthlyReport(employeeId, year, month));
    }

    @Operation(
            summary = "Get today's attendance for a department",
            description = "Fetches the attendance records for all employees in a specific department for today. Requires ADMIN or MANAGER role."
    )
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @GetMapping("/department/{departmentId}/today")
    public ResponseEntity<List<AttendanceDayResponse>> departmentToday(@PathVariable("departmentId") Long departmentId) {
        return ResponseEntity.ok(attendanceService.getTodayForDepartment(departmentId));
    }

    private Long currentEmployeeId(User currentUser) {
        return employeeRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new EmployeeNotFoundException("No employee profile linked to this account"))
                .getId();
    }
}
