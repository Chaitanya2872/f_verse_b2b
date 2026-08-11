package com.acs.crm.api;

import com.acs.crm.model.Enums.Priority;

public class CreateDealRequest {
    private String company;
    private String contact;
    private String product;
    private long value;
    private String accountManager;
    private Priority priority;
    private String stage;
    private String expectedClosureDate;
    private String nextActivity;
    private String nextActivityDueDate;
    private String oemVendor;

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public long getValue() {
        return value;
    }

    public void setValue(long value) {
        this.value = value;
    }

    public String getAccountManager() {
        return accountManager;
    }

    public void setAccountManager(String accountManager) {
        this.accountManager = accountManager;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public String getStage() {
        return stage;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }

    public String getExpectedClosureDate() {
        return expectedClosureDate;
    }

    public void setExpectedClosureDate(String expectedClosureDate) {
        this.expectedClosureDate = expectedClosureDate;
    }

    public String getNextActivity() {
        return nextActivity;
    }

    public void setNextActivity(String nextActivity) {
        this.nextActivity = nextActivity;
    }

    public String getNextActivityDueDate() {
        return nextActivityDueDate;
    }

    public void setNextActivityDueDate(String nextActivityDueDate) {
        this.nextActivityDueDate = nextActivityDueDate;
    }

    public String getOemVendor() {
        return oemVendor;
    }

    public void setOemVendor(String oemVendor) {
        this.oemVendor = oemVendor;
    }
}
