# Sprint 4: Repayment & Receipt Service

## Overview

Sprint 4 implements **Repayment & Receipt Service** which handles loan repayments, payment processing, and automated receipt generation. This sprint provides core payment management functionality with proper financial tracking.

## Sprint Objectives

### Primary Goals
- ✅ Implement loan repayment processing
- ✅ Create payment calculation engine
- ✅ Set up automated receipt generation
- ✅ Develop payment history tracking
- ✅ Implement PDF generation system

### Success Criteria
- Loan repayments can be processed with accurate calculations
- Principal and interest allocations are correctly computed
- Receipts are automatically generated and stored
- Payment history is maintained and auditable
- PDF receipts are professional and complete

## Technical Implementation

### 1. Entity Classes

#### Payment Entity
```java
@Entity
@Table(name = "payment")
public class Payment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paymentId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_id", nullable = false)
    private Loan loan;
    
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;
    
    @Column(name = "principal_paid", nullable = false, precision = 15, scale = 2)
    private BigDecimal principalPaid;
    
    @Column(name = "interest_paid", nullable = false, precision = 15, scale = 2)
    private BigDecimal interestPaid;
    
    @Column(name = "penalty_paid", precision = 15, scale = 2)
    private BigDecimal penaltyPaid;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentMethod paymentMethod;
    
    @Column(name = "payment_date", nullable = false)
    private LocalDate paymentDate;
    
    @Column(name = "transaction_id", unique = true, nullable = false, length = 50)
    private String transactionId;
    
    @Column(name = "receipt_id")
    private String receiptId;
    
    @Column(name = "reference_number", length = 100)
    private String referenceNumber;
    
    @Column(length = 500)
    private String notes;
    
    @Column(name = "processed_by", nullable = false, length = 100)
    private String processedBy;
    
    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    // Constructors, getters, setters
    
    public BigDecimal getTotalPaid() {
        return amount.add(penaltyPaid != null ? penaltyPaid : BigDecimal.ZERO);
    }
    
    public boolean isLatePayment(LocalDate dueDate) {
        return paymentDate.isAfter(dueDate);
    }
    
    public BigDecimal calculateLatePenalty(BigDecimal penaltyRate, int daysLate) {
        if (daysLate <= 0 || penaltyPaid != null) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal dailyPenaltyRate = penaltyRate.divide(BigDecimal.valueOf(100 * 365), 8, RoundingMode.HALF_UP);
        return amount.multiply(dailyPenaltyRate).multiply(BigDecimal.valueOf(daysLate));
    }
}
```

#### Receipt Entity
```java
@Entity
@Table(name = "receipt")
public class Receipt {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long receiptId;
    
    @Column(name = "receipt_number", unique = true, nullable = false, length = 50)
    private String receiptNumber;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReceiptType receiptType;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id")
    private Payment payment;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "saving_transaction_id")
    private LedgerTransaction savingTransaction;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;
    
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;
    
    @Column(length = 200)
    private String description;
    
    @Column(name = "receipt_date", nullable = false)
    private LocalDate receiptDate;
    
    @Column(name = "generated_by", nullable = false, length = 100)
    private String generatedBy;
    
    @Column(name = "file_path")
    private String filePath;
    
    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    // Constructors, getters, setters
}
```

### 2. Enums

#### PaymentMethod
```java
public enum PaymentMethod {
    CASH("Cash Payment"),
    BANK_TRANSFER("Bank Transfer"),
    CHEQUE("Cheque"),
    MOBILE_MONEY("Mobile Money"),
    DIRECT_DEBIT("Direct Debit"),
    SALARY_DEDUCTION("Salary Deduction");
    
    private final String description;
    
    PaymentMethod(String description) {
        this.description = description;
    }
    
    // Getters
}
```

#### ReceiptType
```java
public enum ReceiptType {
    LOAN_PAYMENT("Loan Payment Receipt"),
    SAVINGS_DEPOSIT("Savings Deposit Receipt"),
    SAVINGS_WITHDRAWAL("Savings Withdrawal Receipt"),
    LOAN_DISBURSEMENT("Loan Disbursement Receipt"),
    DIVIDEND_PAYMENT("Dividend Payment Receipt"),
    FINE_PAYMENT("Fine Payment Receipt");
    
    private final String description;
    
    ReceiptType(String description) {
        this.description = description;
    }
    
    // Getters
}
```

