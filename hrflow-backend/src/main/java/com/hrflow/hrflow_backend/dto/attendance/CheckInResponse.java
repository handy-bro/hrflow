package com.hrflow.hrflow_backend.dto.attendance;

import com.hrflow.hrflow_backend.enums.AttendanceStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record CheckInResponse(
        Long id,
        LocalDate workDate,
        LocalDateTime checkInAt,
        AttendanceStatus status
) {}
