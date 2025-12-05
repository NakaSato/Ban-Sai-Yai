# Design Document

## Overview

The Ban Sai Yai Dashboard Ecosystem is a comprehensive, role-based web interface that transforms raw financial data into actionable intelligence for community savings group stakeholders. Built on a React/TypeScript frontend with Material-UI components and a Spring Boot backend with MariaDB persistence, the dashboard provides four distinct user experiences tailored to Officer, Secretary, President, and Member roles.

The design emphasizes modularity through a widget-based architecture, real-time data synchronization, and progressive enhancement to ensure optimal performance across varying network conditions. Each dashboard variant presents role-appropriate KPIs, operational tools, and financial insights while maintaining strict role-based access control enforced at both the frontend and backend layers.

The system leverages Redux Toolkit Query (RTK Query) for efficient data fetching and caching, lazy-loaded chart components for performance optimization, and WebSocket connections for real-time updates to critical widgets. The design integrates seamlessly with the existing Spring Boot REST API architecture and MariaDB database schema, extending current endpoints while maintaining backward compatibility.

## Institutional Context and User Personas

### Operational Environment

The Ban Sai Yai group operates as a "Social Bank," a community-based financial institution that prioritizes community welfare alongside financial sustainability. This dual mission necessitates a dashboard design that balances hard financial metrics with social impact indicators and operational efficiency.

**Key Operational Characteristics**:

- **Capital Base**: Manages over 10 million THB with 360+ members
- **Transaction Volume**: High-volume cyclical operations centered around monthly meetings where savings deposits, loan repayments, and fee collections must be processed rapidly
- **Physical Environment**: Operations often lack the ergonomic stability of corporate banks, requiring a digital interface resilient to interruptions and designed for rapid data entry with immediate verification
- **System Transition**: Moving from manual record-keeping (disparate documents with inconsistencies) to centralized digital database, requiring the dashboard to serve as a verification engine that reassures users the "digital truth" matches the "physical truth" of cash in the box
- **Cash Zero Date**: Real-time reconciliation is central to operational logic, ensuring physical cash matches system records at all times

### User Role Analysis and Dashboard Requirements

The system architecture defines four primary actors, each requiring specialized dashboard views adhering to Role-Based Access Control (RBAC) principles.

#### The Officer (Treasurer/Teller)

**Persona**: The operational engine of the group, responsible for physical cash handling and initial data entry. Works under time pressure during meetings.

**Psychological Needs**:
- Speed and efficiency in transaction processing
- Error reduction mechanisms
- Immediate confirmation of actions
- Fear mitigation: data entry mistakes could lead to cash shortages for which they might be held personally liable

**Dashboard Design Focus**:
- Transaction throughput optimization
- Quick member lookup with financial summary
- Real-time cash balancing
- Point-of-Sale (POS) terminal functionality
- Minimal clicks to complete common operations
- Clear visual confirmation of successful transactions

**Critical Success Factors**:
- Sub-500ms member search response time
- 3-second maximum for complete deposit workflow
- Immediate receipt generation
- Real-time activity feed showing all recent transactions
- Fiscal period status prominently displayed to prevent closed-period transactions

#### The Secretary (Accountant/Controller)

**Persona**: Acts as the financial controller, responsible for General Ledger accuracy, transaction classification, and formal financial statement preparation.

**Psychological Needs**:
- Precision and accuracy in all financial records
- Order and systematic organization
- Auditability and traceability
- Assurance that every Officer-entered transaction is correctly mapped to the Chart of Accounts

**Dashboard Design Focus**:
- Reconciliation tools and exception reporting
- Journal entry management
- Trial balance monitoring with drill-down capability
- Unclassified transaction alerts
- Cash box tally and variance analysis
- Financial statement preview and generation

**Critical Success Factors**:
- Real-time trial balance calculation
- Prominent alerts for unclassified transactions
- Cash reconciliation workflow with variance explanation
- Month-end closing validation (all transactions classified, trial balance balanced, cash reconciled)
- Chart of accounts quick access with real-time balance updates

#### The President (Executive Management)

**Persona**: Strategic leader responsible for long-term organizational health, policy setting, and high-level approvals (e.g., loan issuance).

**Psychological Needs**:
- Strategic insight and big-picture visibility
- Oversight without operational detail overload
- Risk mitigation and early warning systems
- Staff integrity monitoring through audit logs

**Dashboard Design Focus**:
- Strategic KPIs (ROA, liquidity ratios, PAR metrics)
- Trend analysis and comparative performance
- Loan portfolio health monitoring
- Approval queue management
- Member engagement and growth analytics
- Risk classification and alert thresholds

**Critical Success Factors**:
- Portfolio-at-Risk (PAR-30, PAR-60, PAR-90) calculations
- Liquidity ratio monitoring (current ratio, quick ratio, cash-to-deposit ratio)
- Loan approval workflow with complete applicant information
- Member engagement metrics (active/inactive identification)
- Financial performance trends with period-over-period comparison
- Export capabilities for stakeholder reporting

#### The Member (End-User/Beneficiary)

**Persona**: The beneficiary of the system, often risk-averse and concerned about savings safety and dividend distribution fairness.

**Psychological Needs**:
- Trust and transparency in financial operations
- Accessibility to personal financial information
- Reassurance that money is safe and correctly recorded
- Understanding of savings growth and loan obligations

**Dashboard Design Focus**:
- Personal wealth summary (savings + estimated dividends)
- Loan obligations and amortization schedules
- Transaction history with running balances
- Dividend history and projections
- Digital passbook functionality

**Critical Success Factors**:
- Clear display of savings balance with year-to-date growth
- Complete loan details (principal, interest, payment schedule)
- Chronological transaction history with running balance
- Dividend distribution transparency
- Personalized financial insights (savings goals, loan qualification amounts)
- Amortization schedule with payment status indicators

### Design Implications

The institutional context and user personas directly inform several critical design decisions:

1. **Performance Requirements**: The high-volume, time-pressured environment demands sub-second response times and optimistic UI updates
2. **Error Prevention**: Multiple validation layers and confirmation dialogs prevent costly data entry mistakes
3. **Verification Engine**: Real-time reconciliation features and cash balancing tools address the transition from manual to digital systems
4. **Role-Based Complexity**: Each role sees only relevant information, reducing cognitive load and preventing unauthorized access
5. **Trust Building**: Transparency features (activity feeds, audit trails, real-time updates) build confidence in the digital system
6. **Resilience**: Offline capability, cached data, and graceful degradation ensure operations continue despite network issues

## Architecture

