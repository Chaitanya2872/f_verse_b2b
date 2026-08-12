package com.acs.crm.repository;

import com.acs.crm.model.Lead;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LeadRepository extends JpaRepository<Lead, String> {
    List<Lead> findAllByActiveTrueOrderByUpdatedAtDesc();
}
