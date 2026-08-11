package com.acs.crm.repository;

import com.acs.crm.model.Contact;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContactRepository extends JpaRepository<Contact, String> {
    List<Contact> findAllByActiveTrueOrderByNameAsc();
}
