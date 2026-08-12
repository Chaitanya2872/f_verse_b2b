package com.acs.crm.api;

public record ConvertLeadResponse(
        String leadId,
        String accountId,
        String accountName,
        String contactId,
        String contactName,
        String dealId
) {
}
