# Sprint 2: Share & Savings Service

## Overview

Sprint 2 implements the **Share & Savings Service** which manages member savings accounts, share capital contributions, and deposit/withdrawal operations. This sprint provides the core financial transaction capabilities for the savings group.

## Sprint Objectives

### Primary Goals
- ✅ Implement savings account management
- ✅ Create deposit and withdrawal functionality
- ✅ Set up share capital tracking
- ✅ Develop historical balance tracking
- ✅ Implement transaction recording

### Success Criteria
- Members can deposit to share capital and savings
- Withdrawal requests can be processed with validation
- Historical balances are tracked and maintained
- All transactions are properly recorded in the ledger
- Balance calculations are accurate and auditable

## Technical Implementation

### 1. Entity Classes

#### SavingAccount Entity
```java
@Entity
@Table(name = "saving")
public class SavingAccount {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long savingId;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false, unique = true)
    private Member member;
    
    @Column(name = "share_cap", nullable = false, precision = 15, scale = 2)
    private BigDecimal shareCapital;
    
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal deposit;
    
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal balance;
    
    @Column(name = "updated_at")
    @LastModifiedDate
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "savingAccount", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<SavingForward> savingForwards;
    
    // Constructors, getters, setters
    
    public void addToShareCapital(BigDecimal amount) {
        this.shareCapital = this.shareCapital.add(amount);
        this.balance = this.balance.add(amount);
    }
    
    public void addToDeposit(BigDecimal amount) {
        this.deposit = this.deposit.add(amount);
        this.balance = this.balance.add(amount);
    }
    
    public void withdraw(BigDecimal amount) {
        if (amount.compareTo(this.balance) > 0) {
            throw new InsufficientFundsException("Insufficient funds for withdrawal");
        }
        this.balance = this.balance.subtract(amount);
    }
}
```

#### SavingForward Entity
```java
@Entity
@Table(name = "saving_forward")
public class SavingForward {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long forwardId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "saving_id")
    private SavingAccount savingAccount;
    
    @Column(name = "share_fwd", precision = 15, scale = 2)
    private BigDecimal shareForward;
    
    @Column(name = "deposit_fwd", precision = 15, scale = 2)
    private BigDecimal depositForward;
    
    @Column(name = "forward_date", nullable = false)
    private LocalDate forwardDate;
    
    // Constructors, getters, setters
}
```

### 2. Data Transfer Objects (DTOs)

#### DepositDTO
```java
public class DepositDTO {
    
    @NotNull(message = "Member ID is required")
    private Long memberId;
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "1.00", message = "Minimum deposit amount is 1.00")
    @DecimalMax(value = "1000000.00", message = "Maximum deposit amount is 1,000,000.00")
    private BigDecimal amount;
    
    @NotNull(message = "Deposit type is required")
    private DepositType depositType;
    
    @Size(max = 200, message = "Description must not exceed 200 characters")
    private String description;
    
    @NotNull(message = "Transaction date is required")
    private LocalDate transactionDate;
    
    private String receiptReference;
    
    public enum DepositType {
        SHARE_CAPITAL,
        SAVINGS_DEPOSIT,
        INTEREST_EARNED,
        DIVIDEND_RECEIVED
    }
    
    // Constructors, getters, setters
}
```

#### WithdrawalDTO
```java
public class WithdrawalDTO {
    
    @NotNull(message = "Member ID is required")
    private Long memberId;
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "1.00", message = "Minimum withdrawal amount is 1.00")
    @DecimalMax(value = "1000000.00", message = "Maximum withdrawal amount is 1,000,000.00")
    private BigDecimal amount;
    
    @NotBlank(message = "Reason is required")
    @Size(max = 200, message = "Reason must not exceed 200 characters")
    private String reason;
    
    @NotNull(message = "Transaction date is required")
    private LocalDate transactionDate;
    
    private String approvalReference;
    
    private WithdrawalType withdrawalType;
    
    public enum WithdrawalType {
        SAVINGS_WITHDRAWAL,
        EMERGENCY_WITHDRAWAL,
        SHARE_CAPITAL_WITHDRAWAL,
        ACCOUNT_CLOSURE
    }
    
    // Constructors, getters, setters
}
```

