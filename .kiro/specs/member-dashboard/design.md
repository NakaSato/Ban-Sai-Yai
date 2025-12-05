# Member Dashboard Design Document

## Overview

The Member Dashboard is a mobile-first, transparency-focused interface that provides VSLA members with real-time access to their personal financial information. The system addresses the critical trust issue between members and committees by offering immediate visibility into savings, loans, transactions, and guarantor obligations. The dashboard consists of four primary widgets: Digital Passbook Summary, Transaction Timeline, Loan Simulator, and Guarantor Obligations tracker.

The design leverages the existing Spring Boot backend architecture with React/TypeScript frontend, utilizing Redux Toolkit Query (RTK Query) for state management and API communication. The system will integrate with existing entities (Member, Loan, SavingAccount, SavingTransaction, Guarantor) and follow established patterns for authentication, authorization, and data access.

## Architecture

### System Context

```
┌─────────────────────────────────────────────────────────────┐
│                    Member Dashboard                         │
│  ┌─────────────────┐  ┌─────────────────┐  ┌──────────────┐ │
│  │ Digital Passbook│  │   Transaction   │  │     Loan     │ │
│  │    Summary      │  │    Timeline     │  │  Simulator   │ │
│  └─────────────────┘  └─────────────────┘  └──────────────┘ │
│  ┌─────────────────────────────────────────────────────────┐ │
│  │           Guarantor Obligations Tracker                 │ │
│  └─────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
                              │
                              ↓
┌─────────────────────────────────────────────────────────────┐
│                    Backend Services                         │
│  ┌─────────────────┐  ┌─────────────────┐  ┌──────────────┐ │
│  │ MemberDashboard │  │  LoanSimulator  │  │  Guarantor   │ │
│  │    Service      │  │    Service      │  │   Service    │ │
│  └─────────────────┘  └─────────────────┘  └──────────────┘ │
└─────────────────────────────────────────────────────────────┘
                              │
                              ↓
┌─────────────────────────────────────────────────────────────┐
│                    Data Layer                               │
│  ┌─────────────────┐  ┌─────────────────┐  ┌──────────────┐ │
│  │     Member      │  │      Loan       │  │    Saving    │ │
│  │   Repository    │  │   Repository    │  │  Repository  │ │
│  └─────────────────┘  └─────────────────┘  └──────────────┘ │
└─────────────────────────────────────────────────────────────┘
```

### Component Architecture

The Member Dashboard follows a layered architecture:

1. **Presentation Layer** (React Components)
   - `MemberDashboardPage.tsx` - Main dashboard container
   - `DigitalPassbookWidget.tsx` - Passbook summary card
   - `TransactionTimelineWidget.tsx` - Transaction history feed
   - `LoanSimulatorWidget.tsx` - Loan calculation tool
   - `GuarantorObligationsWidget.tsx` - Guarantor tracking

2. **API Layer** (RTK Query)
   - `memberDashboardApi.ts` - API slice with endpoints

3. **Service Layer** (Spring Boot)
   - `MemberDashboardService` - Aggregates member financial data
   - `LoanSimulatorService` - Performs loan calculations
   - `GuarantorService` - Manages guarantor obligations

4. **Controller Layer** (REST API)
   - `MemberDashboardController` - Exposes dashboard endpoints

5. **Data Layer** (JPA Repositories)
   - Existing repositories: `MemberRepository`, `LoanRepository`, `SavingRepository`, `SavingTransactionRepository`

## Components and Interfaces

### Frontend Components

#### 1. MemberDashboardPage Component

Main container component that orchestrates all widgets.

```typescript
interface MemberDashboardPageProps {
  memberId: number;
}

interface MemberDashboardState {
  showBalances: boolean;
  selectedPeriod: 'week' | 'month' | 'year' | 'all';
}
```

#### 2. DigitalPassbookWidget Component

Displays member's financial summary with privacy toggle.

```typescript
interface DigitalPassbookData {
  memberName: string;
  memberPhoto: string;
  totalSavings: number;
  outstandingLoan: number;
  welfareStatus: {
    eligible: boolean;
    balance: number;
    benefits: string[];
  };
}

interface DigitalPassbookWidgetProps {
  data: DigitalPassbookData;
  showBalances: boolean;
  onToggleVisibility: () => void;
}
```

#### 3. TransactionTimelineWidget Component

Chronological feed of all member transactions.