### High-Level Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                     Presentation Layer                          │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │
│  │   Officer    │  │  Secretary   │  │  President   │          │
│  │  Dashboard   │  │  Dashboard   │  │  Dashboard   │          │
│  └──────────────┘  └──────────────┘  └──────────────┘          │
│         │                  │                  │                 │
│         └──────────────────┴──────────────────┘                 │
│                            │                                    │
│                   ┌────────▼────────┐                          │
│                   │  Widget Manager │                          │
│                   │  (React Context)│                          │
│                   └────────┬────────┘                          │
└────────────────────────────┼─────────────────────────────────┘
                             │
┌────────────────────────────┼─────────────────────────────────┐
│                   Data Management Layer                        │
│                   ┌────────▼────────┐                          │
│                   │  RTK Query API  │                          │
│                   │  (dashboardApi) │                          │
│                   └────────┬────────┘                          │
│                            │                                    │
│         ┌──────────────────┼──────────────────┐                │
│         │                  │                  │                │
│  ┌──────▼──────┐  ┌────────▼────────┐  ┌─────▼──────┐        │
│  │   Cache     │  │   WebSocket     │  │   Redux    │        │
│  │  Management │  │   Connection    │  │   Store    │        │
│  └─────────────┘  └─────────────────┘  └────────────┘        │
└────────────────────────────────────────────────────────────────┘
                             │
┌────────────────────────────┼─────────────────────────────────┐
│                    Backend Layer                               │
│                   ┌────────▼────────┐                          │
│                   │  Dashboard      │                          │
│                   │  Controller     │                          │
│                   │  (@RestController)                         │
│                   └────────┬────────┘                          │
│                            │                                    │
│         ┌──────────────────┼──────────────────┐                │
│         │                  │                  │                │
│  ┌──────▼──────┐  ┌────────▼────────┐  ┌─────▼──────┐        │
│  │  Dashboard  │  │   Financial     │  │   Member   │        │
│  │   Service   │  │   Service       │  │   Service  │        │
│  └──────┬──────┘  └────────┬────────┘  └─────┬──────┘        │
│         │                  │                  │                │
│         └──────────────────┴──────────────────┘                │
│                            │                                    │
│                   ┌────────▼────────┐                          │
│                   │   Repository    │                          │
│                   │     Layer       │                          │
│                   └────────┬────────┘                          │
└────────────────────────────┼─────────────────────────────────┘
                             │
                    ┌────────▼────────┐
                    │     MariaDB     │
                    │    Database     │
                    └─────────────────┘
```

### Component Architecture

The dashboard follows a hierarchical component structure:

```
DashboardPage (Role-specific)
├── FiscalPeriodHeader (Shared)
├── OmniSearchBar (Shared)
├── DashboardGrid (Layout Manager)
│   ├── StatCard (Reusable Widget)
│   ├── QuickActionPanel (Role-specific)
│   ├── OperationalWidgets (Officer)
│   │   ├── MemberSearchWidget
│   │   ├── QuickDepositWidget
│   │   ├── QuickLoanPaymentWidget
│   │   └── PendingTasksWidget
│   ├── FinancialWidgets (Secretary)
│   │   ├── TrialBalanceWidget
│   │   ├── UnclassifiedTransactionsWidget
│   │   ├── CashBoxTallyWidget
│   │   └── FinancialStatementPreviewWidget
│   ├── ExecutiveWidgets (President)
│   │   ├── PortfolioHealthWidget
│   │   ├── LiquidityRatioWidget
│   │   ├── ApprovalQueueWidget
│   │   └── MemberEngagementWidget
│   └── PersonalWidgets (Member)
│       ├── SavingsBalanceWidget
│       ├── ActiveLoansWidget
│       ├── TransactionHistoryWidget
│       └── DividendHistoryWidget
└── ChartSection (Lazy-loaded)
    ├── MemberGrowthChart
    ├── LoanPortfolioChart
    └── SavingsGrowthChart
```

## Components and Interfaces

### Frontend Components

#### 1. DashboardPage (Role-specific variants)

**Purpose**: Main container component that orchestrates dashboard layout and widget rendering based on user role.

**Props Interface**:
```typescript
interface DashboardPageProps {
  role: UserRole;
  userId: string;
}
```

**State Management**:
```typescript
interface DashboardState {
  fiscalPeriodStatus: 'OPEN' | 'CLOSED';
  selectedPeriod: string;
  widgetVisibility: Record<string, boolean>;
  refreshInterval: number;
}
```

**Key Responsibilities**:
- Determine user role and load appropriate dashboard configuration
- Manage fiscal period state and propagate to child widgets
- Coordinate widget refresh cycles
- Handle dashboard-level error boundaries

#### 2. Widget Base Component

**Purpose**: Abstract base component providing common widget functionality.

**Interface**:
```typescript
interface WidgetProps {
  title: string;
  icon?: ReactNode;
  refreshInterval?: number;
  collapsible?: boolean;
  exportable?: boolean;
  onRefresh?: () => void;
  onExport?: (format: 'PDF' | 'EXCEL' | 'CSV') => void;
}

interface WidgetState {
  isLoading: boolean;
  error: Error | null;
  lastUpdated: Date;
  isCollapsed: boolean;
}
```

**Common Features**:
- Loading skeleton display
- Error boundary with retry mechanism
- Auto-refresh capability
- Export functionality
- Collapse/expand animation

#### 3. StatCard Component

**Purpose**: Reusable component for displaying key metrics with icon and value.

**Interface**:
```typescript
interface StatCardProps {
  title: string;
  value: string | number;
  icon: ReactNode;
  color: string;
  trend?: {
    direction: 'up' | 'down' | 'neutral';
    percentage: number;
    period: string;
  };
  onClick?: () => void;
}
```

#### 4. MemberSearchWidget

**Purpose**: Provides quick member lookup with financial summary for Officers.

**Interface**:
```typescript
interface MemberSearchWidgetProps {
  onMemberSelect: (member: MemberSearchResult) => void;
}