#### SavingAccountDTO
```java
public class SavingAccountDTO {
    private Long savingId;
    private Long memberId;
    private String memberName;
    private BigDecimal shareCapital;
    private BigDecimal deposit;
    private BigDecimal balance;
    private LocalDateTime updatedAt;
    private List<SavingForwardDTO> historicalBalances;
    
    public static SavingAccountDTO fromEntity(SavingAccount account) {
        SavingAccountDTO dto = new SavingAccountDTO();
        dto.setSavingId(account.getSavingId());
        dto.setMemberId(account.getMember().getMemberId());
        dto.setMemberName(account.getMember().getName());
        dto.setShareCapital(account.getShareCapital());
        dto.setDeposit(account.getDeposit());
        dto.setBalance(account.getBalance());
        dto.setUpdatedAt(account.getUpdatedAt());
        
        if (account.getSavingForwards() != null) {
            dto.setHistoricalBalances(account.getSavingForwards().stream()
                .map(SavingForwardDTO::fromEntity)
                .collect(Collectors.toList()));
        }
        
        return dto;
    }
    
    // Constructors, getters, setters
}
```

### 3. Repository Layer

#### SavingAccountRepository
```java
@Repository
public interface SavingAccountRepository extends JpaRepository<SavingAccount, Long> {
    
    Optional<SavingAccount> findByMemberId(Long memberId);
    
    @Query("SELECT s FROM SavingAccount s WHERE s.balance > :minBalance")
    List<SavingAccount> findByMinimumBalance(@Param("minBalance") BigDecimal minBalance);
    
    @Query("SELECT s FROM SavingAccount s WHERE s.member.name LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<SavingAccount> searchByMemberName(@Param("search") String search, Pageable pageable);
    
    @Query("SELECT SUM(s.balance) FROM SavingAccount s")
    BigDecimal getTotalSavings();
    
    @Query("SELECT COUNT(s) FROM SavingAccount s WHERE s.balance < :minimumBalance")
    long countMembersWithLowBalance(@Param("minimumBalance") BigDecimal minimumBalance);
}
```

#### SavingForwardRepository
```java
@Repository
public interface SavingForwardRepository extends JpaRepository<SavingForward, Long> {
    
    List<SavingForward> findByMemberIdOrderByForwardDateDesc(Long memberId);
    
    List<SavingForward> findByForwardDateBetween(LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT sf FROM SavingForward sf WHERE sf.member.id = :memberId " +
           "AND sf.forwardDate = (SELECT MAX(sf2.forwardDate) FROM SavingForward sf2 " +
           "WHERE sf2.member.id = :memberId AND sf2.forwardDate <= :date)")
    Optional<SavingForward> findLatestBalanceForMember(@Param("memberId") Long memberId, 
                                                    @Param("date") LocalDate date);
}
```

### 4. Service Layer

