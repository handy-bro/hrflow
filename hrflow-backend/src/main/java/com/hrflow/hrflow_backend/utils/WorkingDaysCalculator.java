package com.hrflow.hrflow_backend.utils;

import java.time.DayOfWeek;
import java.time.LocalDate;

public final class WorkingDaysCalculator {

    private WorkingDaysCalculator() {}

    public static int countWorkingDays(LocalDate start, LocalDate end) {
        int days = 0;
        LocalDate current = start;
        while (!current.isAfter(end)) {
            if (current.getDayOfWeek() != DayOfWeek.SATURDAY
                    && current.getDayOfWeek() != DayOfWeek.SUNDAY) {
                days++;
            }
            current = current.plusDays(1);
        }
        return days;
    }
}