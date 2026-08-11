package com.acs.crm.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.acs.crm.api.DealResponse;
import com.acs.crm.model.ApprovalStep;
import com.acs.crm.model.Deal;
import com.acs.crm.model.Enums;
import com.acs.crm.model.Person;
import com.acs.crm.model.PipelineStage;
import com.acs.crm.repository.DealRepository;
import com.acs.crm.repository.DealStageHistoryRepository;
import com.acs.crm.repository.PipelineStageRepository;
import com.acs.crm.repository.PipelineStageTransitionRepository;
import com.acs.crm.repository.ProductCatalogRepository;
import com.acs.crm.repository.WarrantyItemRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

@ExtendWith(MockitoExtension.class)
class CrmServiceApprovalIdentityTest {

    @Mock private DealRepository dealRepository;
    @Mock private WarrantyItemRepository warrantyItemRepository;
    @Mock private PipelineStageRepository pipelineStageRepository;
    @Mock private PipelineStageTransitionRepository pipelineStageTransitionRepository;
    @Mock private DealStageHistoryRepository dealStageHistoryRepository;
    @Mock private ProductCatalogRepository productCatalogRepository;

    private CrmService crmService;

    @BeforeEach
    void setUp() {
        when(pipelineStageRepository.count()).thenReturn(1L);
        when(productCatalogRepository.count()).thenReturn(1L);
        crmService = new CrmService(
            dealRepository,
            warrantyItemRepository,
            pipelineStageRepository,
            pipelineStageTransitionRepository,
            dealStageHistoryRepository,
            productCatalogRepository
        );
    }

    @Test
    void rejectsApproverWithoutIdentityPermission() {
        CrmService.ApprovalActor financeUser = new CrmService.ApprovalActor(
            "finance-user", "Finance User", "finance@example.com",
            Set.of("feature.b2b.approvals.finance")
        );

        assertThatThrownBy(() -> crmService.updateApproval(
            "D-1", Enums.ApprovalRole.RSM, Enums.ApprovalStatus.approved, financeUser
        ))
            .isInstanceOf(AccessDeniedException.class)
            .hasMessageContaining("feature.b2b.approvals.rsm");
    }

    @Test
    void recordsAuthenticatedApproverOnDecision() {
        Deal deal = quotationDeal();
        when(dealRepository.findById("D-1")).thenReturn(Optional.of(deal));
        when(dealRepository.save(deal)).thenReturn(deal);
        CrmService.ApprovalActor rsm = new CrmService.ApprovalActor(
            "rsm-user", "Regional Manager", "rsm@example.com",
            Set.of("feature.b2b.approvals.review", "feature.b2b.approvals.rsm")
        );

        DealResponse response = crmService.updateApproval(
            "D-1", Enums.ApprovalRole.RSM, Enums.ApprovalStatus.approved, rsm
        );

        ApprovalStep step = response.approvals().get(0);
        assertThat(step.getStatus()).isEqualTo(Enums.ApprovalStatus.approved);
        assertThat(step.getActedByUserId()).isEqualTo("rsm-user");
        assertThat(step.getActedByName()).isEqualTo("Regional Manager");
        assertThat(step.getActedByEmail()).isEqualTo("rsm@example.com");
        assertThat(step.getActedAt()).isNotBlank();
    }

    private Deal quotationDeal() {
        PipelineStage stage = new PipelineStage();
        stage.setId("quotation");
        stage.setName("Quotation");
        stage.setProbabilityPercent(55);

        Deal deal = new Deal();
        deal.setId("D-1");
        deal.setCompany("Goa UWR");
        deal.setContact("Director");
        deal.setProduct("AI Ship Silencing");
        deal.setAccountManager(new Person("Sales Manager", "SM"));
        deal.setStage(stage);
        deal.setValue(10_000_000L);
        deal.setPriority(Enums.Priority.high);
        deal.setUpdatedAt(Instant.now().toString());
        deal.setApprovals(List.of(
            new ApprovalStep(Enums.ApprovalRole.RSM, Enums.ApprovalStatus.pending),
            new ApprovalStep(Enums.ApprovalRole.Finance, Enums.ApprovalStatus.pending),
            new ApprovalStep(Enums.ApprovalRole.BusinessHead, Enums.ApprovalStatus.pending)
        ));
        return deal;
    }
}
