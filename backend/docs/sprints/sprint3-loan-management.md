# Sprint 3: Loan Management Service

## Overview

Sprint 3 implements the **Loan Management Service** which handles loan applications, approvals, collateral management, and guarantor validation. This sprint provides the core lending functionality for the savings group with proper risk management controls.

## Sprint Objectives

### Primary Goals
- ✅ Implement loan application processing
- ✅ Create loan eligibility validation
- ✅ Set up collateral and guarantor management
- ✅ Develop loan status tracking
- ✅ Implement loan calculation engine

### Success Criteria
- Members can apply for loans with proper documentation
- Loan eligibility rules are enforced consistently
- Collateral and guarantor requirements are validated
- Loan status transitions are properly managed
- Interest calculations are accurate and transparent

## Technical Implementation

### 1. Entity Classes

#### Loan Entity
```java
@Entity
@Table(name = "loan")
public class Loan {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long loanId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;
    
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;
    
    @Column(name = "interest_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal interestRate;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LoanType loanType;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LoanStatus status;
    
    @Column(length = 500)
    private String purpose;
    
    @Column(name = "term_months")
    private Integer termMonths;
    
    @Column(name = "doc_ref")
    private String docRef;
    
    @Column(name = "application_date")
    @CreationTimestamp
    private LocalDateTime applicationDate;
    
    @Column(name = "approval_date")
    private LocalDateTime approvalDate;
    
    @Column(name = "disbursement_date")
    private LocalDateTime disbursementDate;
    
    @Column(name = "maturity_date")
    private LocalDateTime maturityDate;
    
    @Column(name = "approved_by")
    private String approvedBy;
    
    @Column(name = "approval_notes", length = 1000)
    private String approvalNotes;
    
    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;
    
    @ManyToMany
    @JoinTable(
        name = "loan_guarantor",
        joinColumns = @JoinColumn(name = "loan_id"),
        inverseJoinColumns = @JoinColumn(name = "guarantor_id")
    )
    private Set<Member> guarantors;
    
    @OneToMany(mappedBy = "loan", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<LoanBalance> loanBalances;
    
    @OneToMany(mappedBy = "loan", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Collateral> collaterals;
    
    // Constructors, getters, setters
    
    public void approve(String approvedBy, String approvalNotes) {
        this.status = LoanStatus.APPROVED;
        this.approvedBy = approvedBy;
        this.approvalNotes = approvalNotes;
        this.approvalDate = LocalDateTime.now();
        this.maturityDate = LocalDateTime.now().plusMonths(this.termMonths);
    }
    
    public void reject(String rejectionReason) {
        this.status = LoanStatus.REJECTED;
        this.rejectionReason = rejectionReason;
    }
    
    public void disburse() {
        if (this.status != LoanStatus.APPROVED) {
            throw new BusinessRuleException("Only approved loans can be disbursed");
        }
        this.status = LoanStatus.ACTIVE;
        this.disbursementDate = LocalDateTime.now();
    }
    
    public BigDecimal calculateMonthlyInstallment() {
        // Using simple interest formula
        BigDecimal monthlyRate = interestRate.divide(BigDecimal.valueOf(100 * 12), 8, RoundingMode.HALF_UP);
        return amount.multiply(monthlyRate.add(BigDecimal.ONE))
                  .divide(BigDecimal.valueOf(termMonths), 2, RoundingMode.HALF_UP);
    }
    
    public BigDecimal calculateTotalInterest() {
        return calculateMonthlyInstallment()
               .multiply(BigDecimal.valueOf(termMonths))
               .subtract(amount);
    }
}
```

#### Collateral Entity
```java
@Entity
@Table(name = "collateral")
public class Collateral {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long collateralId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_id", nullable = false)
    private Loan loan;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private CollateralType type;
    
    @Column(nullable = false, length = 200)
    private String description;
    
    @Column(name = "doc_ref")
    private String documentRef;
    
    @Column(precision = 15, scale = 2)
    private BigDecimal estimatedValue;
    
    @Column(name = "valuation_date")
    private LocalDate valuationDate;
    
    @Column(name = "document_path")
    private String documentPath;
    
    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    // Constructors, getters, setters
}
```

### 2. Enums