```typescript
interface TransactionItem {
  transactionId: number;
  transactionNumber: string;
  date: string;
  type: 'DEPOSIT' | 'WITHDRAWAL' | 'INTEREST_CREDIT' | 'FEE_DEDUCTION';
  amount: number;
  description: string;
  officerName: string;
  balanceAfter: number;
}

interface TransactionTimelineWidgetProps {
  transactions: TransactionItem[];
  onLoadMore: () => void;
  hasMore: boolean;
  isLoading: boolean;
}
```

#### 4. LoanSimulatorWidget Component

Interactive loan calculation tool.

```typescript
interface LoanSimulationInput {
  loanType: 'PERSONAL' | 'BUSINESS' | 'EMERGENCY' | 'EDUCATION' | 'HOUSING';
  amount: number;
  termMonths: number;
}

interface LoanSimulationResult {
  monthlyInstallment: number;
  totalInterest: number;
  totalPayment: number;
  interestRate: number;
  amortizationSchedule: Array<{
    month: number;
    payment: number;
    principal: number;
    interest: number;
    balance: number;
  }>;
}

interface LoanSimulatorWidgetProps {
  memberEligibility: {
    eligible: boolean;
    maxAmount: number;
    reason?: string;
  };
  onSimulate: (input: LoanSimulationInput) => void;
  result: LoanSimulationResult | null;
  isCalculating: boolean;
}
```

#### 5. GuarantorObligationsWidget Component

Tracks loans guaranteed by the member.

```typescript
interface GuarantorObligation {
  loanId: number;
  loanNumber: string;
  borrowerName: string;
  borrowerPhoto: string;
  loanAmount: number;
  outstandingBalance: number;
  status: 'CURRENT' | 'OVERDUE' | 'DEFAULTED';
  daysOverdue: number;
  nextPaymentDate: string;
  guaranteeAmount: number;
}

interface GuarantorObligationsWidgetProps {
  obligations: GuarantorObligation[];
  onViewDetails: (loanId: number) => void;
}
```

### Backend DTOs

#### MemberDashboardDTO

```java
public class MemberDashboardDTO {
    private DigitalPassbookDTO passbook;
    private List<TransactionDTO> recentTransactions;
    private List<GuarantorObligationDTO> guarantorObligations;
    private MemberEligibilityDTO loanEligibility;
}
```

#### DigitalPassbookDTO

```java
public class DigitalPassbookDTO {
    private String memberName;
    private String memberPhoto;
    private BigDecimal totalSavings;
    private BigDecimal outstandingLoan;
    private WelfareStatusDTO welfareStatus;
}

public class WelfareStatusDTO {
    private Boolean eligible;
    private BigDecimal balance;
    private List<String> benefits;
}
```

#### TransactionDTO

```java
public class TransactionDTO {
    private Long transactionId;
    private String transactionNumber;
    private LocalDate date;
    private TransactionType type;
    private BigDecimal amount;
    private String description;
    private String officerName;
    private BigDecimal balanceAfter;
}
```

#### LoanSimulationRequestDTO

```java
public class LoanSimulationRequestDTO {
    @NotNull
    private LoanType loanType;
    
    @NotNull
    @DecimalMin("1000.00")
    private BigDecimal amount;
    
    @NotNull
    @Min(1)
    @Max(120)
    private Integer termMonths;
}
```

#### LoanSimulationResultDTO

```java
public class LoanSimulationResultDTO {
    private BigDecimal monthlyInstallment;
    private BigDecimal totalInterest;
    private BigDecimal totalPayment;
    private BigDecimal interestRate;
    private List<AmortizationEntryDTO> amortizationSchedule;
}

public class AmortizationEntryDTO {
    private Integer month;
    private BigDecimal payment;
    private BigDecimal principal;
    private BigDecimal interest;
    private BigDecimal balance;
}
```

#### GuarantorObligationDTO

```java
public class GuarantorObligationDTO {
    private Long loanId;
    private String loanNumber;
    private String borrowerName;
    private String borrowerPhoto;
    private BigDecimal loanAmount;
    private BigDecimal outstandingBalance;
    private LoanStatus status;
    private Long daysOverdue;
    private LocalDate nextPaymentDate;
    private BigDecimal guaranteeAmount;
}
```

### REST API Endpoints

