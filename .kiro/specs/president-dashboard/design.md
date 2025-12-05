# Design Document: President Dashboard

## Overview

The President Dashboard is an Executive Information System (EIS) that provides strategic oversight capabilities for the President role in the savings group management system. Unlike operational dashboards focused on transaction processing, this dashboard emphasizes trend analysis, risk assessment, and governance decision support through four specialized widgets.

The system integrates with existing backend services and repositories to aggregate data from loans, members, payments, and financial records. The dashboard follows a role-based access control model where only users with the PRESIDENT role can access these specialized views.

## Architecture

### System Context

The President Dashboard operates within the existing Spring Boot application architecture:

- **Backend**: Java Spring Boot REST API with role-based security
- **Frontend**: React with Redux for state management
- **Database**: PostgreSQL with JPA/Hibernate ORM
- **Authentication**: JWT-based authentication with role-based authorization

### Component Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    React Frontend                            │
│  ┌──────────────────────────────────────────────────────┐   │
│  │         President Dashboard Page Component           │   │
│  │  ┌────────────┐  ┌────────────┐  ┌────────────┐    │   │
│  │  │ PAR Radar  │  │  Capital   │  │   Loan     │    │   │
│  │  │  Widget    │  │  Growth    │  │  Approval  │    │   │
│  │  │            │  │  Widget    │  │  Widget    │    │   │
│  │  └────────────┘  └────────────┘  └────────────┘    │   │
│  │  ┌────────────┐                                      │   │
│  │  │  Dividend  │                                      │   │
│  │  │ Projection │                                      │   │
│  │  │  Widget    │                                      │   │
│  │  └────────────┘                                      │   │
│  └──────────────────────────────────────────────────────┘   │
│                          │                                   │
│                          ▼                                   │
│  ┌──────────────────────────────────────────────────────┐   │
│  │         RTK Query API Slice (dashboardApi)           │   │
│  └──────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
                           │
                           │ HTTPS/REST
                           ▼
┌─────────────────────────────────────────────────────────────┐
│                    Spring Boot Backend                       │
│  ┌──────────────────────────────────────────────────────┐   │
│  │         DashboardController                          │   │
│  │  @PreAuthorize("hasRole('PRESIDENT')")              │   │
│  └──────────────────────────────────────────────────────┘   │
│                          │                                   │
│                          ▼                                   │
│  ┌──────────────────────────────────────────────────────┐   │
│  │         DashboardService                             │   │
│  │  - calculatePARAnalysis()                            │   │
│  │  - getCapitalGrowthTrends()                          │   │
│  │  - getPendingLoanApplications()                      │   │
│  │  - calculateDividendProjection()                     │   │
│  └──────────────────────────────────────────────────────┘   │
│                          │                                   │
│                          ▼                                   │
│  ┌──────────────────────────────────────────────────────┐   │
│  │         Repository Layer                             │   │
│  │  - LoanRepository                                    │   │
│  │  - MemberRepository                                  │   │
│  │  - PaymentRepository                                 │   │
│  │  - SavingRepository                                  │   │
│  │  - AccountingRepository                              │   │
│  └──────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
                           │
                           ▼
                    ┌──────────────┐
                    │  PostgreSQL  │
                    │   Database   │
                    └──────────────┘
```

## Components and Interfaces

### Backend Components

#### 1. DashboardController Endpoints

New endpoints to be added to the existing `DashboardController`:

```java
// PAR Analysis Widget
@GetMapping("/president/par-analysis")
@PreAuthorize("hasRole('PRESIDENT')")
ResponseEntity<PARAnalysisDTO> getPARAnalysis()

@GetMapping("/president/par-details")
@PreAuthorize("hasRole('PRESIDENT')")
ResponseEntity<List<PARMemberDetailDTO>> getPARDetails(@RequestParam String category)

// Capital Growth Widget
@GetMapping("/president/capital-growth")
@PreAuthorize("hasRole('PRESIDENT')")
ResponseEntity<CapitalGrowthDTO> getCapitalGrowthTrends(@RequestParam(defaultValue = "12") int months)