### 3. Data Transfer Objects (DTOs)

#### PaymentDTO
```java
public class PaymentDTO {
    
    @NotNull(message = "Loan ID is required")
    private Long loanId;
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "1.00", message = "Minimum payment amount is 1.00")
    @DecimalMax(value = "1000000.00", message = "Maximum payment amount is 1,000,000.00")
    private BigDecimal amount;
    
    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;
    
    @NotNull(message = "Payment date is required")
    private LocalDate paymentDate;
    
    @Size(max = 100, message = "Reference number must not exceed 100 characters")
    private String referenceNumber;
    
    @Size(max = 500, message = "Notes must not exceed 500 characters")
    private String notes;
    
    private Boolean waivePenalty = false;
    
    private String waiverReason;
    
    // Constructors, getters, setters
}
```

#### PaymentResponseDTO
```java
public class PaymentResponseDTO {
    private Long paymentId;
    private Long loanId;
    private String memberName;
    private BigDecimal totalAmount;
    private BigDecimal principalPaid;
    private BigDecimal interestPaid;
    private BigDecimal penaltyPaid;
    private BigDecimal remainingBalance;
    private PaymentMethod paymentMethod;
    private LocalDate paymentDate;
    private String transactionId;
    private String receiptId;
    private String receiptUrl;
    private String processedBy;
    private LocalDateTime processedAt;
    
    // Constructors, getters, setters
}
```

#### ReceiptDTO
```java
public class ReceiptDTO {
    private Long receiptId;
    private String receiptNumber;
    private ReceiptType receiptType;
    private Long memberId;
    private String memberName;
    private BigDecimal amount;
    private String description;
    private LocalDate receiptDate;
    private String generatedBy;
    private String fileUrl;
    private Map<String, Object> receiptData;
    
    // Constructors, getters, setters
}
```

### 4. Repository Layer

#### PaymentRepository
```java
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    
    List<Payment> findByLoanIdOrderByPaymentDateDesc(Long loanId);
    
    @Query("SELECT p FROM Payment p WHERE p.loan.id = :loanId AND p.paymentDate BETWEEN :startDate AND :endDate")
    List<Payment> findByLoanIdAndDateRange(@Param("loanId") Long loanId,
                                           @Param("startDate") LocalDate startDate,
                                           @Param("endDate") LocalDate endDate);
    
    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.loan.id = :loanId")
    BigDecimal getTotalPaymentsForLoan(@Param("loanId") Long loanId);
    
    @Query("SELECT SUM(p.principalPaid) FROM Payment p WHERE p.loan.id = :loanId")
    BigDecimal getTotalPrincipalPaidForLoan(@Param("loanId") Long loanId);
    
    @Query("SELECT SUM(p.interestPaid) FROM Payment p WHERE p.loan.id = :loanId")
    BigDecimal getTotalInterestPaidForLoan(@Param("loanId") Long loanId);
    
    @Query("SELECT p FROM Payment p WHERE p.paymentDate = :date")
    List<Payment> findByPaymentDate(@Param("date") LocalDate date);
    
    @Query("SELECT p FROM Payment p WHERE p.transactionId = :transactionId")
    Optional<Payment> findByTransactionId(@Param("transactionId") String transactionId);
}
```

#### ReceiptRepository
```java
@Repository
public interface ReceiptRepository extends JpaRepository<Receipt, Long> {
    
    Optional<Receipt> findByReceiptNumber(String receiptNumber);
    
    List<Receipt> findByMemberIdOrderByReceiptDateDesc(Long memberId);
    
    @Query("SELECT r FROM Receipt r WHERE r.receiptDate BETWEEN :startDate AND :endDate")
    List<Receipt> findByDateRange(@Param("startDate") LocalDate startDate,
                                  @Param("endDate") LocalDate endDate);
    
    @Query("SELECT COUNT(r) FROM Receipt r WHERE r.receiptDate = :date")
    long countByReceiptDate(@Param("date") LocalDate date);
    
    @Query("SELECT MAX(CAST(SUBSTRING(r.receiptNumber, -6) AS INTEGER)) FROM Receipt r WHERE r.receiptDate = :date")
    Integer getLastReceiptSequence(@Param("date") LocalDate date);
}
```

