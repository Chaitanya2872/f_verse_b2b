package com.acs.crm.api;

import com.acs.crm.model.Deal;
import com.acs.crm.model.TrendPoint;
import com.acs.crm.model.WarrantyItem;
import com.acs.crm.service.CrmService;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class CrmController {

    private final CrmService crmService;

    public CrmController(CrmService crmService) {
        this.crmService = crmService;
    }

    @GetMapping("/health")
    public Map<String, Boolean> health() {
        return Map.of("ok", true);
    }

    @GetMapping("/deals")
    @PreAuthorize("hasAuthority('page.b2b')")
    public List<DealResponse> deals(@RequestParam(required = false) String search) {
        return crmService.getDeals(search);
    }

    @PostMapping("/deals")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('feature.b2b.deals.manage')")
    public DealResponse createDeal(@RequestBody CreateDealRequest request) {
        return crmService.createDeal(request);
    }

    @PutMapping("/deals/{dealId}")
    @PreAuthorize("hasAuthority('feature.b2b.deals.manage')")
    public DealResponse updateDeal(@PathVariable String dealId, @RequestBody UpdateDealRequest request) {
        return crmService.updateDeal(dealId, request);
    }

    @PostMapping(value = "/deals/import-excel", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('feature.b2b.deals.manage')")
    public ImportDealsResponse importDeals(
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "suspect") String defaultStage
    ) {
        return crmService.importDeals(file, defaultStage);
    }

    @GetMapping("/deals/import-excel/template")
    @PreAuthorize("hasAuthority('feature.b2b.deals.manage')")
    public ResponseEntity<Resource> downloadImportTemplate() {
        Resource workbook = new ClassPathResource("SWD-Prospects-Formatted-Updated.xlsx");
        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=CRM-Deals-Import-Sample.xlsx"
                )
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                ))
                .body(workbook);
    }

    @GetMapping("/pipeline/stages")
    @PreAuthorize("hasAuthority('page.b2b')")
    public List<PipelineStageResponse> pipelineStages() {
        return crmService.getPipelineStages();
    }

    @PutMapping("/pipeline/stages/{stageId}")
    @PreAuthorize("hasAuthority('feature.b2b.pipeline.manage')")
    public PipelineStageResponse updatePipelineStage(@PathVariable String stageId,
                                                     @RequestBody PipelineStageUpdateRequest request) {
        return crmService.updatePipelineStage(stageId, request);
    }

    @GetMapping("/products")
    @PreAuthorize("hasAuthority('page.b2b')")
    public List<ProductCatalogItemResponse> products(@RequestParam(required = false) String category,
                                                     @RequestParam(required = false) String vendor) {
        return crmService.getProducts(category, vendor);
    }

    @GetMapping("/products/summary")
    @PreAuthorize("hasAuthority('page.b2b')")
    public ProductCatalogSummaryResponse productSummary() {
        return crmService.getProductSummary();
    }

    @PostMapping("/products")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('feature.b2b.products.manage')")
    public ProductCatalogItemResponse createProduct(@RequestBody ProductCatalogRequest request) {
        return crmService.createProduct(request);
    }

    @PutMapping("/products/{productId}")
    @PreAuthorize("hasAuthority('feature.b2b.products.manage')")
    public ProductCatalogItemResponse updateProduct(@PathVariable String productId,
                                                     @RequestBody ProductCatalogRequest request) {
        return crmService.updateProduct(productId, request);
    }

    @DeleteMapping("/products/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('feature.b2b.products.manage')")
    public void deleteProduct(@PathVariable String productId) {
        crmService.deactivateProduct(productId);
    }

    @GetMapping("/accounts")
    @PreAuthorize("hasAuthority('page.b2b')")
    public List<AccountResponse> accounts(@RequestParam(required = false) String search) {
        return crmService.getAccounts(search);
    }

    @PostMapping("/accounts")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('feature.b2b.accounts.manage')")
    public AccountResponse createAccount(@RequestBody AccountRequest request) {
        return crmService.createAccount(request);
    }

    @PutMapping("/accounts/{accountId}")
    @PreAuthorize("hasAuthority('feature.b2b.accounts.manage')")
    public AccountResponse updateAccount(@PathVariable String accountId, @RequestBody AccountRequest request) {
        return crmService.updateAccount(accountId, request);
    }

    @DeleteMapping("/accounts/{accountId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('feature.b2b.accounts.manage')")
    public void deleteAccount(@PathVariable String accountId) {
        crmService.deactivateAccount(accountId);
    }

    @GetMapping("/contacts")
    @PreAuthorize("hasAuthority('page.b2b')")
    public List<ContactResponse> contacts(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String accountName
    ) {
        return crmService.getContacts(search, accountName);
    }

    @PostMapping("/contacts")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('feature.b2b.contacts.manage')")
    public ContactResponse createContact(@RequestBody ContactRequest request) {
        return crmService.createContact(request);
    }

    @PutMapping("/contacts/{contactId}")
    @PreAuthorize("hasAuthority('feature.b2b.contacts.manage')")
    public ContactResponse updateContact(@PathVariable String contactId, @RequestBody ContactRequest request) {
        return crmService.updateContact(contactId, request);
    }

    @DeleteMapping("/contacts/{contactId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('feature.b2b.contacts.manage')")
    public void deleteContact(@PathVariable String contactId) {
        crmService.deactivateContact(contactId);
    }

    @GetMapping("/leads")
    @PreAuthorize("hasAuthority('page.b2b')")
    public List<LeadResponse> leads(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status
    ) {
        return crmService.getLeads(search, status);
    }

    @PostMapping("/leads")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('feature.b2b.leads.manage')")
    public LeadResponse createLead(@RequestBody LeadRequest request) {
        return crmService.createLead(request);
    }

    @PutMapping("/leads/{leadId}")
    @PreAuthorize("hasAuthority('feature.b2b.leads.manage')")
    public LeadResponse updateLead(@PathVariable String leadId, @RequestBody LeadRequest request) {
        return crmService.updateLead(leadId, request);
    }

    @DeleteMapping("/leads/{leadId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('feature.b2b.leads.manage')")
    public void deleteLead(@PathVariable String leadId) {
        crmService.deactivateLead(leadId);
    }

    @PostMapping("/leads/{leadId}/convert")
    @PreAuthorize("hasAuthority('feature.b2b.leads.manage')")
    public ConvertLeadResponse convertLead(@PathVariable String leadId, @RequestBody ConvertLeadRequest request) {
        return crmService.convertLead(leadId, request);
    }

    @PatchMapping("/deals/{dealId}/approvals")
    @PreAuthorize("hasAuthority('feature.b2b.approvals.review')")
    public DealResponse updateApproval(
            @PathVariable String dealId,
            @RequestBody UpdateApprovalRequest request,
            Authentication authentication
    ) {
        com.acs.crm.security.JwtService.IdentityPrincipal principal =
                (com.acs.crm.security.JwtService.IdentityPrincipal) authentication.getPrincipal();
        CrmService.ApprovalActor actor = new CrmService.ApprovalActor(
                principal.userId(),
                principal.name(),
                principal.email(),
                authentication.getAuthorities().stream().map(Object::toString).collect(java.util.stream.Collectors.toSet())
        );
        return crmService.updateApproval(
                dealId,
                request.getRole(),
                normalizeStatus(request.getStatus()),
                actor
        );
    }

    @PatchMapping("/deals/{dealId}/stage")
    @PreAuthorize("hasAuthority('feature.b2b.deals.manage')")
    public DealResponse moveDealStage(@PathVariable String dealId, @RequestBody StageMoveRequest request) {
        return crmService.moveDealToStage(dealId, request);
    }

    @GetMapping("/deals/{dealId}/stage-history")
    @PreAuthorize("hasAuthority('page.b2b')")
    public List<DealStageHistoryResponse> dealStageHistory(@PathVariable String dealId) {
        return crmService.getDealStageHistory(dealId);
    }

    @GetMapping("/approvals")
    @PreAuthorize("hasAuthority('page.b2b')")
    public List<DealResponse> approvals() {
        return crmService.getPendingApprovals();
    }

    @GetMapping("/warranty-items")
    @PreAuthorize("hasAuthority('page.b2b')")
    public List<WarrantyItem> warrantyItems() {
        return crmService.getWarrantyItems();
    }

    @GetMapping("/dashboard/summary")
    @PreAuthorize("hasAuthority('page.b2b')")
    public DashboardSummary summary() {
        return crmService.getDashboardSummary();
    }

    @GetMapping("/dashboard/trend")
    @PreAuthorize("hasAuthority('page.b2b')")
    public List<TrendPoint> trend() {
        return crmService.getTrend();
    }

    @GetMapping("/dashboard/activity")
    @PreAuthorize("hasAuthority('page.b2b')")
    public List<ActivityItem> activity() {
        return crmService.getActivity();
    }

    private com.acs.crm.model.Enums.ApprovalStatus normalizeStatus(com.acs.crm.model.Enums.ApprovalStatus status) {
        return status == null ? com.acs.crm.model.Enums.ApprovalStatus.pending : status;
    }
}
