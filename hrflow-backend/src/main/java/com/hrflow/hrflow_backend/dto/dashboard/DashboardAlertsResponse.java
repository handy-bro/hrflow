package com.hrflow.hrflow_backend.dto.dashboard;

import java.util.List;

public record DashboardAlertsResponse(
        List<BirthdayAlertEntry> birthdaysToday,
        List<ContractExpiryAlertEntry> contractsExpiringSoon,
        List<PendingLeaveAlertEntry> pendingLeaves
) {}