#### LoanType
```java
public enum LoanType {
    PERSONAL("Personal Loan", 50000.00, 24, 15.0),
    BUSINESS("Business Loan", 200000.00, 36, 18.0),
    EMERGENCY("Emergency Loan", 20000.00, 12, 20.0),
    EDUCATION("Education Loan", 100000.00, 48, 12.0),
    HOUSING("Housing Loan", 500000.00, 120, 10.0);
    
    private final String description;
    private final BigDecimal maxAmount;
    private final Integer maxTermMonths;
    private final Double maxInterestRate;
    
    LoanType(String description, BigDecimal maxAmount, Integer maxTermMonths, Double maxInterestRate) {
        this.description = description;
        this.maxAmount = maxAmount;
        this.maxTermMonths = maxTermMonths;
        this.maxInterestRate = maxInterestRate;
    }
    
    // Getters
}
```

#### LoanStatus
```java
public enum LoanStatus {
    PENDING("Pending Approval"),
    APPROVED("Approved"),
    REJECTED("Rejected"),
    ACTIVE("Active"),
    COMPLETED("Completed"),
    DEFAULTED("Defaulted"),
    WRITTEN_OFF("Written Off");
    
    private final String description;
    
    LoanStatus(String description) {
        this.description = description;
    }
    
    // Getters
}
```

#### CollateralType
```java
public enum CollateralType {
    PROPERTY("Real Estate", 0.7),
    VEHICLE("Vehicle", 0.5),
    JEWELRY("Jewelry", 0.8),
    DEPOSIT("Savings Deposit", 1.0),
    LAND("Land Title", 0.6),
    EQUIPMENT("Equipment", 0.4),
    GUARANTOR("Personal Guarantor", 0.3);
    
    private final String description;
    private final BigDecimal ltvRatio; // Loan-to-Value ratio
    
    CollateralType(String description, Double ltvRatio) {
        this.description = description;
        this.ltvRatio = BigDecimal.valueOf(ltvRatio);
    }
    
    // Getters
}
```

### 3. Data Transfer Objects (DTOs)

#### LoanApplicationDTO
```java
public class LoanApplicationDTO {
    
    @NotNull(message = "Member ID is required")
    private Long memberId;
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "1000.00", message = "Minimum loan amount is 1,000.00")
    @DecimalMax(value = "500000.00", message = "Maximum loan amount is 500,000.00")
    private BigDecimal amount;
    
    @NotNull(message = "Interest rate is required")
    @DecimalMin(value = "1.00", message = "Minimum interest rate is 1.00%")
    @DecimalMax(value = "36.00", message = "Maximum interest rate is 36.00%")
    private BigDecimal interestRate;
    
    @NotNull(message = "Loan type is required")
    private LoanType loanType;
    
    @NotBlank(message = "Purpose is required")
    @Size(max = 500, message = "Purpose must not exceed 500 characters")
    private String purpose;
    
    @NotNull(message = "Term is required")
    @Min(value = 1, message = "Minimum term is 1 month")
    @Max(value = 120, message = "Maximum term is 120 months")
    private Integer termMonths;
    
    private List<CollateralDTO> collaterals;
    
    private List<Long> guarantorIds;
    
    @NotBlank(message = "Application reference is required")
    private String applicationReference;
    
    private String notes;
    
    // Constructors, getters, setters
}
```

#### CollateralDTO
```java
public class CollateralDTO {
    
    @NotNull(message = "Collateral type is required")
    private CollateralType type;
    
    @NotBlank(message = "Description is required")
    @Size(max = 200, message = "Description must not exceed 200 characters")
    private String description;
    
    private String documentRef;
    
    @DecimalMin(value = "0.01", message = "Estimated value must be positive")
    @Digits(integer = 13, fraction = 2)
    private BigDecimal estimatedValue;
    
    private LocalDate valuationDate;
    
    private String documentBase64; // Base64 encoded document
    
    // Constructors, getters, setters
}
```

