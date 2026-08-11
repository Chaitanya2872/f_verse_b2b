package com.acs.crm.repository;

import com.acs.crm.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AccountRepository extends JpaRepository<Account, String> {
    List<Account> findAllByActiveTrueOrderByNameAsc();
}
