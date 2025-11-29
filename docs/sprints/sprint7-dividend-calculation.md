# Sprint 7: Dividend Calculation Service

## Overview

Sprint 7 implements **Dividend Calculation Service** which calculates and distributes dividends to members based on their share capital and savings. This sprint provides profit sharing capabilities for the savings group.

## Sprint Objectives

### Primary Goals
- ✅ Implement dividend calculation engine
- ✅ Create profit determination logic
- ✅ Set up dividend distribution system
- ✅ Develop tax calculation for dividends
- ✅ Implement dividend payment processing

### Success Criteria
- Dividends are calculated accurately based on member contributions
- Profit allocation follows predefined rules
- Tax calculations are correct and compliant
- Dividend payments are processed and recorded
- Historical dividend data is maintained

## Technical Implementation

### 1. Entity Classes

#### Dividend Entity
```java
@Entity
@Table(name = "dividend")
public class Dividend {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long dividendId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;
    
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;
    
    @Column(name = "share_dividend", precision = 15, scale = 2)
    private BigDecimal shareDividend;
    
    @Column(name = "deposit_dividend", precision = 15, scale = 2)
    private BigDecimal depositDividend;
    
    @Column(name = "interest_refund", precision = 15, scale = 2)
    private BigDecimal interestRefund;
    
    @Column(name = "tax_amount", precision = 15, scale = 2)
    private BigDecimal taxAmount;
    
    @Column(name = "net_amount", precision = 15, scale = 2)
    private BigDecimal netAmount;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DividendStatus status;
    
    @Column(name = "calculation_date", nullable = false)
    private LocalDate calculationDate;
    
    @Column(name = "payment_date")
    private LocalDate paymentDate;
    
    @Column(name = "calculation_id", nullable = false, length = 50)
    private String calculationId;
    
    @Column(length = 500)
    private String notes;
    
    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    // Constructors, getters, setters
    
    public BigDecimal getGrossAmount() {
        return (shareDividend != null ? shareDividend : BigDecimal.ZERO)
            .add(depositDividend != null ? depositDividend : BigDecimal.ZERO)
            .add(interestRefund != null ? interestRefund : BigDecimal.ZERO);
    }
    
    public void approve(LocalDate paymentDate) {
        this.status = DividendStatus.APPROVED;
        this.paymentDate = paymentDate;
    }
    
    public void pay() {
        this.status = DividendStatus.PAID;
    }
}
```

#### DividendCalculation Entity
```java
@Entity
@Table(name = "dividend_calculation")
public class DividendCalculation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long calculationId;
    
    @Column(name = "calculation_id", unique = true, nullable = false, length = 50)
    private String calculationNumber;
    
    @Column(name = "calculation_date", nullable = false)
    private LocalDate calculationDate;
    
    @Column(name = "period_start", nullable = false)
    private LocalDate periodStart;
    
    @Column(name = "period_end", nullable = false)
    private LocalDate periodEnd;
    
    @Column(name = "total_profit", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalProfit;
    
    @Column(name = "dividend_percentage", nullable = false, precision = 5, scale = 2)
    private BigDecimal dividendPercentage;
    
    @Column(name = "share_rate", precision = 5, scale = 2)
    private BigDecimal shareRate;
    
    @Column(name = "deposit_rate", precision = 5, scale = 2)
    private BigDecimal depositRate;
    
    @Column(name = "interest_refund_rate", precision = 5, scale = 2)
    private BigDecimal interestRefundRate;
    
    @Column(name = "tax_rate", precision = 5, scale = 2)
    private BigDecimal taxRate;
    
    @Column(name = "total_dividends", precision = 15, scale = 2)
    private BigDecimal totalDividends;
    
    @Column(name = "total_tax", precision = 15, scale = 2)
    private BigDecimal totalTax;
    
    @Column(name = "total_net_dividends", precision = 15, scale = 2)
    private BigDecimal totalNetDividends;
    
    @Column(name = "retained_earnings", precision = 15, scale = 2)
    private BigDecimal retainedEarnings;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CalculationStatus status;
    
    @Column(name = "approved_by", length = 100)
    private String approvedBy;
    
    @Column(name = "approved_date")
    private LocalDate approvedDate;
    
    @Column(length = 1000)
    private String notes;
    
    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @OneToMany(mappedBy = "calculation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Dividend> dividends;
    
    // Constructors, getters, setters
    
    public boolean canBeApproved() {
        return this.status == CalculationStatus.PENDING;
    }
    
    public void approve(String approvedBy, String notes) {
        this.status = CalculationStatus.APPROVED;
        this.approvedBy = approvedBy;
        this.approvedDate = LocalDate.now();
        this.notes = notes;
    }
}
```

