package com.hrflow.hrflow_backend.enums;

public enum PayslipStatus {
    GENERATED,
    ARCHIVED // If the payslip is regenerated or corrected, the old one is archived, not deleted
}
