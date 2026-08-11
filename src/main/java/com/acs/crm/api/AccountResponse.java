package com.acs.crm.api;

public record AccountResponse(
        String id,
        String name,
        String industry,
        String website,
        String phone,
        String address,
        String accountManager
) {
}
