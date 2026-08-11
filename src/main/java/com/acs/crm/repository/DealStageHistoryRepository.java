package com.acs.crm.repository;

import com.acs.crm.model.DealStageHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DealStageHistoryRepository extends JpaRepository<DealStageHistory, Long> {
    List<DealStageHistory> findAllByDealIdOrderByChangedAtDesc(String dealId);
}