### 5. Service Layer

#### PaymentService
```java
@Service
@Transactional
public class PaymentService {
    
    @Autowired
    private PaymentRepository paymentRepository;
    
    @Autowired
    private LoanRepository loanRepository;
    
    @Autowired
    private LoanBalanceRepository loanBalanceRepository;
    
    @Autowired
    private ReceiptService receiptService;
    
    @Autowired
    private TransactionService transactionService;
    
    @Autowired
    private PaymentCalculationService calculationService;
    
    public PaymentResponseDTO processPayment(PaymentDTO paymentDTO, String processedBy) {
        // Validate loan exists and is active
        Loan loan = loanRepository.findById(paymentDTO.getLoanId())
            .orElseThrow(() -> new ResourceNotFoundException("Loan not found"));
        
        if (loan.getStatus() != LoanStatus.ACTIVE) {
            throw new BusinessRuleException("Payment can only be processed for active loans");
        }
        
        // Calculate payment breakdown
        PaymentCalculationResult calculation = calculationService.calculatePaymentBreakdown(
            loan, paymentDTO.getAmount(), paymentDTO.getPaymentDate());
        
        // Create payment record
        Payment payment = createPaymentRecord(paymentDTO, loan, calculation, processedBy);
        payment = paymentRepository.save(payment);
        
        // Update loan balance
        updateLoanBalance(loan, payment);
        
        // Record financial transactions
        String transactionId = transactionService.recordPaymentTransactions(
            payment, loan, calculation);
        
        payment.setTransactionId(transactionId);
        payment = paymentRepository.save(payment);
        
        // Generate receipt
        Receipt receipt = receiptService.generateLoanPaymentReceipt(payment, loan, calculation);
        payment.setReceiptId(receipt.getReceiptNumber());
        paymentRepository.save(payment);
        
        // Check if loan is fully paid
        checkLoanCompletion(loan);
        
        return buildPaymentResponse(payment, loan, calculation, receipt);
    }
    
    @Transactional(readOnly = true)
    public List<PaymentResponseDTO> getPaymentHistory(Long loanId) {
        Loan loan = loanRepository.findById(loanId)
            .orElseThrow(() -> new ResourceNotFoundException("Loan not found"));
        
        List<Payment> payments = paymentRepository.findByLoanIdOrderByPaymentDateDesc(loanId);
        
        return payments.stream()
            .map(payment -> buildPaymentResponse(payment, loan, null, null))
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public PaymentCalculationResult calculatePaymentPreview(Long loanId, BigDecimal paymentAmount, LocalDate paymentDate) {
        Loan loan = loanRepository.findById(loanId)
            .orElseThrow(() -> new ResourceNotFoundException("Loan not found"));
        
        return calculationService.calculatePaymentBreakdown(loan, paymentAmount, paymentDate);
    }
    
    @Transactional(readOnly = true)
    public List<PaymentSummaryDTO> getDailyPaymentSummary(LocalDate date) {
        List<Payment> payments = paymentRepository.findByPaymentDate(date);
        
        Map<PaymentMethod, BigDecimal> methodTotals = payments.stream()
            .collect(Collectors.groupingBy(
                Payment::getPaymentMethod,
                Collectors.reducing(BigDecimal.ZERO, Payment::getAmount, BigDecimal::add)));
        
        return methodTotals.entrySet().stream()
            .map(entry -> new PaymentSummaryDTO(entry.getKey(), entry.getValue(), date))
            .collect(Collectors.toList());
    }
    
    private Payment createPaymentRecord(PaymentDTO paymentDTO, Loan loan, 
                                    PaymentCalculationResult calculation, String processedBy) {
        Payment payment = new Payment();
        payment.setLoan(loan);
        payment.setAmount(paymentDTO.getAmount());
        payment.setPrincipalPaid(calculation.getPrincipalAmount());
        payment.setInterestPaid(calculation.getInterestAmount());
        payment.setPenaltyPaid(calculation.getPenaltyAmount());
        payment.setPaymentMethod(paymentDTO.getPaymentMethod());
        payment.setPaymentDate(paymentDTO.getPaymentDate());
        payment.setReferenceNumber(paymentDTO.getReferenceNumber());
        payment.setNotes(paymentDTO.getNotes());
        payment.setProcessedBy(processedBy);
        payment.setTransactionId(generateTransactionId(paymentDTO.getPaymentDate()));
        
        return payment;
    }
    
    private void updateLoanBalance(Loan loan, Payment payment) {
        Optional<LoanBalance> currentBalance = loanBalanceRepository
            .findLatestByLoanId(loan.getLoanId());
        
        if (currentBalance.isPresent()) {
            LoanBalance balance = currentBalance.get();
            BigDecimal newPrincipal = balance.getPrincipal().subtract(payment.getPrincipalPaid());
            BigDecimal newInterest = balance.getInterest().subtract(payment.getInterestPaid());
            
            // Ensure we don't go below zero
            newPrincipal = newPrincipal.max(BigDecimal.ZERO);
            newInterest = newInterest.max(BigDecimal.ZERO);
            
            balance.setPrincipal(newPrincipal);
            balance.setInterest(newInterest);
            balance.setForwardDate(payment.getPaymentDate());
            
            loanBalanceRepository.save(balance);
        }
    }
    
    private void checkLoanCompletion(Loan loan) {
        BigDecimal totalPrincipalPaid = paymentRepository.getTotalPrincipalPaidForLoan(loan.getLoanId());
        BigDecimal totalInterestPaid = paymentRepository.getTotalInterestPaidForLoan(loan.getLoanId());
        
        if (totalPrincipalPaid.compareTo(loan.getAmount()) >= 0) {
            loan.setStatus(LoanStatus.COMPLETED);
            loanRepository.save(loan);
            
            // Send loan completion notification
            sendLoanCompletionNotification(loan);
        }
    }
    
    private PaymentResponseDTO buildPaymentResponse(Payment payment, Loan loan, 
                                               PaymentCalculationResult calculation, Receipt receipt) {
        PaymentResponseDTO response = new PaymentResponseDTO();
        response.setPaymentId(payment.getPaymentId());
        response.setLoanId(loan.getLoanId());
        response.setMemberName(loan.getMember().getName());
        response.setTotalAmount(payment.getAmount());
        response.setPrincipalPaid(payment.getPrincipalPaid());
        response.setInterestPaid(payment.getInterestPaid());
        response.setPenaltyPaid(payment.getPenaltyPaid());
        response.setPaymentMethod(payment.getPaymentMethod());
        response.setPaymentDate(payment.getPaymentDate());
        response.setTransactionId(payment.getTransactionId());
        response.setProcessedBy(payment.getProcessedBy());
        response.setProcessedAt(payment.getCreatedAt());
        
        if (receipt != null) {
            response.setReceiptId(receipt.getReceiptNumber());
            response.setReceiptUrl("/api/receipts/" + receipt.getReceiptNumber());
        }
        
        // Calculate remaining balance
        BigDecimal remainingPrincipal = loan.getAmount().subtract(
            paymentRepository.getTotalPrincipalPaidForLoan(loan.getLoanId()));
        BigDecimal remainingInterest = loan.calculateTotalInterest().subtract(
            paymentRepository.getTotalInterestPaidForLoan(loan.getLoanId()));
        response.setRemainingBalance(remainingPrincipal.add(remainingInterest));
        
        return response;
    }
    
    private String generateTransactionId(LocalDate date) {
        String dateStr = date.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomSuffix = String.format("%04d", new Random().nextInt(10000));
        return "PAY" + dateStr + randomSuffix;
    }
    
    private void sendLoanCompletionNotification(Loan loan) {
        // Implementation for sending notification (email, SMS, etc.)
        log.info("Loan {} for member {} has been completed", 
                 loan.getLoanId(), loan.getMember().getName());
    }
}
```

