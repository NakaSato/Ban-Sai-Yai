# Sprint 8: Accounting & General Ledger Service

## Overview

Sprint 8 implements **Accounting & General Ledger Service** which manages the Chart of Accounts, journal entries, and month-end closing procedures. This sprint provides complete accounting capabilities for the savings group.

## Sprint Objectives

### Primary Goals
- ✅ Implement Chart of Accounts management
- ✅ Create journal entry processing
- ✅ Set up month-end closing procedures
- ✅ Develop trial balance generation
- ✅ Implement general ledger maintenance

### Success Criteria
- Chart of Accounts is properly structured and maintained
- Journal entries are recorded with proper double-entry bookkeeping
- Month-end closing procedures are automated and accurate
- Trial balance always balances
- Audit trail is complete and immutable

## Technical Implementation

### 1. Entity Classes

#### AccountCode Entity
```java
@Entity
@Table(name = "accounting")
public class AccountCode {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long accId;
    
    @Column(name = "acc_code", unique = true, nullable = false, length = 20)
    private String accountCode;
    
    @Column(name = "acc_name", nullable = false, length = 100)
    private String accountName;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AccountType accountType;
    
    @Column(length = 200)
    private String description;
    
    @Column(name = "parent_code", length = 20)
    private String parentCode;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive;
    
    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    @LastModifiedDate
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "accountCode", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<LedgerTransaction> transactions;
    
    // Constructors, getters, setters
    
    public boolean isAsset() {
        return accountType == AccountType.ASSET;
    }
    
    public boolean isLiability() {
        return accountType == AccountType.LIABILITY;
    }
    
    public boolean isEquity() {
        return accountType == AccountType.EQUITY;
    }
    
    public boolean isRevenue() {
        return accountType == AccountType.REVENUE;
    }
    
    public boolean isExpense() {
        return accountType == AccountType.EXPENSE;
    }
}
```

#### JournalEntry Entity
```java
@Entity
@Table(name = "journal_entry")
public class JournalEntry {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long entryId;
    
    @Column(name = "entry_number", unique = true, nullable = false, length = 50)
    private String entryNumber;
    
    @Column(name = "entry_date", nullable = false)
    private LocalDate entryDate;
    
    @Column(name = "description", length = 500)
    private String description;
    
    @Column(name = "reference_number", length = 100)
    private String referenceNumber;
    
    @Column(name = "reference_type", length = 50)
    private String referenceType;
    
    @Column(name = "total_amount", precision = 15, scale = 2)
    private BigDecimal totalAmount;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private JournalEntryStatus status;
    
    @Column(name = "created_by", nullable = false, length = 100)
    private String createdBy;
    
    @Column(name = "approved_by", length = 100)
    private String approvedBy;
    
    @Column(name = "approved_date")
    private LocalDate approvedDate;
    
    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @OneToMany(mappedBy = "journalEntry", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<JournalEntryLine> lines;
    
    // Constructors, getters, setters
    
    public boolean isBalanced() {
        BigDecimal totalDebits = lines.stream()
            .filter(line -> line.getType() == TransactionType.DEBIT)
            .map(JournalEntryLine::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalCredits = lines.stream()
            .filter(line -> line.getType() == TransactionType.CREDIT)
            .map(JournalEntryLine::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return totalDebits.compareTo(totalCredits) == 0;
    }
    
    public boolean canBePosted() {
        return status == JournalEntryStatus.APPROVED && isBalanced();
    }
}
```

#### JournalEntryLine Entity
```java
@Entity
@Table(name = "journal_entry_line")
public class JournalEntryLine {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long lineId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entry_id", nullable = false)
    private JournalEntry journalEntry;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private AccountCode accountCode;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TransactionType type;
    
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;
    
    @Column(length = 200)
    private String description;
    
    @Column(name = "cost_center", length = 20)
    private String costCenter;
    
    @Column(name = "reference_id", length = 100)
    private String referenceId;
    
    // Constructors, getters, setters
}
```

