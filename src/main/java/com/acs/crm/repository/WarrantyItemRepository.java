package com.acs.crm.repository;

import com.acs.crm.model.WarrantyItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WarrantyItemRepository extends JpaRepository<WarrantyItem, String> {
}
