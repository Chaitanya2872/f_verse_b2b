package com.acs.crm.repository;

import com.acs.crm.model.PipelineStage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PipelineStageRepository extends JpaRepository<PipelineStage, String> {
    List<PipelineStage> findAllByActiveTrueOrderByDisplayOrderAsc();
}
