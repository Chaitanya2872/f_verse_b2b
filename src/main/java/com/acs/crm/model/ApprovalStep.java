package com.acs.crm.model;

import com.acs.crm.model.Enums.ApprovalRole;
import com.acs.crm.model.Enums.ApprovalStatus;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Embeddable
public class ApprovalStep {
    @Enumerated(EnumType.STRING)
    private ApprovalRole role;
    @Enumerated(EnumType.STRING)
    private ApprovalStatus status;

    public ApprovalStep() {
    }

    public ApprovalStep(ApprovalRole role, ApprovalStatus status) {
        this.role = role;
        this.status = status;
    }

    public ApprovalRole getRole() {
        return role;
    }

    public void setRole(ApprovalRole role) {
        this.role = role;
    }

    public ApprovalStatus getStatus() {
        return status;
    }

    public void setStatus(ApprovalStatus status) {
        this.status = status;
    }
}
