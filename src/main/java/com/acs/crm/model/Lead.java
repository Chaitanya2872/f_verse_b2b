package com.acs.crm.model;

import com.acs.crm.model.Enums.LeadStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "leads")
public class Lead {

    @Id
    private String id;

    @Column(nullable = false)
    private String company;

    private String contactName;
    private String email;
    private String phone;
    private String source;
    private String owner;

    @Enumerated(EnumType.STRING)
    private LeadStatus status = LeadStatus.new_lead;

    private int score;
    private String notes;
    private String createdAt;
    private String updatedAt;

    private String convertedAccountId;
    private String convertedContactId;
    private String convertedDealId;

    @Column(nullable = false)
    private boolean active = true;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public LeadStatus getStatus() {
        return status;
    }

    public void setStatus(LeadStatus status) {
        this.status = status;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getConvertedAccountId() {
        return convertedAccountId;
    }

    public void setConvertedAccountId(String convertedAccountId) {
        this.convertedAccountId = convertedAccountId;
    }

    public String getConvertedContactId() {
        return convertedContactId;
    }

    public void setConvertedContactId(String convertedContactId) {
        this.convertedContactId = convertedContactId;
    }

    public String getConvertedDealId() {
        return convertedDealId;
    }

    public void setConvertedDealId(String convertedDealId) {
        this.convertedDealId = convertedDealId;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