### 2. Enums

#### DividendStatus
```java
public enum DividendStatus {
    PENDING("Pending Approval"),
    APPROVED("Approved"),
    PAID("Paid"),
    CANCELLED("Cancelled");
    
    private final String description;
    
    DividendStatus(String description) {
        this.description = description;
    }
    
    // Getters
}
```

#### CalculationStatus
```java
public enum CalculationStatus {
    PENDING("Pending Approval"),
    APPROVED("Approved"),
    PROCESSED("Processed"),
    CANCELLED("Cancelled");
    
    private final String description;
    
    CalculationStatus(String description) {
        this.description = description;
    }
    
    // Getters
}
```

### 3. Data Transfer Objects (DTOs)

#### DividendCalculationDTO
```java
public class DividendCalculationDTO {
    
    @NotNull(message = "Period start is required")
    private LocalDate periodStart;
    
    @NotNull(message = "Period end is required")
    private LocalDate periodEnd;
    
    @NotNull(message = "Dividend percentage is required")
    @DecimalMin(value = "0.01", message = "Minimum dividend percentage is 0.01%")
    @DecimalMax(value = "100.00", message = "Maximum dividend percentage is 100%")
    private BigDecimal dividendPercentage;
    
    @DecimalMin(value = "0.01", message = "Minimum share rate is 0.01%")
    @DecimalMax(value = "50.00", message = "Maximum share rate is 50%")
    private BigDecimal shareRate;
    
    @DecimalMin(value = "0.01", message = "Minimum deposit rate is 0.01%")
    @DecimalMax(value = "30.00", message = "Maximum deposit rate is 30%")
    private BigDecimal depositRate;
    
    @DecimalMin(value = "0.01", message = "Minimum interest refund rate is 0.01%")
    @DecimalMax(value = "20.00", message = "Maximum interest refund rate is 20%")
    private BigDecimal interestRefundRate;
    
    @DecimalMin(value = "0.00", message = "Minimum tax rate is 0%")
    @DecimalMax(value = "35.00", message = "Maximum tax rate is 35%")
    private BigDecimal taxRate;
    
    private BigDecimal minimumProfitAmount;
    
    private Boolean retainEarnings = false;
    
    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;
    
    // Constructors, getters, setters
}
```

#### DividendCalculationResultDTO
```java
public class DividendCalculationResultDTO {
    private String calculationId;
    private LocalDate calculationDate;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private BigDecimal totalProfit;
    private BigDecimal dividendPercentage;
    private BigDecimal totalDividends;
    private BigDecimal totalTax;
    private BigDecimal totalNetDividends;
    private BigDecimal retainedEarnings;
    private Integer totalMembers;
    private BigDecimal averageDividend;
    private CalculationStatus status;
    private List<DividendMemberDTO> memberDividends;
    
    // Constructors, getters, setters
}
```

#### DividendMemberDTO
```java
public class DividendMemberDTO {
    private Long memberId;
    private String memberName;
    private String idCard;
    private BigDecimal shareCapital;
    private BigDecimal savingsDeposit;
    private BigDecimal totalContributions;
    private BigDecimal shareDividend;
    private BigDecimal depositDividend;
    private BigDecimal interestRefund;
    private BigDecimal grossDividend;
    private BigDecimal taxAmount;
    private BigDecimal netDividend;
    private String calculationId;
    
    // Constructors, getters, setters
}
```

### 4. Repository Layer