// Loan Approval Queue Widget
@GetMapping("/president/loan-approval-queue")
@PreAuthorize("hasRole('PRESIDENT')")
ResponseEntity<List<LoanApprovalItemDTO>> getLoanApprovalQueue()

@PostMapping("/president/loan-approval-queue/{loanId}/approve")
@PreAuthorize("hasRole('PRESIDENT')")
ResponseEntity<LoanApprovalResponseDTO> approveLoan(@PathVariable Long loanId, @RequestBody LoanApprovalDecisionRequest request)

@PostMapping("/president/loan-approval-queue/{loanId}/reject")
@PreAuthorize("hasRole('PRESIDENT')")
ResponseEntity<LoanApprovalResponseDTO> rejectLoan(@PathVariable Long loanId, @RequestBody LoanApprovalDecisionRequest request)

// Dividend Projection Widget
@GetMapping("/president/dividend-projection")
@PreAuthorize("hasRole('PRESIDENT')")
ResponseEntity<DividendProjectionDTO> getDividendProjection()
```

#### 2. DashboardService Methods

New service methods to be implemented:

```java
public class DashboardService {
    // PAR Analysis
    PARAnalysisDTO calculatePARAnalysis();
    List<PARMemberDetailDTO> getPARMemberDetails(String category);
    
    // Capital Growth
    CapitalGrowthDTO getCapitalGrowthTrends(int months);
    
    // Loan Approval Queue
    List<LoanApprovalItemDTO> getPendingLoanApplications();
    LoanApprovalResponseDTO approveLoanApplication(Long loanId, String approvedBy, String notes);
    LoanApprovalResponseDTO rejectLoanApplication(Long loanId, String rejectedBy, String reason);
    
    // Dividend Projection
    DividendProjectionDTO calculateDividendProjection();
}
```

#### 3. Repository Query Methods

New repository methods needed:

```java
// LoanRepository
List<Loan> findByStatusAndDaysOverdueBetween(LoanStatus status, int minDays, int maxDays);
List<Loan> findByStatus(LoanStatus status);
BigDecimal sumOutstandingBalanceByStatus(LoanStatus status);

// MemberRepository
Long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
List<Object[]> getMembershipTrendsByMonth(int months);

// SavingRepository
BigDecimal sumTotalSavingsByDateRange(LocalDate start, LocalDate end);
List<Object[]> getSavingsGrowthByMonth(int months);

// AccountingRepository
BigDecimal sumRetainedEarnings();
BigDecimal sumIncomeByDateRange(LocalDate start, LocalDate end);
BigDecimal sumExpensesByDateRange(LocalDate start, LocalDate end);
BigDecimal sumStatutoryReserves();
```

### Frontend Components

#### 1. President Dashboard Page

Main container component that orchestrates the four widgets:

```typescript
interface PresidentDashboardPageProps {}

const PresidentDashboardPage: React.FC<PresidentDashboardPageProps> = () => {
  // Component implementation
}
```

#### 2. PAR Radar Widget

Displays portfolio at risk visualization:

```typescript
interface PARRadarWidgetProps {
  data: PARAnalysisData;
  onSegmentClick: (category: string) => void;
}

const PARRadarWidget: React.FC<PARRadarWidgetProps> = ({ data, onSegmentClick }) => {
  // Donut chart or traffic light visualization
}
```

#### 3. Capital Growth Widget

Shows capital and membership trends:

```typescript
interface CapitalGrowthWidgetProps {
  data: CapitalGrowthData;
  months: number;
}

const CapitalGrowthWidget: React.FC<CapitalGrowthWidgetProps> = ({ data, months }) => {
  // Dual-axis line chart
}
```

#### 4. Loan Approval Queue Widget

Displays pending loan applications:

```typescript
interface LoanApprovalQueueWidgetProps {
  applications: LoanApplication[];
  onApprove: (loanId: number, notes: string) => void;
  onReject: (loanId: number, reason: string) => void;
}