#### PaymentCalculationService
```java
@Service
public class PaymentCalculationService {
    
    @Autowired
    private LoanBalanceRepository loanBalanceRepository;
    
    @Autowired
    private SystemParameterService parameterService;
    
    public PaymentCalculationResult calculatePaymentBreakdown(Loan loan, BigDecimal paymentAmount, LocalDate paymentDate) {
        // Get current loan balance
        Optional<LoanBalance> balanceOpt = loanBalanceRepository.findLatestByLoanId(loan.getLoanId());
        
        if (!balanceOpt.isPresent()) {
            throw new BusinessRuleException("No loan balance found for calculation");
        }
        
        LoanBalance currentBalance = balanceOpt.get();
        
        // Calculate interest due
        BigDecimal interestDue = calculateInterestDue(currentBalance, loan, paymentDate);
        
        // Calculate penalty if late
        BigDecimal penaltyDue = calculatePenaltyDue(loan, paymentDate);
        
        // Determine payment allocation
        BigDecimal availableForPrincipal = paymentAmount.subtract(interestDue).subtract(penaltyDue);
        
        if (availableForPrincipal.compareTo(BigDecimal.ZERO) < 0) {
            // Payment insufficient to cover interest and penalty
            throw new InsufficientPaymentException("Payment amount insufficient to cover interest and penalties");
        }
        
        // Calculate principal payment (capped at remaining principal)
        BigDecimal principalPayment = availableForPrincipal.min(currentBalance.getPrincipal());
        
        // Check for overpayment
        BigDecimal overpayment = BigDecimal.ZERO;
        if (principalPayment.compareTo(currentBalance.getPrincipal()) > 0) {
            overpayment = principalPayment.subtract(currentBalance.getPrincipal());
            principalPayment = currentBalance.getPrincipal();
        }
        
        return PaymentCalculationResult.builder()
            .totalPayment(paymentAmount)
            .interestAmount(interestDue)
            .penaltyAmount(penaltyDue)
            .principalAmount(principalPayment)
            .overpaymentAmount(overpayment)
            .remainingPrincipal(currentBalance.getPrincipal().subtract(principalPayment))
            .remainingInterest(currentBalance.getInterest().subtract(interestDue))
            .build();
    }
    
    private BigDecimal calculateInterestDue(LoanBalance balance, Loan loan, LocalDate paymentDate) {
        // Simple interest calculation for the period
        BigDecimal dailyRate = loan.getInterestRate().divide(BigDecimal.valueOf(100 * 365), 8, RoundingMode.HALF_UP);
        
        // Calculate days since last payment
        LocalDate lastPaymentDate = balance.getForwardDate();
        int daysSinceLastPayment = (int) ChronoUnit.DAYS.between(lastPaymentDate, paymentDate);
        
        // Interest on outstanding principal
        BigDecimal interestOnPrincipal = balance.getPrincipal()
            .multiply(dailyRate)
            .multiply(BigDecimal.valueOf(daysSinceLastPayment));
        
        // Interest on outstanding interest
        BigDecimal interestOnInterest = balance.getInterest()
            .multiply(dailyRate)
            .multiply(BigDecimal.valueOf(daysSinceLastPayment))
            .multiply(BigDecimal.valueOf(0.5)); // Reduced rate for interest compounding
        
        return interestOnPrincipal.add(interestOnInterest).setScale(2, RoundingMode.HALF_UP);
    }
    
    private BigDecimal calculatePenaltyDue(Loan loan, LocalDate paymentDate) {
        // Get penalty rate from system parameters
        BigDecimal penaltyRate = parameterService.getParameter("LATE_PENALTY_RATE", new BigDecimal("0.05"));
        
        // Calculate days late (assuming monthly due date)
        LocalDate dueDate = loan.getDisbursementDate().plusMonths(1);
        int daysLate = (int) ChronoUnit.DAYS.between(dueDate, paymentDate);
        
        if (daysLate <= 0) {
            return BigDecimal.ZERO;
        }
        
        // Penalty based on outstanding balance
        BigDecimal outstandingBalance = loan.getLoanBalances().stream()
            .map(LoanBalance::getPrincipal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal dailyPenaltyRate = penaltyRate.divide(BigDecimal.valueOf(100 * 365), 8, RoundingMode.HALF_UP);
        return outstandingBalance.multiply(dailyPenaltyRate).multiply(BigDecimal.valueOf(daysLate))
                          .setScale(2, RoundingMode.HALF_UP);
    }
}
```

