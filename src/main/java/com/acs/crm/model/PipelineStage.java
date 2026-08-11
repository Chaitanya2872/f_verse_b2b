package com.acs.crm.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "pipeline_stages")
public class PipelineStage {

    @Id
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String shortLabel;

    @Column(nullable = false)
    private int displayOrder;

    @Column(nullable = false)
    private int probabilityPercent;

    @Column(nullable = false)
    private String color;

    @Column(nullable = false)
    private int maxExpectedDurationDays;

    @Column(nullable = false)
    private boolean active = true;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<String> mandatoryFields = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<String> requiredApprovals = new ArrayList<>();

    @OneToMany(mappedBy = "fromStage", fetch = FetchType.EAGER)
    @OrderBy("displayOrder asc")
    @JsonIgnore
    private List<PipelineStageTransition> allowedTransitions = new ArrayList<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
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

    public List<PipelineStageTransition> getAllowedTransitions() {
        return allowedTransitions;
    }

    public void setAllowedTransitions(List<PipelineStageTransition> allowedTransitions) {
        this.allowedTransitions = allowedTransitions;
    }
}