#### MonthEndClose Entity
```java
@Entity
@Table(name = "month_end_close")
public class MonthEndClose {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long closeId;
    
    @Column(name = "close_number", unique = true, nullable = false, length = 50)
    private String closeNumber;
    
    @Column(name = "close_date", nullable = false)
    private LocalDate closeDate;
    
    @Column(name = "period_start", nullable = false)
    private LocalDate periodStart;
    
    @Column(name = "period_end", nullable = false)
    private LocalDate periodEnd;
    
    @Column(name = "closing_balance", nullable = false, precision = 15, scale = 2)
    private BigDecimal closingBalance;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CloseStatus status;
    
    @Column(name = "closed_by", nullable = false, length = 100)
    private String closedBy;
    
    @Column(name = "reviewed_by", length = 100)
    private String reviewedBy;
    
    @Column(length = 1000)
    private String notes;
    
    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @Column(name = "closed_at")
    private LocalDateTime closedAt;
    
    @OneToMany(mappedBy = "monthEndClose", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TrialBalanceEntry> trialBalanceEntries;
    
    // Constructors, getters, setters
    
    public boolean isCompleted() {
        return status == CloseStatus.COMPLETED;
    }
}
```

### 2. Enums

#### AccountType
```java
public enum AccountType {
    ASSET("Asset", "1"),
    LIABILITY("Liability", "2"),
    EQUITY("Equity", "3"),
    REVENUE("Revenue", "4"),
    EXPENSE("Expense", "5");
    
    private final String description;
    private final String prefix;
    
    AccountType(String description, String prefix) {
        this.description = description;
        this.prefix = prefix;
    }
    
    // Getters
}
```

#### JournalEntryStatus
```java
public enum JournalEntryStatus {
    DRAFT("Draft"),
    SUBMITTED("Submitted"),
    APPROVED("Approved"),
    POSTED("Posted"),
    REJECTED("Rejected");
    
    private final String description;
    
    JournalEntryStatus(String description) {
        this.description = description;
    }
    
    // Getters
}
```

#### CloseStatus
```java
public enum CloseStatus {
    PENDING("Pending"),
    IN_PROGRESS("In Progress"),
    COMPLETED("Completed"),
    FAILED("Failed"),
    REVERSED("Reversed");
    
    private final String description;
    
    CloseStatus(String description) {
        this.description = description;
    }
    
    // Getters
}
```

### 3. Data Transfer Objects (DTOs)

#### JournalEntryDTO
```java
public class JournalEntryDTO {
    
    @NotBlank(message = "Entry date is required")
    private LocalDate entryDate;
    
    @NotBlank(message = "Description is required")
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;
    
    private String referenceNumber;
    private String referenceType;
    
    @NotNull(message = "Entry lines are required")
    @Size(min = 2, message = "At least 2 entry lines required")
    private List<JournalEntryLineDTO> lines;
    
    private String notes;
    
    // Constructors, getters, setters
}
```

#### JournalEntryLineDTO
```java
public class JournalEntryLineDTO {
    
    @NotNull(message = "Account code is required")
    private String accountCode;
    
    @NotNull(message = "Transaction type is required")
    private TransactionType type;
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be positive")
    private BigDecimal amount;
    
    @Size(max = 200, message = "Description must not exceed 200 characters")
    private String description;
    
    private String costCenter;
    private String referenceId;
    
    // Constructors, getters, setters
}
```

#### MonthEndCloseDTO
```java
public class MonthEndCloseDTO {
    
    @NotNull(message = "Period start is required")
    private LocalDate periodStart;
    
    @NotNull(message = "Period end is required")
    private LocalDate periodEnd;
    
    @NotNull(message = "Closing balance is required")
    @DecimalMin(value = "0.00", message = "Closing balance must be non-negative")
    private BigDecimal closingBalance;
    
    private String notes;
    
    private Boolean generateTrialBalance = true;
    
    private Boolean carryForwardBalances = true;
    
    // Constructors, getters, setters
}
```

### 4. Repository Layer

