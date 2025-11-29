# Data Validation Reference

## Overview

This document outlines comprehensive data validation rules and constraints for the Ban Sai Yai Savings Group Financial Accounting System. These validations ensure data integrity, consistency, and regulatory compliance.

## Validation Framework

### Validation Layers
1. **Input Validation**: Client-side and server-side input validation
2. **Business Logic Validation**: Business rule enforcement
3. **Database Constraints**: Database-level validation
4. **Application Validation**: Application-level validation
5. **Integration Validation**: External system validation

### Validation Types
- **Required Field Validation**: Mandatory fields must be populated
- **Format Validation**: Data format validation (email, phone, etc.)
- **Range Validation**: Numeric and date range validation
- **Length Validation**: String length validation
- **Pattern Validation**: Regular expression pattern matching
- **Cross-Field Validation**: Validation across multiple fields
- **Business Rule Validation**: Complex business logic validation

## Member Data Validation

### Personal Information Validation
```java
public class MemberDTO {
    
    @NotBlank(message = "Member name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    @Pattern(regexp = "^[a-zA-Z\\s\\-\\.']+$", message = "Name contains invalid characters")
    private String name;
    
    @NotBlank(message = "ID card number is required")
    @Pattern(regexp = "^\\d{13}$", message = "ID card must be exactly 13 digits")
    private String idCard;
    
    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    @Temporal(TemporalType.DATE)
    private LocalDate dateOfBirth;
    
    @Min(value = 18, message = "Age must be at least 18 years")
    private Integer age;
    
    @NotBlank(message = "Address is required")
    @Size(min = 10, max = 200, message = "Address must be between 10 and 200 characters")
    private String address;
    
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[0-9]{9,10}$", message = "Phone number must be 9-10 digits")
    private String phone;
    
    @Email(message = "Invalid email format")
    private String email;
}
```

### Membership Validation Rules
```java
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = MemberValidator.class)
public @interface ValidMember {
    String message() default "Invalid member data";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

public class MemberValidator implements ConstraintValidator<ValidMember, MemberDTO> {
    
    @Override
    public boolean isValid(MemberDTO member, ConstraintValidatorContext context) {
        boolean isValid = true;
        
        // Validate age calculation
        if (member.getDateOfBirth() != null) {
            int calculatedAge = Period.between(member.getDateOfBirth(), LocalDate.now()).getYears();
            if (calculatedAge < 18) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("Member must be at least 18 years old")
                    .addPropertyNode("dateOfBirth")
                    .addConstraintViolation();
                isValid = false;
            }
        }
        
        // Validate ID card uniqueness (would check database)
        if (isDuplicateIdCard(member.getIdCard())) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("ID card number already exists")
                .addPropertyNode("idCard")
                .addConstraintViolation();
            isValid = false;
        }
        
        return isValid;
    }
}
```

## Savings Account Validation

### Account Creation Validation
```java
public class SavingAccountDTO {
    
    @NotNull(message = "Member ID is required")
    @Positive(message = "Member ID must be positive")
    private Long memberId;
    
    @NotNull(message = "Share capital is required")
    @DecimalMin(value = "1000.00", message = "Minimum share capital is THB 1,000")
    @DecimalMax(value = "1000000.00", message = "Maximum share capital is THB 1,000,000")
    private BigDecimal shareCapital;
    
    @DecimalMin(value = "100.00", message = "Minimum initial deposit is THB 100")
    private BigDecimal initialDeposit;
    
    @Pattern(regexp = "^[A-Za-z0-9]{6,20}$", message = "Account number must be 6-20 alphanumeric characters")
    private String accountNumber;
}
```

### Transaction Validation
```java
public class SavingTransactionDTO {
    
    @NotNull(message = "Account ID is required")
    @Positive(message = "Account ID must be positive")
    private Long accountId;
    
    @NotNull(message = "Transaction type is required")
    private TransactionType transactionType;
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "100.00", message = "Minimum transaction amount is THB 100")
    @DecimalMax(value = "1000000.00", message = "Maximum transaction amount is THB 1,000,000")
    @Digits(integer = 7, fraction = 2, message = "Amount must have maximum 7 integer digits and 2 decimal digits")
    private BigDecimal amount;
    
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;
    
    @NotNull(message = "Transaction date is required")
    @PastOrPresent(message = "Transaction date cannot be in the future")
    private LocalDate transactionDate;
}
```

