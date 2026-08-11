package com.acs.crm.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "pipeline_stage_transitions")
public class PipelineStageTransition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_stage_id", nullable = false)
    private PipelineStage fromStage;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "to_stage_id", nullable = false)
    private PipelineStage toStage;

    @Column(nullable = false)
    private int displayOrder;

    @Column(nullable = false)
    private boolean confirmationRequired;

    public Long getId() {
        return id;
    }

    public PipelineStage getFromStage() {
        return fromStage;
    }

    public void setFromStage(PipelineStage fromStage) {
        this.fromStage = fromStage;
    }

    public PipelineStage getToStage() {
        return toStage;
    }

    public void setToStage(PipelineStage toStage) {
        this.toStage = toStage;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }

    public boolean isConfirmationRequired() {
        return confirmationRequired;
    }

    public void setConfirmationRequired(boolean confirmationRequired) {
        this.confirmationRequired = confirmationRequired;
    }
}