#### LoanResponseDTO
```java
public class LoanResponseDTO {
    private Long loanId;
    private Long memberId;
    private String memberName;
    private BigDecimal amount;
    private BigDecimal interestRate;
    private LoanType loanType;
    private LoanStatus status;
    private String purpose;
    private Integer termMonths;
    private BigDecimal monthlyInstallment;
    private BigDecimal totalInterest;
    private LocalDateTime applicationDate;
    private LocalDateTime approvalDate;
    private LocalDateTime disbursementDate;
    private LocalDateTime maturityDate;
    private String approvedBy;
    private String approvalNotes;
    private List<CollateralDTO> collaterals;
    private List<GuarantorDTO> guarantors;
    
    public static LoanResponseDTO fromEntity(Loan loan) {
        LoanResponseDTO dto = new LoanResponseDTO();
        dto.setLoanId(loan.getLoanId());
        dto.setMemberId(loan.getMember().getMemberId());
        dto.setMemberName(loan.getMember().getName());
        dto.setAmount(loan.getAmount());
        dto.setInterestRate(loan.getInterestRate());
        dto.setLoanType(loan.getLoanType());
        dto.setStatus(loan.getStatus());
        dto.setPurpose(loan.getPurpose());
        dto.setTermMonths(loan.getTermMonths());
        dto.setMonthlyInstallment(loan.calculateMonthlyInstallment());
        dto.setTotalInterest(loan.calculateTotalInterest());
        dto.setApplicationDate(loan.getApplicationDate());
        dto.setApprovalDate(loan.getApprovalDate());
        dto.setDisbursementDate(loan.getDisbursementDate());
        dto.setMaturityDate(loan.getMaturityDate());
        dto.setApprovedBy(loan.getApprovedBy());
        dto.setApprovalNotes(loan.getApprovalNotes());
        
        if (loan.getCollaterals() != null) {
            dto.setCollaterals(loan.getCollaterals().stream()
                .map(CollateralDTO::fromEntity)
                .collect(Collectors.toList()));
        }
        
        if (loan.getGuarantors() != null) {
            dto.setGuarantors(loan.getGuarantors().stream()
                .map(member -> new GuarantorDTO(member.getMemberId(), member.getName()))
                .collect(Collectors.toList()));
        }
        
        return dto;
    }
    
    // Constructors, getters, setters
}
```

### 4. Repository Layer

#### LoanRepository
```java
@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {
    
    List<Loan> findByMemberId(Long memberId);
    
    List<Loan> findByStatus(LoanStatus status);
    
    @Query("SELECT l FROM Loan l WHERE l.status = :status ORDER BY l.applicationDate DESC")
    List<Loan> findByStatusOrderByApplicationDateDesc(@Param("status") LoanStatus status);
    
    @Query("SELECT l FROM Loan l WHERE l.member.id = :memberId AND l.status IN :statuses")
    List<Loan> findByMemberIdAndStatusIn(@Param("memberId") Long memberId, 
                                         @Param("statuses") List<LoanStatus> statuses);
    
    @Query("SELECT COUNT(l) FROM Loan l WHERE l.status = :status")
    long countByStatus(@Param("status") LoanStatus status);
    
    @Query("SELECT SUM(l.amount) FROM Loan l WHERE l.status = :status")
    BigDecimal sumAmountByStatus(@Param("status") LoanStatus status);
    
    @Query("SELECT l FROM Loan l WHERE l.applicationDate BETWEEN :startDate AND :endDate")
    List<Loan> findByApplicationDateBetween(@Param("startDate") LocalDateTime startDate,
                                           @Param("endDate") LocalDateTime endDate);
}
```

#### CollateralRepository
```java
@Repository
public interface CollateralRepository extends JpaRepository<Collateral, Long> {
    
    List<Collateral> findByLoanId(Long loanId);
    
    @Query("SELECT c FROM Collateral c WHERE c.loan.member.id = :memberId")
    List<Collateral> findByMemberId(@Param("memberId") Long memberId);
    
    @Query("SELECT SUM(c.estimatedValue) FROM Collateral c WHERE c.loan.id = :loanId")
    BigDecimal getTotalCollateralValue(@Param("loanId") Long loanId);
}
```

### 5. Service Layer