```
GET  /api/member-dashboard/{memberId}
     - Returns complete dashboard data for a member
     - Response: MemberDashboardDTO

GET  /api/member-dashboard/{memberId}/passbook
     - Returns digital passbook summary
     - Response: DigitalPassbookDTO

GET  /api/member-dashboard/{memberId}/transactions
     - Returns paginated transaction timeline
     - Query params: page, size, startDate, endDate, type
     - Response: Page<TransactionDTO>

POST /api/member-dashboard/{memberId}/loan-simulation
     - Calculates loan simulation
     - Request body: LoanSimulationRequestDTO
     - Response: LoanSimulationResultDTO

GET  /api/member-dashboard/{memberId}/guarantor-obligations
     - Returns all loans guaranteed by member
     - Response: List<GuarantorObligationDTO>

GET  /api/member-dashboard/{memberId}/loan-eligibility
     - Checks member's loan eligibility
     - Response: MemberEligibilityDTO
```

## Data Models

### Existing Entities (No Changes Required)

The design leverages existing entities:

- **Member**: Contains member profile and registration data
- **Loan**: Stores loan contracts and terms
- **SavingAccount**: Tracks member savings balances
- **SavingTransaction**: Records all savings transactions
- **Guarantor**: Links members to loans they guarantee
- **LoanBalance**: Monthly loan balance snapshots

### New Entity: WelfareFund

```java
@Entity
@Table(name = "welfare_fund")
public class WelfareFund extends BaseEntity {
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false, unique = true)
    private Member member;
    
    @Column(name = "balance", nullable = false, precision = 15, scale = 2)
    private BigDecimal balance;
    
    @Column(name = "is_eligible", nullable = false)
    private Boolean isEligible;
    
    @Column(name = "last_contribution_date")
    private LocalDate lastContributionDate;
    
    @Column(name = "total_contributions", precision = 15, scale = 2)
    private BigDecimal totalContributions;
    
    @Column(name = "total_benefits_received", precision = 15, scale = 2)
    private BigDecimal totalBenefitsReceived;
}
```

### Database Indexes

Add indexes for performance optimization:

```sql
-- Transaction queries by member and date
CREATE INDEX idx_saving_transaction_member_date 
ON saving_transaction(saving_account_id, transaction_date DESC);

-- Guarantor queries
CREATE INDEX idx_guarantor_member_loan 
ON guarantor(member_id, loan_id);

-- Loan status queries for guarantor obligations
CREATE INDEX idx_loan_status_maturity 
ON loan(status, maturity_date);
```

## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system—essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

### Property 1: Passbook balance consistency

*For any* member, the total savings displayed in the digital passbook SHALL equal the current balance in the member's saving account entity.

**Validates: Requirements 1.2**

### Property 2: Outstanding loan calculation accuracy

*For any* member with active loans, the outstanding loan amount displayed SHALL equal the sum of principal plus accrued interest for all active loans.

**Validates: Requirements 1.3**

### Property 3: Transaction timeline completeness

*For any* member, all transactions recorded in the saving_transaction table for that member's account SHALL appear in the transaction timeline.

**Validates: Requirements 2.1, 2.3**

### Property 4: Transaction timeline ordering

*For any* transaction timeline display, transactions SHALL be ordered in reverse chronological order by transaction date.

**Validates: Requirements 2.1**

### Property 5: Transaction officer attribution

*For any* transaction displayed, if the transaction has a teller_id, the officer name displayed SHALL match the name associated with that teller_id.

**Validates: Requirements 2.2**

### Property 6: Loan simulation calculation accuracy

*For any* valid loan simulation input (amount, term, loan type), the monthly installment calculated SHALL equal the result of the reducing balance formula using the loan type's interest rate.

**Validates: Requirements 3.2, 3.3**

### Property 7: Loan simulation interest rate consistency

*For any* loan simulation, the interest rate used SHALL match the interest rate defined in the loan type configuration.

**Validates: Requirements 3.4**

### Property 8: Loan simulation validation

*For any* loan simulation input with invalid amount or term (negative, zero, or exceeding loan type limits), the system SHALL reject the calculation and return validation errors.

**Validates: Requirements 3.5**

### Property 9: Guarantor obligation completeness

*For any* member who is a guarantor, all loans in the guarantor table where the member is listed SHALL appear in the guarantor obligations widget.

**Validates: Requirements 4.1**

### Property 10: Guarantor obligation status accuracy

*For any* guaranteed loan displayed, the status SHALL be "OVERDUE" if and only if the loan's maturity date is past and the outstanding balance is greater than zero.

**Validates: Requirements 4.3**

### Property 11: Guarantor notification trigger

*For any* guaranteed loan where a payment becomes overdue, the guarantor SHALL receive an immediate notification.

**Validates: Requirements 4.4**

### Property 12: Guarantor obligation removal

*For any* guaranteed loan that reaches fully repaid status (outstanding balance = 0), the loan SHALL be removed from the active guarantor obligations list.