#### DividendRepository
```java
@Repository
public interface DividendRepository extends JpaRepository<Dividend, Long> {
    
    List<Dividend> findByMemberIdOrderByCalculationDateDesc(Long memberId);
    
    @Query("SELECT d FROM Dividend d WHERE d.calculationId = :calculationId")
    List<Dividend> findByCalculationId(@Param("calculationId") String calculationId);
    
    @Query("SELECT d FROM Dividend d WHERE d.status = :status ORDER BY d.calculationDate DESC")
    List<Dividend> findByStatusOrderByCalculationDateDesc(@Param("status") DividendStatus status);
    
    @Query("SELECT SUM(d.netAmount) FROM Dividend d WHERE d.calculationId = :calculationId")
    BigDecimal getTotalNetDividends(@Param("calculationId") String calculationId);
    
    @Query("SELECT COUNT(d) FROM Dividend d WHERE d.calculationId = :calculationId AND d.status = :status")
    long countByCalculationIdAndStatus(@Param("calculationId") String calculationId,
                                       @Param("status") DividendStatus status);
    
    @Query("SELECT d FROM Dividend d WHERE d.calculationDate BETWEEN :startDate AND :endDate")
    List<Dividend> findByCalculationDateBetween(@Param("startDate") LocalDate startDate,
                                             @Param("endDate") LocalDate endDate);
}
```

#### DividendCalculationRepository
```java
@Repository
public interface DividendCalculationRepository extends JpaRepository<DividendCalculation, Long> {
    
    Optional<DividendCalculation> findByStatus(CalculationStatus status);
    
    @Query("SELECT dc FROM DividendCalculation dc ORDER BY dc.calculationDate DESC")
    List<DividendCalculation> findAllOrderByCalculationDateDesc();
    
    @Query("SELECT dc FROM DividendCalculation dc WHERE dc.calculationDate BETWEEN :startDate AND :endDate")
    List<DividendCalculation> findByCalculationDateBetween(@Param("startDate") LocalDate startDate,
                                                        @Param("endDate") LocalDate endDate);
    
    @Query("SELECT SUM(dc.totalProfit) FROM DividendCalculation dc WHERE dc.calculationDate BETWEEN :startDate AND :endDate")
    BigDecimal getTotalProfitByPeriod(@Param("startDate") LocalDate startDate,
                                   @Param("endDate") LocalDate endDate);
}
```

### 5. Service Layer

