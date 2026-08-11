package com.acs.crm.api;

import com.acs.crm.model.ApprovalStep;
import com.acs.crm.model.Enums;

import java.util.List;
import java.util.Map;

public record DealResponse(
        String id,
        String company,
        String contact,
        String product,
        PersonResponse accountManager,
        String stage,
        String stageLabel,
        long value,
        Enums.Priority priority,
        String updatedAt,
        String expectedClosureDate,
        String nextActivity,
        String nextActivityDueDate,
        String oemVendor,
        Enums.RiskStatus riskStatus,
        int probabilityPercent,
        long weightedValue,
        long daysInStage,
        List<ApprovalStep> approvals,
        Map<String, String> extraFields
) {
}
