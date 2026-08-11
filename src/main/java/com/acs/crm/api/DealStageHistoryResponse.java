package com.acs.crm.api;

public record DealStageHistoryResponse(
        String fromStage,
        String toStage,
        String changedAt,
        String changedBy,
        String remarks
) {
}
