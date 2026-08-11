package com.acs.crm.api;

import java.util.List;

public record ProductCatalogSummaryResponse(
        List<String> categories,
        List<String> vendors
) {
}