interface MemberSearchResult {
  memberId: number;
  firstName: string;
  lastName: string;
  thumbnailUrl: string;
  status: MemberStatus;
  financials: {
    savingsBalance: number;
    loanPrincipal: number;
    loanStatus: string;
  };
}
```

**Features**:
- Debounced search input (300ms)
- Autocomplete with member photos
- Financial summary popup on selection
- Quick action buttons (deposit, withdrawal, loan payment)

#### 5. TrialBalanceWidget

**Purpose**: Displays current period trial balance for Secretaries.

**Interface**:
```typescript
interface TrialBalanceData {
  totalDebits: number;
  totalCredits: number;
  variance: number;
  isBalanced: boolean;
  fiscalPeriod: string;
  accounts: Array<{
    accountCode: string;
    accountName: string;
    debitBalance: number;
    creditBalance: number;
  }>;
}
```

**Features**:
- Real-time balance calculation
- Visual indicator for balanced/unbalanced state
- Drill-down to account transactions
- Export to Excel functionality

#### 6. PortfolioHealthWidget

**Purpose**: Displays loan portfolio risk metrics for Presidents.

**Interface**:
```typescript
interface PortfolioHealthData {
  totalOutstanding: number;
  totalLoans: number;
  par30: number;
  par60: number;
  par90: number;
  riskDistribution: {
    low: number;
    medium: number;
    high: number;
    critical: number;
  };
  trendIndicator: 'improving' | 'stable' | 'deteriorating';
}
```

**Features**:
- Color-coded risk indicators
- Trend visualization
- Drill-down to at-risk loans
- Alert threshold configuration

### Backend Components

#### 1. DashboardController

**Purpose**: REST controller handling dashboard-specific API endpoints.

**Endpoints**:
```java
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    
    @GetMapping("/stats")
    public ResponseEntity<DashboardStats> getDashboardStats(
        @RequestParam(required = false) String role,
        @RequestParam(defaultValue = "month") String period
    );
    
    @GetMapping("/fiscal-period")
    public ResponseEntity<FiscalPeriod> getFiscalPeriod();
    
    @GetMapping("/members/search")
    public ResponseEntity<List<MemberSearchResult>> searchMembers(
        @RequestParam String q,
        @RequestParam(defaultValue = "5") int limit
    );
    
    @GetMapping("/members/{memberId}/financials")
    public ResponseEntity<MemberFinancials> getMemberFinancials(
        @PathVariable Long memberId
    );
    
    @PostMapping("/transactions/deposit")
    public ResponseEntity<TransactionResponse> processDeposit(
        @RequestBody DepositRequest request
    );
    
    @PostMapping("/transactions/loan-payment")
    public ResponseEntity<TransactionResponse> processLoanPayment(
        @RequestBody LoanPaymentRequest request
    );
    
    @GetMapping("/secretary/trial-balance")
    public ResponseEntity<TrialBalanceData> getTrialBalance();
    
    @GetMapping("/secretary/unclassified-count")
    public ResponseEntity<UnclassifiedCount> getUnclassifiedCount();
    
    @GetMapping("/president/portfolio-health")
    public ResponseEntity<PortfolioHealthData> getPortfolioHealth();
    
    @GetMapping("/president/approval-queue")
    public ResponseEntity<List<LoanApproval>> getApprovalQueue();
}
```

#### 2. DashboardService

**Purpose**: Business logic layer for dashboard operations.

**Key Methods**:
```java
@Service
public class DashboardService {
    
    public DashboardStats calculateDashboardStats(UserRole role, String period);
    
    public List<MemberSearchResult> searchMembers(String query, int limit);
    
    public MemberFinancials getMemberFinancials(Long memberId);
    
    public TransactionResponse processQuickDeposit(DepositRequest request);
    
    public TransactionResponse processQuickLoanPayment(LoanPaymentRequest request);
    
    public TrialBalanceData calculateTrialBalance();
    
    public PortfolioHealthData calculatePortfolioHealth();
    
    public List<LoanApproval> getApprovalQueue();
    
    public LiquidityRatios calculateLiquidityRatios();
    
    public MemberEngagementMetrics calculateEngagementMetrics();
}
```

#### 3. KPI Calculation Service

**Purpose**: Specialized service for calculating financial KPIs.

**Key Methods**:
```java
@Service
public class KPICalculationService {
    
    // Portfolio-at-Risk calculations
    public BigDecimal calculatePAR30();
    public BigDecimal calculatePAR60();
    public BigDecimal calculatePAR90();
    
    // Liquidity ratios
    public BigDecimal calculateCurrentRatio();
    public BigDecimal calculateQuickRatio();
    public BigDecimal calculateCashToDepositRatio();
    
    // Member engagement
    public int calculateActiveMembers();
    public BigDecimal calculateAverageTransactionsPerMember();
    public List<Member> getInactiveMembers(int days);
    
    // Portfolio composition
    public Map<LoanType, BigDecimal> getLoanTypeDistribution();
    public Map<LoanStatus, Integer> getLoanStatusDistribution();
    public Map<String, BigDecimal> getLoanAgingAnalysis();
}
```

## Data Models

### Frontend Data Models

#### DashboardStats
```typescript
interface DashboardStats {
  totalMembers: number;
  activeLoans: number;
  totalSavings: number;
  totalLoanPortfolio: number;
  overduePayments: number;
  monthlyRepayments: number;
  monthlyRevenue: number;
  newMembersThisMonth: number;
  loanApplicationsPending: number;
  period: string;
  lastUpdated: string;
}
```

#### FiscalPeriod
```typescript
interface FiscalPeriod {
  period: string;
  status: 'OPEN' | 'CLOSED';
  startDate: string;
  endDate: string;
  closedBy?: string;
  closedAt?: string;
}
```

#### PortfolioHealthData
```typescript
interface PortfolioHealthData {
  totalOutstanding: number;
  totalLoans: number;
  par30: number;
  par60: number;
  par90: number;
  riskDistribution: {
    low: number;
    medium: number;
    high: number;
    critical: number;
  };
  trendIndicator: 'improving' | 'stable' | 'deteriorating';
  alerts: Array<{
    severity: 'warning' | 'critical';
    message: string;
    affectedLoans: number;
  }>;
}
```

#### LiquidityRatios
```typescript
interface LiquidityRatios {
  currentRatio: number;
  quickRatio: number;
  cashToDepositRatio: number;
  workingCapital: number;
  trendIndicators: {
    currentRatio: 'improving' | 'stable' | 'declining';
    quickRatio: 'improving' | 'stable' | 'declining';
    cashToDepositRatio: 'improving' | 'stable' | 'declining';
  };
  thresholds: {
    currentRatioMin: number;
    quickRatioMin: number;
    cashToDepositRatioMin: number;
  };
}
```

#### MemberEngagementMetrics
```typescript
interface MemberEngagementMetrics {
  totalActiveMembers: number;
  newMembersThisMonth: number;
  inactiveMemberCount: number;
  averageTransactionsPerMember: number;
  memberGrowthRate: number;
  retentionRate: number;
  activityDistribution: {
    veryActive: number;
    active: number;
    moderate: number;
    inactive: number;
  };
  inactiveMembers: Array<{
    memberId: number;
    name: string;
    lastActivityDate: string;
    daysSinceLastActivity: number;
  }>;
}
```

### Backend Data Models

#### DashboardStatsDTO
```java
@Data
public class DashboardStatsDTO {
    private Integer totalMembers;
    private Integer activeLoans;
    private BigDecimal totalSavings;
    private BigDecimal totalLoanPortfolio;
    private Integer overduePayments;
    private BigDecimal monthlyRepayments;
    private BigDecimal monthlyRevenue;
    private Integer newMembersThisMonth;
    private Integer loanApplicationsPending;
    private String period;
    private LocalDateTime lastUpdated;
}
```

#### TrialBalanceDTO
```java
@Data
public class TrialBalanceDTO {
    private BigDecimal totalDebits;
    private BigDecimal totalCredits;
    private BigDecimal variance;
    private Boolean isBalanced;
    private String fiscalPeriod;
    private List<AccountBalanceDTO> accounts;
}

