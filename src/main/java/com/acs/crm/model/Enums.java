package com.acs.crm.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public final class Enums {

    private Enums() {
    }

    public enum StageId {
        suspect,
        prospect,
        quotation,
        negotiation,
        order,
        delivery,
        invoicing,
        payment
    }

    public enum Priority {
        low,
        medium,
        high
    }

    public enum RiskStatus {
        healthy,
        attention,
        overdue,
        stalled,
        high_risk
    }

    public enum ApprovalRole {
        /** Legacy persistence value only; rejected by approval APIs and omitted from responses. */
        Solution,
        RSM,
        Finance,
        @JsonProperty("Business Head")
        BusinessHead
    }

    public enum ApprovalStatus {
        pending,
        approved,
        rejected
    }

    public enum WarrantyStatus {
        active,
        expiring,
        expired
    }

    public enum AmcStatus {
        active,
        due,
        none
    }
}
