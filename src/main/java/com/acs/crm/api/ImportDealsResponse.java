package com.acs.crm.api;

import java.util.List;

public record ImportDealsResponse(
        int importedCount,
        int skippedRows,
        List<String> detectedHeaders,
        List<String> dynamicHeaders
) {
}