@Data
public class AccountBalanceDTO {
    private String accountCode;
    private String accountName;
    private AccountType accountType;
    private BigDecimal debitBalance;
    private BigDecimal creditBalance;
}
```

#### LoanApprovalDTO
```java
@Data
public class LoanApprovalDTO {
    private Long loanId;
    private String loanNumber;
    private Long memberId;
    private String memberName;
    private String memberPhoto;
    private BigDecimal requestedAmount;
    private BigDecimal interestRate;
    private LoanType loanType;
    private String purpose;
    private LocalDate applicationDate;
    private CollateralDTO collateral;
    private List<GuarantorDTO> guarantors;
    private Integer daysPending;
}
```

## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system—essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

### Property 1: Member search response time and completeness
*For any* member search query entered by an Officer, the System should return results within 500 milliseconds, and each result should contain member photo, name, ID, and current financial summary.
**Validates: Requirements 1.3**

### Property 2: Quick deposit transaction workflow
*For any* valid deposit request initiated by an Officer, the System should validate the member account, process the deposit, update the balance, and generate a receipt within 3 seconds.
**Validates: Requirements 1.4**

### Property 3: Loan payment allocation
*For any* valid loan payment initiated by an Officer, the System should correctly calculate principal and interest allocation, update the loan balance, and generate a payment receipt.
**Validates: Requirements 1.5**

### Property 4: Unclassified transaction alerting
*For any* system state where unclassified transactions exist, the Dashboard should display an alert widget showing the correct count and total amount with navigation to the classification interface.
**Validates: Requirements 2.3**

### Property 5: Month-end closing validation
*For any* month-end closing attempt by a Secretary, the System should validate that all transactions are classified, the trial balance is balanced, and cash reconciliation is complete before allowing closure.
**Validates: Requirements 2.5**

### Property 6: Liquidity ratio calculation
*For any* financial state, when a President views the liquidity ratio widget, the System should correctly calculate current ratio, quick ratio, and cash-to-deposit ratio with appropriate trend indicators.
**Validates: Requirements 3.3**

### Property 7: Approval queue completeness
*For any* pending loan application, when displayed in the approval queue widget, the System should include applicant details, requested amount, collateral information, and guarantor status.
**Validates: Requirements 3.4**

### Property 8: Loan approval workflow
*For any* loan approval or rejection by a President, the System should record the decision, update loan status, notify relevant parties, and update portfolio metrics.
**Validates: Requirements 3.5**

### Property 9: Member loan display
*For any* Member with active loans, the Dashboard should display complete loan details including principal balance, interest accrued, next payment due date, and payment history.
**Validates: Requirements 4.3**

### Property 10: Transaction history ordering
*For any* Member's transaction history, the System should display transactions in chronological order with date, type, amount, and running balance for each transaction.
**Validates: Requirements 4.4**

### Property 11: Dividend history completeness
*For any* Member's dividend history, the System should display all dividend distributions by period with calculation basis and payment status.
**Validates: Requirements 4.5**

### Property 12: Role-based dashboard loading
*For any* authenticated user, the System should determine the user role and load the corresponding dashboard configuration with appropriate widgets and permissions.
**Validates: Requirements 5.1**

### Property 13: Unauthorized access denial
*For any* attempt to access a widget not authorized for the user's role, the System should deny access and log the attempt.
**Validates: Requirements 5.2**

### Property 14: Cross-role access prevention
*For any* Officer attempting to access Secretary or President functions, the System should display an insufficient permissions message and deny access.
**Validates: Requirements 5.3**

### Property 15: Member data isolation
*For any* Member attempting to access data, the System should deny access to other members' data and return only the authenticated member's information.
**Validates: Requirements 5.4**

### Property 16: Role change propagation
*For any* user whose role changes, the System should update dashboard access permissions within 60 seconds without requiring re-authentication.
**Validates: Requirements 5.5**

### Property 17: Financial statement drill-down
*For any* financial statement line item clicked by a Secretary, the System should navigate to the detailed transaction listing supporting that line item.
**Validates: Requirements 6.3**

### Property 18: Dynamic report generation
*For any* valid date range selected by a Secretary, the System should regenerate financial statement previews for the specified period within 2 seconds.
**Validates: Requirements 6.4**

### Property 19: Financial statement export formatting
*For any* financial statement export by a Secretary, the System should generate a PDF formatted according to accounting standards with proper headers, footers, and signatures.
**Validates: Requirements 6.5**

### Property 20: Real-time activity feed updates
*For any* transaction that occurs, the System should update the activity feed widget within 1 second without requiring page refresh.
**Validates: Requirements 7.1**

### Property 21: Activity feed data completeness
*For any* transaction displayed in the activity feed, the System should show transaction type, member name, amount, timestamp, and processing officer.
**Validates: Requirements 7.2**

### Property 22: Activity feed detail navigation
*For any* activity feed item clicked by an Officer, the System should display detailed transaction information including receipt number and supporting documentation.
**Validates: Requirements 7.3**

### Property 23: Activity feed pagination
*For any* activity feed containing more than 20 items, the System should implement pagination with load-more functionality.
**Validates: Requirements 7.4**

### Property 24: Error event display
*For any* system error that occurs, the System should display the error event in the activity feed with severity level and resolution status.
**Validates: Requirements 7.5**

### Property 25: Transaction frequency calculation
*For any* data state, when a President views transaction frequency metrics, the System should correctly calculate and display average transactions per member per month.
**Validates: Requirements 8.2**

### Property 26: Inactive member identification
*For any* request to identify inactive members, the System should provide a list of all members with no transactions in the past 90 days.
**Validates: Requirements 8.4**

### Property 27: Member growth trend calculation
*For any* data state, when a President views member growth trends, the System should correctly calculate and display month-over-month member acquisition and attrition rates.
**Validates: Requirements 8.5**

### Property 28: Cash position alerting
*For any* system state where cash position falls below the minimum threshold, the Dashboard should display a critical alert with current balance and threshold value.
**Validates: Requirements 9.1**

### Property 29: PAR threshold alerting
*For any* system state where PAR-30 exceeds 5 percent, the Dashboard should display a warning alert indicating portfolio risk with affected loan count.
**Validates: Requirements 9.2**

### Property 30: Unreconciled transaction alerting
*For any* system state where unreconciled transactions exceed 10 items, the Dashboard should display an alert prompting immediate reconciliation.
**Validates: Requirements 9.3**

### Property 31: Trial balance validation
*For any* system state where the trial balance is out of balance, the Dashboard should display a critical alert preventing month-end closing with variance amount.
**Validates: Requirements 9.4**

### Property 32: Dividend calculation reminder
*For any* system state where dividend calculation is pending for more than 30 days after period close, the Dashboard should display a reminder alert with days overdue.
**Validates: Requirements 9.5**

### Property 33: Fiscal period UI state - closed
*For any* system state where the fiscal period is CLOSED, the System should disable transaction entry widgets and display a message indicating the period is closed.
**Validates: Requirements 10.2**

### Property 34: Fiscal period UI state - open
*For any* system state where the fiscal period is OPEN, the System should enable all transaction entry widgets and display the period end date.
**Validates: Requirements 10.3**

### Property 35: Fiscal period synchronization
*For any* fiscal period closure by a Secretary, the System should update the fiscal period indicator across all active user sessions within 5 seconds.
**Validates: Requirements 10.4**

### Property 36: Automatic period transition
*For any* new fiscal period opening, the System should automatically update the Dashboard to reflect the new period and enable transaction entry.
**Validates: Requirements 10.5**

### Property 37: Loan type distribution calculation
*For any* loan portfolio state, when a President views loan type distribution, the System should correctly calculate and display breakdown by loan type with percentages and amounts.
**Validates: Requirements 11.2**

### Property 38: Loan status distribution calculation
*For any* loan portfolio state, when a President views loan status distribution, the System should correctly display counts and amounts for all loan statuses.
**Validates: Requirements 11.3**

### Property 39: Loan aging categorization
*For any* loan portfolio state, when a President views loan aging analysis, the System should correctly categorize loans by days outstanding into defined aging buckets.
**Validates: Requirements 11.4**

### Property 40: Portfolio drill-down navigation
*For any* portfolio segment clicked by a President, the System should navigate to a detailed loan listing filtered by the selected category.
**Validates: Requirements 11.5**

### Property 41: Amortization schedule completeness
*For any* amortization schedule viewed by a Member, the System should display payment number, due date, principal amount, interest amount, total payment, and remaining balance for each payment.
**Validates: Requirements 12.2**

### Property 42: Amortization schedule update
*For any* payment made by a Member, the System should update the amortization schedule to reflect the payment and adjust future payment calculations.
**Validates: Requirements 12.3**

### Property 43: Payment status indication
*For any* payment in an amortization schedule, the System should correctly indicate whether the payment is paid, pending, or overdue with visual indicators.
**Validates: Requirements 12.4**

### Property 44: Amortization schedule export
*For any* amortization schedule download by a Member, the System should generate a PDF with complete payment schedule and loan terms.
**Validates: Requirements 12.5**

### Property 45: Cash denomination calculation
*For any* denomination count entered by a Secretary, the System should calculate the total physical cash amount in real-time.
**Validates: Requirements 13.2**

### Property 46: Cash reconciliation success
*For any* cash reconciliation where physical cash matches expected balance, the System should display a success indicator and record the reconciliation with timestamp.
**Validates: Requirements 13.3**

### Property 47: Cash variance calculation
*For any* cash reconciliation where physical cash differs from expected balance, the System should correctly calculate and display the variance amount with over/short indicator.
**Validates: Requirements 13.4**

### Property 48: Variance explanation requirement
*For any* cash reconciliation with a variance, the System should require the Secretary to enter a variance explanation before completing reconciliation.
**Validates: Requirements 13.5**

### Property 49: Task detail completeness
*For any* task assigned to an Officer, the System should display task description, priority level, due date, and assignment source.
**Validates: Requirements 14.2**

### Property 50: Task completion workflow
*For any* task completed by an Officer, the System should remove the task from the pending list and update the task status to completed.
**Validates: Requirements 14.3**

### Property 51: Overdue task handling
*For any* task that becomes overdue, the System should highlight the task in red and send a notification to the Officer.
**Validates: Requirements 14.4**

### Property 52: Task navigation
*For any* task clicked by an Officer, the System should navigate to the appropriate interface to complete the task.
**Validates: Requirements 14.5**

### Property 53: Revenue trend calculation
*For any* data state, when a President views revenue trends, the System should correctly display interest income, fee income, and total revenue with comparison to previous 12 months.
**Validates: Requirements 15.2**

### Property 54: Expense trend calculation
*For any* data state, when a President views expense trends, the System should correctly display operating expenses, loan loss provisions, and total expenses with month-over-month comparison.
**Validates: Requirements 15.3**

### Property 55: Profitability metrics calculation
*For any* financial state, when a President views profitability metrics, the System should correctly calculate and display net income, return on assets, and return on equity with trend indicators.
**Validates: Requirements 15.4**

### Property 56: Dynamic metric recalculation
*For any* comparison period selected by a President, the System should recalculate all comparative metrics for the selected timeframe within 2 seconds.
**Validates: Requirements 15.5**

### Property 57: Dashboard initial load performance
*For any* user navigation to the Dashboard, the System should display the initial page layout within 1 second.
**Validates: Requirements 16.1**

### Property 58: Widget interaction responsiveness
*For any* user interaction with a widget, the System should respond to user input within 200 milliseconds.
**Validates: Requirements 16.3**

### Property 59: Graceful network degradation
*For any* situation where network latency exceeds 3 seconds, the System should display loading indicators and allow users to continue interacting with cached data.
**Validates: Requirements 16.5**

### Property 60: Account category expansion
*For any* account category expanded by a Secretary, the System should display individual account codes, names, and current balances.
**Validates: Requirements 17.2**

### Property 61: Account drill-down navigation
*For any* account clicked by a Secretary, the System should navigate to the transaction ledger for that account.
**Validates: Requirements 17.3**

### Property 62: Real-time balance updates
*For any* account balance change, the System should update the chart of accounts widget within 2 seconds.
**Validates: Requirements 17.4**

### Property 63: Account search filtering
*For any* search query entered by a Secretary, the System should filter the chart of accounts display to matching accounts in real-time.
**Validates: Requirements 17.5**

### Property 64: Loan payoff projection
*For any* Member with an active loan, the System should display the projected payoff date based on current payment patterns.
**Validates: Requirements 18.2**

### Property 65: Loan qualification calculation
*For any* Member's savings balance increase, the System should correctly calculate and display the potential loan amount the Member qualifies for based on share capital.
**Validates: Requirements 18.3**

### Property 66: Dividend projection calculation
*For any* Member viewing dividend projections, the System should estimate potential dividend earnings based on current balance and historical dividend rates.
**Validates: Requirements 18.4**

### Property 67: Savings habit recommendation
*For any* Member with irregular transaction patterns, the System should display a recommendation to establish regular savings habits with suggested monthly amounts.
**Validates: Requirements 18.5**

### Property 68: Multi-field member search
*For any* member search query entered by an Officer, the System should search by member ID, name, or ID card number and return matching results.
**Validates: Requirements 19.1**

### Property 69: Search result completeness
*For any* member search result, the System should display member photo, full name, member ID, and account status.
**Validates: Requirements 19.2**

### Property 70: Member selection financial summary
*For any* member selected from search results by an Officer, the System should display a financial summary popup with savings balance, active loans, and recent transactions.
**Validates: Requirements 19.3**

### Property 71: Quick action button availability
*For any* financial summary displayed, the System should include quick action buttons for deposit, withdrawal, and loan payment.
**Validates: Requirements 19.4**

### Property 72: Quick action form pre-population
*For any* quick action initiated by an Officer, the System should pre-populate the transaction form with member information and display the transaction interface.
**Validates: Requirements 19.5**

### Property 73: Export button availability
*For any* dashboard widget containing tabular or chart data viewed by a President, the System should provide an export button.
**Validates: Requirements 20.1**

### Property 74: Export format options
*For any* widget data export by a President, the System should offer format options including PDF, Excel, and CSV.
**Validates: Requirements 20.2**

### Property 75: Chart export quality
*For any* chart export by a President, the System should generate a high-resolution image suitable for presentations.
**Validates: Requirements 20.3**

### Property 76: Financial statement export formatting
*For any* financial statement export by a President, the System should include proper formatting, headers, footers, and digital signatures.
**Validates: Requirements 20.4**

### Property 77: Export completion workflow
*For any* completed export, the System should provide a download link and optionally send the exported file to the President's registered email address.
**Validates: Requirements 20.5**



## Error Handling

### Frontend Error Handling

#### 1. Widget-Level Error Boundaries

Each widget implements an error boundary to prevent cascade failures:

```typescript
class WidgetErrorBoundary extends React.Component<Props, State> {
  componentDidCatch(error: Error, errorInfo: React.ErrorInfo) {
    // Log error to monitoring service
    logError(error, errorInfo);
    
    // Update state to show fallback UI
    this.setState({ hasError: true, error });
  }
  