#### DividendCalculationService
```java
@Service
@Transactional
public class DividendCalculationService {
    
    @Autowired
    private DividendCalculationRepository calculationRepository;
    
    @Autowired
    private DividendRepository dividendRepository;
    
    @Autowired
    private SavingAccountRepository savingAccountRepository;
    
    @Autowired
    private TransactionService transactionService;
    
    @Autowired
    private MemberRepository memberRepository;
    
    @Autowired
    private NotificationService notificationService;
    
    public DividendCalculationResultDTO calculateDividends(DividendCalculationDTO calculationDTO, String calculatedBy) {
        // Validate business rules
        validateCalculationRequest(calculationDTO);
        
        // Calculate total profit for the period
        BigDecimal totalProfit = calculateTotalProfit(calculationDTO.getPeriodStart(), calculationDTO.getPeriodEnd());
        
        // Validate minimum profit requirement
        if (calculationDTO.getMinimumProfitAmount() != null && 
            totalProfit.compareTo(calculationDTO.getMinimumProfitAmount()) < 0) {
            throw new BusinessRuleException("Profit below minimum threshold for dividend calculation");
        }
        
        // Calculate total dividends
        BigDecimal totalDividends = totalProfit.multiply(calculationDTO.getDividendPercentage())
            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        
        // Get all members with savings
        List<SavingAccount> savingAccounts = savingAccountRepository.findAll();
        List<DividendMemberDTO> memberDividends = new ArrayList<>();
        BigDecimal totalShareCapital = BigDecimal.ZERO;
        BigDecimal totalSavings = BigDecimal.ZERO;
        
        // Calculate member contributions
        for (SavingAccount account : savingAccounts) {
            DividendMemberDTO memberDividend = calculateMemberDividend(
                account, calculationDTO, totalDividends, savingAccounts);
            
            memberDividends.add(memberDividend);
            totalShareCapital = totalShareCapital.add(account.getShareCapital());
            totalSavings = totalSavings.add(account.getDeposit());
        }
        
        // Create calculation record
        DividendCalculation calculation = createCalculationRecord(calculationDTO, totalProfit, 
                                                            totalDividends, calculatedBy);
        calculation = calculationRepository.save(calculation);
        
        // Create dividend records
        List<Dividend> dividends = createDividendRecords(memberDividends, calculation);
        dividendRepository.saveAll(dividends);
        
        // Calculate totals
        BigDecimal totalTax = memberDividends.stream()
            .map(DividendMemberDTO::getTaxAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalNetDividends = totalDividends.subtract(totalTax);
        BigDecimal retainedEarnings = totalProfit.subtract(totalDividends);
        
        // Build result
        return buildCalculationResult(calculation, memberDividends, totalProfit, 
                                  totalDividends, totalTax, totalNetDividends, retainedEarnings);
    }
    
    @PreAuthorize("hasAnyRole('SECRETARY', 'PRESIDENT')")
    public DividendCalculationResultDTO approveCalculation(String calculationId, String approvedBy, String notes) {
        DividendCalculation calculation = calculationRepository.findByCalculationNumber(calculationId)
            .orElseThrow(() -> new ResourceNotFoundException("Dividend calculation not found"));
        
        if (!calculation.canBeApproved()) {
            throw new BusinessRuleException("Calculation cannot be approved in current status");
        }
        
        // Approve calculation
        calculation.approve(approvedBy, notes);
        calculationRepository.save(calculation);
        
        // Update dividend statuses
        List<Dividend> dividends = dividendRepository.findByCalculationId(calculationId);
        dividends.forEach(dividend -> dividend.approve(LocalDate.now()));
        dividendRepository.saveAll(dividends);
        
        // Send notifications
        notificationService.notifyDividendsApproved(dividends, calculation);
        
        return getCalculationResult(calculationId);
    }
    
    @PreAuthorize("hasAnyRole('SECRETARY', 'PRESIDENT')")
    public void payDividends(String calculationId) {
        List<Dividend> dividends = dividendRepository.findByCalculationId(calculationId);
        List<Dividend> approvedDividends = dividends.stream()
            .filter(d -> d.getStatus() == DividendStatus.APPROVED)
            .collect(Collectors.toList());
        
        for (Dividend dividend : approvedDividends) {
            // Credit member's savings account
            creditDividendToSavings(dividend);
            
            // Record transaction
            recordDividendTransaction(dividend);
            
            // Update status
            dividend.pay();
        }
        
        dividendRepository.saveAll(approvedDividends);
        
        // Send payment notifications
        notificationService.notifyDividendsPaid(approvedDividends);
    }
    
    @Transactional(readOnly = true)
    public DividendCalculationResultDTO getCalculationResult(String calculationId) {
        DividendCalculation calculation = calculationRepository.findByCalculationNumber(calculationId)
            .orElseThrow(() -> new ResourceNotFoundException("Dividend calculation not found"));
        
        List<Dividend> dividends = dividendRepository.findByCalculationId(calculationId);
        List<DividendMemberDTO> memberDividends = dividends.stream()
            .map(this::convertToMemberDTO)
            .collect(Collectors.toList());
        
        BigDecimal totalTax = dividends.stream()
            .map(Dividend::getTaxAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return buildCalculationResult(calculation, memberDividends, calculation.getTotalProfit(),
                                  calculation.getTotalDividends(), totalTax, 
                                  calculation.getTotalNetDividends(), calculation.getRetainedEarnings());
    }
    
    private BigDecimal calculateTotalProfit(LocalDate startDate, LocalDate endDate) {
        // Get income from loans (interest income)
        BigDecimal interestIncome = transactionService.getInterestIncome(startDate, endDate);
        
        // Get income from other sources
        BigDecimal otherIncome = transactionService.getOtherIncome(startDate, endDate);
        
        // Get expenses (interest paid on savings, operating expenses)
        BigDecimal interestExpense = transactionService.getInterestExpense(startDate, endDate);
        BigDecimal operatingExpenses = transactionService.getOperatingExpenses(startDate, endDate);
        
        // Calculate net profit
        return interestIncome.add(otherIncome).subtract(interestExpense).subtract(operatingExpenses);
    }
    
    private DividendMemberDTO calculateMemberDividend(SavingAccount account, 
                                                    DividendCalculationDTO calculationDTO,
                                                    BigDecimal totalDividends,
                                                    List<SavingAccount> allAccounts) {
        DividendMemberDTO memberDividend = new DividendMemberDTO();
        memberDividend.setMemberId(account.getMember().getMemberId());
        memberDividend.setMemberName(account.getMember().getName());
        memberDividend.setIdCard(account.getMember().getIdCard());
        memberDividend.setShareCapital(account.getShareCapital());
        memberDividend.setSavingsDeposit(account.getDeposit());
        memberDividend.setTotalContributions(account.getBalance());
        
        // Calculate share dividend
        BigDecimal totalShareCapital = allAccounts.stream()
            .map(SavingAccount::getShareCapital)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal shareDividend = BigDecimal.ZERO;
        if (totalShareCapital.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal shareRatio = account.getShareCapital().divide(totalShareCapital, 8, RoundingMode.HALF_UP);
            BigDecimal shareAllocation = totalDividends.multiply(calculationDTO.getShareRate())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            shareDividend = shareAllocation.multiply(shareRatio);
        }
        
        // Calculate deposit dividend
        BigDecimal totalSavings = allAccounts.stream()
            .map(SavingAccount::getDeposit)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal depositDividend = BigDecimal.ZERO;
        if (totalSavings.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal depositRatio = account.getDeposit().divide(totalSavings, 8, RoundingMode.HALF_UP);
            BigDecimal depositAllocation = totalDividends.multiply(calculationDTO.getDepositRate())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            depositDividend = depositAllocation.multiply(depositRatio);
        }
        
        // Calculate interest refund (simplified calculation)
        BigDecimal interestRefund = calculateInterestRefund(account, calculationDTO);
        
        BigDecimal grossDividend = shareDividend.add(depositDividend).add(interestRefund);
        
        // Calculate tax
        BigDecimal taxAmount = grossDividend.multiply(calculationDTO.getTaxRate())
            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        
        BigDecimal netDividend = grossDividend.subtract(taxAmount);
        
        memberDividend.setShareDividend(shareDividend);
        memberDividend.setDepositDividend(depositDividend);
        memberDividend.setInterestRefund(interestRefund);
        memberDividend.setGrossDividend(grossDividend);
        memberDividend.setTaxAmount(taxAmount);
        memberDividend.setNetDividend(netDividend);
        
        return memberDividend;
    }
    
    private BigDecimal calculateInterestRefund(SavingAccount account, DividendCalculationDTO calculationDTO) {
        // Simplified: Return a percentage of interest paid on loans
        // In real implementation, this would be based on actual loan interest paid by the member
        BigDecimal averageLoanBalance = account.getShareCapital().multiply(BigDecimal.valueOf(2)); // Assume 2x share capital as average loan
        BigDecimal refundRate = calculationDTO.getInterestRefundRate().divide(BigDecimal.valueOf(100), 8, RoundingMode.HALF_UP);
        return averageLoanBalance.multiply(refundRate);
    }
    
    private void validateCalculationRequest(DividendCalculationDTO calculationDTO) {
        // Validate period
        if (calculationDTO.getPeriodEnd().isBefore(calculationDTO.getPeriodStart())) {
            throw new ValidationException("Period end must be after period start");
        }
        
        // Validate rates sum doesn't exceed 100%
        BigDecimal totalAllocation = calculationDTO.getShareRate()
            .add(calculationDTO.getDepositRate())
            .add(calculationDTO.getInterestRefundRate());
        
        if (totalAllocation.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new ValidationException("Sum of allocation rates cannot exceed 100%");
        }
        
        // Validate no existing calculation for the same period
        List<DividendCalculation> existing = calculationRepository.findByCalculationDateBetween(
            calculationDTO.getPeriodStart(), calculationDTO.getPeriodEnd());
        
        if (!existing.isEmpty()) {
            throw new BusinessRuleException("Dividend calculation already exists for this period");
        }
    }
    
    private DividendCalculation createCalculationRecord(DividendCalculationDTO dto, BigDecimal totalProfit,
                                                    BigDecimal totalDividends, String calculatedBy) {
        DividendCalculation calculation = new DividendCalculation();
        calculation.setCalculationNumber(generateCalculationNumber());
        calculation.setCalculationDate(LocalDate.now());
        calculation.setPeriodStart(dto.getPeriodStart());
        calculation.setPeriodEnd(dto.getPeriodEnd());
        calculation.setTotalProfit(totalProfit);
        calculation.setDividendPercentage(dto.getDividendPercentage());
        calculation.setShareRate(dto.getShareRate());
        calculation.setDepositRate(dto.getDepositRate());
        calculation.setInterestRefundRate(dto.getInterestRefundRate());
        calculation.setTaxRate(dto.getTaxRate());
        calculation.setTotalDividends(totalDividends);
        calculation.setTotalTax(totalDividends.multiply(dto.getTaxRate())
            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
        calculation.setTotalNetDividends(calculation.getTotalDividends().subtract(calculation.getTotalTax()));
        calculation.setRetainedEarnings(totalProfit.subtract(totalDividends));
        calculation.setStatus(CalculationStatus.PENDING);
        calculation.setNotes(dto.getNotes());
        
        return calculation;
    }
    
    private List<Dividend> createDividendRecords(List<DividendMemberDTO> memberDividends, 
                                               DividendCalculation calculation) {
        return memberDividends.stream()
            .map(memberDividend -> {
                Dividend dividend = new Dividend();
                dividend.setMember(memberRepository.findById(memberDividend.getMemberId()).orElse(null));
                dividend.setAmount(memberDividend.getNetDividend());
                dividend.setShareDividend(memberDividend.getShareDividend());
                dividend.setDepositDividend(memberDividend.getDepositDividend());
                dividend.setInterestRefund(memberDividend.getInterestRefund());
                dividend.setTaxAmount(memberDividend.getTaxAmount());
                dividend.setNetAmount(memberDividend.getNetDividend());
                dividend.setStatus(DividendStatus.PENDING);
                dividend.setCalculationDate(calculation.getCalculationDate());
                dividend.setCalculationId(calculation.getCalculationNumber());
                
                return dividend;
            })
            .collect(Collectors.toList());
    }
    
    private String generateCalculationNumber() {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomSuffix = String.format("%04d", new Random().nextInt(10000));
        return "DIV" + dateStr + randomSuffix;
    }
    
    private void creditDividendToSavings(Dividend dividend) {
        // Add dividend to member's savings account
        SavingAccount account = savingAccountRepository.findByMemberId(dividend.getMember().getMemberId())
            .orElseThrow(() -> new ResourceNotFoundException("Savings account not found"));
        
        account.addToDeposit(dividend.getNetAmount());
        savingAccountRepository.save(account);
    }
    
    private void recordDividendTransaction(Dividend dividend) {
        // Record dividend payment in general ledger
        transactionService.recordDividendTransaction(dividend);
    }
    
    private DividendMemberDTO convertToMemberDTO(Dividend dividend) {
        DividendMemberDTO dto = new DividendMemberDTO();
        dto.setMemberId(dividend.getMember().getMemberId());
        dto.setMemberName(dividend.getMember().getName());
        dto.setIdCard(dividend.getMember().getIdCard());
        dto.setShareDividend(dividend.getShareDividend());
        dto.setDepositDividend(dividend.getDepositDividend());
        dto.setInterestRefund(dividend.getInterestRefund());
        dto.setGrossDividend(dividend.getGrossAmount());
        dto.setTaxAmount(dividend.getTaxAmount());
        dto.setNetDividend(dividend.getNetAmount());
        dto.setCalculationId(dividend.getCalculationId());
        
        return dto;
    }
    
    private DividendCalculationResultDTO buildCalculationResult(DividendCalculation calculation,
                                                          List<DividendMemberDTO> memberDividends,
                                                          BigDecimal totalProfit,
                                                          BigDecimal totalDividends,
                                                          BigDecimal totalTax,
                                                          BigDecimal totalNetDividends,
                                                          BigDecimal retainedEarnings) {
        DividendCalculationResultDTO result = new DividendCalculationResultDTO();
        result.setCalculationId(calculation.getCalculationNumber());
        result.setCalculationDate(calculation.getCalculationDate());
        result.setPeriodStart(calculation.getPeriodStart());
        result.setPeriodEnd(calculation.getPeriodEnd());
        result.setTotalProfit(totalProfit);
        result.setDividendPercentage(calculation.getDividendPercentage());
        result.setTotalDividends(totalDividends);
        result.setTotalTax(totalTax);
        result.setTotalNetDividends(totalNetDividends);
        result.setRetainedEarnings(retainedEarnings);
        result.setTotalMembers(memberDividends.size());
        result.setAverageDividend(memberDividends.isEmpty() ? BigDecimal.ZERO : 
            totalNetDividends.divide(BigDecimal.valueOf(memberDividends.size()), 2, RoundingMode.HALF_UP));
        result.setStatus(calculation.getStatus());
        result.setMemberDividends(memberDividends);
        
        return result;
    }
}
```

