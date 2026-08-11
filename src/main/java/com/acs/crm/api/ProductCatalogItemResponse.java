package com.acs.crm.api;

public record ProductCatalogItemResponse(
        String id,
        String name,
        String category,
        String vendor,
        String sku
) {
}