  render() {
    if (this.state.hasError) {
      return (
        <WidgetErrorFallback 
          error={this.state.error}
          onRetry={() => this.setState({ hasError: false })}
        />
      );
    }
    return this.props.children;
  }
}
```

#### 2. API Error Handling

RTK Query provides centralized error handling:

```typescript
const dashboardApi = apiSlice.injectEndpoints({
  endpoints: (builder) => ({
    getDashboardStats: builder.query({
      query: () => '/dashboard/stats',
      transformErrorResponse: (response) => {
        // Transform backend errors to user-friendly messages
        return {
          status: response.status,
          message: getErrorMessage(response.status),
          originalError: response.data
        };
      },
      // Retry logic for transient failures
      extraOptions: {
        maxRetries: 3,
        backoff: (attempt) => Math.min(1000 * 2 ** attempt, 30000)
      }
    })
  })
});
```

#### 3. Network Error Handling

```typescript
// Detect offline status
window.addEventListener('offline', () => {
  store.dispatch(setNetworkStatus('offline'));
  showNotification('You are offline. Using cached data.', 'warning');
});

window.addEventListener('online', () => {
  store.dispatch(setNetworkStatus('online'));
  showNotification('Connection restored.', 'success');
  // Trigger data refresh
  store.dispatch(dashboardApi.util.invalidateTags(['Dashboard']));
});
```

### Backend Error Handling

#### 1. Global Exception Handler

```java
@ControllerAdvice
public class DashboardExceptionHandler {
    