#### LoanService
```java
@Service
@Transactional
public class LoanService {
    
    @Autowired
    private LoanRepository loanRepository;
    
    @Autowired
    private MemberRepository memberRepository;
    
    @Autowired
    private CollateralRepository collateralRepository;
    
    @Autowired
    private SavingAccountRepository savingAccountRepository;
    
    @Autowired
    private LoanEligibilityService eligibilityService;
    
    @Autowired
    private CollateralService collateralService;
    
    @Autowired
    private FileStorageService fileStorageService;
    
    public LoanResponseDTO applyForLoan(LoanApplicationDTO applicationDTO) {
        // Validate member exists and is active
        Member member = memberRepository.findById(applicationDTO.getMemberId())
            .orElseThrow(() -> new ResourceNotFoundException("Member not found"));
        
        // Validate loan eligibility
        LoanEligibilityResult eligibility = eligibilityService.checkEligibility(member, applicationDTO);
        if (!eligibility.isEligible()) {
            throw new BusinessRuleException("Loan not eligible: " + eligibility.getReason());
        }
        
        // Create loan entity
        Loan loan = createLoanFromDTO(applicationDTO, member);
        
        // Process collaterals
        if (applicationDTO.getCollaterals() != null && !applicationDTO.getCollaterals().isEmpty()) {
            processCollaterals(applicationDTO.getCollaterals(), loan);
        }
        
        // Process guarantors
        if (applicationDTO.getGuarantorIds() != null && !applicationDTO.getGuarantorIds().isEmpty()) {
            processGuarantors(applicationDTO.getGuarantorIds(), loan);
        }
        
        // Validate collateral and guarantor requirements
        validateLoanRequirements(loan, applicationDTO);
        
        // Save loan
        loan = loanRepository.save(loan);
        
        return LoanResponseDTO.fromEntity(loan);
    }
    
    @Transactional(readOnly = true)
    public LoanResponseDTO getLoan(Long loanId) {
        Loan loan = loanRepository.findById(loanId)
            .orElseThrow(() -> new ResourceNotFoundException("Loan not found"));
        
        return LoanResponseDTO.fromEntity(loan);
    }
    
    @PreAuthorize("hasRole('PRESIDENT')")
    public LoanResponseDTO approveLoan(Long loanId, LoanApprovalDTO approvalDTO) {
        Loan loan = loanRepository.findById(loanId)
            .orElseThrow(() -> new ResourceNotFoundException("Loan not found"));
        
        if (loan.getStatus() != LoanStatus.PENDING) {
            throw new BusinessRuleException("Only pending loans can be approved");
        }
        
        // Final eligibility check before approval
        LoanEligibilityResult finalCheck = eligibilityService.finalEligibilityCheck(loan);
        if (!finalCheck.isEligible()) {
            throw new BusinessRuleException("Loan approval failed: " + finalCheck.getReason());
        }
        
        // Approve the loan
        loan.approve(approvalDTO.getApprovedBy(), approvalDTO.getApprovalNotes());
        
        // Create loan balance record
        createInitialLoanBalance(loan);
        
        loan = loanRepository.save(loan);
        
        return LoanResponseDTO.fromEntity(loan);
    }
    
    @PreAuthorize("hasRole('PRESIDENT')")
    public LoanResponseDTO rejectLoan(Long loanId, LoanRejectionDTO rejectionDTO) {
        Loan loan = loanRepository.findById(loanId)
            .orElseThrow(() -> new ResourceNotFoundException("Loan not found"));
        
        if (loan.getStatus() != LoanStatus.PENDING) {
            throw new BusinessRuleException("Only pending loans can be rejected");
        }
        
        loan.reject(rejectionDTO.getRejectionReason());
        loan = loanRepository.save(loan);
        
        return LoanResponseDTO.fromEntity(loan);
    }
    
    @Transactional(readOnly = true)
    public Page<LoanResponseDTO> getPendingLoans(Pageable pageable) {
        Page<Loan> loans = loanRepository.findByStatusOrderByApplicationDateDesc(
            LoanStatus.PENDING, pageable);
        
        return loans.map(LoanResponseDTO::fromEntity);
    }
    
    @Transactional(readOnly = true)
    public List<LoanResponseDTO> getMemberLoans(Long memberId) {
        List<Loan> loans = loanRepository.findByMemberId(memberId);
        
        return loans.stream()
            .map(LoanResponseDTO::fromEntity)
            .collect(Collectors.toList());
    }
    
    private Loan createLoanFromDTO(LoanApplicationDTO dto, Member member) {
        Loan loan = new Loan();
        loan.setMember(member);
        loan.setAmount(dto.getAmount());
        loan.setInterestRate(dto.getInterestRate());
        loan.setLoanType(dto.getLoanType());
        loan.setStatus(LoanStatus.PENDING);
        loan.setPurpose(dto.getPurpose());
        loan.setTermMonths(dto.getTermMonths());
        
        return loan;
    }
    
    private void processCollaterals(List<CollateralDTO> collateralDTOs, Loan loan) {
        for (CollateralDTO collateralDTO : collateralDTOs) {
            Collateral collateral = collateralService.createCollateral(collateralDTO, loan);
            loan.getCollaterals().add(collateral);
        }
    }
    
    private void processGuarantors(List<Long> guarantorIds, Loan loan) {
        Set<Member> guarantors = new HashSet<>();
        
        for (Long guarantorId : guarantorIds) {
            Member guarantor = memberRepository.findById(guarantorId)
                .orElseThrow(() -> new ResourceNotFoundException("Guarantor not found: " + guarantorId));
            
            // Validate guarantor eligibility
            if (!eligibilityService.isValidGuarantor(guarantor, loan.getAmount())) {
                throw new BusinessRuleException("Guarantor not eligible: " + guarantor.getName());
            }
            
            guarantors.add(guarantor);
        }
        
        loan.setGuarantors(guarantors);
    }
    
    private void validateLoanRequirements(Loan loan, LoanApplicationDTO applicationDTO) {
        // Check if loan amount exceeds collateral value
        BigDecimal totalCollateralValue = collateralRepository.getTotalCollateralValue(loan.getLoanId());
        if (totalCollateralValue != null) {
            BigDecimal maxLoanAmount = totalCollateralValue.multiply(new BigDecimal("0.8")); // 80% LTV
            if (loan.getAmount().compareTo(maxLoanAmount) > 0) {
                throw new BusinessRuleException("Loan amount exceeds collateral coverage");
            }
        }
        
        // Check minimum guarantor requirements for large loans
        if (loan.getAmount().compareTo(new BigDecimal("100000.00")) > 0 && 
            (loan.getGuarantors() == null || loan.getGuarantors().size() < 2)) {
            throw new BusinessRuleException("Loans over 100,000 require at least 2 guarantors");
        }
    }
    
    private void createInitialLoanBalance(Loan loan) {
        LoanBalance balance = new LoanBalance();
        balance.setLoan(loan);
        balance.setPrincipal(loan.getAmount());
        balance.setInterest(BigDecimal.ZERO);
        balance.setForwardDate(LocalDate.now());
        
        loan.getLoanBalances().add(balance);
    }
}
```