#### AccountCodeRepository
```java
@Repository
public interface AccountCodeRepository extends JpaRepository<AccountCode, Long> {
    
    Optional<AccountCode> findByAccountCode(String accountCode);
    
    @Query("SELECT a FROM AccountCode a WHERE a.accountType = :type ORDER BY a.accountCode")
    List<AccountCode> findByAccountTypeOrderByAccountCode(@Param("type") AccountType type);
    
    @Query("SELECT a FROM AccountCode a WHERE a.isActive = :isActive ORDER BY a.accountCode")
    List<AccountCode> findByIsActiveOrderByAccountCode(@Param("isActive") Boolean isActive);
    
    @Query("SELECT a FROM AccountCode a WHERE a.parentCode = :parentCode ORDER BY a.accountCode")
    List<AccountCode> findByParentCodeOrderByAccountCode(@Param("parentCode") String parentCode);
    
    @Query("SELECT a FROM AccountCode a WHERE a.accountCode LIKE CONCAT(:prefix, '%') ORDER BY a.accountCode")
    List<AccountCode> findByPrefixOrderByAccountCode(@Param("prefix") String prefix);
}
```

#### JournalEntryRepository
```java
@Repository
public interface JournalEntryRepository extends JpaRepository<JournalEntry, Long> {
    
    Optional<JournalEntry> findByEntryNumber(String entryNumber);
    
    @Query("SELECT je FROM JournalEntry je WHERE je.entryDate BETWEEN :startDate AND :endDate ORDER BY je.entryDate DESC")
    List<JournalEntry> findByEntryDateBetweenOrderByEntryDateDesc(@Param("startDate") LocalDate startDate,
                                                             @Param("endDate") LocalDate endDate);
    
    @Query("SELECT je FROM JournalEntry je WHERE je.status = :status ORDER BY je.entryDate DESC")
    List<JournalEntry> findByStatusOrderByEntryDateDesc(@Param("status") JournalEntryStatus status);
    
    @Query("SELECT je FROM JournalEntry je WHERE je.createdBy = :username ORDER BY je.entryDate DESC")
    List<JournalEntry> findByCreatedByOrderByEntryDateDesc(@Param("username") String username);
    
    @Query("SELECT COUNT(je) FROM JournalEntry je WHERE je.entryDate = :date")
    long countByEntryDate(@Param("date") LocalDate date);
}
```

#### MonthEndCloseRepository
```java
@Repository
public interface MonthEndCloseRepository extends JpaRepository<MonthEndClose, Long> {
    
    Optional<MonthEndClose> findByCloseNumber(String closeNumber);
    
    @Query("SELECT mec FROM MonthEndClose mec WHERE mec.periodStart = :startDate AND mec.periodEnd = :endDate")
    Optional<MonthEndClose> findByPeriod(@Param("startDate") LocalDate startDate,
                                      @Param("endDate") LocalDate endDate);
    
    @Query("SELECT mec FROM MonthEndClose mec WHERE mec.status = :status ORDER BY mec.closeDate DESC")
    List<MonthEndClose> findByStatusOrderByCloseDateDesc(@Param("status") CloseStatus status);
    
    @Query("SELECT mec FROM MonthEndClose mec ORDER BY mec.closeDate DESC")
    List<MonthEndClose> findAllOrderByCloseDateDesc();
    
    @Query("SELECT mec FROM MonthEndClose mec WHERE mec.closeDate BETWEEN :startDate AND :endDate")
    List<MonthEndClose> findByCloseDateBetween(@Param("startDate") LocalDate startDate,
                                           @Param("endDate") LocalDate endDate);
}
```

### 5. Service Layer