#### SavingService
```java
@Service
@Transactional
public class SavingService {
    
    @Autowired
    private SavingAccountRepository savingAccountRepository;
    
    @Autowired
    private SavingForwardRepository savingForwardRepository;
    
    @Autowired
    private TransactionService transactionService;
    
    @Autowired
    private ReceiptService receiptService;
    
    @Autowired
    private MemberRepository memberRepository;
    
    public DepositResponseDTO processDeposit(DepositDTO depositDTO) {
        // Validate member exists
        Member member = memberRepository.findById(depositDTO.getMemberId())
            .orElseThrow(() -> new ResourceNotFoundException("Member not found"));
        
        // Get or create savings account
        SavingAccount account = savingAccountRepository.findByMemberId(depositDTO.getMemberId())
            .orElseGet(() -> createNewSavingsAccount(member));
        
        // Process deposit based on type
        BigDecimal previousBalance = account.getBalance();
        
        switch (depositDTO.getDepositType()) {
            case SHARE_CAPITAL:
                account.addToShareCapital(depositDTO.getAmount());
                break;
            case SAVINGS_DEPOSIT:
                account.addToDeposit(depositDTO.getAmount());
                break;
            default:
                throw new ValidationException("Unsupported deposit type");
        }
        
        // Save updated account
        account = savingAccountRepository.save(account);
        
        // Record transaction in ledger
        String transactionId = transactionService.recordDepositTransaction(
            depositDTO, member, account, previousBalance);
        
        // Generate receipt
        String receiptUrl = receiptService.generateDepositReceipt(
            depositDTO, member, account, transactionId);
        
        return DepositResponseDTO.builder()
            .transactionId(transactionId)
            .memberId(member.getMemberId())
            .memberName(member.getName())
            .amount(depositDTO.getAmount())
            .depositType(depositDTO.getDepositType())
            .previousBalance(previousBalance)
            .newBalance(account.getBalance())
            .transactionDate(depositDTO.getTransactionDate())
            .receiptUrl(receiptUrl)
            .build();
    }
    
    public WithdrawalResponseDTO processWithdrawal(WithdrawalDTO withdrawalDTO) {
        // Validate member exists
        Member member = memberRepository.findById(withdrawalDTO.getMemberId())
            .orElseThrow(() -> new ResourceNotFoundException("Member not found"));
        
        // Get savings account
        SavingAccount account = savingAccountRepository.findByMemberId(withdrawalDTO.getMemberId())
            .orElseThrow(() -> new ValidationException("No savings account found for member"));
        
        // Validate withdrawal rules
        validateWithdrawalRules(withdrawalDTO, account, member);
        
        BigDecimal previousBalance = account.getBalance();
        account.withdraw(withdrawalDTO.getAmount());
        account = savingAccountRepository.save(account);
        
        // Record transaction in ledger
        String transactionId = transactionService.recordWithdrawalTransaction(
            withdrawalDTO, member, account, previousBalance);
        
        // Generate receipt
        String receiptUrl = receiptService.generateWithdrawalReceipt(
            withdrawalDTO, member, account, transactionId);
        
        return WithdrawalResponseDTO.builder()
            .transactionId(transactionId)
            .memberId(member.getMemberId())
            .memberName(member.getName())
            .amount(withdrawalDTO.getAmount())
            .reason(withdrawalDTO.getReason())
            .previousBalance(previousBalance)
            .newBalance(account.getBalance())
            .transactionDate(withdrawalDTO.getTransactionDate())
            .receiptUrl(receiptUrl)
            .build();
    }
    
    @Transactional(readOnly = true)
    public SavingAccountDTO getMemberSavings(Long memberId) {
        SavingAccount account = savingAccountRepository.findByMemberId(memberId)
            .orElseThrow(() -> new ResourceNotFoundException("Savings account not found"));
        
        return SavingAccountDTO.fromEntity(account);
    }
    
    @Scheduled(cron = "0 0 0 L * ?") // Last day of every month at midnight
    public void performMonthlyBalanceForward() {
        LocalDate forwardDate = LocalDate.now();
        
        List<SavingAccount> allAccounts = savingAccountRepository.findAll();
        
        for (SavingAccount account : allAccounts) {
            // Create forward record
            SavingForward forward = new SavingForward();
            forward.setMember(account.getMember());
            forward.setSavingAccount(account);
            forward.setShareForward(account.getShareCapital());
            forward.setDepositForward(account.getDeposit());
            forward.setForwardDate(forwardDate);
            
            savingForwardRepository.save(forward);
            
            log.info("Balance forward completed for member: {}", account.getMember().getName());
        }
    }
    
    @Transactional(readOnly = true)
    public SavingsSummaryDTO getSavingsSummary() {
        BigDecimal totalSavings = savingAccountRepository.getTotalSavings();
        long totalMembers = savingAccountRepository.count();
        long lowBalanceMembers = savingAccountRepository.countMembersWithLowBalance(new BigDecimal("1000.00"));
        
        return SavingsSummaryDTO.builder()
            .totalSavings(totalSavings)
            .totalMembers(totalMembers)
            .averageBalance(totalSavings.divide(BigDecimal.valueOf(totalMembers), 2, RoundingMode.HALF_UP))
            .membersWithLowBalance(lowBalanceMembers)
            .summaryDate(LocalDate.now())
            .build();
    }
    
    private SavingAccount createNewSavingsAccount(Member member) {
        SavingAccount account = new SavingAccount();
        account.setMember(member);
        account.setShareCapital(BigDecimal.ZERO);
        account.setDeposit(BigDecimal.ZERO);
        account.setBalance(BigDecimal.ZERO);
        
        return savingAccountRepository.save(account);
    }
    
    private void validateWithdrawalRules(WithdrawalDTO withdrawalDTO, SavingAccount account, Member member) {
        // Check sufficient funds
        if (withdrawalDTO.getAmount().compareTo(account.getBalance()) > 0) {
            throw new InsufficientFundsException("Insufficient funds for withdrawal");
        }
        
        // Check minimum balance requirement
        BigDecimal minBalance = new BigDecimal("1000.00");
        if (account.getBalance().subtract(withdrawalDTO.getAmount()).compareTo(minBalance) < 0) {
            throw new BusinessRuleException("Withdrawal would leave account below minimum balance");
        }
        
        // Check withdrawal limits for share capital
        if (withdrawalDTO.getWithdrawalType() == WithdrawalType.SHARE_CAPITAL_WITHDRAWAL) {
            validateShareCapitalWithdrawal(withdrawalDTO.getAmount(), account.getShareCapital(), member);
        }
        
        // Check member tenure requirements for certain withdrawal types
        if (withdrawalDTO.getWithdrawalType() == WithdrawalType.SHARE_CAPITAL_WITHDRAWAL) {
            LocalDate sixMonthsAgo = LocalDate.now().minusMonths(6);
            if (member.getDateRegist().isAfter(sixMonthsAgo)) {
                throw new BusinessRuleException("Share capital withdrawal requires at least 6 months membership");
            }
        }
    }
    
    private void validateShareCapitalWithdrawal(BigDecimal withdrawalAmount, 
                                           BigDecimal shareCapital, Member member) {
        // Can only withdraw up to 50% of share capital
        BigDecimal maxWithdrawal = shareCapital.multiply(new BigDecimal("0.5"));
        if (withdrawalAmount.compareTo(maxWithdrawal) > 0) {
            throw new BusinessRuleException("Share capital withdrawal limited to 50% of total share capital");
        }
    }
}
```

