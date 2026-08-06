package com.hrflow.hrflow_backend.dto.attendance;

import java.math.BigDecimal;
import java.util.List;

public record MonthlyAttendanceReportResponse(
        Long employeeId,
        String employeeName,
        int year,
        int month,
        BigDecimal contractualHours,
        BigDecimal workedHours,
        BigDecimal hoursDelta,          // worked - contractual (can e negative)
        int presentDays,
        int lateDays,
        int absentDays,
        int onLeaveDays,
        List<AttendanceDayResponse> days
) {}
