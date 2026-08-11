package com.acs.crm.model;

public class TrendPoint {
    private String label;
    private long value;

    public TrendPoint() {
    }

    public TrendPoint(String label, long value) {
        this.label = label;
        this.value = value;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public long getValue() {
        return value;
    }

    public void setValue(long value) {
        this.value = value;
    }
}