### Custom Account Balance Validation
```java
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = AccountBalanceValidator.class)
public @interface ValidAccountBalance {
    String message() default "Invalid account balance";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

public class AccountBalanceValidator implements ConstraintValidator<ValidAccountBalance, SavingTransactionDTO> {
    
    @Override
    public boolean isValid(SavingTransactionDTO transaction, ConstraintValidatorContext context) {
        if (transaction.getTransactionType() == TransactionType.WITHDRAWAL) {
            BigDecimal currentBalance = getCurrentBalance(transaction.getAccountId());
            BigDecimal newBalance = currentBalance.subtract(transaction.getAmount());
            
            if (newBalance.compareTo(BigDecimal.valueOf(1000)) < 0) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(
                    "Withdrawal would reduce balance below minimum THB 1,000")
                    .addPropertyNode("amount")
                    .addConstraintViolation();
                return false;
            }
        }
        return true;
    }
}
```

## Loan Application Validation

### Loan Application Data Validation
```java
public class LoanApplicationDTO {
    
    @NotNull(message = "Member ID is required")
    @Positive(message = "Member ID must be positive")
    private Long memberId;
    
    @NotBlank(message = "Loan type is required")
    @Pattern(regexp = "^(PERSONAL|BUSINESS|EMERGENCY|EDUCATION|HOUSING)$", 
             message = "Invalid loan type")
    private String loanType;
    
    @NotNull(message = "Loan amount is required")
    @DecimalMin(value = "1000.00", message = "Minimum loan amount is THB 1,000")
    @DecimalMax(value = "500000.00", message = "Maximum loan amount is THB 500,000")
    private BigDecimal loanAmount;
    
    @NotNull(message = "Loan term is required")
    @Min(value = 1, message = "Minimum loan term is 1 month")
    @Max(value = 120, message = "Maximum loan term is 120 months")
    private Integer loanTermMonths;
    
    @NotNull(message = "Interest rate is required")
    @DecimalMin(value = "0.01", message = "Minimum interest rate is 0.01%")
    @DecimalMax(value = "36.00", message = "Maximum interest rate is 36%")
    private BigDecimal interestRate;
    
    @NotBlank(message = "Purpose is required")
    @Size(min = 10, max = 500, message = "Purpose must be between 10 and 500 characters")
    private String purpose;
    
    @Valid
    private List<CollateralDTO> collaterals;
    
    @Valid
    private List<GuarantorDTO> guarantors;
}
```

### Collateral Validation
```java
public class CollateralDTO {
    
    @NotBlank(message = "Collateral type is required")
    @Pattern(regexp = "^(PROPERTY|VEHICLE|SAVINGS|FIXED_DEPOSIT|OTHER)$", 
             message = "Invalid collateral type")
    private String collateralType;
    
    @NotBlank(message = "Description is required")
    @Size(min = 10, max = 200, message = "Description must be between 10 and 200 characters")
    private String description;
    
    @NotNull(message = "Collateral value is required")
    @DecimalMin(value = "1000.00", message = "Minimum collateral value is THB 1,000")
    @Digits(integer = 10, fraction = 2, message = "Invalid value format")
    private BigDecimal estimatedValue;
    
    @NotBlank(message = "Document reference is required")
    @Size(max = 100, message = "Document reference must not exceed 100 characters")
    private String documentReference;
}
```

### Guarantor Validation
```java
public class GuarantorDTO {
    
    @NotNull(message = "Guarantor member ID is required")
    @Positive(message = "Guarantor member ID must be positive")
    private Long guarantorMemberId;
    
    @NotNull(message = "Guarantee amount is required")
    @DecimalMin(value = "1000.00", message = "Minimum guarantee amount is THB 1,000")
    @Digits(integer = 10, fraction = 2, message = "Invalid guarantee amount format")
    private BigDecimal guaranteeAmount;
    
    @Size(max = 500, message = "Relationship must not exceed 500 characters")
    private String relationship;
    
    @Size(max = 500, message = "Notes must not exceed 500 characters")
    private String notes;
}
```

### Loan Eligibility Validation
```java
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = LoanEligibilityValidator.class)
public @interface ValidLoanEligibility {
    String message() default "Member not eligible for loan";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

public class LoanEligibilityValidator implements ConstraintValidator<ValidLoanEligibility, LoanApplicationDTO> {
    
    @Override
    public boolean isValid(LoanApplicationDTO loanApp, ConstraintValidatorContext context) {
        boolean isValid = true;
        
        // Check member eligibility
        Member member = getMemberById(loanApp.getMemberId());
        
        // Check membership tenure
        if (member.getRegistrationDate().isAfter(LocalDate.now().minusMonths(6))) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                "Member must be active for at least 6 months to apply for loan")
                .addPropertyNode("memberId")
                .addConstraintViolation();
            isValid = false;
        }
        
        // Check existing loans
        if (getActiveLoansCount(loanApp.getMemberId()) >= 2) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                "Member cannot have more than 2 active loans")
                .addPropertyNode("memberId")
                .addConstraintViolation();
            isValid = false;
        }
        
        // Check loan-to-share ratio
        BigDecimal shareCapital = getShareCapital(loanApp.getMemberId());
        BigDecimal maxLoanAmount = shareCapital.multiply(BigDecimal.valueOf(5));
        
        if (loanApp.getLoanAmount().compareTo(maxLoanAmount) > 0) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                "Loan amount cannot exceed 5 times share capital")
                .addPropertyNode("loanAmount")
                .addConstraintViolation();
            isValid = false;
        }
        
        return isValid;
    }
}
```