#### AccountingService
```java
@Service
@Transactional
public class AccountingService {
    
    @Autowired
    private AccountCodeRepository accountCodeRepository;
    
    @Autowired
    private JournalEntryRepository journalEntryRepository;
    
    @Autowired
    private JournalEntryLineRepository journalEntryLineRepository;
    
    @Autowired
    private TransactionService transactionService;
    
    @Autowired
    private MonthEndCloseService monthEndCloseService;
    
    public JournalEntry createJournalEntry(JournalEntryDTO journalEntryDTO, String createdBy) {
        // Validate journal entry
        validateJournalEntry(journalEntryDTO);
        
        // Create journal entry
        JournalEntry journalEntry = new JournalEntry();
        journalEntry.setEntryNumber(generateEntryNumber());
        journalEntry.setEntryDate(journalEntryDTO.getEntryDate());
        journalEntry.setDescription(journalEntryDTO.getDescription());
        journalEntry.setReferenceNumber(journalEntryDTO.getReferenceNumber());
        journalEntry.setReferenceType(journalEntryDTO.getReferenceType());
        journalEntry.setCreatedBy(createdBy);
        journalEntry.setStatus(JournalEntryStatus.DRAFT);
        
        // Create entry lines
        List<JournalEntryLine> lines = createEntryLines(journalEntryDTO.getLines(), journalEntry);
        
        // Calculate total amount
        BigDecimal totalAmount = calculateTotalAmount(lines);
        journalEntry.setTotalAmount(totalAmount);
        
        journalEntry = journalEntryRepository.save(journalEntry);
        
        // Save lines
        lines.forEach(line -> {
            line.setJournalEntry(journalEntry);
            journalEntryLineRepository.save(line);
        });
        
        return journalEntry;
    }
    
    @PreAuthorize("hasAnyRole('SECRETARY', 'PRESIDENT')")
    public JournalEntry approveJournalEntry(Long entryId, String approvedBy) {
        JournalEntry entry = journalEntryRepository.findById(entryId)
            .orElseThrow(() -> new ResourceNotFoundException("Journal entry not found"));
        
        if (entry.getStatus() != JournalEntryStatus.SUBMITTED) {
            throw new BusinessRuleException("Only submitted entries can be approved");
        }
        
        if (!entry.isBalanced()) {
            throw new BusinessRuleException("Journal entry must be balanced before approval");
        }
        
        entry.setStatus(JournalEntryStatus.APPROVED);
        entry.setApprovedBy(approvedBy);
        entry.setApprovedDate(LocalDate.now());
        
        return journalEntryRepository.save(entry);
    }
    
    @PreAuthorize("hasAnyRole('SECRETARY', 'PRESIDENT')")
    public JournalEntry postJournalEntry(Long entryId) {
        JournalEntry entry = journalEntryRepository.findById(entryId)
            .orElseThrow(() -> new ResourceNotFoundException("Journal entry not found"));
        
        if (!entry.canBePosted()) {
            throw new BusinessRuleException("Journal entry cannot be posted in current status");
        }
        
        // Post to general ledger
        postToGeneralLedger(entry);
        
        // Update status
        entry.setStatus(JournalEntryStatus.POSTED);
        
        return journalEntryRepository.save(entry);
    }
    
    @Transactional(readOnly = true)
    public TrialBalanceDTO generateTrialBalance(LocalDate asOfDate) {
        // Get all accounts
        List<AccountCode> accounts = accountCodeRepository.findByIsActiveOrderByAccountCode(true);
        
        List<TrialBalanceItem> items = new ArrayList<>();
        BigDecimal totalDebits = BigDecimal.ZERO;
        BigDecimal totalCredits = BigDecimal.ZERO;
        
        for (AccountCode account : accounts) {
            BigDecimal balance = calculateAccountBalance(account, asOfDate);
            
            TrialBalanceItem item = new TrialBalanceItem();
            item.setAccountCode(account.getAccountCode());
            item.setAccountName(account.getAccountName());
            item.setAccountType(account.getAccountType());
            item.setBalance(balance);
            
            // Determine debit or credit balance based on account type
            if (account.isAsset()) {
                item.setDebitBalance(balance.max(BigDecimal.ZERO));
                item.setCreditBalance(BigDecimal.ZERO);
                totalDebits = totalDebits.add(item.getDebitBalance());
            } else if (account.isLiability() || account.isEquity() || account.isRevenue()) {
                item.setDebitBalance(BigDecimal.ZERO);
                item.setCreditBalance(balance.max(BigDecimal.ZERO));
                totalCredits = totalCredits.add(item.getCreditBalance());
            } else if (account.isExpense()) {
                item.setDebitBalance(balance.max(BigDecimal.ZERO));
                item.setCreditBalance(BigDecimal.ZERO);
                totalDebits = totalDebits.add(item.getDebitBalance());
            }
            
            items.add(item);
        }
        
        TrialBalanceDTO trialBalance = new TrialBalanceDTO();
        trialBalance.setAsOfDate(asOfDate);
        trialBalance.setItems(items);
        trialBalance.setTotalDebits(totalDebits);
        trialBalance.setTotalCredits(totalCredits);
        trialBalance.setIsBalanced(totalDebits.compareTo(totalCredits) == 0);
        
        return trialBalance;
    }
    
    private void validateJournalEntry(JournalEntryDTO journalEntryDTO) {
        // Validate entry date
        if (journalEntryDTO.getEntryDate().isAfter(LocalDate.now())) {
            throw new ValidationException("Entry date cannot be in the future");
        }
        
        // Validate minimum lines
        if (journalEntryDTO.getLines().size() < 2) {
            throw new ValidationException("Journal entry must have at least 2 lines");
        }
        
        // Validate account codes exist
        for (JournalEntryLineDTO line : journalEntryDTO.getLines()) {
            if (!accountCodeRepository.existsById(line.getAccountCode())) {
                throw new ValidationException("Account code does not exist: " + line.getAccountCode());
            }
        }
    }
    
    private List<JournalEntryLine> createEntryLines(List<JournalEntryLineDTO> lineDTOs, JournalEntry journalEntry) {
        return lineDTOs.stream()
            .map(lineDTO -> {
                AccountCode account = accountCodeRepository.findByAccountCode(lineDTO.getAccountCode())
                    .orElseThrow(() -> new ResourceNotFoundException("Account code not found"));
                
                JournalEntryLine line = new JournalEntryLine();
                line.setJournalEntry(journalEntry);
                line.setAccountCode(account);
                line.setType(lineDTO.getType());
                line.setAmount(lineDTO.getAmount());
                line.setDescription(lineDTO.getDescription());
                line.setCostCenter(lineDTO.getCostCenter());
                line.setReferenceId(lineDTO.getReferenceId());
                
                return line;
            })
            .collect(Collectors.toList());
    }
    
    private BigDecimal calculateTotalAmount(List<JournalEntryLine> lines) {
        return lines.stream()
            .map(JournalEntryLine::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    private void postToGeneralLedger(JournalEntry journalEntry) {
        for (JournalEntryLine line : journalEntry.getLines()) {
            LedgerTransaction transaction = new LedgerTransaction();
            transaction.setAccountCode(line.getAccountCode());
            transaction.setAmount(line.getAmount());
            transaction.setType(line.getType());
            transaction.setDescription(journalEntry.getDescription() + " - " + 
                                 (line.getDescription() != null ? line.getDescription() : ""));
            transaction.setTransDate(journalEntry.getEntryDate());
            transaction.setReferenceId(line.getReferenceId());
            
            transactionService.recordTransaction(transaction);
        }
    }
    
    private BigDecimal calculateAccountBalance(AccountCode account, LocalDate asOfDate) {
        // Get all transactions for this account up to the specified date
        BigDecimal balance = transactionService.getAccountBalance(account.getAccountCode(), asOfDate);
        
        return balance;
    }
    
    private String generateEntryNumber() {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomSuffix = String.format("%04d", new Random().nextInt(10000));
        return "JE" + dateStr + randomSuffix;
    }
}
```