#### ReceiptService
```java
@Service
public class ReceiptService {
    
    @Autowired
    private ReceiptRepository receiptRepository;
    
    @Autowired
    private PDFGeneratorService pdfGeneratorService;
    
    @Autowired
    private FileStorageService fileStorageService;
    
    public Receipt generateLoanPaymentReceipt(Payment payment, Loan loan, PaymentCalculationResult calculation) {
        // Generate receipt number
        String receiptNumber = generateReceiptNumber(ReceiptType.LOAN_PAYMENT, LocalDate.now());
        
        // Create receipt entity
        Receipt receipt = new Receipt();
        receipt.setReceiptNumber(receiptNumber);
        receipt.setReceiptType(ReceiptType.LOAN_PAYMENT);
        receipt.setPayment(payment);
        receipt.setMember(loan.getMember());
        receipt.setAmount(payment.getAmount());
        receipt.setDescription("Loan payment - " + loan.getLoanId());
        receipt.setReceiptDate(LocalDate.now());
        receipt.setGeneratedBy(payment.getProcessedBy());
        
        // Generate receipt data map for PDF
        Map<String, Object> receiptData = buildLoanPaymentReceiptData(payment, loan, calculation);
        
        // Generate PDF
        byte[] pdfBytes = pdfGeneratorService.generateLoanPaymentReceipt(receiptData);
        
        // Save PDF file
        String fileName = "receipt_" + receiptNumber + ".pdf";
        String filePath = fileStorageService.saveFile(pdfBytes, "receipts/" + fileName);
        receipt.setFilePath(filePath);
        
        // Save receipt
        receipt = receiptRepository.save(receipt);
        
        return receipt;
    }
    
    @Transactional(readOnly = true)
    public byte[] getReceiptPdf(String receiptNumber) {
        Receipt receipt = receiptRepository.findByReceiptNumber(receiptNumber)
            .orElseThrow(() -> new ResourceNotFoundException("Receipt not found"));
        
        if (receipt.getFilePath() != null) {
            try {
                return fileStorageService.getFile(receipt.getFilePath());
            } catch (IOException e) {
                throw new FileStorageException("Failed to retrieve receipt PDF", e);
            }
        }
        
        // Generate PDF on the fly if file not found
        Map<String, Object> receiptData = buildReceiptData(receipt);
        return pdfGeneratorService.generateReceipt(receiptData);
    }
    
    @Transactional(readOnly = true)
    public List<ReceiptDTO> getMemberReceipts(Long memberId) {
        List<Receipt> receipts = receiptRepository.findByMemberIdOrderByReceiptDateDesc(memberId);
        
        return receipts.stream()
            .map(this::buildReceiptDTO)
            .collect(Collectors.toList());
    }
    
    private String generateReceiptNumber(ReceiptType type, LocalDate date) {
        String dateStr = date.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String typePrefix = getTypePrefix(type);
        
        // Get last sequence for today
        Integer lastSequence = receiptRepository.getLastReceiptSequence(date);
        int sequence = (lastSequence != null ? lastSequence : 0) + 1;
        
        return String.format("%s%s%04d", typePrefix, dateStr, sequence);
    }
    
    private String getTypePrefix(ReceiptType type) {
        switch (type) {
            case LOAN_PAYMENT: return "LP";
            case SAVINGS_DEPOSIT: return "SD";
            case SAVINGS_WITHDRAWAL: return "SW";
            case LOAN_DISBURSEMENT: return "LD";
            case DIVIDEND_PAYMENT: return "DP";
            default: return "RC";
        }
    }
    
    private Map<String, Object> buildLoanPaymentReceiptData(Payment payment, Loan loan, PaymentCalculationResult calculation) {
        Map<String, Object> data = new HashMap<>();
        
        data.put("receiptNumber", payment.getReceiptId());
        data.put("receiptDate", LocalDate.now());
        data.put("memberName", loan.getMember().getName());
        data.put("memberId", loan.getMember().getMemberId());
        data.put("memberAddress", loan.getMember().getAddress());
        
        data.put("loanId", loan.getLoanId());
        data.put("loanAmount", loan.getAmount());
        data.put("interestRate", loan.getInterestRate());
        data.put("loanType", loan.getLoanType());
        
        data.put("paymentAmount", payment.getAmount());
        data.put("principalPaid", calculation.getPrincipalAmount());
        data.put("interestPaid", calculation.getInterestAmount());
        data.put("penaltyPaid", calculation.getPenaltyAmount());
        data.put("paymentMethod", payment.getPaymentMethod());
        data.put("paymentDate", payment.getPaymentDate());
        data.put("transactionId", payment.getTransactionId());
        
        data.put("remainingBalance", calculation.getRemainingPrincipal().add(calculation.getRemainingInterest()));
        data.put("processedBy", payment.getProcessedBy());
        
        return data;
    }
    
    private ReceiptDTO buildReceiptDTO(Receipt receipt) {
        ReceiptDTO dto = new ReceiptDTO();
        dto.setReceiptId(receipt.getReceiptId());
        dto.setReceiptNumber(receipt.getReceiptNumber());
        dto.setReceiptType(receipt.getReceiptType());
        dto.setMemberId(receipt.getMember().getMemberId());
        dto.setMemberName(receipt.getMember().getName());
        dto.setAmount(receipt.getAmount());
        dto.setDescription(receipt.getDescription());
        dto.setReceiptDate(receipt.getReceiptDate());
        dto.setGeneratedBy(receipt.getGeneratedBy());
        dto.setFileUrl(receipt.getFilePath());
        
        return dto;
    }
}
```

