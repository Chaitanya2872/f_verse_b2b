package com.acs.crm.api;

import java.util.List;

public record PipelineStageResponse(
        String id,
        String name,
        String shortLabel,
        int displayOrder,
        int probabilityPercent,
        String color,
        int maxExpectedDurationDays,
        List<String> mandatoryFields,
        List<String> requiredApprovals,
        List<AllowedStageTransitionResponse> allowedNextStages
) {
}