#### LoanEligibilityService
```java
@Service
public class LoanEligibilityService {
    
    @Autowired
    private MemberRepository memberRepository;
    
    @Autowired
    private LoanRepository loanRepository;
    
    @Autowired
    private SavingAccountRepository savingAccountRepository;
    
    public LoanEligibilityResult checkEligibility(Member member, LoanApplicationDTO applicationDTO) {
        List<String> reasons = new ArrayList<>();
        
        // Check member tenure
        LocalDate minRegistrationDate = LocalDate.now().minusMonths(6);
        if (member.getDateRegist().isAfter(minRegistrationDate)) {
            reasons.add("Member must be registered for at least 6 months");
        }
        
        // Check existing loans
        List<Loan> existingLoans = loanRepository.findByMemberIdAndStatusIn(
            member.getMemberId(), Arrays.asList(LoanStatus.ACTIVE, LoanStatus.PENDING));
        
        if (!existingLoans.isEmpty()) {
            reasons.add("Member has existing active or pending loans");
        }
        
        // Check savings balance
        Optional<SavingAccount> savings = savingAccountRepository.findByMemberId(member.getMemberId());
        if (savings.isPresent()) {
            BigDecimal minRequiredSavings = applicationDTO.getAmount().multiply(new BigDecimal("0.2"));
            if (savings.get().getBalance().compareTo(minRequiredSavings) < 0) {
                reasons.add("Insufficient savings balance (minimum 20% of loan amount)");
            }
        } else {
            reasons.add("No savings account found");
        }
        
        // Check loan amount limits based on loan type
        LoanType loanType = applicationDTO.getLoanType();
        if (applicationDTO.getAmount().compareTo(loanType.getMaxAmount()) > 0) {
            reasons.add("Loan amount exceeds maximum for loan type");
        }
        
        // Check credit history (simplified)
        if (hasDefaultedLoans(member.getMemberId())) {
            reasons.add("Member has history of loan defaults");
        }
        
        // Check age requirements for certain loan types
        if (applicationDTO.getLoanType() == LoanType.HOUSING) {
            LocalDate minAgeForHousing = LocalDate.now().minusYears(25);
            // Assuming member has birthDate field
            // if (member.getBirthDate().isAfter(minAgeForHousing)) {
            //     reasons.add("Housing loans require minimum age of 25");
            // }
        }
        
        boolean isEligible = reasons.isEmpty();
        String reason = isEligible ? "Eligible" : String.join("; ", reasons);
        
        return new LoanEligibilityResult(isEligible, reason);
    }
    
    public boolean isValidGuarantor(Member guarantor, BigDecimal loanAmount) {
        // Check if guarantor has sufficient savings
        Optional<SavingAccount> guarantorSavings = savingAccountRepository
            .findByMemberId(guarantor.getMemberId());
        
        if (!guarantorSavings.isPresent()) {
            return false;
        }
        
        // Guarantor must have at least 50% of loan amount in savings
        BigDecimal minGuarantorSavings = loanAmount.multiply(new BigDecimal("0.5"));
        return guarantorSavings.get().getBalance().compareTo(minGuarantorSavings) >= 0;
    }
    
    public LoanEligibilityResult finalEligibilityCheck(Loan loan) {
        // Additional checks during approval phase
        List<String> reasons = new ArrayList<>();
        
        // Re-check guarantor eligibility with current data
        if (loan.getGuarantors() != null) {
            for (Member guarantor : loan.getGuarantors()) {
                if (!isValidGuarantor(guarantor, loan.getAmount())) {
                    reasons.add("Guarantor " + guarantor.getName() + " no longer eligible");
                }
            }
        }
        
        // Check if member status has changed
        // if (!loan.getMember().isActive()) {
        //     reasons.add("Member status is no longer active");
        // }
        
        boolean isEligible = reasons.isEmpty();
        String reason = isEligible ? "Eligible for approval" : String.join("; ", reasons);
        
        return new LoanEligibilityResult(isEligible, reason);
    }
    
    private boolean hasDefaultedLoans(Long memberId) {
        List<Loan> memberLoans = loanRepository.findByMemberId(memberId);
        return memberLoans.stream()
            .anyMatch(loan -> loan.getStatus() == LoanStatus.DEFAULTED || 
                              loan.getStatus() == LoanStatus.WRITTEN_OFF);
    }
}
```