const LoanApprovalQueueWidget: React.FC<LoanApprovalQueueWidgetProps> = ({ applications, onApprove, onReject }) => {
  // Table with approve/reject actions
}
```

#### 5. Dividend Projection Widget

Shows dividend estimates:

```typescript
interface DividendProjectionWidgetProps {
  data: DividendProjectionData;
}

const DividendProjectionWidget: React.FC<DividendProjectionWidgetProps> = ({ data }) => {
  // Metric display with calculation breakdown
}
```

## Data Models

### DTOs (Data Transfer Objects)

#### PARAnalysisDTO

```java
public class PARAnalysisDTO {
    private int currentCount;           // 0 days overdue
    private BigDecimal currentAmount;
    
    private int watchlistCount;         // 1-30 days overdue
    private BigDecimal watchlistAmount;
    
    private int substandardCount;       // 31-90 days overdue
    private BigDecimal substandardAmount;
    
    private int defaultCount;           // >90 days overdue
    private BigDecimal defaultAmount;
    
    private BigDecimal totalAtRisk;
    private BigDecimal totalPortfolio;
    private double parPercentage;
}
```

#### PARMemberDetailDTO

```java
public class PARMemberDetailDTO {
    private Long memberId;
    private String memberName;
    private String memberNumber;
    private Long loanId;
    private String loanNumber;
    private BigDecimal loanAmount;
    private BigDecimal outstandingBalance;
    private int daysOverdue;
    private LocalDate dueDate;
    private List<GuarantorInfo> guarantors;
}

public class GuarantorInfo {
    private Long guarantorId;
    private String guarantorName;
    private String guarantorMemberNumber;
    private BigDecimal guaranteeAmount;
}
```

#### CapitalGrowthDTO

```java
public class CapitalGrowthDTO {
    private List<MonthlyDataPoint> dataPoints;
    private BigDecimal currentTotalAssets;
    private int currentMemberCount;
    private double assetGrowthRate;
    private double memberGrowthRate;
}