#### TransactionService
```java
@Service
@Transactional
public class TransactionService {
    
    @Autowired
    private LedgerTransactionRepository transactionRepository;
    
    @Autowired
    private AccountCodeRepository accountCodeRepository;
    
    public String recordDepositTransaction(DepositDTO depositDTO, Member member, 
                                       SavingAccount account, BigDecimal previousBalance) {
        
        // Get appropriate account codes
        AccountCode cashAccount = accountCodeRepository.findByAccountCode("1001")
            .orElseThrow(() -> new ResourceNotFoundException("Cash account not found"));
        
        AccountCode savingsAccount = accountCodeRepository.findByAccountCode("2001")
            .orElseThrow(() -> new ResourceNotFoundException("Savings account not found"));
        
        // Create debit transaction (cash received)
        LedgerTransaction debitTransaction = new LedgerTransaction();
        debitTransaction.setAccountCode(cashAccount);
        debitTransaction.setAmount(depositDTO.getAmount());
        debitTransaction.setType(TransactionType.DEBIT);
        debitTransaction.setDescription(depositDTO.getDescription() != null ? 
            depositDTO.getDescription() : "Deposit from " + member.getName());
        debitTransaction.setTransDate(depositDTO.getTransactionDate());
        debitTransaction.setReferenceId(account.getSavingId());
        
        // Create credit transaction (savings increased)
        LedgerTransaction creditTransaction = new LedgerTransaction();
        creditTransaction.setAccountCode(savingsAccount);
        creditTransaction.setAmount(depositDTO.getAmount());
        creditTransaction.setType(TransactionType.CREDIT);
        creditTransaction.setDescription(depositDTO.getDescription() != null ? 
            depositDTO.getDescription() : "Deposit from " + member.getName());
        creditTransaction.setTransDate(depositDTO.getTransactionDate());
        creditTransaction.setReferenceId(account.getSavingId());
        
        // Save transactions
        transactionRepository.save(debitTransaction);
        transactionRepository.save(creditTransaction);
        
        // Generate transaction ID
        return generateTransactionId(depositDTO.getTransactionDate(), "DEP");
    }
    
    public String recordWithdrawalTransaction(WithdrawalDTO withdrawalDTO, Member member,
                                            SavingAccount account, BigDecimal previousBalance) {
        
        // Get appropriate account codes
        AccountCode cashAccount = accountCodeRepository.findByAccountCode("1001")
            .orElseThrow(() -> new ResourceNotFoundException("Cash account not found"));
        
        AccountCode savingsAccount = accountCodeRepository.findByAccountCode("2001")
            .orElseThrow(() -> new ResourceNotFoundException("Savings account not found"));
        
        // Create credit transaction (cash paid out)
        LedgerTransaction creditTransaction = new LedgerTransaction();
        creditTransaction.setAccountCode(cashAccount);
        creditTransaction.setAmount(withdrawalDTO.getAmount());
        creditTransaction.setType(TransactionType.CREDIT);
        creditTransaction.setDescription(withdrawalDTO.getReason() + " - " + member.getName());
        creditTransaction.setTransDate(withdrawalDTO.getTransactionDate());
        creditTransaction.setReferenceId(account.getSavingId());
        
        // Create debit transaction (savings decreased)
        LedgerTransaction debitTransaction = new LedgerTransaction();
        debitTransaction.setAccountCode(savingsAccount);
        debitTransaction.setAmount(withdrawalDTO.getAmount());
        debitTransaction.setType(TransactionType.DEBIT);
        debitTransaction.setDescription(withdrawalDTO.getReason() + " - " + member.getName());
        debitTransaction.setTransDate(withdrawalDTO.getTransactionDate());
        debitTransaction.setReferenceId(account.getSavingId());
        
        // Save transactions
        transactionRepository.save(debitTransaction);
        transactionRepository.save(debitTransaction);
        
        // Generate transaction ID
        return generateTransactionId(withdrawalDTO.getTransactionDate(), "WD");
    }
    
    private String generateTransactionId(LocalDate date, String prefix) {
        String dateStr = date.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomSuffix = String.format("%04d", new Random().nextInt(10000));
        return prefix + dateStr + randomSuffix;
    }
}
```