### 6. Controller Layer

#### LoanController
```java
@RestController
@RequestMapping("/api/loans")
@Validated
public class LoanController {
    
    @Autowired
    private LoanService loanService;
    
    @PostMapping("/apply")
    @PreAuthorize("hasAnyRole('OFFICER', 'SECRETARY', 'PRESIDENT')")
    public ResponseEntity<ApiResponse<LoanResponseDTO>> applyForLoan(
            @Valid @RequestBody LoanApplicationDTO applicationDTO) {
        
        LoanResponseDTO response = loanService.applyForLoan(applicationDTO);
        
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(response, "Loan application submitted successfully"));
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('OFFICER') or hasRole('SECRETARY') or hasRole('PRESIDENT') or " +
            "@loanSecurity.canViewLoan(#id, authentication)")
    public ResponseEntity<ApiResponse<LoanResponseDTO>> getLoan(@PathVariable Long id) {
        
        LoanResponseDTO response = loanService.getLoan(id);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('PRESIDENT')")
    public ResponseEntity<ApiResponse<LoanResponseDTO>> approveLoan(
            @PathVariable Long id,
            @Valid @RequestBody LoanApprovalDTO approvalDTO) {
        
        LoanResponseDTO response = loanService.approveLoan(id, approvalDTO);
        
        return ResponseEntity.ok(ApiResponse.success(response, "Loan approved successfully"));
    }
    
    @PutMapping("/{id}/reject")
    @PreAuthorize("hasRole('PRESIDENT')")
    public ResponseEntity<ApiResponse<LoanResponseDTO>> rejectLoan(
            @PathVariable Long id,
            @Valid @RequestBody LoanRejectionDTO rejectionDTO) {
        
        LoanResponseDTO response = loanService.rejectLoan(id, rejectionDTO);
        
        return ResponseEntity.ok(ApiResponse.success(response, "Loan rejected"));
    }
    
    @GetMapping("/pending")
    @PreAuthorize("hasRole('PRESIDENT')")
    public ResponseEntity<ApiResponse<Page<LoanResponseDTO>>> getPendingLoans(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "applicationDate") String sort,
            @RequestParam(defaultValue = "desc") String direction) {
        
        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") ? 
            Sort.Direction.DESC : Sort.Direction.ASC;
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
        
        Page<LoanResponseDTO> loans = loanService.getPendingLoans(pageable);
        
        return ResponseEntity.ok(ApiResponse.success(loans));
    }
    
    @GetMapping("/member/{memberId}")
    @PreAuthorize("hasRole('OFFICER') or hasRole('SECRETARY') or hasRole('PRESIDENT') or " +
            "@memberSecurity.canViewMemberDetails(#memberId, authentication)")
    public ResponseEntity<ApiResponse<List<LoanResponseDTO>>> getMemberLoans(
            @PathVariable Long memberId) {
        
        List<LoanResponseDTO> loans = loanService.getMemberLoans(memberId);
        
        return ResponseEntity.ok(ApiResponse.success(loans));
    }
    
    @GetMapping("/my-loans")
    @PreAuthorize("hasRole('MEMBER')")
    public ResponseEntity<ApiResponse<List<LoanResponseDTO>>> getMyLoans(
            Authentication authentication) {
        
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long memberId = userDetails.getUser().getMember().getMemberId();
        
        List<LoanResponseDTO> loans = loanService.getMemberLoans(memberId);
        
        return ResponseEntity.ok(ApiResponse.success(loans));
    }
}
```

