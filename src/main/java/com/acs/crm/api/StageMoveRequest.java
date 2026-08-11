package com.acs.crm.api;

public class StageMoveRequest {
    private String targetStageId;
    private String remarks;
    private boolean confirmed;

    public String getTargetStageId() {
        return targetStageId;
    }

    public void setTargetStageId(String targetStageId) {
        this.targetStageId = targetStageId;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public void setConfirmed(boolean confirmed) {
        this.confirmed = confirmed;
    }
}