public class MonthlyDataPoint {
    private String month;              // "2024-01"
    private BigDecimal totalAssets;    // Savings + Retained Earnings
    private int memberCount;
    private int newMembers;
    private int resignedMembers;
}
```

#### LoanApprovalItemDTO

```java
public class LoanApprovalItemDTO {
    private Long loanId;
    private String loanNumber;
    private Long memberId;
    private String memberName;
    private BigDecimal requestedAmount;
    private String purpose;
    private String loanType;           // "EMERGENCY" or "PRODUCTION"
    private BigDecimal debtToSavingsRatio;
    private int guarantorCount;
    private boolean hasOverleveragedGuarantor;
    private boolean hasHighDebt;
    private LocalDate applicationDate;
    private String riskLevel;          // "LOW", "MEDIUM", "HIGH"
}
```

#### LoanApprovalDecisionRequest

```java
public class LoanApprovalDecisionRequest {
    private String notes;              // For approval
    private String reason;             // For rejection
}
```

#### LoanApprovalResponseDTO

```java
public class LoanApprovalResponseDTO {
    private Long loanId;
    private String loanNumber;
    private String status;
    private String message;
    private LocalDateTime processedAt;
}
```

#### DividendProjectionDTO

```java
public class DividendProjectionDTO {
    private BigDecimal totalIncome;
    private BigDecimal totalExpenses;
    private BigDecimal statutoryReserves;
    private BigDecimal netIncome;
    private BigDecimal totalSharesOutstanding;
    private BigDecimal estimatedDividendPerShare;
    private double estimatedDividendRate;
    private boolean hasInsufficientData;
    private String projectionPeriod;
}
```

## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system-essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

### Property 1: PAR Classification Consistency

*For any* loan in the system, the PAR category assigned (Current, Watchlist, Substandard, Default) should match the calculated days overdue based on the difference between the current date and the loan's due date or payment timestamp.

**Validates: Requirements 1.2**

### Property 2: PAR Segment Totals Match Portfolio

*For any* PAR analysis calculation, the sum of loan amounts across all PAR categories (Current + Watchlist + Substandard + Default) should equal the total active loan portfolio.

**Validates: Requirements 1.1, 1.2**

### Property 3: Guarantor Information Completeness

*For any* member in a PAR detail list, if the member has an active loan, then the guarantor information returned should include all guarantors associated with that loan from the member reference table.

**Validates: Requirements 1.4**

### Property 4: Capital Growth Calculation Accuracy

*For any* time period, the total assets value should equal the sum of all savings balances plus retained earnings from the financial records at that point in time.

**Validates: Requirements 2.1, 2.5**

### Property 5: Membership Trend Consistency

*For any* monthly data point in the membership trends, the member count should equal the previous month's count plus new members minus resignations.

**Validates: Requirements 2.2**

### Property 6: Loan Approval Queue Filtering

*For any* loan approval queue request, all returned loan applications should have a status of PENDING and should not include loans with status APPROVED, REJECTED, or ACTIVE.

**Validates: Requirements 3.1**

### Property 7: Debt-to-Savings Ratio Calculation

*For any* loan application in the approval queue, the debt-to-savings ratio should equal the sum of the applicant's outstanding loan balances divided by their total savings balance.

**Validates: Requirements 3.1, 3.3**

### Property 8: Risk Flag Accuracy

*For any* loan application flagged as high risk, either the applicant's debt-to-savings ratio should exceed a threshold (e.g., 3.0) or at least one guarantor should have a debt-to-savings ratio exceeding the threshold.

**Validates: Requirements 3.3**

### Property 9: Loan Approval State Transition

*For any* loan application, when the President approves it, the loan status should transition from PENDING to APPROVED and the approval metadata (approvedBy, approvedDate, approvalNotes) should be populated.

**Validates: Requirements 3.4**

### Property 10: Loan Rejection State Transition

*For any* loan application, when the President rejects it, the loan status should transition from PENDING to REJECTED and the rejection reason should be recorded.

**Validates: Requirements 3.5**

### Property 11: Dividend Calculation Formula

*For any* dividend projection calculation, the estimated dividend per share should equal (total income minus total expenses minus statutory reserves) divided by total shares outstanding, when all values are available.

**Validates: Requirements 4.2**

### Property 12: Dividend Projection Data Availability

*For any* dividend projection request, if any of the required financial data (total income, total expenses, statutory reserves, or total shares outstanding) is unavailable or zero, then the response should indicate insufficient data.

**Validates: Requirements 4.3**

## Error Handling

### Backend Error Handling

1. **Authorization Errors**
   - Return 403 Forbidden if user lacks PRESIDENT role
   - Log unauthorized access attempts

2. **Data Not Found Errors**
   - Return 404 Not Found for invalid loan IDs in approval actions
   - Return empty collections for queries with no results

3. **Business Logic Errors**
   - Return 400 Bad Request for invalid loan approval/rejection attempts (e.g., loan not in PENDING status)
   - Include descriptive error messages

4. **Calculation Errors**
   - Handle division by zero in ratio calculations (return null or special indicator)
   - Handle missing financial data gracefully in dividend projections

5. **Database Errors**
   - Catch and log SQLException
   - Return 500 Internal Server Error with generic message
   - Implement transaction rollback for approval/rejection operations

### Frontend Error Handling

1. **API Error Handling**
   - Display user-friendly error messages for failed API calls
   - Implement retry logic for transient failures
   - Show loading states during API calls

2. **Data Validation**
   - Validate approval notes and rejection reasons before submission
   - Prevent submission of empty required fields

3. **Chart Rendering Errors**
   - Handle empty datasets gracefully
   - Display "No data available" messages
   - Prevent chart library errors from crashing the page

## Testing Strategy

### Unit Testing

The testing strategy employs both unit tests and property-based tests to ensure comprehensive coverage:

- **Unit tests** verify specific examples, edge cases, and integration points
- **Property-based tests** verify universal properties across all inputs
- Together they provide complete validation: unit tests catch concrete bugs, property tests verify general correctness

#### Backend Unit Tests

1. **Service Layer Tests**
   - Test PAR calculation with known loan data
   - Test capital growth aggregation with sample data
   - Test loan approval/rejection workflows
   - Test dividend calculation with known financial data
   - Test edge cases: empty datasets, null values, boundary conditions

2. **Controller Layer Tests**
   - Test endpoint authorization (verify 403 for non-PRESIDENT users)
   - Test request/response mapping
   - Test error responses

3. **Repository Layer Tests**
   - Test custom query methods with test data
   - Verify correct SQL generation
   - Test date range queries

#### Frontend Unit Tests

1. **Component Tests**
   - Test widget rendering with mock data
   - Test user interactions (button clicks, segment selection)
   - Test conditional rendering (loading states, error states, empty states)

2. **API Integration Tests**
   - Test RTK Query hooks with mock API responses
   - Test error handling
   - Test cache invalidation after approval/rejection

### Property-Based Testing

Property-based testing will be implemented using **jqwik** for Java backend tests. Each property-based test will run a minimum of 100 iterations to ensure thorough validation.

#### Property Test Configuration

```java
@Property(tries = 100)
```

Each property-based test will be tagged with a comment explicitly referencing the correctness property from the design document using this format:

```java
/**
 * Feature: president-dashboard, Property 1: PAR Classification Consistency
 */
