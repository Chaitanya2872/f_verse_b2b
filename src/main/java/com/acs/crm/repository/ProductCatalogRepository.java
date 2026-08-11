package com.acs.crm.repository;

import com.acs.crm.model.ProductCatalogItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductCatalogRepository extends JpaRepository<ProductCatalogItem, String> {
    List<ProductCatalogItem> findAllByActiveTrueOrderByCategoryAscVendorAscNameAsc();
}