### 6. Controller Layer

#### PaymentController
```java
@RestController
@RequestMapping("/api/payments")
@Validated
public class PaymentController {
    
    @Autowired
    private PaymentService paymentService;
    
    @PostMapping("/process")
    @PreAuthorize("hasAnyRole('OFFICER', 'SECRETARY', 'PRESIDENT')")
    public ResponseEntity<ApiResponse<PaymentResponseDTO>> processPayment(
            @Valid @RequestBody PaymentDTO paymentDTO,
            Authentication authentication) {
        
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String processedBy = userDetails.getUsername();
        
        PaymentResponseDTO response = paymentService.processPayment(paymentDTO, processedBy);
        
        return ResponseEntity.ok(ApiResponse.success(response, "Payment processed successfully"));
    }
    
    @PostMapping("/calculate-preview")
    @PreAuthorize("hasAnyRole('OFFICER', 'SECRETARY', 'PRESIDENT')")
    public ResponseEntity<ApiResponse<PaymentCalculationResult>> calculatePaymentPreview(
            @Valid @RequestBody PaymentPreviewDTO previewDTO) {
        
        PaymentCalculationResult result = paymentService.calculatePaymentPreview(
            previewDTO.getLoanId(), previewDTO.getPaymentAmount(), previewDTO.getPaymentDate());
        
        return ResponseEntity.ok(ApiResponse.success(result));
    }
    
    @GetMapping("/loan/{loanId}/history")
    @PreAuthorize("hasRole('OFFICER') or hasRole('SECRETARY') or hasRole('PRESIDENT') or " +
            "@loanSecurity.canViewLoan(#loanId, authentication)")
    public ResponseEntity<ApiResponse<List<PaymentResponseDTO>>> getPaymentHistory(@PathVariable Long loanId) {
        
        List<PaymentResponseDTO> history = paymentService.getPaymentHistory(loanId);
        
        return ResponseEntity.ok(ApiResponse.success(history));
    }
    
    @GetMapping("/daily-summary")
    @PreAuthorize("hasAnyRole('SECRETARY', 'PRESIDENT')")
    public ResponseEntity<ApiResponse<List<PaymentSummaryDTO>>> getDailyPaymentSummary(
            @RequestParam LocalDate date) {
        
        List<PaymentSummaryDTO> summary = paymentService.getDailyPaymentSummary(date);
        
        return ResponseEntity.ok(ApiResponse.success(summary));
    }
}
```

