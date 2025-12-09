# Database Schema & JPA Entity Mappings

## Overview

The Ban Sai Yai Savings Group system uses **MariaDB** as the primary database with **Spring Data JPA (Hibernate)** for ORM. The schema consists of 8 core tables that support the complete financial management functionality.

## Entity-Relationship Diagram

```
┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│    user     │    │   member    │    │    loan     │
│─────────────│    │─────────────│    │─────────────│
│ user_id PK  │◄───│ member_id PK│◄───│ loan_id PK  │
│ username    │    │ name        │    │ member_id FK│
│ password    │    │ id_card     │    │ amount      │
│ role        │    │ address     │    │ interest_rate│
│ created_at  │    │ date_regist │    │ status      │
└─────────────┘    │ user_id FK  │    │ created_at  │
                   └─────────────┘    └─────────────┘
                          │                    │
                          │                    │
┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│    saving   │    │loan_forward │    │ transaction │
│─────────────│    │─────────────│    │─────────────│
│ saving_id PK│    │ forward_id PK│    │ trans_id PK │
│ member_id FK│    │ loan_id FK   │    │ account_id  │
│ share_cap   │    │ principal    │    │ amount      │
│ deposit     │    │ interest     │    │ type        │
│ balance     │    │ forward_date │    │ description │
│ updated_at  │    └─────────────┘    │ trans_date  │
└─────────────┘                        └─────────────┘
                          │                    │
                          │                    │
┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│ accounting  │    │   dividend  │    │ saving_fwd  │
│─────────────│    │─────────────│    │─────────────│
│ acc_id PK   │    │ div_id PK   │    │ forward_id PK│
│ acc_code    │    │ member_id FK│    │ member_id FK│
│ acc_name    │    │ amount      │    │ share_fwd   │
│ acc_type    │    │ calc_date   │    │ deposit_fwd │
│ description │    │ status      │    │ forward_date │
└─────────────┘    └─────────────┘    └─────────────┘
```

## JPA Entity Definitions

### 1. UserEntity (user table)

**Purpose**: Stores login credentials and user roles for authentication.

```java
@Entity
@Table(name = "user")
public class UserEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;
    
    @Column(unique = true, nullable = false, length = 50)
    private String username;
    
    @Column(nullable = false)
    private String password;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;
    
    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY)
    private Member member;
    
    // Constructors, getters, setters
}

public enum UserRole {
    ROLE_PRESIDENT,
    ROLE_SECRETARY,
    ROLE_OFFICER,
    ROLE_MEMBER
}
```

**Fields**:
- `userId` (PK) - Auto-increment primary key
- `username` - Unique login identifier
- `password` - Encrypted password (BCrypt)
- `role` - User role for RBAC
- `createdAt` - Account creation timestamp

### 2. Member (member table)

**Purpose**: Stores member profile information and registration details.

```java
@Entity
@Table(name = "member")
public class Member {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long memberId;
    
    @Column(nullable = false, length = 100)
    private String name;
    
    @Column(name = "id_card", unique = true, nullable = false, length = 20)
    private String idCard;
    
    @Column(nullable = false, length = 200)
    private String address;
    
    @Column(name = "date_regist")
    @CreationTimestamp
    private LocalDate dateRegist;
    
    @Column(name = "photo_path")
    private String photoPath;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "userId")
    private UserEntity user;
    
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Loan> loans;
    
    @OneToOne(mappedBy = "member", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private SavingAccount savingAccount;
    
    // Constructors, getters, setters
}
```

**Fields**:
- `memberId` (PK) - Auto-increment primary key
- `name` - Full name of member
- `idCard` - Unique identification number
- `address` - Residential address
- `dateRegist` - Registration date
- `photoPath` - Path to member photo
- `userId` (FK) - Reference to user account

### 3. Loan (loan table)

**Purpose**: Stores loan contract information and terms.

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
    
    @Column(name = "doc_ref")
    private String docRef;
    
    @Column(name = "approval_date")
    private LocalDate approvalDate;
    
    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @ManyToMany
    @JoinTable(
        name = "loan_guarantor",
        joinColumns = @JoinColumn(name = "loan_id"),
        inverseJoinColumns = @JoinColumn(name = "guarantor_id")
    )
    private Set<Member> guarantors;
    
    @OneToMany(mappedBy = "loan", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<LoanBalance> loanBalances;
    
    // Constructors, getters, setters
}

public enum LoanType {
    PERSONAL,
    BUSINESS,
    EMERGENCY
}

public enum LoanStatus {
    PENDING,
    APPROVED,
    REJECTED,
    ACTIVE,
    COMPLETED,
    DEFAULTED
}
```

**Fields**:
- `loanId` (PK) - Auto-increment primary key
- `memberId` (FK) - Borrowing member
- `amount` - Loan principal amount
- `interestRate` - Annual interest rate
- `loanType` - Category of loan
- `status` - Current loan status
- `docRef` - Collateral document reference
- `approvalDate` - Date loan was approved

### 4. SavingAccount (saving table)

**Purpose**: Tracks member share capital and deposit balances.

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
}
```

**Fields**:
- `savingId` (PK) - Auto-increment primary key
- `memberId` (FK) - Account owner
- `shareCapital` - Total share capital contribution
- `deposit` - Total savings deposits
- `balance` - Current account balance
- `updatedAt` - Last update timestamp

### 5. LoanBalance (loan_forward table)

**Purpose**: Monthly snapshots of loan debt with principal and interest breakdowns.