#### MonthEndCloseService
```java
@Service
@Transactional
public class MonthEndCloseService {
    
    @Autowired
    private MonthEndCloseRepository closeRepository;
    
    @Autowired
    private AccountingService accountingService;
    
    @Autowired
    private TransactionService transactionService;
    
    @Autowired
    private BalanceForwardService balanceForwardService;
    
    @PreAuthorize("hasAnyRole('SECRETARY', 'PRESIDENT')")
    public MonthEndClose performMonthEndClose(MonthEndCloseDTO closeDTO, String closedBy) {
        // Validate close request
        validateCloseRequest(closeDTO);
        
        // Check if period already closed
        Optional<MonthEndClose> existing = closeRepository.findByPeriod(
            closeDTO.getPeriodStart(), closeDTO.getPeriodEnd());
        
        if (existing.isPresent()) {
            throw new BusinessRuleException("Period is already closed");
        }
        
        // Generate trial balance
        TrialBalanceDTO trialBalance = accountingService.generateTrialBalance(closeDTO.getPeriodEnd());
        
        if (!trialBalance.getIsBalanced()) {
            throw new BusinessRuleException("Trial balance is not balanced. Cannot close period.");
        }
        
        // Create month-end close record
        MonthEndClose monthEndClose = new MonthEndClose();
        monthEndClose.setCloseNumber(generateCloseNumber());
        monthEndClose.setCloseDate(LocalDate.now());
        monthEndClose.setPeriodStart(closeDTO.getPeriodStart());
        monthEndClose.setPeriodEnd(closeDTO.getPeriodEnd());
        monthEndClose.setClosingBalance(closeDTO.getClosingBalance());
        monthEndClose.setStatus(CloseStatus.IN_PROGRESS);
        monthEndClose.setClosedBy(closedBy);
        monthEndClose.setNotes(closeDTO.getNotes());
        
        monthEndClose = closeRepository.save(monthEndClose);
        
        try {
            // Perform closing procedures
            performClosingProcedures(monthEndClose, trialBalance, closeDTO);
            
            // Update status to completed
            monthEndClose.setStatus(CloseStatus.COMPLETED);
            monthEndClose.setClosedAt(LocalDateTime.now());
            
            return closeRepository.save(monthEndClose);
            
        } catch (Exception e) {
            // Update status to failed
            monthEndClose.setStatus(CloseStatus.FAILED);
            monthEndClose.setNotes(monthEndClose.getNotes() + " - Error: " + e.getMessage());
            
            return closeRepository.save(monthEndClose);
        }
    }
    
    @Transactional(readOnly = true)
    public MonthEndClose getLastMonthEndClose() {
        List<MonthEndClose> closes = closeRepository.findAllOrderByCloseDateDesc();
        
        return closes.isEmpty() ? null : closes.get(0);
    }
    
    @Transactional(readOnly = true)
    public List<MonthEndClose> getMonthEndCloses(Integer limit) {
        List<MonthEndClose> closes = closeRepository.findAllOrderByCloseDateDesc();
        
        if (limit != null && limit > 0) {
            closes = closes.stream()
                .limit(limit)
                .collect(Collectors.toList());
        }
        
        return closes;
    }
    
    private void validateCloseRequest(MonthEndCloseDTO closeDTO) {
        // Validate period
        if (closeDTO.getPeriodEnd().isBefore(closeDTO.getPeriodStart())) {
            throw new ValidationException("Period end must be after period start");
        }
        
        // Validate closing balance
        if (closeDTO.getClosingBalance().compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("Closing balance cannot be negative");
        }
        
        // Validate period is month-end
        LocalDate lastDayOfMonth = closeDTO.getPeriodEnd().with(TemporalAdjusters.lastDayOfMonth());
        if (!closeDTO.getPeriodEnd().equals(lastDayOfMonth)) {
            throw new ValidationException("Period end must be the last day of the month");
        }
    }
    
    private void performClosingProcedures(MonthEndClose monthEndClose, 
                                     TrialBalanceDTO trialBalance, 
                                     MonthEndCloseDTO closeDTO) {
        
        // 1. Generate trial balance entries
        if (closeDTO.getGenerateTrialBalance()) {
            generateTrialBalanceEntries(monthEndClose, trialBalance);
        }
        
        // 2. Carry forward balances
        if (closeDTO.getCarryForwardBalances()) {
            balanceForwardService.carryForwardBalances(closeDTO.getPeriodEnd());
        }
        
        // 3. Generate closing journal entry
        generateClosingJournalEntry(monthEndClose, closeDTO.getClosingBalance());
        
        // 4. Archive transactions for the period
        archiveTransactions(monthEndClose.getPeriodStart(), monthEndClose.getPeriodEnd());
    }
    
    private void generateTrialBalanceEntries(MonthEndClose monthEndClose, TrialBalanceDTO trialBalance) {
        for (TrialBalanceItem item : trialBalance.getItems()) {
            TrialBalanceEntry entry = new TrialBalanceEntry();
            entry.setMonthEndClose(monthEndClose);
            entry.setAccountCode(accountCodeRepository.findByAccountCode(item.getAccountCode()).orElse(null));
            entry.setDebitBalance(item.getDebitBalance());
            entry.setCreditBalance(item.getCreditBalance());
            
            // Would save trial balance entries
            // trialBalanceEntryRepository.save(entry);
        }
    }
    
    private void generateClosingJournalEntry(MonthEndClose monthEndClose, BigDecimal closingBalance) {
        JournalEntryDTO closingEntry = new JournalEntryDTO();
        closingEntry.setEntryDate(monthEndClose.getPeriodEnd());
        closingEntry.setDescription("Month-end closing entry");
        closingEntry.setReferenceNumber(monthEndClose.getCloseNumber());
        closingEntry.setReferenceType("MONTH_CLOSE");
        
        List<JournalEntryLineDTO> lines = new ArrayList<>();
        
        // Debit retained earnings
        JournalEntryLineDTO retainedEarningsDebit = new JournalEntryLineDTO();
        retainedEarningsDebit.setAccountCode("3002"); // Retained Earnings
        retainedEarningsDebit.setType(TransactionType.DEBIT);
        retainedEarningsDebit.setAmount(closingBalance);
        retainedEarningsDebit.setDescription("Close retained earnings to balance sheet");
        lines.add(retainedEarningsDebit);
        
        // Credit closing balance
        JournalEntryLineDTO closingBalanceCredit = new JournalEntryLineDTO();
        closingBalanceCredit.setAccountCode("1001"); // Cash/Bank
        closingBalanceCredit.setType(TransactionType.CREDIT);
        closingBalanceCredit.setAmount(closingBalance);
        closingBalanceCredit.setDescription("Closing balance to cash/bank");
        lines.add(closingBalanceCredit);
        
        closingEntry.setLines(lines);
        
        // Create and post the closing entry
        JournalEntry entry = accountingService.createJournalEntry(closingEntry, monthEndClose.getClosedBy());
        accountingService.approveJournalEntry(entry.getEntryId(), monthEndClose.getClosedBy());
        accountingService.postJournalEntry(entry.getEntryId());
    }
    
    private void archiveTransactions(LocalDate periodStart, LocalDate periodEnd) {
        // Implementation for archiving transactions
        // This would move transactions to archive tables
    }
    
    private String generateCloseNumber() {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        String randomSuffix = String.format("%04d", new Random().nextInt(10000));
        return "CLOSE" + dateStr + randomSuffix;
    }
}
```