## Payment Validation

### Payment Processing Validation
```java
public class PaymentDTO {
    
    @NotNull(message = "Loan ID is required")
    @Positive(message = "Loan ID must be positive")
    private Long loanId;
    
    @NotNull(message = "Payment amount is required")
    @DecimalMin(value = "1.00", message = "Minimum payment amount is THB 1")
    @Digits(integer = 7, fraction = 2, message = "Invalid payment amount format")
    private BigDecimal paymentAmount;
    
    @NotNull(message = "Payment type is required")
    private PaymentType paymentType;
    
    @Pattern(regexp = "^[A-Za-z0-9]{5,50}$", message = "Reference number must be 5-50 alphanumeric characters")
    private String referenceNumber;
    
    @NotNull(message = "Payment date is required")
    @PastOrPresent(message = "Payment date cannot be in the future")
    private LocalDate paymentDate;
    
    @Size(max = 500, message = "Notes must not exceed 500 characters")
    private String notes;
}
```

### Payment Validation Logic
```java
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PaymentValidator.class)
public @interface ValidPayment {
    String message() default "Invalid payment data";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

public class PaymentValidator implements ConstraintValidator<ValidPayment, PaymentDTO> {
    
    @Override
    public boolean isValid(PaymentDTO payment, ConstraintValidatorContext context) {
        boolean isValid = true;
        
        Loan loan = getLoanById(payment.getLoanId());
        
        // Check if payment amount is reasonable
        if (payment.getPaymentAmount().compareTo(loan.getOutstandingBalance()) > 0) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                "Payment amount cannot exceed outstanding balance")
                .addPropertyNode("paymentAmount")
                .addConstraintViolation();
            isValid = false;
        }
        
        // Check minimum payment for installment loans
        if (loan.getLoanType().equals(LoanType.INSTALLMENT)) {
            BigDecimal minimumPayment = calculateMinimumPayment(loan);
            if (payment.getPaymentAmount().compareTo(minimumPayment) < 0) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(
                    "Payment amount is below minimum installment amount")
                    .addPropertyNode("paymentAmount")
                    .addConstraintViolation();
                isValid = false;
            }
        }
        
        return isValid;
    }
}
```

## Financial Reporting Validation

### Report Parameter Validation
```java
public class ReportRequestDTO {
    
    @NotNull(message = "Report type is required")
    private ReportType reportType;
    
    @NotNull(message = "Start date is required")
    @PastOrPresent(message = "Start date cannot be in the future")
    private LocalDate startDate;
    
    @NotNull(message = "End date is required")
    @PastOrPresent(message = "End date cannot be in the future")
    private LocalDate endDate;
    
    @Size(max = 1000, message = "Parameters must not exceed 1000 characters")
    private String parameters;
}
```

### Report Date Range Validation
```java
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ReportDateRangeValidator.class)
public @interface ValidReportDateRange {
    String message() default "Invalid report date range";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

public class ReportDateRangeValidator implements ConstraintValidator<ValidReportDateRange, ReportRequestDTO> {
    
    @Override
    public boolean isValid(ReportRequestDTO report, ConstraintValidatorContext context) {
        boolean isValid = true;
        
        // Check if end date is after start date
        if (report.getEndDate().isBefore(report.getStartDate())) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                "End date must be after or equal to start date")
                .addPropertyNode("endDate")
                .addConstraintViolation();
            isValid = false;
        }
        
        // Check date range limitations
        long daysBetween = ChronoUnit.DAYS.between(report.getStartDate(), report.getEndDate());
        long maxDays = getMaxDaysForReportType(report.getReportType());
        
        if (daysBetween > maxDays) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                "Date range exceeds maximum allowed period of " + maxDays + " days")
                .addPropertyNode("endDate")
                .addConstraintViolation();
            isValid = false;
        }
        
        return isValid;
    }
    
    private long getMaxDaysForReportType(ReportType reportType) {
        switch (reportType) {
            case DAILY_REPORT: return 1;
            case WEEKLY_REPORT: return 7;
            case MONTHLY_REPORT: return 31;
            case QUARTERLY_REPORT: return 92;
            case ANNUAL_REPORT: return 366;
            default: return 365;
        }
    }
}
```