    @ExceptionHandler(DashboardDataException.class)
    public ResponseEntity<ErrorResponse> handleDashboardDataException(
        DashboardDataException ex
    ) {
        ErrorResponse error = ErrorResponse.builder()
            .code("DASHBOARD_DATA_ERROR")
            .message(ex.getMessage())
            .timestamp(LocalDateTime.now())
            .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    @ExceptionHandler(InsufficientPermissionsException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientPermissions(
        InsufficientPermissionsException ex
    ) {
        ErrorResponse error = ErrorResponse.builder()
            .code("INSUFFICIENT_PERMISSIONS")
            .message("You do not have permission to access this resource")
            .timestamp(LocalDateTime.now())
            .build();
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }
    
    @ExceptionHandler(FiscalPeriodClosedException.class)
    public ResponseEntity<ErrorResponse> handleFiscalPeriodClosed(
        FiscalPeriodClosedException ex
    ) {
        ErrorResponse error = ErrorResponse.builder()
            .code("FISCAL_PERIOD_CLOSED")
            .message("Cannot perform transaction: fiscal period is closed")
            .timestamp(LocalDateTime.now())
            .build();
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }
}
```

#### 2. Service-Level Error Handling

```java
@Service
public class DashboardService {
    
    public DashboardStats calculateDashboardStats(UserRole role, String period) {
        try {
            // Validate inputs
            validateRole(role);
            validatePeriod(period);
            
            // Calculate stats
            DashboardStats stats = performCalculation(role, period);
            
            // Validate results
            validateStats(stats);
            
            return stats;
        } catch (ValidationException ex) {
            log.error("Validation error in dashboard stats calculation", ex);
            throw new DashboardDataException("Invalid input parameters", ex);
        } catch (DataAccessException ex) {
            log.error("Database error in dashboard stats calculation", ex);
            throw new DashboardDataException("Error accessing data", ex);
        }
    }
}
```

#### 3. Transaction Error Handling

```java
@Transactional
public TransactionResponse processQuickDeposit(DepositRequest request) {
    try {
        // Validate fiscal period is open
        if (!fiscalPeriodService.isCurrentPeriodOpen()) {
            throw new FiscalPeriodClosedException();
        }
        
        // Validate member exists and is active
        Member member = memberRepository.findById(request.getMemberId())
            .orElseThrow(() -> new MemberNotFoundException(request.getMemberId()));
        
        if (!member.isActive()) {
            throw new InactiveMemberException(request.getMemberId());
        }
        
        // Process deposit
        SavingAccount account = savingAccountRepository
            .findByMemberId(request.getMemberId())
            .orElseThrow(() -> new AccountNotFoundException(request.getMemberId()));
        
        account.setBalance(account.getBalance().add(request.getAmount()));
        savingAccountRepository.save(account);
        
        // Create ledger transaction
        LedgerTransaction transaction = createLedgerTransaction(request);
        transactionRepository.save(transaction);
        
        // Generate receipt
        String receiptNumber = receiptService.generateReceipt(transaction);
        
        return TransactionResponse.builder()
            .transactionId(transaction.getTransId())
            .transactionNumber(receiptNumber)
            .type("DEPOSIT")
            .amount(request.getAmount())
            .timestamp(LocalDateTime.now())
            .status("SUCCESS")
            .message("Deposit processed successfully")
            .build();
            
    } catch (Exception ex) {
        log.error("Error processing quick deposit", ex);
        throw new TransactionProcessingException("Failed to process deposit", ex);
    }
}
```

### Error Recovery Strategies

#### 1. Automatic Retry
- Network timeouts: 3 retries with exponential backoff
- Transient database errors: 2 retries with 1-second delay
- Rate limit errors: Retry after specified delay

#### 2. Graceful Degradation
- Display cached data when real-time data unavailable
- Show partial dashboard with available widgets
- Disable unavailable features with clear messaging

#### 3. User Notification
- Toast notifications for non-critical errors
- Modal dialogs for critical errors requiring user action
- Inline error messages for form validation errors

## Testing Strategy

### Unit Testing

#### Frontend Unit Tests

**Framework**: Jest + React Testing Library

**Coverage Areas**:
1. Widget component rendering
2. State management logic
3. Data transformation functions
4. Utility functions

**Example Test**:
```typescript
describe('StatCard Component', () => {
  it('should render stat card with correct values', () => {
    const props = {
      title: 'Total Members',
      value: '150',
      icon: <People />,
      color: '#1976d2'
    };
    
    render(<StatCard {...props} />);
    
    expect(screen.getByText('Total Members')).toBeInTheDocument();
    expect(screen.getByText('150')).toBeInTheDocument();
  });
  
  it('should display trend indicator when provided', () => {
    const props = {
      title: 'Total Members',
      value: '150',
      icon: <People />,
      color: '#1976d2',
      trend: {
        direction: 'up',
        percentage: 5.2,
        period: 'vs last month'
      }
    };
    
    render(<StatCard {...props} />);
    
    expect(screen.getByText('+5.2%')).toBeInTheDocument();
    expect(screen.getByText('vs last month')).toBeInTheDocument();
  });
});
```

#### Backend Unit Tests

**Framework**: JUnit 5 + Mockito

**Coverage Areas**:
1. Service layer business logic
2. KPI calculation methods
3. Data transformation
4. Validation logic

**Example Test**:
```java
@ExtendWith(MockitoExtension.class)
class KPICalculationServiceTest {
    
    @Mock
    private LoanRepository loanRepository;
    
    @InjectMocks
    private KPICalculationService kpiService;
    
    @Test
    void shouldCalculatePAR30Correctly() {
        // Given
        List<Loan> overdueLoans = createOverdueLoans(30);
        BigDecimal totalPortfolio = new BigDecimal("1000000");
        BigDecimal overdueAmount = new BigDecimal("50000");
        
        when(loanRepository.findOverdueLoans(30))
            .thenReturn(overdueLoans);
        when(loanRepository.getTotalOutstandingPrincipal())
            .thenReturn(totalPortfolio);
        
        // When
        BigDecimal par30 = kpiService.calculatePAR30();
        
        // Then
        assertEquals(new BigDecimal("5.00"), par30);
    }
}
```

### Property-Based Testing

**Framework**: 
- Frontend: fast-check (JavaScript/TypeScript)
- Backend: jqwik (Java)

**Configuration**: Each property test runs a minimum of 100 iterations

**Property Test Examples**:

#### Property 1: Member search response time and completeness
```typescript
import fc from 'fast-check';

describe('Property: Member search response time and completeness', () => {
  it('should return results within 500ms with complete data for any search query', () => {
    fc.assert(
      fc.asyncProperty(
        fc.string({ minLength: 1, maxLength: 50 }), // Random search query
        async (searchQuery) => {
          const startTime = Date.now();
          const results = await searchMembers(searchQuery);
          const endTime = Date.now();
          
          // Property 1: Response time < 500ms
          expect(endTime - startTime).toBeLessThan(500);
          
          // Property 2: Each result has required fields
          results.forEach(result => {
            expect(result).toHaveProperty('memberId');
            expect(result).toHaveProperty('firstName');
            expect(result).toHaveProperty('lastName');
            expect(result).toHaveProperty('thumbnailUrl');
            expect(result).toHaveProperty('financials');
            expect(result.financials).toHaveProperty('savingsBalance');
            expect(result.financials).toHaveProperty('loanPrincipal');
          });
        }
      ),
      { numRuns: 100 }
    );
  });
});
```

#### Property 2: Loan payment allocation
```java
@Property
void loanPaymentShouldCorrectlyAllocatePrincipalAndInterest(
    @ForAll @BigRange(min = "1000", max = "100000") BigDecimal paymentAmount,
    @ForAll @BigRange(min = "10000", max = "500000") BigDecimal principal,
    @ForAll @BigRange(min = "0", max = "50000") BigDecimal interest
) {
    // Given
    Loan loan = createLoan(principal, interest);
    LoanPaymentRequest request = new LoanPaymentRequest(
        loan.getLoanId(),
        paymentAmount
    );
    
    // When
    TransactionResponse response = dashboardService.processLoanPayment(request);
    
    // Then - Interest is paid first
    BigDecimal expectedInterestPaid = paymentAmount.min(interest);
    BigDecimal expectedPrincipalPaid = paymentAmount.subtract(expectedInterestPaid);
    
    assertEquals(expectedInterestPaid, response.getInterestPaid());
    assertEquals(expectedPrincipalPaid, response.getPrincipalPaid());
    
    // Verify balance updated correctly
    Loan updatedLoan = loanRepository.findById(loan.getLoanId()).get();
    assertEquals(
        principal.subtract(expectedPrincipalPaid),
        updatedLoan.getPrincipalBalance()
    );
    assertEquals(
        interest.subtract(expectedInterestPaid),
        updatedLoan.getInterestBalance()
    );
}
```

#### Property 3: Role-based access control
```typescript
describe('Property: Role-based access control', () => {
  it('should deny access to unauthorized widgets for any user role', () => {
    fc.assert(
      fc.property(
        fc.constantFrom('OFFICER', 'SECRETARY', 'PRESIDENT', 'MEMBER'),
        fc.constantFrom(
          'TrialBalanceWidget',
          'ApprovalQueueWidget',
          'CashBoxTallyWidget',
          'PortfolioHealthWidget'
        ),
        (userRole, widgetName) => {
          const user = createUserWithRole(userRole);
          const widget = getWidget(widgetName);
          const isAuthorized = checkWidgetAuthorization(user, widget);
          
          // Define authorization matrix
          const authMatrix = {
            OFFICER: ['CashBoxTallyWidget'],
            SECRETARY: ['TrialBalanceWidget', 'CashBoxTallyWidget'],
            PRESIDENT: ['ApprovalQueueWidget', 'PortfolioHealthWidget'],
            MEMBER: []
          };
          
          const expectedAuthorization = authMatrix[userRole].includes(widgetName);
          expect(isAuthorized).toBe(expectedAuthorization);
          
          if (!isAuthorized) {
            expect(() => renderWidget(widget, user)).toThrow('Insufficient permissions');
          }
        }
      ),
      { numRuns: 100 }
    );
  });
});
```

#### Property 4: Cash variance calculation
```java
@Property
void cashVarianceShouldBeCorrectlyCalculated(
    @ForAll @BigRange(min = "0", max = "1000000") BigDecimal expectedCash,
    @ForAll List<@From("denominationGenerator") Denomination> denominations
) {
    // Given
    CashBoxTallyRequest request = new CashBoxTallyRequest(
        expectedCash,
        denominations
    );
    
    // When
    CashReconciliationResult result = dashboardService.reconcileCash(request);
    
    // Then
    BigDecimal physicalCash = calculatePhysicalCash(denominations);
    BigDecimal expectedVariance = physicalCash.subtract(expectedCash);
    
    assertEquals(expectedVariance, result.getVariance());
    assertEquals(physicalCash, result.getPhysicalCash());
    assertEquals(expectedCash, result.getExpectedCash());
    
    // Verify variance indicator
    if (expectedVariance.compareTo(BigDecimal.ZERO) > 0) {
        assertEquals("OVER", result.getVarianceIndicator());
    } else if (expectedVariance.compareTo(BigDecimal.ZERO) < 0) {
        assertEquals("SHORT", result.getVarianceIndicator());
    } else {
        assertEquals("BALANCED", result.getVarianceIndicator());
    }
}

@Provide
Arbitrary<Denomination> denominationGenerator() {
    return Combinators.combine(
        Arbitraries.of(1000, 500, 100, 50, 20, 10, 5, 1),
        Arbitraries.integers().between(0, 100)
    ).as((value, count) -> new Denomination(value, count));
}
```

### Integration Testing

**Framework**: 
- Frontend: Cypress for E2E testing
- Backend: Spring Boot Test with TestContainers

**Coverage Areas**:
1. Complete user workflows (login → dashboard → transaction)
2. API endpoint integration
3. Database transaction integrity
4. Real-time update propagation

**Example Integration Test**:
```typescript
describe('Officer Dashboard Workflow', () => {
  beforeEach(() => {
    cy.login('officer@test.com', 'password');
  });
  
  it('should complete quick deposit workflow', () => {
    // Navigate to dashboard
    cy.visit('/dashboard');
    
    // Verify fiscal period is open
    cy.get('[data-testid="fiscal-period-indicator"]')
      .should('contain', 'OPEN');
    
    // Search for member
    cy.get('[data-testid="member-search-input"]')
      .type('John Doe');
    cy.get('[data-testid="member-search-results"]')
      .should('be.visible')
      .within(() => {
        cy.contains('John Doe').click();
      });
    
    // Verify financial summary appears
    cy.get('[data-testid="financial-summary-popup"]')
      .should('be.visible')
      .within(() => {
        cy.get('[data-testid="savings-balance"]').should('exist');
        cy.get('[data-testid="quick-deposit-button"]').click();
      });
    
    // Fill deposit form
    cy.get('[data-testid="deposit-amount-input"]')
      .type('5000');
    cy.get('[data-testid="deposit-notes-input"]')
      .type('Monthly savings');
    cy.get('[data-testid="submit-deposit-button"]').click();
    
    // Verify success
    cy.get('[data-testid="transaction-success-message"]')
      .should('be.visible')
      .and('contain', 'Deposit processed successfully');
    
    // Verify receipt generated
    cy.get('[data-testid="receipt-number"]').should('exist');
    
    // Verify activity feed updated
    cy.get('[data-testid="activity-feed"]')
      .should('contain', 'DEPOSIT')
      .and('contain', 'John Doe')
      .and('contain', '5,000.00');
  });
});
```

### Performance Testing

**Tools**: 
- Frontend: Lighthouse, WebPageTest
- Backend: JMeter, Gatling

**Performance Targets**:
- Dashboard initial load: < 1 second
- Widget interaction response: < 200ms
- API response time: < 500ms (p95)
- Concurrent users: 100+ without degradation

**Load Test Scenario**:
```scala
// Gatling load test
class DashboardLoadTest extends Simulation {
  val httpProtocol = http
    .baseUrl("http://localhost:8080")
    .acceptHeader("application/json")
  
  val scn = scenario("Dashboard Load Test")
    .exec(http("Login")
      .post("/api/auth/login")
      .body(StringBody("""{"username":"officer","password":"password"}"""))
      .check(jsonPath("$.token").saveAs("token")))
    .pause(1)
    .exec(http("Get Dashboard Stats")
      .get("/api/dashboard/stats")
      .header("Authorization", "Bearer ${token}")
      .check(status.is(200))
      .check(responseTimeInMillis.lte(500)))
    .pause(2)
    .exec(http("Search Members")
      .get("/api/dashboard/members/search?q=John")
      .header("Authorization", "Bearer ${token}")
      .check(status.is(200))
      .check(responseTimeInMillis.lte(500)))
  
  setUp(
    scn.inject(
      rampUsers(100) during (60 seconds)
    )
  ).protocols(httpProtocol)
   .assertions(
     global.responseTime.max.lt(1000),
     global.successfulRequests.percent.gt(95)
   )
}
```

---

**Related Documentation**:
- [Requirements Document](requirements.md) - Complete requirements specification
- [Implementation Tasks](tasks.md) - Detailed implementation plan
- [API Documentation](../../docs/api/rest-endpoints.md) - REST API reference
- [Database Schema](../../docs/architecture/database-schema.md) - Data model reference