### 6. Controller Layer

#### DividendController
```java
@RestController
@RequestMapping("/api/dividends")
@Validated
public class DividendController {
    
    @Autowired
    private DividendCalculationService dividendService;
    
    @PostMapping("/calculate")
    @PreAuthorize("hasAnyRole('SECRETARY', 'PRESIDENT')")
    public ResponseEntity<ApiResponse<DividendCalculationResultDTO>> calculateDividends(
            @Valid @RequestBody DividendCalculationDTO calculationDTO,
            Authentication authentication) {
        
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String calculatedBy = userDetails.getUsername();
        
        DividendCalculationResultDTO result = dividendService.calculateDividends(calculationDTO, calculatedBy);
        
        return ResponseEntity.ok(ApiResponse.success(result, "Dividend calculation completed"));
    }
    
    @PutMapping("/calculation/{calculationId}/approve")
    @PreAuthorize("hasAnyRole('SECRETARY', 'PRESIDENT')")
    public ResponseEntity<ApiResponse<DividendCalculationResultDTO>> approveCalculation(
            @PathVariable String calculationId,
            @RequestBody Map<String, String> approvalRequest) {
        
        String approvedBy = approvalRequest.get("approvedBy");
        String notes = approvalRequest.get("notes");
        
        DividendCalculationResultDTO result = dividendService.approveCalculation(calculationId, approvedBy, notes);
        
        return ResponseEntity.ok(ApiResponse.success(result, "Dividend calculation approved"));
    }
    
    @PutMapping("/calculation/{calculationId}/pay")
    @PreAuthorize("hasAnyRole('SECRETARY', 'PRESIDENT')")
    public ResponseEntity<ApiResponse<String>> payDividends(@PathVariable String calculationId) {
        
        dividendService.payDividends(calculationId);
        
        return ResponseEntity.ok(ApiResponse.success("Dividends paid successfully"));
    }
    
    @GetMapping("/calculation/{calculationId}")
    @PreAuthorize("hasAnyRole('SECRETARY', 'PRESIDENT')")
    public ResponseEntity<ApiResponse<DividendCalculationResultDTO>> getCalculationResult(@PathVariable String calculationId) {
        
        DividendCalculationResultDTO result = dividendService.getCalculationResult(calculationId);
        
        return ResponseEntity.ok(ApiResponse.success(result));
    }
    
    @GetMapping("/member/{memberId}")
    @PreAuthorize("hasRole('OFFICER') or hasRole('SECRETARY') or hasRole('PRESIDENT') or " +
            "@memberSecurity.canViewMemberDetails(#memberId, authentication)")
    public ResponseEntity<ApiResponse<List<Dividend>>> getMemberDividends(@PathVariable Long memberId) {
        
        List<Dividend> dividends = dividendService.getMemberDividends(memberId);
        
        return ResponseEntity.ok(ApiResponse.success(dividends));
    }
    
    @GetMapping("/my-dividends")
    @PreAuthorize("hasRole('MEMBER')")
    public ResponseEntity<ApiResponse<List<Dividend>>> getMyDividends(Authentication authentication) {
        
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long memberId = userDetails.getUser().getMember().getMemberId();
        
        List<Dividend> dividends = dividendService.getMemberDividends(memberId);
        
        return ResponseEntity.ok(ApiResponse.success(dividends));
    }
    
    @GetMapping("/calculations")
    @PreAuthorize("hasAnyRole('SECRETARY', 'PRESIDENT')")
    public ResponseEntity<ApiResponse<List<DividendCalculation>>> getCalculations() {
        
        List<DividendCalculation> calculations = dividendService.getAllCalculations();
        
        return ResponseEntity.ok(ApiResponse.success(calculations));
    }
}
```