## Business Rules & Validation

### Loan Eligibility Rules
1. **Member Tenure**: Minimum 6 months membership
2. **Existing Loans**: No active or pending loans
3. **Savings Requirement**: Minimum 20% of loan amount in savings
4. **Credit History**: No previous defaults
5. **Age Requirements**: Minimum age based on loan type

### Collateral Requirements
1. **Loan-to-Value**: Maximum 80% of collateral value
2. **Documentation**: Proper documentation required
3. **Valuation**: Recent valuation within 6 months
4. **Ownership**: Clear ownership verified

### Guarantor Requirements
1. **Savings Balance**: Minimum 50% of loan amount
2. **Relationship**: Cannot be immediate family
3. **Credit History**: Clean credit record
4. **Capacity**: Maximum 3 active guarantees

### Approval Rules
1. **Authority**: Only President can approve loans
2. **Final Checks**: Re-verify all requirements
3. **Documentation**: All documents properly filed
4. **Limits**: Respect individual and group limits

## Testing Strategy

### Unit Tests
```java
@ExtendWith(MockitoExtension.class)
class LoanServiceTest {
    
    @Mock
    private LoanRepository loanRepository;
    
    @Mock
    private MemberRepository memberRepository;
    
    @Mock
    private LoanEligibilityService eligibilityService;
    
    @InjectMocks
    private LoanService loanService;
    
    @Test
    void testApplyForLoan_EligibleMember_Success() {
        // Given
        LoanApplicationDTO dto = createValidLoanApplication();
        Member member = createTestMember();
        LoanEligibilityResult eligibility = new LoanEligibilityResult(true, "Eligible");
        
        when(memberRepository.findById(dto.getMemberId())).thenReturn(Optional.of(member));
        when(eligibilityService.checkEligibility(member, dto)).thenReturn(eligibility);
        when(loanRepository.save(any(Loan.class))).thenReturn(createTestLoan());
        
        // When
        LoanResponseDTO response = loanService.applyForLoan(dto);
        
        // Then
        assertThat(response.getMemberId()).isEqualTo(dto.getMemberId());
        assertThat(response.getStatus()).isEqualTo(LoanStatus.PENDING);
        verify(loanRepository).save(any(Loan.class));
    }
    
    @Test
    void testApplyForLoan_IneligibleMember_ThrowsException() {
        // Given
        LoanApplicationDTO dto = createValidLoanApplication();
        Member member = createTestMember();
        LoanEligibilityResult eligibility = new LoanEligibilityResult(false, "Insufficient tenure");
        
        when(memberRepository.findById(dto.getMemberId())).thenReturn(Optional.of(member));
        when(eligibilityService.checkEligibility(member, dto)).thenReturn(eligibility);
        
        // When & Then
        assertThrows(BusinessRuleException.class, () -> loanService.applyForLoan(dto));
    }
}
```

---

**Related Documentation**:
- [Database Schema](../architecture/database-schema.md) - Entity definitions
- [API Documentation](../api/rest-endpoints.md) - Endpoint details
- [Business Rules](../reference/business-rules.md) - Detailed loan rules
