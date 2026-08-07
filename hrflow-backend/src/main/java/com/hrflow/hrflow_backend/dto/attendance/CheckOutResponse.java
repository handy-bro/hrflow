package com.hrflow.hrflow_backend.dto.attendance;

import com.hrflow.hrflow_backend.enums.AttendanceStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record CheckOutResponse(
        Long id,
        LocalDate workDate,
        LocalDateTime checkInAt,
        LocalDateTime checkOutAt,
        int workedMinutes,
        AttendanceStatus status
) {}