```java
@Entity
@Table(name = "loan_forward")
public class LoanBalance {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long forwardId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_id", nullable = false)
    private Loan loan;
    
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal principal;
    
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal interest;
    
    @Column(name = "forward_date", nullable = false)
    private LocalDate forwardDate;
    
    // Constructors, getters, setters
}
```

**Fields**:
- `forwardId` (PK) - Auto-increment primary key
- `loanId` (FK) - Related loan
- `principal` - Outstanding principal amount
- `interest` - Outstanding interest amount
- `forwardDate` - Snapshot date

### 6. LedgerTransaction (transaction table)

**Purpose**: General ledger transactions for accounting.

```java
@Entity
@Table(name = "transaction")
public class LedgerTransaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long transId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private AccountCode accountCode;
    
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TransactionType type;
    
    @Column(length = 200)
    private String description;
    
    @Column(name = "trans_date", nullable = false)
    private LocalDate transDate;
    
    @Column(name = "reference_id")
    private Long referenceId; // Links to loan, saving, etc.
    
    // Constructors, getters, setters
}

public enum TransactionType {
    DEBIT,
    CREDIT
}
```

**Fields**:
- `transId` (PK) - Auto-increment primary key
- `accountId` (FK) - Related chart of account
- `amount` - Transaction amount
- `type` - Debit or Credit
- `description` - Transaction description
- `transDate` - Transaction date
- `referenceId` - Optional reference to source document

### 7. AccountCode (accounting table)

**Purpose**: Chart of Accounts for financial reporting.

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
    
    @OneToMany(mappedBy = "accountCode", fetch = FetchType.LAZY)
    private Set<LedgerTransaction> transactions;
    
    // Constructors, getters, setters
}

public enum AccountType {
    ASSET,
    LIABILITY,
    EQUITY,
    REVENUE,
    EXPENSE
}
```

**Fields**:
- `accId` (PK) - Auto-increment primary key
- `accountCode` - Unique account code
- `accountName` - Account description
- `accountType` - Account classification
- `description` - Additional details

### 8. Dividend (dividend table)

**Purpose**: Records of dividend distributions to members.

```java
@Entity
@Table(name = "dividend")
public class Dividend {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long divId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;
    
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;
    
    @Column(name = "calc_date", nullable = false)
    private LocalDate calcDate;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DividendStatus status;
    
    // Constructors, getters, setters
}

public enum DividendStatus {
    PENDING,
    APPROVED,
    PAID
}
```

**Fields**:
- `divId` (PK) - Auto-increment primary key
- `memberId` (FK) - Dividend recipient
- `amount` - Dividend amount
- `calcDate` - Calculation date
- `status` - Payment status

### 9. SavingForward (saving_forward table)

**Purpose**: Historical tracking of savings balances.

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

## Database Constraints

### Primary Keys
All tables use auto-increment integer primary keys for simplicity and performance.

### Foreign Key Relationships
- `member.user_id` → `user.user_id`
- `loan.member_id` → `member.member_id`
- `saving.member_id` → `member.member_id`
- `loan_forward.loan_id` → `loan.loan_id`
- `transaction.account_id` → `accounting.acc_id`
- `dividend.member_id` → `member.member_id`
- `saving_forward.member_id` → `member.member_id`

### Unique Constraints
- `user.username` - Unique login identifier
- `member.id_card` - Unique member identification
- `accounting.acc_code` - Unique chart of account code
- `saving.member_id` - One savings account per member

### Indexes
```sql
-- Performance indexes
CREATE INDEX idx_loan_member_id ON loan(member_id);
CREATE INDEX idx_loan_status ON loan(status);
CREATE INDEX idx_transaction_account_id ON transaction(account_id);
CREATE INDEX idx_transaction_trans_date ON transaction(trans_date);
CREATE INDEX idx_dividend_member_id ON dividend(member_id);
CREATE INDEX idx_saving_forward_date ON saving_forward(forward_date);
```

## Data Validation

### Business Rules
1. **Loan Amount**: Cannot exceed member's share capital × multiplier
2. **Interest Rate**: Must be within regulatory limits
3. **Member Registration**: ID card must be unique
4. **Account Balance**: Cannot go negative
5. **Transaction Dates**: Cannot be future-dated

### JPA Validation Annotations
```java
@Entity
public class Loan {
    
    @Min(value = 1000, message = "Loan amount must be at least 1,000")
    @Max(value = 1000000, message = "Loan amount cannot exceed 1,000,000")
    private BigDecimal amount;
    
    @DecimalMin(value = "0.01", message = "Interest rate must be positive")
    @DecimalMax(value = "36.00", message = "Interest rate cannot exceed 36%")
    private BigDecimal interestRate;
}
```

## Database Migration Strategy

### Flyway Integration
```sql
-- V1__Create_user_table.sql
CREATE TABLE user (
    user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- V2__Create_member_table.sql
CREATE TABLE member (
    member_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    id_card VARCHAR(20) UNIQUE NOT NULL,
    address VARCHAR(200) NOT NULL,
    date_regist DATE NOT NULL,
    photo_path VARCHAR(255),
    user_id BIGINT UNIQUE,
    FOREIGN KEY (user_id) REFERENCES user(user_id)
);
```

---

**Related Documentation**:
- [JPA Entity Reference](../reference/jpa-entities.md) - Detailed entity relationships
- [Business Rules](../reference/business-rules.md) - Data validation rules
- [System Design](system-design.md) - Overall architecture