## Dividend Calculation Validation

### Dividend Calculation Validation
```java
public class DividendCalculationDTO {
    
    @NotNull(message = "Period start is required")
    @PastOrPresent(message = "Period start cannot be in the future")
    private LocalDate periodStart;
    
    @NotNull(message = "Period end is required")
    @PastOrPresent(message = "Period end cannot be in the future")
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
}
```

### Dividend Rate Validation
```java
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = DividendRateValidator.class)
public @interface ValidDividendRates {
    String message() default "Invalid dividend rates";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

public class DividendRateValidator implements ConstraintValidator<ValidDividendRates, DividendCalculationDTO> {
    
    @Override
    public boolean isValid(DividendCalculationDTO dividend, ConstraintValidatorContext context) {
        boolean isValid = true;
        
        // Check if allocation rates sum to 100%
        BigDecimal totalAllocation = dividend.getShareRate()
            .add(dividend.getDepositRate())
            .add(dividend.getInterestRefundRate());
        
        if (totalAllocation.compareTo(BigDecimal.valueOf(100)) != 0) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                "Sum of allocation rates must equal 100%")
                .addPropertyNode("shareRate")
                .addConstraintViolation();
            isValid = false;
        }
        
        return isValid;
    }
}
```

## Global Validation Constraints

### Database Entity Validation
```java
@Entity
@Table(name = "member")
public class Member {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long memberId;
    
    @Column(nullable = false, unique = true, length = 13)
    @Pattern(regexp = "^\\d{13}$", message = "ID card must be exactly 13 digits")
    private String idCard;
    
    @Column(nullable = false, length = 100)
    @NotBlank
    @Size(min = 2, max = 100)
    private String name;
    
    @Column(nullable = false)
    @Past
    private LocalDate dateOfBirth;
    
    @Column(nullable = false, length = 200)
    @NotBlank
    @Size(min = 10, max = 200)
    private String address;
    
    @Column(nullable = false, length = 20)
    @Pattern(regexp = "^[0-9]{9,10}$", message = "Phone number must be 9-10 digits")
    private String phone;
    
    @Column(length = 100)
    @Email
    private String email;
    
    @Column(nullable = false)
    private LocalDate registrationDate;
    
    @Column(nullable = false)
    private Boolean isActive = true;
}
```

### API Response Validation
```java
public class ApiResponse<T> {
    
    @NotNull(message = "Response status is required")
    private ResponseStatus status;
    
    @Size(max = 200, message = "Message must not exceed 200 characters")
    private String message;
    
    @Valid
    private T data;
    
    private List<ApiError> errors;
    
    @AssertTrue(message = "Data must be null when there are errors")
    private boolean isValidResponseStructure() {
        if (errors != null && !errors.isEmpty()) {
            return data == null;
        }
        return true;
    }
}
```

## Error Handling and Validation

### Custom Validation Exceptions
```java
public class ValidationException extends RuntimeException {
    private final List<FieldError> fieldErrors;
    
    public ValidationException(String message) {
        super(message);
        this.fieldErrors = new ArrayList<>();
    }
    
    public ValidationException(String message, List<FieldError> fieldErrors) {
        super(message);
        this.fieldErrors = fieldErrors;
    }
}

public class BusinessRuleException extends RuntimeException {
    private final String businessRule;
    private final Object[] parameters;
    
    public BusinessRuleException(String businessRule, String message) {
        super(message);
        this.businessRule = businessRule;
        this.parameters = new Object[0];
    }
}
```

### Global Exception Handler
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        
        List<ApiError> errors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(error -> new ApiError(
                error.getField(),
                error.getDefaultMessage(),
                error.getRejectedValue()
            ))
            .collect(Collectors.toList());
        
        return ResponseEntity.badRequest()
            .body(ApiResponse.error("Validation failed", errors));
    }
    
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiResponse<Void>> handleCustomValidationExceptions(
            ValidationException ex) {
        
        return ResponseEntity.badRequest()
            .body(ApiResponse.error(ex.getMessage()));
    }
    
    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessRuleExceptions(
            BusinessRuleException ex) {
        
        return ResponseEntity.unprocessableEntity()
            .body(ApiResponse.error(ex.getMessage()));
    }
}
```

---

**Related Documentation**:
- [Business Rules](business-rules.md) - Comprehensive business rule definitions
- [API Documentation](../api/rest-endpoints.md) - API endpoint specifications
- [Database Schema](../architecture/database-schema.md) - Database constraints and validation