**Validates: Requirements 4.5**

### Property 13: Privacy toggle state preservation

*For any* member dashboard session, toggling the show/hide control SHALL obscure sensitive information when hidden and reveal it when shown, without affecting the underlying data.

**Validates: Requirements 1.5**

## Error Handling

### Validation Errors

1. **Invalid Member ID**
   - HTTP 404: Member not found
   - Message: "Member with ID {memberId} does not exist"

2. **Unauthorized Access**
   - HTTP 403: Forbidden
   - Message: "You do not have permission to view this member's dashboard"

3. **Invalid Loan Simulation Input**
   - HTTP 400: Bad Request
   - Message: "Loan amount must be between {min} and {max} for {loanType}"
   - Message: "Loan term must be between 1 and {maxTerm} months"

### Business Logic Errors

1. **Member Not Eligible for Loan**
   - Return eligibility status with reason
   - Reasons: "Member must be active", "Member must be registered for at least 6 months", "Member already has maximum active loans"

2. **No Transactions Found**
   - Return empty list with appropriate message
   - Message: "No transactions found for the selected period"

3. **Welfare Fund Not Configured**
   - Return default welfare status
   - eligible: false, balance: 0, benefits: []

### System Errors

1. **Database Connection Failure**
   - HTTP 503: Service Unavailable
   - Message: "Dashboard service temporarily unavailable"
   - Log error and retry with exponential backoff

2. **Calculation Overflow**
   - HTTP 500: Internal Server Error
   - Message: "Unable to calculate loan simulation"
   - Log error with input parameters

### Error Response Format

```json
{
  "timestamp": "2025-12-05T10:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Loan amount must be between 1000.00 and 1000000.00 for PERSONAL",
  "path": "/api/member-dashboard/123/loan-simulation",
  "validationErrors": [
    {
      "field": "amount",
      "rejectedValue": 50000000,
      "message": "Maximum amount for PERSONAL loan is 1000000.00"
    }
  ]
}
```

## Testing Strategy

### Unit Testing

Unit tests will verify individual components and business logic:

1. **Service Layer Tests**
   - `MemberDashboardService`: Test data aggregation logic
   - `LoanSimulatorService`: Test calculation formulas
   - `GuarantorService`: Test obligation tracking logic

2. **Controller Tests**
   - Test endpoint request/response handling
   - Test validation error responses
   - Test authorization checks

3. **Component Tests (Frontend)**
   - Test widget rendering with mock data
   - Test user interactions (toggle visibility, form inputs)
   - Test error state handling

### Property-Based Testing

Property-based tests will verify universal properties using **jqwik** (Java) and **fast-check** (TypeScript):

1. **Backend Property Tests** (jqwik)
   - Test loan calculation formulas with random valid inputs
   - Test transaction ordering with random transaction sets
   - Test balance consistency with random transaction sequences

2. **Frontend Property Tests** (fast-check)
   - Test UI state consistency with random user interactions
   - Test data transformation functions with random inputs

### Integration Testing

Integration tests will verify end-to-end workflows:

1. **Dashboard Data Loading**
   - Test complete dashboard data retrieval
   - Verify all widgets receive correct data
   - Test with various member states (new member, active loans, guarantor)

2. **Loan Simulation Flow**
   - Test simulation request through full stack
   - Verify calculation accuracy
   - Test validation error handling

3. **Transaction Timeline Pagination**
   - Test loading multiple pages
   - Verify correct ordering across pages
   - Test filtering by date range and type

### Performance Testing

1. **Response Time**
   - Dashboard load time < 2 seconds
   - Loan simulation < 500ms
   - Transaction timeline pagination < 1 second

2. **Concurrent Users**
   - Support 100 concurrent member dashboard views
   - No degradation in response time

3. **Data Volume**
   - Test with members having 1000+ transactions
   - Test with members guaranteeing 10+ loans

### Security Testing

1. **Authorization**
   - Verify members can only access their own dashboard
   - Verify officers cannot access member dashboard without permission
   - Test JWT token validation

2. **Data Privacy**
   - Verify sensitive data is not exposed in logs
   - Test privacy toggle functionality
   - Verify no data leakage in error messages

---

**Testing Framework Configuration:**

- **Backend**: JUnit 5, Mockito, jqwik, Spring Boot Test
- **Frontend**: Jest, React Testing Library, fast-check
- **Integration**: TestContainers for database, MockMvc for API testing
- **Property Tests**: Minimum 100 iterations per property test