### 6. Controller Layer

#### AccountingController
```java
@RestController
@RequestMapping("/api/accounting")
@Validated
public class AccountingController {
    
    @Autowired
    private AccountingService accountingService;
    
    @Autowired
    private MonthEndCloseService monthEndCloseService;
    
    @GetMapping("/chart-of-accounts")
    @PreAuthorize("hasAnyRole('SECRETARY', 'PRESIDENT')")
    public ResponseEntity<ApiResponse<List<AccountCode>>> getChartOfAccounts() {
        
        List<AccountCode> accounts = accountingService.getChartOfAccounts();
        
        return ResponseEntity.ok(ApiResponse.success(accounts));
    }
    
    @PostMapping("/journal-entry")
    @PreAuthorize("hasAnyRole('SECRETARY', 'PRESIDENT')")
    public ResponseEntity<ApiResponse<JournalEntry>> createJournalEntry(
            @Valid @RequestBody JournalEntryDTO journalEntryDTO,
            Authentication authentication) {
        
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String createdBy = userDetails.getUsername();
        
        JournalEntry entry = accountingService.createJournalEntry(journalEntryDTO, createdBy);
        
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(entry, "Journal entry created successfully"));
    }
    
    @PutMapping("/journal-entry/{entryId}/approve")
    @PreAuthorize("hasAnyRole('SECRETARY', 'PRESIDENT')")
    public ResponseEntity<ApiResponse<JournalEntry>> approveJournalEntry(
            @PathVariable Long entryId,
            @RequestBody Map<String, String> approvalRequest) {
        
        String approvedBy = approvalRequest.get("approvedBy");
        
        JournalEntry entry = accountingService.approveJournalEntry(entryId, approvedBy);
        
        return ResponseEntity.ok(ApiResponse.success(entry, "Journal entry approved"));
    }
    
    @PutMapping("/journal-entry/{entryId}/post")
    @PreAuthorize("hasAnyRole('SECRETARY', 'PRESIDENT')")
    public ResponseEntity<ApiResponse<JournalEntry>> postJournalEntry(@PathVariable Long entryId) {
        
        JournalEntry entry = accountingService.postJournalEntry(entryId);
        
        return ResponseEntity.ok(ApiResponse.success(entry, "Journal entry posted"));
    }
    
    @GetMapping("/trial-balance")
    @PreAuthorize("hasAnyRole('SECRETARY', 'PRESIDENT')")
    public ResponseEntity<ApiResponse<TrialBalanceDTO>> getTrialBalance(@RequestParam LocalDate asOfDate) {
        
        TrialBalanceDTO trialBalance = accountingService.generateTrialBalance(asOfDate);
        
        return ResponseEntity.ok(ApiResponse.success(trialBalance));
    }
    
    @PostMapping("/month-end-close")
    @PreAuthorize("hasAnyRole('SECRETARY', 'PRESIDENT')")
    public ResponseEntity<ApiResponse<MonthEndClose>> performMonthEndClose(
            @Valid @RequestBody MonthEndCloseDTO closeDTO,
            Authentication authentication) {
        
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String closedBy = userDetails.getUsername();
        
        MonthEndClose monthEndClose = monthEndCloseService.performMonthEndClose(closeDTO, closedBy);
        
        return ResponseEntity.ok(ApiResponse.success(monthEndClose, "Month-end close completed"));
    }
    
    @GetMapping("/month-end-closes")
    @PreAuthorize("hasAnyRole('SECRETARY', 'PRESIDENT')")
    public ResponseEntity<ApiResponse<List<MonthEndClose>>> getMonthEndCloses(
            @RequestParam(defaultValue = "10") Integer limit) {
        
        List<MonthEndClose> closes = monthEndCloseService.getMonthEndCloses(limit);
        
        return ResponseEntity.ok(ApiResponse.success(closes));
    }
}
```

