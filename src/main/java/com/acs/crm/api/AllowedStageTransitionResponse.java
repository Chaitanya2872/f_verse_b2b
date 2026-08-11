package com.acs.crm.api;

public record AllowedStageTransitionResponse(
        String stageId,
        boolean confirmationRequired
) {
}
