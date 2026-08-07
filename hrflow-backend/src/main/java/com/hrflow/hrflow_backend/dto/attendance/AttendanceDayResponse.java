package com.hrflow.hrflow_backend.dto.attendance;

import com.hrflow.hrflow_backend.enums.AttendanceStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record AttendanceDayResponse(
        LocalDate workDate,
        LocalDateTime checkInAt,
        LocalDateTime checkOutAt,
        Integer workedMinutes,
        AttendanceStatus status
) {}