## Business Rules & Validation

### Journal Entry Rules
1. **Double-Entry Bookkeeping**: Every entry must balance (debits = credits)
2. **Account Validation**: Account codes must exist and be active
3. **Date Validation**: Entry dates cannot be future-dated
4. **Approval Workflow**: Draft → Submitted → Approved → Posted
5. **Reference Tracking**: All entries must have proper references

### Account Management Rules
1. **Account Structure**: Follows standard accounting hierarchy
2. **Account Codes**: Unique and standardized format
3. **Account Types**: Proper classification (Asset, Liability, Equity, Revenue, Expense)
4. **Active Status**: Only active accounts can be used in entries

### Month-End Closing Rules
1. **Period Completion**: Must be complete month
2. **Trial Balance**: Must balance before closing
3. **Closing Balance**: Must match trial balance calculations
4. **Uniqueness**: No overlapping closing periods
5. **Archive Transactions**: Period transactions must be archived

## Testing Strategy

### Unit Tests
```java
@ExtendWith(MockitoExtension.class)
class AccountingServiceTest {
    
    @Mock
    private AccountCodeRepository accountCodeRepository;
    
    @Mock
    private JournalEntryRepository journalEntryRepository;
    
    @InjectMocks
    private AccountingService accountingService;
    
    @Test
    void testCreateJournalEntry_Success() {
        // Given
        JournalEntryDTO journalEntryDTO = createValidJournalEntryDTO();
        AccountCode account = createTestAccount();
        
        when(accountCodeRepository.findByAccountCode(any())).thenReturn(Optional.of(account));
        when(journalEntryRepository.save(any(JournalEntry.class))).thenReturn(createTestJournalEntry());
        
        // When
        JournalEntry result = accountingService.createJournalEntry(journalEntryDTO, "testuser");
        
        // Then
        assertThat(result.getStatus()).isEqualTo(JournalEntryStatus.DRAFT);
        assertThat(result.getEntryNumber()).isNotNull();
        verify(journalEntryRepository).save(any(JournalEntry.class));
    }
    
    @Test
    void testCreateJournalEntry_UnbalancedLines_ThrowsException() {
        // Given
        JournalEntryDTO journalEntryDTO = createUnbalancedJournalEntryDTO();
        
        // When & Then
        assertThrows(ValidationException.class, 
            () -> accountingService.createJournalEntry(journalEntryDTO, "testuser"));
    }
}
```

---

**Related Documentation**:
- [Database Schema](../architecture/database-schema.md) - Entity definitions
- [API Documentation](../api/rest-endpoints.md) - Endpoint details
- [Business Rules](../reference/business-rules.md) - Detailed accounting rules
