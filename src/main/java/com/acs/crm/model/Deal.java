package com.acs.crm.model;

import com.acs.crm.model.Enums.Priority;
import com.acs.crm.model.Enums.RiskStatus;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "deals")
public class Deal {
    @Id
    private String id;
    private String company;
    private String contact;
    private String product;
    @Embedded
    private Person accountManager;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "stage_id")
    private PipelineStage stage;
    private long value;
    @Enumerated(EnumType.STRING)
    private Priority priority;
    private String updatedAt;
    private String expectedClosureDate;
    private String nextActivity;
    private String nextActivityDueDate;
    private String oemVendor;
    @Enumerated(EnumType.STRING)
    private RiskStatus riskStatus = RiskStatus.healthy;
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "deal_approvals", joinColumns = @JoinColumn(name = "deal_id"))
    @OrderColumn(name = "approval_order")
    private List<ApprovalStep> approvals = new ArrayList<>();
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, String> extraFields = new LinkedHashMap<>();

    public Deal() {
    }

    public Deal(String id, String company, String contact, String product, Person accountManager,
                PipelineStage stage, long value, Priority priority, String updatedAt, List<ApprovalStep> approvals) {
        this.id = id;
        this.company = company;
        this.contact = contact;
        this.product = product;
        this.accountManager = accountManager;
        this.stage = stage;
        this.value = value;
        this.priority = priority;
        this.updatedAt = updatedAt;
        this.approvals = approvals;
    }

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

    public Person getAccountManager() {
        return accountManager;
    }

    public void setAccountManager(Person accountManager) {
        this.accountManager = accountManager;
    }

    public PipelineStage getStage() {
        return stage;
    }

    public void setStage(PipelineStage stage) {
        this.stage = stage;
    }

    public long getValue() {
        return value;
    }

    public void setValue(long value) {
        this.value = value;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
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

    public RiskStatus getRiskStatus() {
        return riskStatus;
    }

    public void setRiskStatus(RiskStatus riskStatus) {
        this.riskStatus = riskStatus;
    }

    public List<ApprovalStep> getApprovals() {
        return approvals;
    }

    public void setApprovals(List<ApprovalStep> approvals) {
        this.approvals = approvals;
    }

    public Map<String, String> getExtraFields() {
        return extraFields;
    }

    public void setExtraFields(Map<String, String> extraFields) {
        this.extraFields = extraFields;
    }
}
