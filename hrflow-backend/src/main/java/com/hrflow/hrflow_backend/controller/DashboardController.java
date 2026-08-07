package com.hrflow.hrflow_backend.controller;

import com.hrflow.hrflow_backend.dto.dashboard.*;
import com.hrflow.hrflow_backend.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Dashboard", description = "Endpoints for retrieving dashboard data and statistics.")
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
public class DashboardController {

    private final DashboardService dashboardService;

    @Operation(
            summary = "Get dashboard summary",
            description = "Fetches a summary of key metrics for the dashboard."
    )
    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryResponse> summary() {
        return ResponseEntity.ok(dashboardService.getSummary());
    }

    @Operation(
            summary = "Get department distribution",
            description = "Fetches the distribution of employees across departments."
    )
    @GetMapping("/department-distribution")
    public ResponseEntity<List<DepartmentDistributionEntry>> departmentDistribution() {
        return ResponseEntity.ok(dashboardService.getDepartmentDistribution());
    }

    @Operation(
            summary = "Get workforce trend",
            description = "Fetches the workforce evolution over the past 12 months."
    )
    @GetMapping("/workforce-trend")
    public ResponseEntity<List<WorkforceTrendEntry>> workforceTrend() {
        return ResponseEntity.ok(dashboardService.getWorkforceTrend());
    }

    @Operation(
            summary = "Get attendance rate",
            description = "Fetches the monthly attendance rate by department."
    )
    @GetMapping("/attendance-rate")
    public ResponseEntity<List<DepartmentAttendanceRateEntry>> attendanceRate(
            @RequestParam int year, @RequestParam int month) {
        return ResponseEntity.ok(dashboardService.getDepartmentAttendanceRates(year, month));
    }

    @Operation(
            summary = "Get alerts",
            description = "Fetches alerts such as birthdays, contract expirations, and pending leaves."
    )
    @GetMapping("/alerts")
    public ResponseEntity<DashboardAlertsResponse> alerts() {
        return ResponseEntity.ok(dashboardService.getAlerts());
    }

    @Operation(
            summary = "Get new employees",
            description = "Fetches the list of new employees for the current month."
    )
    @GetMapping("/new-employees")
    public ResponseEntity<List<NewEmployeeEntry>> newEmployees() {
        return ResponseEntity.ok(dashboardService.getNewEmployeesThisMonth());
    }
}