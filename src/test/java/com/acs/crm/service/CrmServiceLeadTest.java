package com.acs.crm.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.acs.crm.api.ConvertLeadRequest;
import com.acs.crm.api.ConvertLeadResponse;
import com.acs.crm.api.LeadRequest;
import com.acs.crm.api.LeadResponse;
import com.acs.crm.model.Account;
import com.acs.crm.model.Contact;
import com.acs.crm.model.Deal;
import com.acs.crm.model.Enums;
import com.acs.crm.model.Lead;
import com.acs.crm.model.PipelineStage;
import com.acs.crm.repository.AccountRepository;
import com.acs.crm.repository.ContactRepository;
import com.acs.crm.repository.DealRepository;
import com.acs.crm.repository.DealStageHistoryRepository;
import com.acs.crm.repository.LeadRepository;
import com.acs.crm.repository.PipelineStageRepository;
import com.acs.crm.repository.PipelineStageTransitionRepository;
import com.acs.crm.repository.ProductCatalogRepository;
import com.acs.crm.repository.WarrantyItemRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CrmServiceLeadTest {

    @Mock private DealRepository dealRepository;
    @Mock private WarrantyItemRepository warrantyItemRepository;
    @Mock private PipelineStageRepository pipelineStageRepository;
    @Mock private PipelineStageTransitionRepository pipelineStageTransitionRepository;
    @Mock private DealStageHistoryRepository dealStageHistoryRepository;
    @Mock private ProductCatalogRepository productCatalogRepository;
    @Mock private AccountRepository accountRepository;
    @Mock private ContactRepository contactRepository;
    @Mock private LeadRepository leadRepository;

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
            productCatalogRepository,
            accountRepository,
            contactRepository,
            leadRepository
        );
    }

    private LeadRequest leadRequest(String company) {
        LeadRequest request = new LeadRequest();
        request.setCompany(company);
        request.setContactName("Ravi Teja");
        request.setEmail("ravi@example.com");
        request.setPhone("9999999999");
        request.setSource("Website enquiry");
        request.setOwner("Asha Rao");
        request.setScore(40);
        return request;
    }

    @Test
    void createLeadDefaultsToNewStatus() {
        when(leadRepository.save(any(Lead.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LeadResponse response = crmService.createLead(leadRequest("Vindhya Auto"));

        assertThat(response.status()).isEqualTo(Enums.LeadStatus.new_lead);
        assertThat(response.company()).isEqualTo("Vindhya Auto");
        assertThat(response.createdAt()).isNotBlank();
    }

    @Test
    void createLeadRejectsDirectConvertedStatus() {
        LeadRequest request = leadRequest("Vindhya Auto");
        request.setStatus(Enums.LeadStatus.converted);

        assertThatThrownBy(() -> crmService.createLead(request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("convert endpoint");
    }

    @Test
    void convertLeadRejectsAlreadyConvertedLead() {
        Lead lead = new Lead();
        lead.setId("LEAD-1");
        lead.setCompany("Vindhya Auto");
        lead.setStatus(Enums.LeadStatus.converted);
        when(leadRepository.findById("LEAD-1")).thenReturn(Optional.of(lead));

        ConvertLeadRequest request = new ConvertLeadRequest();
        request.setAccountName("Vindhya Auto");

        assertThatThrownBy(() -> crmService.convertLead("LEAD-1", request))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("already converted");
    }

    @Test
    void convertLeadCreatesAccountContactAndOpportunity() {
        Lead lead = new Lead();
        lead.setId("LEAD-1");
        lead.setCompany("Vindhya Auto");
        lead.setContactName("Ravi Teja");
        lead.setEmail("ravi@example.com");
        lead.setPhone("9999999999");
        lead.setOwner("Asha Rao");
        lead.setStatus(Enums.LeadStatus.qualified);
        when(leadRepository.findById("LEAD-1")).thenReturn(Optional.of(lead));
        when(leadRepository.save(any(Lead.class))).thenAnswer(invocation -> invocation.getArgument(0));

        when(accountRepository.findAllByActiveTrueOrderByNameAsc()).thenReturn(List.of());
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        when(contactRepository.findAllByActiveTrueOrderByNameAsc()).thenReturn(List.of());
        when(contactRepository.save(any(Contact.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PipelineStage stage = new PipelineStage();
        stage.setId("suspect");
        stage.setName("Suspect");
        stage.setProbabilityPercent(10);
        stage.setMaxExpectedDurationDays(10);
        when(pipelineStageRepository.findAllByActiveTrueOrderByDisplayOrderAsc()).thenReturn(List.of(stage));
        when(dealRepository.save(any(Deal.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ConvertLeadRequest request = new ConvertLeadRequest();
        request.setAccountName("Vindhya Auto");
        request.setCreateOpportunity(true);
        request.setProduct("Enterprise Wi-Fi");
        request.setValue(500_000);

        ConvertLeadResponse response = crmService.convertLead("LEAD-1", request);

        assertThat(response.accountName()).isEqualTo("Vindhya Auto");
        assertThat(response.contactName()).isEqualTo("Ravi Teja");
        assertThat(response.dealId()).isNotBlank();
        assertThat(lead.getStatus()).isEqualTo(Enums.LeadStatus.converted);
        assertThat(lead.getConvertedAccountId()).isNotBlank();
        assertThat(lead.getConvertedContactId()).isNotBlank();
        assertThat(lead.getConvertedDealId()).isNotBlank();
    }

    @Test
    void convertLeadLinksExistingAccountByNameCaseInsensitively() {
        Lead lead = new Lead();
        lead.setId("LEAD-2");
        lead.setCompany("Goa UWR");
        lead.setStatus(Enums.LeadStatus.qualified);
        when(leadRepository.findById("LEAD-2")).thenReturn(Optional.of(lead));
        when(leadRepository.save(any(Lead.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Account existing = new Account();
        existing.setId("ACC-EXIST");
        existing.setName("goa uwr");
        when(accountRepository.findAllByActiveTrueOrderByNameAsc()).thenReturn(List.of(existing));

        ConvertLeadRequest request = new ConvertLeadRequest();
        request.setAccountName("Goa UWR");

        ConvertLeadResponse response = crmService.convertLead("LEAD-2", request);

        assertThat(response.accountId()).isEqualTo("ACC-EXIST");
        assertThat(response.dealId()).isNull();
    }
}
