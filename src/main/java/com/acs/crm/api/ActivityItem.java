package com.acs.crm.api;

public class ActivityItem {
    private String id;
    private String company;
    private String stage;
    private String updatedAt;

    public ActivityItem() {
    }

    public ActivityItem(String id, String company, String stage, String updatedAt) {
        this.id = id;
        this.company = company;
        this.stage = stage;
        this.updatedAt = updatedAt;
    }

    public String getId() {
        return id;
    }

    public String getCompany() {
        return company;
    }

    public String getStage() {
        return stage;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }
}