#### ReceiptController
```java
@RestController
@RequestMapping("/api/receipts")
public class ReceiptController {
    
    @Autowired
    private ReceiptService receiptService;
    
    @GetMapping("/{receiptNumber}")
    @PreAuthorize("hasAnyRole('OFFICER', 'SECRETARY', 'PRESIDENT') or " +
            "@receiptSecurity.canViewReceipt(#receiptNumber, authentication)")
    public ResponseEntity<byte[]> getReceiptPdf(@PathVariable String receiptNumber) {
        
        byte[] pdfBytes = receiptService.getReceiptPdf(receiptNumber);
        
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=receipt_" + receiptNumber + ".pdf")
            .header(HttpHeaders.CONTENT_TYPE, "application/pdf")
            .body(pdfBytes);
    }
    
    @GetMapping("/member/{memberId}")
    @PreAuthorize("hasRole('OFFICER') or hasRole('SECRETARY') or hasRole('PRESIDENT') or " +
            "@memberSecurity.canViewMemberDetails(#memberId, authentication)")
    public ResponseEntity<ApiResponse<List<ReceiptDTO>>> getMemberReceipts(@PathVariable Long memberId) {
        
        List<ReceiptDTO> receipts = receiptService.getMemberReceipts(memberId);
        
        return ResponseEntity.ok(ApiResponse.success(receipts));
    }
    
    @GetMapping("/my-receipts")
    @PreAuthorize("hasRole('MEMBER')")
    public ResponseEntity<ApiResponse<List<ReceiptDTO>>> getMyReceipts(Authentication authentication) {
        
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long memberId = userDetails.getUser().getMember().getMemberId();
        
        List<ReceiptDTO> receipts = receiptService.getMemberReceipts(memberId);
        
        return ResponseEntity.ok(ApiResponse.success(receipts));
    }
}
```

