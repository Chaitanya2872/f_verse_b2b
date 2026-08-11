package com.acs.crm.api;

import java.util.ArrayList;
import java.util.List;

public class PipelineStageUpdateRequest {
    private String name;
    private String shortLabel;
    private int displayOrder;
    private int probabilityPercent;
    private String color;
    private int maxExpectedDurationDays;
    private List<String> mandatoryFields = new ArrayList<>();
    private List<String> requiredApprovals = new ArrayList<>();
    private List<AllowedStageTransitionRequest> allowedNextStages = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShortLabel() {
        return shortLabel;
    }

    public void setShortLabel(String shortLabel) {
        this.shortLabel = shortLabel;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }

    public int getProbabilityPercent() {
        return probabilityPercent;
    }

    public void setProbabilityPercent(int probabilityPercent) {
        this.probabilityPercent = probabilityPercent;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public int getMaxExpectedDurationDays() {
        return maxExpectedDurationDays;
    }

    public void setMaxExpectedDurationDays(int maxExpectedDurationDays) {
        this.maxExpectedDurationDays = maxExpectedDurationDays;
    }

    public List<String> getMandatoryFields() {
        return mandatoryFields;
    }

    public void setMandatoryFields(List<String> mandatoryFields) {
        this.mandatoryFields = mandatoryFields;
    }

    public List<String> getRequiredApprovals() {
        return requiredApprovals;
    }

    public void setRequiredApprovals(List<String> requiredApprovals) {
        this.requiredApprovals = requiredApprovals;
    }

    public List<AllowedStageTransitionRequest> getAllowedNextStages() {
        return allowedNextStages;
    }

    public void setAllowedNextStages(List<AllowedStageTransitionRequest> allowedNextStages) {
        this.allowedNextStages = allowedNextStages;
    }

    public static class AllowedStageTransitionRequest {
        private String stageId;
        private boolean confirmationRequired;

        public String getStageId() {
            return stageId;
        }

        public void setStageId(String stageId) {
            this.stageId = stageId;
        }

        public boolean isConfirmationRequired() {
            return confirmationRequired;
        }

        public void setConfirmationRequired(boolean confirmationRequired) {
            this.confirmationRequired = confirmationRequired;
        }
    }
}