## Business Rules & Validation

### Dividend Calculation Rules
1. **Profit Requirement**: Minimum profit threshold before dividend calculation
2. **Allocation Rates**: Sum of allocation rates must equal 100%
3. **Member Eligibility**: Only members with active savings accounts qualify
4. **Period Uniqueness**: No overlapping calculation periods
5. **Rate Limits**: Maximum rates for each dividend component

### Tax Calculation Rules
1. **Tax Rates**: Based on current tax regulations
2. **Tax Thresholds**: Progressive tax rates may apply
3. **Tax Exemptions**: Certain dividend types may be tax-exempt
4. **Withholding**: Tax must be withheld at source

### Payment Rules
1. **Payment Method**: Dividends credited to savings accounts
2. **Approval Required**: Calculations must be approved before payment
3. **Payment Timing**: Payments made within specified timeframe
4. **Record Keeping**: All payments must be recorded in ledger

## Testing Strategy

### Unit Tests
```java
@ExtendWith(MockitoExtension.class)
class DividendCalculationServiceTest {
    
    @Mock
    private SavingAccountRepository savingAccountRepository;
    
    @Mock
    private TransactionService transactionService;
    
    @Mock
    private DividendCalculationRepository calculationRepository;
    
    @InjectMocks
    private DividendCalculationService dividendService;
    
    @Test
    void testCalculateDividends_Success() {
        // Given
        DividendCalculationDTO calculationDTO = createValidCalculationDTO();
        List<SavingAccount> accounts = createTestSavingAccounts();
        
        when(savingAccountRepository.findAll()).thenReturn(accounts);
        when(transactionService.getInterestIncome(any(), any())).thenReturn(BigDecimal.valueOf(1000000));
        when(calculationRepository.save(any(DividendCalculation.class))).thenReturn(createTestCalculation());
        
        // When
        DividendCalculationResultDTO result = dividendService.calculateDividends(calculationDTO, "testuser");
        
        // Then
        assertThat(result.getTotalMembers()).isEqualTo(2);
        assertThat(result.getTotalProfit()).isPositive();
        verify(calculationRepository).save(any(DividendCalculation.class));
    }
    
    @Test
    void testCalculateDividends_InvalidRates_ThrowsException() {
        // Given
        DividendCalculationDTO calculationDTO = createInvalidRateCalculationDTO();
        
        // When & Then
        assertThrows(ValidationException.class, 
            () -> dividendService.calculateDividends(calculationDTO, "testuser"));
    }
}
```

---

**Related Documentation**:
- [Database Schema](../architecture/database-schema.md) - Entity definitions
- [API Documentation](../api/rest-endpoints.md) - Endpoint details
- [Business Rules](../reference/business-rules.md) - Detailed dividend rules