## Business Rules & Validation

### Payment Rules
1. **Active Loans Only**: Payments only accepted for active loans
2. **Minimum Payment**: Must cover interest and penalties
3. **Payment Allocation**: Proper allocation between principal, interest, penalties
4. **Overpayment Handling**: Process overpayments appropriately
5. **Payment Date**: Cannot be future-dated (except with approval)

### Receipt Generation Rules
1. **Unique Numbers**: Receipt numbers must be unique per day
2. **Sequential**: Receipt numbers generated sequentially
3. **Immediate Generation**: Receipts generated immediately upon payment
4. **File Storage**: PDF receipts stored securely
5. **Data Accuracy**: All payment details accurately reflected

### Calculation Rules
1. **Interest Calculation**: Simple interest based on outstanding balance
2. **Penalty Calculation**: Late payment penalties applied
3. **Principal Allocation**: Payment allocated to interest first, then principal
4. **Rounding Rules**: All monetary values rounded to 2 decimal places

## Testing Strategy

### Unit Tests
```java
@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {
    
    @Mock
    private PaymentRepository paymentRepository;
    
    @Mock
    private LoanRepository loanRepository;
    
    @Mock
    private PaymentCalculationService calculationService;
    
    @InjectMocks
    private PaymentService paymentService;
    
    @Test
    void testProcessPayment_Success() {
        // Given
        PaymentDTO paymentDTO = createValidPaymentDTO();
        Loan loan = createTestActiveLoan();
        PaymentCalculationResult calculation = createValidCalculation();
        
        when(loanRepository.findById(paymentDTO.getLoanId())).thenReturn(Optional.of(loan));
        when(calculationService.calculatePaymentBreakdown(any(), any(), any())).thenReturn(calculation);
        when(paymentRepository.save(any(Payment.class))).thenReturn(createTestPayment());
        
        // When
        PaymentResponseDTO response = paymentService.processPayment(paymentDTO, "testuser");
        
        // Then
        assertThat(response.getLoanId()).isEqualTo(paymentDTO.getLoanId());
        assertThat(response.getTotalAmount()).isEqualTo(paymentDTO.getAmount());
        verify(paymentRepository).save(any(Payment.class));
    }
    
    @Test
    void testProcessPayment_InactiveLoan_ThrowsException() {
        // Given
        PaymentDTO paymentDTO = createValidPaymentDTO();
        Loan loan = createTestInactiveLoan();
        
        when(loanRepository.findById(paymentDTO.getLoanId())).thenReturn(Optional.of(loan));
        
        // When & Then
        assertThrows(BusinessRuleException.class, () -> paymentService.processPayment(paymentDTO, "testuser"));
    }
}
```

---

**Related Documentation**:
- [Database Schema](../architecture/database-schema.md) - Entity definitions
- [API Documentation](../api/rest-endpoints.md) - Endpoint details
- [PDF Generation](../reference/pdf-generation.md) - Receipt generation details
