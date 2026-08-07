package com.hrflow.hrflow_backend.dto.dashboard;

public record DashboardSummaryResponse(
        long totalActiveEmployees,
        long employeesLastMonth,
        double monthlyGrowthPercent,
        long pendingLeaveRequests,
        long urgentLeaveRequests, // Pending for more than 03 days
        long newEmployeesThisMonth
) {}