### 5. Controller Layer

#### SavingsController
```java
@RestController
@RequestMapping("/api/savings")
@Validated
public class SavingsController {
    
    @Autowired
    private SavingService savingService;
    
    @PostMapping("/deposit")
    @PreAuthorize("hasAnyRole('OFFICER', 'SECRETARY', 'PRESIDENT')")
    public ResponseEntity<ApiResponse<DepositResponseDTO>> processDeposit(
            @Valid @RequestBody DepositDTO depositDTO) {
        
        DepositResponseDTO response = savingService.processDeposit(depositDTO);
        
        return ResponseEntity.ok(ApiResponse.success(response, "Deposit processed successfully"));
    }
    
    @PostMapping("/withdrawal")
    @PreAuthorize("hasAnyRole('OFFICER', 'SECRETARY', 'PRESIDENT')")
    public ResponseEntity<ApiResponse<WithdrawalResponseDTO>> processWithdrawal(
            @Valid @RequestBody WithdrawalDTO withdrawalDTO) {
        
        WithdrawalResponseDTO response = savingService.processWithdrawal(withdrawalDTO);
        
        return ResponseEntity.ok(ApiResponse.success(response, "Withdrawal processed successfully"));
    }
    
    @GetMapping("/member/{memberId}")
    @PreAuthorize("hasRole('OFFICER') or hasRole('SECRETARY') or hasRole('PRESIDENT') or " +
            "@memberSecurity.canViewMemberDetails(#memberId, authentication)")
    public ResponseEntity<ApiResponse<SavingAccountDTO>> getMemberSavings(
            @PathVariable Long memberId) {
        
        SavingAccountDTO savings = savingService.getMemberSavings(memberId);
        
        return ResponseEntity.ok(ApiResponse.success(savings));
    }
    
    @GetMapping("/my-balance")
    @PreAuthorize("hasRole('MEMBER')")
    public ResponseEntity<ApiResponse<SavingAccountDTO>> getMySavings(
            Authentication authentication) {
        
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long memberId = userDetails.getUser().getMember().getMemberId();
        
        SavingAccountDTO savings = savingService.getMemberSavings(memberId);
        
        return ResponseEntity.ok(ApiResponse.success(savings));
    }
    
    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('SECRETARY', 'PRESIDENT')")
    public ResponseEntity<ApiResponse<SavingsSummaryDTO>> getSavingsSummary() {
        
        SavingsSummaryDTO summary = savingService.getSavingsSummary();
        
        return ResponseEntity.ok(ApiResponse.success(summary));
    }
    
    @PostMapping("/forward-balance")
    @PreAuthorize("hasAnyRole('SECRETARY', 'PRESIDENT')")
    public ResponseEntity<ApiResponse<String>> performBalanceForward() {
        
        savingService.performMonthlyBalanceForward();
        
        return ResponseEntity.ok(ApiResponse.success(null, "Balance forward completed successfully"));
    }
}
```

## Business Rules & Validation

### Deposit Rules
1. **Minimum Deposit**: 1.00 THB
2. **Maximum Deposit**: 1,000,000.00 THB per transaction
3. **Share Capital**: Only increases, no withdrawals except under special conditions
4. **Transaction Date**: Cannot be future-dated
5. **Member Status**: Member must be active

