package com.acs.crm.api;

import com.acs.crm.model.Enums.LeadStatus;

public record LeadResponse(
        String id,
        String company,
        String contactName,
        String email,
        String phone,
        String source,
        String owner,
        LeadStatus status,
        int score,
        String notes,
        String createdAt,
        String updatedAt,
        String convertedAccountId,
        String convertedContactId,
        String convertedDealId
) {
}
