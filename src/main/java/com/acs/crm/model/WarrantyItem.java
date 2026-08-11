package com.acs.crm.model;

import com.acs.crm.model.Enums.AmcStatus;
import com.acs.crm.model.Enums.WarrantyStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "warranty_items")
public class WarrantyItem {
    @Id
    private String id;
    private String company;
    private String product;
    private String serialNumber;
    private String startDate;
    private String endDate;
    @Enumerated(EnumType.STRING)
    private WarrantyStatus status;
    @Enumerated(EnumType.STRING)
    private AmcStatus amcStatus;

    public WarrantyItem() {
    }

    public WarrantyItem(String id, String company, String product, String serialNumber,
                        String startDate, String endDate, WarrantyStatus status, AmcStatus amcStatus) {
        this.id = id;
        this.company = company;
        this.product = product;
        this.serialNumber = serialNumber;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.amcStatus = amcStatus;
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

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public WarrantyStatus getStatus() {
        return status;
    }

    public void setStatus(WarrantyStatus status) {
        this.status = status;
    }

    public AmcStatus getAmcStatus() {
        return amcStatus;
    }

    public void setAmcStatus(AmcStatus amcStatus) {
        this.amcStatus = amcStatus;
    }
}
