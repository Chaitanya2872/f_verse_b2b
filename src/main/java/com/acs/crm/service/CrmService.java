package com.acs.crm.service;

import com.acs.crm.api.AccountRequest;
import com.acs.crm.api.AccountResponse;
import com.acs.crm.api.ActivityItem;
import com.acs.crm.api.AllowedStageTransitionResponse;
import com.acs.crm.api.ContactRequest;
import com.acs.crm.api.ContactResponse;
import com.acs.crm.api.ConvertLeadRequest;
import com.acs.crm.api.ConvertLeadResponse;
import com.acs.crm.api.CreateDealRequest;
import com.acs.crm.api.DashboardSummary;
import com.acs.crm.api.DealResponse;
import com.acs.crm.api.DealStageHistoryResponse;
import com.acs.crm.api.ImportDealsResponse;
import com.acs.crm.api.LeadRequest;
import com.acs.crm.api.LeadResponse;
import com.acs.crm.api.PersonResponse;
import com.acs.crm.api.PipelineStageResponse;
import com.acs.crm.api.PipelineStageUpdateRequest;
import com.acs.crm.api.ProductCatalogItemResponse;
import com.acs.crm.api.ProductCatalogRequest;
import com.acs.crm.api.ProductCatalogSummaryResponse;
import com.acs.crm.api.StageMoveRequest;
import com.acs.crm.api.UpdateDealRequest;
import com.acs.crm.model.Account;
import com.acs.crm.model.ApprovalStep;
import com.acs.crm.model.Contact;
import com.acs.crm.model.Deal;
import com.acs.crm.model.DealStageHistory;
import com.acs.crm.model.Enums;
import com.acs.crm.model.Lead;
import com.acs.crm.model.Person;
import com.acs.crm.model.PipelineStage;
import com.acs.crm.model.PipelineStageTransition;
import com.acs.crm.model.ProductCatalogItem;
import com.acs.crm.model.TrendPoint;
import com.acs.crm.model.WarrantyItem;
import com.acs.crm.repository.AccountRepository;
import com.acs.crm.repository.ContactRepository;
import com.acs.crm.repository.DealStageHistoryRepository;
import com.acs.crm.repository.DealRepository;
import com.acs.crm.repository.LeadRepository;
import com.acs.crm.repository.PipelineStageRepository;
import com.acs.crm.repository.PipelineStageTransitionRepository;
import com.acs.crm.repository.ProductCatalogRepository;
import com.acs.crm.repository.WarrantyItemRepository;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.WeekFields;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CrmService {

    private static final Map<String, Set<String>> HEADER_ALIASES = Map.of(
            "company", Set.of("company", "customer", "account", "client", "organization", "prospect"),
            "contact", Set.of("contact", "contact person", "customer contact", "contact name"),
            "product", Set.of("product", "requirement", "item", "solution", "opportunity"),
            "accountManager", Set.of("account manager", "owner", "salesperson", "sales person", "manager", "reference"),
            "stage", Set.of("stage", "pipeline stage", "sales stage", "current stage"),
            "value", Set.of("value", "amount", "deal value", "opportunity value", "value (₹ lakhs)", "value (lakhs)"),
            "priority", Set.of("priority", "deal priority", "probability"),
            "expectedClosureDate", Set.of("expected closure", "expected closure date", "est. closure", "est closure")
    );

    private static final Map<String, String> STAGE_ALIASES = Map.of(
            "initial", "suspect",
            "discussed", "prospect",
            "budget approval", "qualified",
            "user gsqr", "solutioning",
            "spec finalisation", "solutioning",
            "pilot demo", "solutioning",
            "proposalshared", "quotation",
            "proposal shared", "quotation",
            "rfp publication", "quotation"
    );

    private final DealRepository dealRepository;
    private final WarrantyItemRepository warrantyItemRepository;
    private final PipelineStageRepository pipelineStageRepository;
    private final PipelineStageTransitionRepository pipelineStageTransitionRepository;
    private final DealStageHistoryRepository dealStageHistoryRepository;
    private final ProductCatalogRepository productCatalogRepository;
    private final AccountRepository accountRepository;
    private final ContactRepository contactRepository;
    private final LeadRepository leadRepository;
    private final DataFormatter dataFormatter = new DataFormatter();

    public CrmService(
            DealRepository dealRepository,
            WarrantyItemRepository warrantyItemRepository,
            PipelineStageRepository pipelineStageRepository,
            PipelineStageTransitionRepository pipelineStageTransitionRepository,
            DealStageHistoryRepository dealStageHistoryRepository,
            ProductCatalogRepository productCatalogRepository,
            AccountRepository accountRepository,
            ContactRepository contactRepository,
            LeadRepository leadRepository
    ) {
        this.dealRepository = dealRepository;
        this.warrantyItemRepository = warrantyItemRepository;
        this.pipelineStageRepository = pipelineStageRepository;
        this.pipelineStageTransitionRepository = pipelineStageTransitionRepository;
        this.dealStageHistoryRepository = dealStageHistoryRepository;
        this.productCatalogRepository = productCatalogRepository;
        this.accountRepository = accountRepository;
        this.contactRepository = contactRepository;
        this.leadRepository = leadRepository;
        ensureDefaultStages();
        ensureDefaultProducts();
    }

    @Transactional(readOnly = true)
    public List<DealResponse> getDeals(String search) {
        String query = search == null ? "" : search.trim().toLowerCase(Locale.ENGLISH);
        List<Deal> deals = dealRepository.findAll().stream()
                .sorted(Comparator.comparing(Deal::getUpdatedAt).reversed())
                .toList();
        if (query.isBlank()) {
            return deals.stream().map(this::toDealResponse).toList();
        }

        return deals.stream()
                .filter(deal -> deal.getCompany().toLowerCase(Locale.ENGLISH).contains(query)
                        || deal.getProduct().toLowerCase(Locale.ENGLISH).contains(query)
                        || deal.getAccountManager().getName().toLowerCase(Locale.ENGLISH).contains(query))
                .map(this::toDealResponse)
                .toList();
    }

    @Transactional
    public DealResponse createDeal(CreateDealRequest request) {
        Deal deal = new Deal();
        deal.setId("D-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ENGLISH));
        populateDealFromCreateRequest(deal, request);
        deal.setUpdatedAt(Instant.now().toString());
        deal.setApprovals(defaultApprovals(deal.getStage().getId()));
        deal.setExtraFields(new LinkedHashMap<>());
        deal.setRiskStatus(calculateRiskStatus(deal));

        return toDealResponse(dealRepository.save(deal));
    }

    @Transactional
    public DealResponse updateDeal(String dealId, UpdateDealRequest request) {
        Deal deal = dealRepository.findById(dealId)
                .orElseThrow(() -> new IllegalArgumentException("Deal not found"));

        populateDealFromUpdateRequest(deal, request);
        deal.setUpdatedAt(Instant.now().toString());
        deal.setRiskStatus(calculateRiskStatus(deal));
        validateRequiredFields(deal, deal.getStage());

        return toDealResponse(dealRepository.save(deal));
    }

    @Transactional
    public DealResponse updateApproval(
            String dealId,
            Enums.ApprovalRole role,
            Enums.ApprovalStatus status,
            ApprovalActor actor
    ) {
        if (role == null || role == Enums.ApprovalRole.Solution) {
            throw new IllegalArgumentException("Approval role must be RSM, Finance, or Business Head");
        }
        String requiredAuthority = switch (role) {
            case RSM -> "feature.b2b.approvals.rsm";
            case Finance -> "feature.b2b.approvals.finance";
            case BusinessHead -> "feature.b2b.approvals.business-head";
            case Solution -> throw new IllegalArgumentException("Solution is a requirement, not an approval role");
        };
        if (actor == null || !actor.authorities().contains(requiredAuthority)) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Missing identity permission: " + requiredAuthority
            );
        }
        Deal deal = dealRepository.findById(dealId)
                .orElseThrow(() -> new IllegalArgumentException("Deal not found"));

        int stepIndex = -1;
        for (int i = 0; i < deal.getApprovals().size(); i++) {
            if (deal.getApprovals().get(i).getRole() == role) {
                stepIndex = i;
                break;
            }
        }

        if (stepIndex < 0) {
            throw new IllegalArgumentException("Approval step not found");
        }

        for (int i = stepIndex; i < deal.getApprovals().size(); i++) {
            ApprovalStep step = deal.getApprovals().get(i);
            if (i == stepIndex) {
                applyApprovalDecision(step, status, actor);
            } else if (status == Enums.ApprovalStatus.rejected) {
                applyApprovalDecision(step, Enums.ApprovalStatus.rejected, actor);
            }
        }

        deal.setUpdatedAt(Instant.now().toString());
        deal.setRiskStatus(calculateRiskStatus(deal));
        return toDealResponse(dealRepository.save(deal));
    }

    private void applyApprovalDecision(
            ApprovalStep step,
            Enums.ApprovalStatus status,
            ApprovalActor actor
    ) {
        step.setStatus(status);
        if (status == Enums.ApprovalStatus.pending) {
            step.setActedByUserId(null);
            step.setActedByName(null);
            step.setActedByEmail(null);
            step.setActedAt(null);
            return;
        }
        step.setActedByUserId(actor.userId());
        step.setActedByName(actor.name());
        step.setActedByEmail(actor.email());
        step.setActedAt(Instant.now().toString());
    }

    public record ApprovalActor(
            String userId,
            String name,
            String email,
            Set<String> authorities
    ) {
        public ApprovalActor {
            authorities = authorities == null ? Set.of() : Set.copyOf(authorities);
        }
    }

    @Transactional(readOnly = true)
    public List<DealResponse> getPendingApprovals() {
        return dealRepository.findAll().stream()
                .filter(deal -> deal.getApprovals().stream().anyMatch(step ->
                        step.getRole() != Enums.ApprovalRole.Solution
                                && step.getStatus() == Enums.ApprovalStatus.pending))
                .sorted(Comparator.comparing(Deal::getUpdatedAt).reversed())
                .map(this::toDealResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<WarrantyItem> getWarrantyItems() {
        return warrantyItemRepository.findAll();
    }

    @Transactional(readOnly = true)
    public DashboardSummary getDashboardSummary() {
        List<Deal> deals = dealRepository.findAll();
        long totalValue = deals.stream().mapToLong(Deal::getValue).sum();
        long weightedPipelineValue = deals.stream().mapToLong(this::calculateWeightedValue).sum();
        int pendingApprovals = (int) deals.stream()
                .filter(deal -> deal.getApprovals().stream().anyMatch(step -> step.getStatus() == Enums.ApprovalStatus.pending))
                .count();
        int inDelivery = (int) deals.stream().filter(deal -> "procurement_fulfilment".equals(deal.getStage().getId())).count();
        int stalledDeals = (int) deals.stream().filter(deal -> calculateRiskStatus(deal) == Enums.RiskStatus.stalled).count();

        Map<String, Integer> funnelCounts = new LinkedHashMap<>();
        for (PipelineStage stage : pipelineStageRepository.findAllByActiveTrueOrderByDisplayOrderAsc()) {
            int count = (int) deals.stream().filter(deal -> deal.getStage().getId().equals(stage.getId())).count();
            funnelCounts.put(stage.getId(), count);
        }

        return new DashboardSummary(totalValue, weightedPipelineValue, deals.size(), pendingApprovals, inDelivery, stalledDeals, funnelCounts);
    }

    @Transactional(readOnly = true)
    public List<TrendPoint> getTrend() {
        List<Deal> deals = dealRepository.findAll();
        if (deals.isEmpty()) {
            return List.of();
        }

        WeekFields weekFields = WeekFields.ISO;
        Map<String, Long> totalsByWeek = deals.stream()
                .collect(Collectors.groupingBy(
                        deal -> {
                            OffsetDateTime timestamp = OffsetDateTime.parse(deal.getUpdatedAt());
                            int week = timestamp.get(weekFields.weekOfWeekBasedYear());
                            int year = timestamp.get(weekFields.weekBasedYear());
                            return year + "-W" + week;
                        },
                        LinkedHashMap::new,
                        Collectors.summingLong(Deal::getValue)
                ));

        List<Map.Entry<String, Long>> entries = new ArrayList<>(totalsByWeek.entrySet());
        int fromIndex = Math.max(0, entries.size() - 8);

        return entries.subList(fromIndex, entries.size()).stream()
                .map(entry -> new TrendPoint(entry.getKey(), entry.getValue()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ActivityItem> getActivity() {
        return dealRepository.findAll().stream()
                .sorted(Comparator.comparing(Deal::getUpdatedAt).reversed())
                .limit(7)
                .map(deal -> new ActivityItem(deal.getId(), deal.getCompany(), deal.getStage().getId(), deal.getUpdatedAt()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PipelineStageResponse> getPipelineStages() {
        return pipelineStageRepository.findAllByActiveTrueOrderByDisplayOrderAsc().stream()
                .map(this::toPipelineStageResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductCatalogItemResponse> getProducts(String category, String vendor) {
        String categoryFilter = category == null ? "" : category.trim().toLowerCase(Locale.ENGLISH);
        String vendorFilter = vendor == null ? "" : vendor.trim().toLowerCase(Locale.ENGLISH);

        return productCatalogRepository.findAllByActiveTrueOrderByCategoryAscVendorAscNameAsc().stream()
                .filter(item -> categoryFilter.isBlank() || item.getCategory().toLowerCase(Locale.ENGLISH).equals(categoryFilter))
                .filter(item -> vendorFilter.isBlank() || item.getVendor().toLowerCase(Locale.ENGLISH).equals(vendorFilter))
                .map(this::toProductResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductCatalogSummaryResponse getProductSummary() {
        List<ProductCatalogItem> products = productCatalogRepository.findAllByActiveTrueOrderByCategoryAscVendorAscNameAsc();
        return new ProductCatalogSummaryResponse(
                products.stream().map(ProductCatalogItem::getCategory).distinct().toList(),
                products.stream().map(ProductCatalogItem::getVendor).distinct().toList()
        );
    }

    @Transactional
    public ProductCatalogItemResponse createProduct(ProductCatalogRequest request) {
        ProductCatalogItem item = new ProductCatalogItem();
        item.setId("P-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ENGLISH));
        item.setName(requireText(request.getName(), "Product name"));
        item.setCategory(requireText(request.getCategory(), "Product category"));
        item.setVendor(requireText(request.getVendor(), "Vendor"));
        item.setSku(requireText(request.getSku(), "SKU"));

        return toProductResponse(productCatalogRepository.save(item));
    }

    @Transactional
    public ProductCatalogItemResponse updateProduct(String productId, ProductCatalogRequest request) {
        ProductCatalogItem item = requireProduct(productId);
        item.setName(requireText(request.getName(), "Product name"));
        item.setCategory(requireText(request.getCategory(), "Product category"));
        item.setVendor(requireText(request.getVendor(), "Vendor"));
        item.setSku(requireText(request.getSku(), "SKU"));

        return toProductResponse(productCatalogRepository.save(item));
    }

    @Transactional
    public void deactivateProduct(String productId) {
        ProductCatalogItem item = requireProduct(productId);
        item.setActive(false);
        productCatalogRepository.save(item);
    }

    private ProductCatalogItem requireProduct(String productId) {
        return productCatalogRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));
    }

    @Transactional(readOnly = true)
    public List<AccountResponse> getAccounts(String search) {
        String query = search == null ? "" : search.trim().toLowerCase(Locale.ENGLISH);
        List<Account> accounts = accountRepository.findAllByActiveTrueOrderByNameAsc();
        if (query.isBlank()) {
            return accounts.stream().map(this::toAccountResponse).toList();
        }

        return accounts.stream()
                .filter(account -> account.getName().toLowerCase(Locale.ENGLISH).contains(query)
                        || (account.getIndustry() != null && account.getIndustry().toLowerCase(Locale.ENGLISH).contains(query))
                        || (account.getAccountManager() != null && account.getAccountManager().toLowerCase(Locale.ENGLISH).contains(query)))
                .map(this::toAccountResponse)
                .toList();
    }

    @Transactional
    public AccountResponse createAccount(AccountRequest request) {
        Account account = new Account();
        account.setId("ACC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ENGLISH));
        populateAccount(account, request);

        return toAccountResponse(accountRepository.save(account));
    }

    @Transactional
    public AccountResponse updateAccount(String accountId, AccountRequest request) {
        Account account = requireAccount(accountId);
        populateAccount(account, request);

        return toAccountResponse(accountRepository.save(account));
    }

    @Transactional
    public void deactivateAccount(String accountId) {
        Account account = requireAccount(accountId);
        account.setActive(false);
        accountRepository.save(account);
    }

    private void populateAccount(Account account, AccountRequest request) {
        account.setName(requireText(request.getName(), "Account name"));
        account.setIndustry(trimToEmpty(request.getIndustry()));
        account.setWebsite(trimToEmpty(request.getWebsite()));
        account.setPhone(trimToEmpty(request.getPhone()));
        account.setAddress(trimToEmpty(request.getAddress()));
        account.setAccountManager(trimToEmpty(request.getAccountManager()));
    }

    private Account requireAccount(String accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + accountId));
    }

    private AccountResponse toAccountResponse(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getName(),
                account.getIndustry(),
                account.getWebsite(),
                account.getPhone(),
                account.getAddress(),
                account.getAccountManager()
        );
    }

    @Transactional(readOnly = true)
    public List<ContactResponse> getContacts(String search, String accountName) {
        String query = search == null ? "" : search.trim().toLowerCase(Locale.ENGLISH);
        String accountFilter = accountName == null ? "" : accountName.trim().toLowerCase(Locale.ENGLISH);
        List<Contact> contacts = contactRepository.findAllByActiveTrueOrderByNameAsc();

        return contacts.stream()
                .filter(contact -> accountFilter.isBlank()
                        || (contact.getAccountName() != null && contact.getAccountName().toLowerCase(Locale.ENGLISH).equals(accountFilter)))
                .filter(contact -> query.isBlank()
                        || contact.getName().toLowerCase(Locale.ENGLISH).contains(query)
                        || (contact.getEmail() != null && contact.getEmail().toLowerCase(Locale.ENGLISH).contains(query))
                        || (contact.getAccountName() != null && contact.getAccountName().toLowerCase(Locale.ENGLISH).contains(query)))
                .map(this::toContactResponse)
                .toList();
    }

    @Transactional
    public ContactResponse createContact(ContactRequest request) {
        Contact contact = new Contact();
        contact.setId("CON-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ENGLISH));
        populateContact(contact, request);

        return toContactResponse(contactRepository.save(contact));
    }

    @Transactional
    public ContactResponse updateContact(String contactId, ContactRequest request) {
        Contact contact = requireContact(contactId);
        populateContact(contact, request);

        return toContactResponse(contactRepository.save(contact));
    }

    @Transactional
    public void deactivateContact(String contactId) {
        Contact contact = requireContact(contactId);
        contact.setActive(false);
        contactRepository.save(contact);
    }

    private void populateContact(Contact contact, ContactRequest request) {
        contact.setName(requireText(request.getName(), "Contact name"));
        contact.setEmail(trimToEmpty(request.getEmail()));
        contact.setPhone(trimToEmpty(request.getPhone()));
        contact.setTitle(trimToEmpty(request.getTitle()));
        contact.setAccountName(trimToEmpty(request.getAccountName()));
    }

    private Contact requireContact(String contactId) {
        return contactRepository.findById(contactId)
                .orElseThrow(() -> new IllegalArgumentException("Contact not found: " + contactId));
    }

    private ContactResponse toContactResponse(Contact contact) {
        return new ContactResponse(
                contact.getId(),
                contact.getName(),
                contact.getEmail(),
                contact.getPhone(),
                contact.getTitle(),
                contact.getAccountName()
        );
    }

    @Transactional
    public PipelineStageResponse updatePipelineStage(String stageId, PipelineStageUpdateRequest request) {
        PipelineStage stage = requireStage(stageId);

        stage.setName(requireText(request.getName(), "Stage name"));
        stage.setShortLabel(requireText(request.getShortLabel(), "Stage short label"));
        stage.setDisplayOrder(request.getDisplayOrder());
        stage.setProbabilityPercent(clampProbability(request.getProbabilityPercent()));
        stage.setColor(requireText(request.getColor(), "Stage color"));
        stage.setMaxExpectedDurationDays(Math.max(1, request.getMaxExpectedDurationDays()));
        stage.setMandatoryFields(normalizeStringList(request.getMandatoryFields()));
        stage.setRequiredApprovals(normalizeStringList(request.getRequiredApprovals()));
        pipelineStageRepository.save(stage);

        pipelineStageTransitionRepository.deleteByFromStageId(stageId);
        for (int index = 0; index < request.getAllowedNextStages().size(); index++) {
            PipelineStageUpdateRequest.AllowedStageTransitionRequest transitionRequest = request.getAllowedNextStages().get(index);
            if (transitionRequest.getStageId() == null
                    || transitionRequest.getStageId().isBlank()
                    || stageId.equals(transitionRequest.getStageId())) {
                continue;
            }
            transition(stageId, transitionRequest.getStageId(), index + 1, transitionRequest.isConfirmationRequired());
        }

        return toPipelineStageResponse(requireStage(stageId));
    }

    @Transactional
    public DealResponse moveDealToStage(String dealId, StageMoveRequest request) {
        Deal deal = dealRepository.findById(dealId)
                .orElseThrow(() -> new IllegalArgumentException("Deal not found"));

        PipelineStage currentStage = deal.getStage();
        PipelineStage targetStage = requireStage(request.getTargetStageId());

        PipelineStageTransition transition = pipelineStageTransitionRepository.findAllByFromStageIdOrderByDisplayOrderAsc(currentStage.getId()).stream()
                .filter(item -> item.getToStage().getId().equals(targetStage.getId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Stage transition is not allowed"));

        validateRequiredFields(deal, currentStage);
        validateRequiredApprovals(deal, currentStage);
        validateRequiredFields(deal, targetStage);

        if (transition.isConfirmationRequired() && !request.isConfirmed()) {
            throw new IllegalStateException("This stage move requires confirmation");
        }

        deal.setStage(targetStage);
        deal.setUpdatedAt(Instant.now().toString());
        deal.setRiskStatus(calculateRiskStatus(deal));
        dealRepository.save(deal);

        dealStageHistoryRepository.save(new DealStageHistory(
                deal.getId(),
                currentStage.getId(),
                targetStage.getId(),
                Instant.now().toString(),
                "system",
                request.getRemarks()
        ));

        return toDealResponse(deal);
    }

    @Transactional(readOnly = true)
    public List<DealStageHistoryResponse> getDealStageHistory(String dealId) {
        return dealStageHistoryRepository.findAllByDealIdOrderByChangedAtDesc(dealId).stream()
                .map(item -> new DealStageHistoryResponse(
                        item.getFromStage(),
                        item.getToStage(),
                        item.getChangedAt(),
                        item.getChangedBy(),
                        item.getRemarks()
                ))
                .toList();
    }

    @Transactional
    public ImportDealsResponse importDeals(MultipartFile file, String defaultStage) {
        try (var workbook = WorkbookFactory.create(file.getInputStream())) {
            var sheet = workbook.getSheetAt(0);
            if (sheet == null || sheet.getPhysicalNumberOfRows() == 0) {
                return new ImportDealsResponse(0, 0, List.of(), List.of());
            }

            Row headerRow = findHeaderRow(sheet);
            if (headerRow == null) {
                throw new IllegalArgumentException("No recognizable deal header row was found in the workbook");
            }

            List<String> headers = readHeaders(headerRow);
            Map<Integer, String> canonicalHeaders = new LinkedHashMap<>();
            List<String> dynamicHeaders = new ArrayList<>();
            for (int index = 0; index < headers.size(); index++) {
                String canonical = resolveCanonicalHeader(headers.get(index));
                canonicalHeaders.put(index, canonical);
                if (canonical == null && !headers.get(index).isBlank()) {
                    dynamicHeaders.add(headers.get(index));
                }
            }

            List<Deal> importedDeals = new ArrayList<>();
            int skippedRows = 0;

            for (int rowIndex = headerRow.getRowNum() + 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null || isBlankRow(row, headers.size())) {
                    continue;
                }

                Deal deal = mapRowToDeal(row, headers, canonicalHeaders, defaultStage);
                if (deal == null) {
                    skippedRows++;
                    continue;
                }
                importedDeals.add(deal);
            }

            if (!importedDeals.isEmpty()) {
                dealRepository.saveAll(importedDeals);
            }

            return new ImportDealsResponse(
                    importedDeals.size(),
                    skippedRows,
                    headers,
                    dynamicHeaders.stream().distinct().toList()
            );
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to import Excel file", exception);
        }
    }

    private List<ApprovalStep> defaultApprovals(String stageId) {
        if ("quotation".equals(stageId)) {
            return List.of(
                    new ApprovalStep(Enums.ApprovalRole.RSM, Enums.ApprovalStatus.pending),
                    new ApprovalStep(Enums.ApprovalRole.Finance, Enums.ApprovalStatus.pending),
                    new ApprovalStep(Enums.ApprovalRole.BusinessHead, Enums.ApprovalStatus.pending)
            );
        }
        if ("order_placed".equals(stageId)) {
            return List.of(
                    new ApprovalStep(Enums.ApprovalRole.RSM, Enums.ApprovalStatus.pending),
                    new ApprovalStep(Enums.ApprovalRole.Finance, Enums.ApprovalStatus.pending)
            );
        }
        return new ArrayList<>();
    }

    private void populateDealFromCreateRequest(Deal deal, CreateDealRequest request) {
        deal.setCompany(requireText(request.getCompany(), "Company"));
        deal.setContact(requireText(request.getContact(), "Contact"));
        deal.setProduct(requireText(request.getProduct(), "Product"));
        String accountManager = requireText(request.getAccountManager(), "Account manager");
        deal.setAccountManager(new Person(accountManager, initialsFromName(accountManager)));
        deal.setStage(requireStage(request.getStage()));
        deal.setValue(Math.max(0, request.getValue()));
        deal.setPriority(request.getPriority() == null ? Enums.Priority.medium : request.getPriority());
        deal.setExpectedClosureDate(trimToEmpty(request.getExpectedClosureDate()));
        deal.setNextActivity(trimToEmpty(request.getNextActivity()));
        deal.setNextActivityDueDate(trimToEmpty(request.getNextActivityDueDate()));
        deal.setOemVendor(trimToEmpty(request.getOemVendor()));
    }

    private void populateDealFromUpdateRequest(Deal deal, UpdateDealRequest request) {
        deal.setCompany(requireText(request.getCompany(), "Company"));
        deal.setContact(requireText(request.getContact(), "Contact"));
        deal.setProduct(requireText(request.getProduct(), "Product"));
        String accountManager = requireText(request.getAccountManager(), "Account manager");
        deal.setAccountManager(new Person(accountManager, initialsFromName(accountManager)));
        deal.setValue(Math.max(0, request.getValue()));
        deal.setPriority(request.getPriority() == null ? Enums.Priority.medium : request.getPriority());
        deal.setExpectedClosureDate(trimToEmpty(request.getExpectedClosureDate()));
        deal.setNextActivity(trimToEmpty(request.getNextActivity()));
        deal.setNextActivityDueDate(trimToEmpty(request.getNextActivityDueDate()));
        deal.setOemVendor(trimToEmpty(request.getOemVendor()));
        deal.setExtraFields(request.getExtraFields() == null ? new LinkedHashMap<>() : new LinkedHashMap<>(request.getExtraFields()));
    }

    private String initialsFromName(String name) {
        return java.util.Arrays.stream(name.trim().split("\\s+"))
                .filter(part -> !part.isBlank())
                .limit(2)
                .map(part -> String.valueOf(Character.toUpperCase(part.charAt(0))))
                .reduce("", String::concat);
    }

    private List<String> readHeaders(Row headerRow) {
        List<String> headers = new ArrayList<>();
        for (int cellIndex = 0; cellIndex < headerRow.getLastCellNum(); cellIndex++) {
            headers.add(normalizeHeader(readCell(headerRow.getCell(cellIndex))));
        }
        return headers;
    }

    private Row findHeaderRow(org.apache.poi.ss.usermodel.Sheet sheet) {
        Row bestMatch = null;
        int bestScore = 0;
        int lastCandidate = Math.min(sheet.getLastRowNum(), sheet.getFirstRowNum() + 19);
        for (int rowIndex = sheet.getFirstRowNum(); rowIndex <= lastCandidate; rowIndex++) {
            Row candidate = sheet.getRow(rowIndex);
            if (candidate == null) {
                continue;
            }
            Set<String> canonical = readHeaders(candidate).stream()
                    .map(this::resolveCanonicalHeader)
                    .filter(java.util.Objects::nonNull)
                    .collect(Collectors.toSet());
            int score = canonical.size();
            if (canonical.contains("company") && canonical.contains("product") && score > bestScore) {
                bestMatch = candidate;
                bestScore = score;
            }
        }
        return bestMatch;
    }

    private String resolveCanonicalHeader(String header) {
        if (header == null || header.isBlank()) {
            return null;
        }

        return HEADER_ALIASES.entrySet().stream()
                .filter(entry -> entry.getValue().contains(header))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }

    private Deal mapRowToDeal(Row row, List<String> headers, Map<Integer, String> canonicalHeaders, String defaultStageId) {
        Map<String, String> dynamicFields = new LinkedHashMap<>();
        Map<String, String> knownFields = new LinkedHashMap<>();

        for (int cellIndex = 0; cellIndex < headers.size(); cellIndex++) {
            String value = readCell(row.getCell(cellIndex));
            if (value.isBlank()) {
                continue;
            }

            String canonical = canonicalHeaders.get(cellIndex);
            if (canonical == null) {
                dynamicFields.put(headers.get(cellIndex), value);
            } else {
                knownFields.put(canonical, value);
            }
        }

        if (!knownFields.containsKey("company") || !knownFields.containsKey("product")) {
            return null;
        }

        Deal deal = new Deal();
        deal.setId("D-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ENGLISH));
        deal.setCompany(knownFields.get("company"));
        deal.setContact(knownFields.getOrDefault("contact", "Imported Contact"));
        deal.setProduct(knownFields.get("product"));

        String accountManager = knownFields.getOrDefault("accountManager", "Imported User");
        deal.setAccountManager(new Person(accountManager, initialsFromName(accountManager)));
        deal.setStage(requireStage(parseStage(knownFields.get("stage"), defaultStageId)));
        long value = parseLong(knownFields.get("value"));
        if (isLakhsHeader(headers, canonicalHeaders)) {
            value = Math.multiplyExact(value, 100_000L);
        }
        deal.setValue(value);
        deal.setPriority(parsePriority(knownFields.get("priority")));
        deal.setUpdatedAt(Instant.now().toString());
        deal.setExpectedClosureDate(parseExpectedClosureDate(knownFields.get("expectedClosureDate")));
        deal.setNextActivity(knownFields.getOrDefault("nextActivity", ""));
        deal.setNextActivityDueDate(knownFields.getOrDefault("nextActivityDueDate", ""));
        deal.setOemVendor(knownFields.getOrDefault("oemVendor", dynamicFields.getOrDefault("oem", "")));
        deal.setApprovals(defaultApprovals(deal.getStage().getId()));
        deal.setRiskStatus(calculateRiskStatus(deal));
        deal.setExtraFields(dynamicFields);
        return deal;
    }

    private String parseStage(String rawStage, String defaultStage) {
        if (rawStage == null || rawStage.isBlank()) {
            return defaultStage;
        }

        String normalized = rawStage.trim().toLowerCase(Locale.ENGLISH);
        return STAGE_ALIASES.getOrDefault(
                normalized,
                normalized.replace(' ', '_').replace('/', '_')
        );
    }

    private boolean isLakhsHeader(List<String> headers, Map<Integer, String> canonicalHeaders) {
        return canonicalHeaders.entrySet().stream()
                .filter(entry -> "value".equals(entry.getValue()))
                .map(entry -> headers.get(entry.getKey()))
                .anyMatch(header -> header.contains("lakh"));
    }

    private String parseExpectedClosureDate(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return "";
        }
        String normalized = rawValue.trim().replace('\u2019', '\'').replace('\u2018', '\'');
        try {
            return YearMonth.parse(normalized, DateTimeFormatter.ofPattern("MMM''yy", Locale.ENGLISH))
                    .atEndOfMonth()
                    .toString();
        } catch (DateTimeParseException ignored) {
            return rawValue.trim();
        }
    }

    private Enums.Priority parsePriority(String rawPriority) {
        if (rawPriority == null || rawPriority.isBlank()) {
            return Enums.Priority.medium;
        }

        return Arrays.stream(Enums.Priority.values())
                .filter(priority -> priority.name().equalsIgnoreCase(rawPriority.trim()))
                .findFirst()
                .orElse(Enums.Priority.medium);
    }

    private long parseLong(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return 0;
        }

        String digitsOnly = rawValue.replaceAll("[^0-9.]", "");
        if (digitsOnly.isBlank()) {
            return 0;
        }

        return Math.round(Double.parseDouble(digitsOnly));
    }

    private boolean isBlankRow(Row row, int columnCount) {
        return java.util.stream.IntStream.range(0, columnCount)
                .mapToObj(index -> readCell(row.getCell(index)))
                .allMatch(String::isBlank);
    }

    private String readCell(Cell cell) {
        return cell == null ? "" : dataFormatter.formatCellValue(cell).trim();
    }

    private String normalizeHeader(String value) {
        return Arrays.stream(value.toLowerCase(Locale.ENGLISH).trim().split("\\s+"))
                .filter(part -> !part.isBlank())
                .collect(Collectors.joining(" "));
    }

    private PipelineStageResponse toPipelineStageResponse(PipelineStage stage) {
        return new PipelineStageResponse(
                stage.getId(),
                stage.getName(),
                stage.getShortLabel(),
                stage.getDisplayOrder(),
                stage.getProbabilityPercent(),
                stage.getColor(),
                stage.getMaxExpectedDurationDays(),
                stage.getMandatoryFields(),
                stage.getRequiredApprovals(),
                pipelineStageTransitionRepository.findAllByFromStageIdOrderByDisplayOrderAsc(stage.getId()).stream()
                        .map(transition -> new AllowedStageTransitionResponse(
                                transition.getToStage().getId(),
                                transition.isConfirmationRequired()
                        ))
                        .toList()
        );
    }

    private void ensureDefaultStages() {
        if (pipelineStageRepository.count() > 0) {
            return;
        }

        List<PipelineStage> stages = List.of(
                stage("suspect", "Suspect", "Suspect", 1, 10, "#94a3b8", 10, List.of("company", "product"), List.of()),
                stage("prospect", "Prospect", "Prospect", 2, 20, "#38bdf8", 14, List.of("company", "product", "accountManager"), List.of()),
                stage("qualified", "Qualified", "Qualified", 3, 30, "#0ea5e9", 10, List.of("company", "product", "accountManager"), List.of()),
                stage("solutioning", "Solutioning", "Solve", 4, 40, "#6366f1", 12, List.of("company", "product"), List.of("Solution")),
                stage("quotation", "Quotation", "Quote", 5, 55, "#8b5cf6", 14, List.of("company", "product", "value"), List.of("RSM")),
                stage("negotiation", "Negotiation", "Negotiate", 6, 65, "#f59e0b", 14, List.of("company", "value"), List.of()),
                stage("order_placed", "Order Placed", "Order", 7, 80, "#f97316", 10, List.of("company", "value"), List.of("Finance")),
                stage("procurement_fulfilment", "Procurement/Fulfilment", "Fulfil", 8, 90, "#06b6d4", 20, List.of(), List.of()),
                stage("invoiced", "Invoiced", "Invoice", 9, 95, "#ec4899", 15, List.of(), List.of()),
                stage("payment_pending", "Payment Pending", "Payment", 10, 98, "#10b981", 20, List.of(), List.of()),
                stage("won", "Won", "Won", 11, 100, "#16a34a", 1, List.of(), List.of()),
                stage("lost", "Lost", "Lost", 12, 0, "#dc2626", 1, List.of(), List.of()),
                stage("on_hold", "On Hold", "Hold", 13, 5, "#64748b", 30, List.of(), List.of())
        );

        pipelineStageRepository.saveAll(stages);

        transition("suspect", "prospect", 1, false);
        transition("prospect", "qualified", 1, false);
        transition("qualified", "solutioning", 1, false);
        transition("solutioning", "quotation", 1, false);
        transition("quotation", "negotiation", 1, false);
        transition("negotiation", "order_placed", 1, true);
        transition("order_placed", "procurement_fulfilment", 1, false);
        transition("procurement_fulfilment", "invoiced", 1, false);
        transition("invoiced", "payment_pending", 1, false);
        transition("payment_pending", "won", 1, true);
        transition("suspect", "lost", 99, true);
        transition("prospect", "lost", 99, true);
        transition("qualified", "lost", 99, true);
        transition("solutioning", "lost", 99, true);
        transition("quotation", "lost", 99, true);
        transition("negotiation", "lost", 99, true);
        transition("payment_pending", "on_hold", 98, false);
        transition("prospect", "on_hold", 98, false);
        transition("qualified", "on_hold", 98, false);
    }

    private void ensureDefaultProducts() {
        if (productCatalogRepository.count() > 0) {
            return;
        }

        productCatalogRepository.saveAll(List.of(
                product("Structured Cabling", "Network Infrastructure", "CommScope", "ACS-NET-001"),
                product("Enterprise Wi-Fi", "Wireless", "Cisco", "ACS-WIFI-001"),
                product("Core Switch Stack", "Switching", "Aruba", "ACS-SW-001"),
                product("Firewall Cluster", "Security", "Fortinet", "ACS-SEC-001"),
                product("CCTV Surveillance", "Physical Security", "Hikvision", "ACS-CCTV-001"),
                product("Access Control", "Physical Security", "Honeywell", "ACS-ACS-001")
        ));
    }

    private PipelineStage stage(String id, String name, String shortLabel, int order, int probability, String color,
                                int maxDurationDays, List<String> mandatoryFields, List<String> requiredApprovals) {
        PipelineStage stage = new PipelineStage();
        stage.setId(id);
        stage.setName(name);
        stage.setShortLabel(shortLabel);
        stage.setDisplayOrder(order);
        stage.setProbabilityPercent(probability);
        stage.setColor(color);
        stage.setMaxExpectedDurationDays(maxDurationDays);
        stage.setMandatoryFields(mandatoryFields);
        stage.setRequiredApprovals(requiredApprovals);
        return stage;
    }

    private void transition(String fromStageId, String toStageId, int displayOrder, boolean confirmationRequired) {
        PipelineStageTransition transition = new PipelineStageTransition();
        transition.setFromStage(requireStage(fromStageId));
        transition.setToStage(requireStage(toStageId));
        transition.setDisplayOrder(displayOrder);
        transition.setConfirmationRequired(confirmationRequired);
        pipelineStageTransitionRepository.save(transition);
    }

    private ProductCatalogItem product(String name, String category, String vendor, String sku) {
        ProductCatalogItem item = new ProductCatalogItem();
        item.setId("P-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ENGLISH));
        item.setName(name);
        item.setCategory(category);
        item.setVendor(vendor);
        item.setSku(sku);
        return item;
    }

    private PipelineStage requireStage(String stageId) {
        return pipelineStageRepository.findById(stageId)
                .orElseThrow(() -> new IllegalArgumentException("Stage not found: " + stageId));
    }

    private ProductCatalogItemResponse toProductResponse(ProductCatalogItem item) {
        return new ProductCatalogItemResponse(
                item.getId(),
                item.getName(),
                item.getCategory(),
                item.getVendor(),
                item.getSku()
        );
    }

    private String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        return value.trim();
    }

    private String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    private List<String> normalizeStringList(List<String> values) {
        if (values == null) {
            return List.of();
        }

        return values.stream()
                .filter(value -> value != null && !value.isBlank())
                .map(String::trim)
                .distinct()
                .toList();
    }

    private int clampProbability(int probabilityPercent) {
        return Math.max(0, Math.min(100, probabilityPercent));
    }

    private long calculateWeightedValue(Deal deal) {
        return Math.round(deal.getValue() * (deal.getStage().getProbabilityPercent() / 100.0));
    }

    private long calculateDaysInStage(Deal deal) {
        return java.time.Duration.between(Instant.parse(deal.getUpdatedAt()), Instant.now()).toDays();
    }

    private Enums.RiskStatus calculateRiskStatus(Deal deal) {
        if (deal.getNextActivityDueDate() != null && !deal.getNextActivityDueDate().isBlank()) {
            try {
                Instant due = OffsetDateTime.parse(deal.getNextActivityDueDate()).toInstant();
                if (due.isBefore(Instant.now())) {
                    return Enums.RiskStatus.overdue;
                }
            } catch (Exception ignored) {
            }
        }

        long daysInStage = calculateDaysInStage(deal);
        if (daysInStage > deal.getStage().getMaxExpectedDurationDays() + 7L) {
            return Enums.RiskStatus.stalled;
        }
        if (daysInStage > deal.getStage().getMaxExpectedDurationDays()) {
            return Enums.RiskStatus.attention;
        }
        if (deal.getPriority() == Enums.Priority.high && deal.getApprovals().stream().anyMatch(step -> step.getStatus() == Enums.ApprovalStatus.rejected)) {
            return Enums.RiskStatus.high_risk;
        }
        return Enums.RiskStatus.healthy;
    }

    private void validateRequiredFields(Deal deal, PipelineStage currentStage) {
        List<String> missingFields = currentStage.getMandatoryFields().stream()
                .filter(field -> switch (field) {
                    case "company" -> deal.getCompany() == null || deal.getCompany().isBlank();
                    case "contact" -> deal.getContact() == null || deal.getContact().isBlank();
                    case "product" -> deal.getProduct() == null || deal.getProduct().isBlank();
                    case "accountManager" -> deal.getAccountManager() == null || deal.getAccountManager().getName() == null || deal.getAccountManager().getName().isBlank();
                    case "value" -> deal.getValue() <= 0;
                    case "expectedClosureDate" -> deal.getExpectedClosureDate() == null || deal.getExpectedClosureDate().isBlank();
                    case "nextActivity" -> deal.getNextActivity() == null || deal.getNextActivity().isBlank();
                    case "nextActivityDueDate" -> deal.getNextActivityDueDate() == null || deal.getNextActivityDueDate().isBlank();
                    case "oemVendor" -> deal.getOemVendor() == null || deal.getOemVendor().isBlank();
                    default -> false;
                })
                .toList();

        if (!missingFields.isEmpty()) {
            throw new IllegalStateException("Mandatory fields incomplete: " + String.join(", ", missingFields));
        }
    }

    private void validateRequiredApprovals(Deal deal, PipelineStage currentStage) {
        List<String> missingApprovals = currentStage.getRequiredApprovals().stream()
                .filter(role -> deal.getApprovals().stream().noneMatch(step ->
                        role.equals(step.getRole().name()) || role.equals(step.getRole().name().replace("BusinessHead", "Business Head"))))
                .toList();

        boolean hasPendingRequiredApprovals = currentStage.getRequiredApprovals().stream().anyMatch(role ->
                deal.getApprovals().stream().anyMatch(step ->
                        (role.equals(step.getRole().name()) || role.equals("Business Head") && step.getRole() == Enums.ApprovalRole.BusinessHead)
                                && step.getStatus() != Enums.ApprovalStatus.approved));

        if (!missingApprovals.isEmpty() || hasPendingRequiredApprovals) {
            throw new IllegalStateException("Required approvals are not completed for this transition");
        }
    }

    private DealResponse toDealResponse(Deal deal) {
        return new DealResponse(
                deal.getId(),
                deal.getCompany(),
                deal.getContact(),
                deal.getProduct(),
                new PersonResponse(deal.getAccountManager().getName(), deal.getAccountManager().getInitials()),
                deal.getStage().getId(),
                deal.getStage().getName(),
                deal.getValue(),
                deal.getPriority(),
                deal.getUpdatedAt(),
                deal.getExpectedClosureDate(),
                deal.getNextActivity(),
                deal.getNextActivityDueDate(),
                deal.getOemVendor(),
                calculateRiskStatus(deal),
                deal.getStage().getProbabilityPercent(),
                calculateWeightedValue(deal),
                calculateDaysInStage(deal),
                deal.getApprovals().stream()
                        .filter(step -> step.getRole() != Enums.ApprovalRole.Solution)
                        .toList(),
                deal.getExtraFields()
        );
    }

    @Transactional(readOnly = true)
    public List<LeadResponse> getLeads(String search, String status) {
        String query = search == null ? "" : search.trim().toLowerCase(Locale.ENGLISH);
        String statusFilter = status == null ? "" : status.trim().toLowerCase(Locale.ENGLISH);
        List<Lead> leads = leadRepository.findAllByActiveTrueOrderByUpdatedAtDesc();

        return leads.stream()
                .filter(lead -> statusFilter.isBlank() || lead.getStatus().name().equalsIgnoreCase(statusFilter))
                .filter(lead -> query.isBlank()
                        || lead.getCompany().toLowerCase(Locale.ENGLISH).contains(query)
                        || (lead.getContactName() != null && lead.getContactName().toLowerCase(Locale.ENGLISH).contains(query))
                        || (lead.getOwner() != null && lead.getOwner().toLowerCase(Locale.ENGLISH).contains(query)))
                .map(this::toLeadResponse)
                .toList();
    }

    @Transactional
    public LeadResponse createLead(LeadRequest request) {
        Lead lead = new Lead();
        lead.setId("LEAD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ENGLISH));
        populateLead(lead, request);
        lead.setCreatedAt(Instant.now().toString());
        lead.setUpdatedAt(lead.getCreatedAt());

        return toLeadResponse(leadRepository.save(lead));
    }

    @Transactional
    public LeadResponse updateLead(String leadId, LeadRequest request) {
        Lead lead = requireLead(leadId);
        populateLead(lead, request);
        lead.setUpdatedAt(Instant.now().toString());

        return toLeadResponse(leadRepository.save(lead));
    }

    @Transactional
    public void deactivateLead(String leadId) {
        Lead lead = requireLead(leadId);
        lead.setActive(false);
        leadRepository.save(lead);
    }

    @Transactional
    public ConvertLeadResponse convertLead(String leadId, ConvertLeadRequest request) {
        Lead lead = requireLead(leadId);
        if (lead.getStatus() == Enums.LeadStatus.converted) {
            throw new IllegalStateException("Lead is already converted");
        }

        String accountName = requireText(request.getAccountName(), "Account name");
        Account account = accountRepository.findAllByActiveTrueOrderByNameAsc().stream()
                .filter(existing -> existing.getName().equalsIgnoreCase(accountName))
                .findFirst()
                .orElseGet(() -> {
                    Account created = new Account();
                    created.setId("ACC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ENGLISH));
                    created.setName(accountName);
                    created.setIndustry("");
                    created.setWebsite("");
                    created.setPhone(trimToEmpty(lead.getPhone()));
                    created.setAddress("");
                    created.setAccountManager(trimToEmpty(lead.getOwner()));
                    return accountRepository.save(created);
                });

        String contactName = firstNonBlank(request.getContactName(), lead.getContactName());
        Contact contact = null;
        if (!contactName.isBlank()) {
            String finalContactName = contactName;
            contact = contactRepository.findAllByActiveTrueOrderByNameAsc().stream()
                    .filter(existing -> existing.getName().equalsIgnoreCase(finalContactName)
                            && accountName.equalsIgnoreCase(trimToEmpty(existing.getAccountName())))
                    .findFirst()
                    .orElseGet(() -> {
                        Contact created = new Contact();
                        created.setId("CON-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ENGLISH));
                        created.setName(finalContactName);
                        created.setEmail(firstNonBlank(request.getContactEmail(), lead.getEmail()));
                        created.setPhone(firstNonBlank(request.getContactPhone(), lead.getPhone()));
                        created.setTitle("");
                        created.setAccountName(accountName);
                        return contactRepository.save(created);
                    });
        }

        String dealId = null;
        if (request.isCreateOpportunity()) {
            String product = requireText(request.getProduct(), "Product");
            PipelineStage stage = request.getStageId() != null && !request.getStageId().isBlank()
                    ? requireStage(request.getStageId())
                    : pipelineStageRepository.findAllByActiveTrueOrderByDisplayOrderAsc().stream()
                            .findFirst()
                            .orElseThrow(() -> new IllegalStateException("No pipeline stages configured"));

            Deal deal = new Deal();
            deal.setId("D-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ENGLISH));
            deal.setCompany(lead.getCompany());
            deal.setContact(contactName.isBlank() ? "Unknown contact" : contactName);
            deal.setProduct(product);
            String ownerName = firstNonBlank(lead.getOwner(), "Imported User");
            deal.setAccountManager(new Person(ownerName, initialsFromName(ownerName)));
            deal.setStage(stage);
            deal.setValue(Math.max(0, request.getValue()));
            deal.setPriority(Enums.Priority.medium);
            deal.setUpdatedAt(Instant.now().toString());
            deal.setExpectedClosureDate("");
            deal.setNextActivity("");
            deal.setNextActivityDueDate("");
            deal.setOemVendor("");
            deal.setApprovals(defaultApprovals(stage.getId()));
            deal.setExtraFields(new LinkedHashMap<>());
            deal.setRiskStatus(calculateRiskStatus(deal));
            dealRepository.save(deal);
            dealId = deal.getId();
        }

        lead.setStatus(Enums.LeadStatus.converted);
        lead.setConvertedAccountId(account.getId());
        lead.setConvertedContactId(contact != null ? contact.getId() : null);
        lead.setConvertedDealId(dealId);
        lead.setUpdatedAt(Instant.now().toString());
        leadRepository.save(lead);

        return new ConvertLeadResponse(
                lead.getId(),
                account.getId(),
                account.getName(),
                contact != null ? contact.getId() : null,
                contact != null ? contact.getName() : null,
                dealId
        );
    }

    private void populateLead(Lead lead, LeadRequest request) {
        lead.setCompany(requireText(request.getCompany(), "Company"));
        lead.setContactName(trimToEmpty(request.getContactName()));
        lead.setEmail(trimToEmpty(request.getEmail()));
        lead.setPhone(trimToEmpty(request.getPhone()));
        lead.setSource(trimToEmpty(request.getSource()));
        lead.setOwner(trimToEmpty(request.getOwner()));
        lead.setScore(Math.max(0, Math.min(100, request.getScore())));
        lead.setNotes(trimToEmpty(request.getNotes()));

        Enums.LeadStatus requestedStatus = request.getStatus();
        if (requestedStatus == Enums.LeadStatus.converted && lead.getStatus() != Enums.LeadStatus.converted) {
            throw new IllegalArgumentException("Use the convert endpoint to mark a lead as converted");
        }
        lead.setStatus(requestedStatus == null ? Enums.LeadStatus.new_lead : requestedStatus);
    }

    private String firstNonBlank(String primary, String fallback) {
        String trimmedPrimary = trimToEmpty(primary);
        if (!trimmedPrimary.isBlank()) {
            return trimmedPrimary;
        }
        return trimToEmpty(fallback);
    }

    private Lead requireLead(String leadId) {
        return leadRepository.findById(leadId)
                .orElseThrow(() -> new IllegalArgumentException("Lead not found: " + leadId));
    }

    private LeadResponse toLeadResponse(Lead lead) {
        return new LeadResponse(
                lead.getId(),
                lead.getCompany(),
                lead.getContactName(),
                lead.getEmail(),
                lead.getPhone(),
                lead.getSource(),
                lead.getOwner(),
                lead.getStatus(),
                lead.getScore(),
                lead.getNotes(),
                lead.getCreatedAt(),
                lead.getUpdatedAt(),
                lead.getConvertedAccountId(),
                lead.getConvertedContactId(),
                lead.getConvertedDealId()
        );
    }
}
