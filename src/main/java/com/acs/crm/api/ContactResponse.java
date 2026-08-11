package com.acs.crm.api;

public record ContactResponse(
        String id,
        String name,
        String email,
        String phone,
        String title,
        String accountName
) {
}
