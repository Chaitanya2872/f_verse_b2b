package com.acs.crm.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.acs.crm.api.ImportDealsResponse;
import com.acs.crm.model.Deal;
import com.acs.crm.model.Enums;
import com.acs.crm.model.PipelineStage;
import com.acs.crm.repository.DealRepository;
import com.acs.crm.repository.DealStageHistoryRepository;
import com.acs.crm.repository.PipelineStageRepository;
import com.acs.crm.repository.PipelineStageTransitionRepository;
import com.acs.crm.repository.ProductCatalogRepository;
import com.acs.crm.repository.WarrantyItemRepository;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

@ExtendWith(MockitoExtension.class)
class CrmServiceWorkbookImportTest {

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
        when(pipelineStageRepository.findById(anyString())).thenAnswer(invocation -> {
            String id = invocation.getArgument(0);
            PipelineStage stage = new PipelineStage();
            stage.setId(id);
            stage.setName(id);
            stage.setProbabilityPercent(50);
            return Optional.of(stage);
        });
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
    @SuppressWarnings({"rawtypes", "unchecked"})
    void importsFormattedSwdProspectSheet() throws Exception {
        try (InputStream input = getClass().getResourceAsStream("/SWD-Prospects-Formatted-Updated.xlsx")) {
            assertThat(input).isNotNull();
            MockMultipartFile file = new MockMultipartFile(
                "file",
                "SWD-Prospects-Formatted-Updated.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                input
            );

            ImportDealsResponse response = crmService.importDeals(file, "suspect");

            assertThat(response.importedCount()).isEqualTo(15);
            assertThat(response.skippedRows()).isEqualTo(2);
            assertThat(response.detectedHeaders()).contains(
                "prospect", "solution", "current stage", "value (₹ lakhs)", "est. closure"
            );
            assertThat(response.dynamicHeaders()).containsExactly("s. no.", "reference", "remarks");

            ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);
            verify(dealRepository).saveAll(captor.capture());
            List<Deal> imported = (List<Deal>) captor.getValue();
            Deal goa = imported.stream()
                .filter(deal -> "Goa UWR".equals(deal.getCompany()))
                .findFirst()
                .orElseThrow();

            assertThat(goa.getProduct()).isEqualTo("AI Ship Silencing");
            assertThat(goa.getStage().getId()).isEqualTo("solutioning");
            assertThat(goa.getValue()).isEqualTo(10_000_000L);
            assertThat(goa.getPriority()).isEqualTo(Enums.Priority.high);
            assertThat(goa.getExpectedClosureDate()).isEqualTo("2026-11-30");
            assertThat(goa.getExtraFields())
                .containsEntry("reference", "Cdr (Retd) Bandari")
                .containsEntry("remarks", "Demo scheduled in week of 24 Aug");

            Deal dlrl = imported.stream()
                .filter(deal -> "DLRL".equals(deal.getCompany()))
                .findFirst()
                .orElseThrow();
            assertThat(dlrl.getStage().getId()).isEqualTo("quotation");
            assertThat(dlrl.getApprovals())
                .extracting(step -> step.getRole())
                .containsExactly(
                    Enums.ApprovalRole.RSM,
                    Enums.ApprovalRole.Finance,
                    Enums.ApprovalRole.BusinessHead
                );
        }
    }
}