### Withdrawal Rules
1. **Sufficient Funds**: Cannot withdraw more than available balance
2. **Minimum Balance**: Account must maintain minimum 1,000.00 THB
3. **Share Capital**: Maximum 50% withdrawal after 6 months membership
4. **Withdrawal Limits**: Daily and monthly limits apply
5. **Approval Required**: Large withdrawals require additional approval

### Balance Forward Rules
1. **Monthly Schedule**: Automatically runs on last day of each month
2. **Historical Tracking**: All balances preserved in forward table
3. **Audit Trail**: Complete transaction history maintained
4. **Data Integrity**: Forward balances matched with current balances

## Testing Strategy

### Unit Tests
```java
@ExtendWith(MockitoExtension.class)
class SavingServiceTest {
    
    @Mock
    private SavingAccountRepository savingAccountRepository;
    
    @Mock
    private MemberRepository memberRepository;
    
    @Mock
    private TransactionService transactionService;
    
    @Mock
    private ReceiptService receiptService;
    
    @InjectMocks
    private SavingService savingService;
    
    @Test
    void testProcessDeposit_Success() {
        // Given
        DepositDTO depositDTO = createValidDepositDTO();
        Member member = createTestMember();
        SavingAccount account = createTestSavingsAccount();
        
        when(memberRepository.findById(depositDTO.getMemberId())).thenReturn(Optional.of(member));
        when(savingAccountRepository.findByMemberId(depositDTO.getMemberId()))
            .thenReturn(Optional.of(account));
        when(transactionService.recordDepositTransaction(any(), any(), any(), any()))
            .thenReturn("TXN001");
        when(receiptService.generateDepositReceipt(any(), any(), any(), any()))
            .thenReturn("/receipts/TXN001.pdf");
        
        // When
        DepositResponseDTO response = savingService.processDeposit(depositDTO);
        
        // Then
        assertThat(response.getAmount()).isEqualTo(depositDTO.getAmount());
        assertThat(response.getTransactionId()).isEqualTo("TXN001");
        verify(savingAccountRepository).save(any(SavingAccount.class));
    }
    
    @Test
    void testProcessWithdrawal_InsufficientFunds_ThrowsException() {
        // Given
        WithdrawalDTO withdrawalDTO = createValidWithdrawalDTO();
        Member member = createTestMember();
        SavingAccount account = createTestSavingsAccountWithLowBalance();
        
        when(memberRepository.findById(withdrawalDTO.getMemberId())).thenReturn(Optional.of(member));
        when(savingAccountRepository.findByMemberId(withdrawalDTO.getMemberId()))
            .thenReturn(Optional.of(account));
        
        // When & Then
        assertThrows(InsufficientFundsException.class, 
            () -> savingService.processWithdrawal(withdrawalDTO));
    }
}
```

### Integration Tests
```java
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class SavingsControllerIntegrationTest {
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Test
    void testProcessDeposit_Success() {
        // Given
        DepositDTO depositDTO = createValidDepositDTO();
        HttpHeaders headers = createAuthHeaders("ROLE_OFFICER");
        HttpEntity<DepositDTO> request = new HttpEntity<>(depositDTO, headers);
        
        // When
        ResponseEntity<ApiResponse> response = restTemplate.postForEntity(
            "/api/savings/deposit", request, ApiResponse.class);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().isSuccess()).isTrue();
    }
}
```

## Performance Considerations

### Database Optimization
1. **Indexing**: Proper indexes on member_id, balance columns
2. **Batch Processing**: For balance forward operations
3. **Connection Pooling**: Optimize database connections
4. **Query Optimization**: Efficient queries for balance calculations

### Caching Strategy
1. **Balance Caching**: Cache current balances for frequent access
2. **Member Data**: Cache member information
3. **Account Codes**: Cache chart of accounts
4. **Report Data**: Cache summary calculations

## Monitoring & Auditing

### Transaction Auditing
```java
@EntityListeners(AuditingEntityListener.class)
public class LedgerTransaction {
    
    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "created_by")
    private String createdBy;
}
```

### Financial Metrics
- Daily deposit/withdrawal volumes
- Average account balances
- Member participation rates
- Transaction success/failure rates

---

**Related Documentation**:
- [Database Schema](../architecture/database-schema.md) - Entity definitions
- [API Documentation](../api/rest-endpoints.md) - Endpoint details
- [Transaction Rules](../reference/business-rules.md) - Business logic details
