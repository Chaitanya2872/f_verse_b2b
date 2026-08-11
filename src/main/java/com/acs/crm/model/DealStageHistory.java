package com.acs.crm.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "deal_stage_history")
public class DealStageHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String dealId;

    @Column(nullable = false)
    private String fromStage;

    @Column(nullable = false)
    private String toStage;

    @Column(nullable = false)
    private String changedAt;

    @Column(nullable = false)
    private String changedBy;

    private String remarks;

    public DealStageHistory() {
    }

    public DealStageHistory(String dealId, String fromStage, String toStage, String changedAt, String changedBy, String remarks) {
        this.dealId = dealId;
        this.fromStage = fromStage;
        this.toStage = toStage;
        this.changedAt = changedAt;
        this.changedBy = changedBy;
        this.remarks = remarks;
    }

    public Long getId() {
        return id;
    }

    public String getDealId() {
        return dealId;
    }

    public String getFromStage() {
        return fromStage;
    }

    public String getToStage() {
        return toStage;
    }

    public String getChangedAt() {
        return changedAt;
    }

    public String getChangedBy() {
        return changedBy;
    }

    public String getRemarks() {
        return remarks;
    }
}
