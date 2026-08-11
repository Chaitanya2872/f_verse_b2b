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
    private String actedByUserId;
    private String actedByName;
    private String actedByEmail;
    private String actedAt;

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

    public String getActedByUserId() {
        return actedByUserId;
    }

    public void setActedByUserId(String actedByUserId) {
        this.actedByUserId = actedByUserId;
    }

    public String getActedByName() {
        return actedByName;
    }

    public void setActedByName(String actedByName) {
        this.actedByName = actedByName;
    }

    public String getActedByEmail() {
        return actedByEmail;
    }

    public void setActedByEmail(String actedByEmail) {
        this.actedByEmail = actedByEmail;
    }

    public String getActedAt() {
        return actedAt;
    }

    public void setActedAt(String actedAt) {
        this.actedAt = actedAt;
    }
}