@Property(tries = 100)
void testPARClassificationConsistency(@ForAll Loan loan) {
    // Test implementation
}
```

#### Property Test Generators

Custom generators will be created for:

1. **Loan Generators**
   - Generate loans with various overdue statuses
   - Generate loans with different due dates relative to current date
   - Constrain to valid loan states

2. **Member Generators**
   - Generate members with various savings balances
   - Generate members with different loan portfolios
   - Include guarantor relationships

3. **Financial Data Generators**
   - Generate income/expense records
   - Generate savings transactions
   - Ensure data consistency across related entities

#### Property Tests to Implement

1. **PAR Classification Property Test**
   - Generate random loans with various due dates
   - Verify PAR category matches calculated days overdue
   - **Validates: Property 1**

2. **PAR Totals Property Test**
   - Generate random loan portfolios
   - Verify sum of PAR segments equals total portfolio
   - **Validates: Property 2**

3. **Capital Growth Calculation Property Test**
   - Generate random savings and earnings data
   - Verify total assets equals savings plus retained earnings
   - **Validates: Property 4**

4. **Membership Trend Consistency Property Test**
   - Generate random membership change events
   - Verify monthly counts follow addition/subtraction rules
   - **Validates: Property 5**

5. **Debt-to-Savings Ratio Property Test**
   - Generate random member financial data
   - Verify ratio calculation accuracy
   - **Validates: Property 7**

6. **Loan Approval State Transition Property Test**
   - Generate random pending loans
   - Verify approval transitions and metadata population
   - **Validates: Property 9**

7. **Loan Rejection State Transition Property Test**
   - Generate random pending loans
   - Verify rejection transitions and reason recording
   - **Validates: Property 10**

8. **Dividend Calculation Property Test**
   - Generate random financial data
   - Verify dividend formula accuracy
   - **Validates: Property 11**

### Integration Testing

1. **End-to-End API Tests**
   - Test complete workflows: view PAR → click segment → view details
   - Test loan approval workflow: view queue → approve loan → verify status change
   - Test with real database (test containers)

2. **Frontend Integration Tests**
   - Test complete user journeys through the dashboard
   - Test data flow from API to UI rendering
   - Test error recovery scenarios

### Performance Testing

1. **Load Testing**
   - Test PAR calculation with large loan portfolios (10,000+ loans)
   - Test capital growth queries with multi-year data
   - Verify response times under load

2. **Query Optimization**
   - Profile database queries
   - Add indexes as needed
   - Implement caching for expensive calculations

### Security Testing

1. **Authorization Testing**
   - Verify all endpoints require PRESIDENT role
   - Test with various user roles to ensure proper access control
   - Test JWT token validation

2. **Input Validation Testing**
   - Test SQL injection prevention
   - Test XSS prevention in approval notes/rejection reasons
   - Test parameter tampering (e.g., manipulating loan IDs)
