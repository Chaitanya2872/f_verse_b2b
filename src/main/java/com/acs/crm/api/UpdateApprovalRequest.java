package com.acs.crm.api;

import com.acs.crm.model.Enums.ApprovalRole;
import com.acs.crm.model.Enums.ApprovalStatus;

public class UpdateApprovalRequest {
    private ApprovalRole role;
    private ApprovalStatus status;

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
