package com.acs.crm.repository;

import com.acs.crm.model.PipelineStageTransition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PipelineStageTransitionRepository extends JpaRepository<PipelineStageTransition, Long> {
    List<PipelineStageTransition> findAllByFromStageIdOrderByDisplayOrderAsc(String fromStageId);

    void deleteByFromStageId(String fromStageId);
}
