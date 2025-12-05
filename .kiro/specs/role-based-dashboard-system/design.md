# Design Document

## Overview

The role-based dashboard system provides tailored views and key performance indicators (KPIs) for different user roles within the Ban Sai Yai Savings Group Financial Accounting System. The design leverages the existing Java/Spring Boot backend with MariaDB database, and the React/TypeScript frontend with Material-UI components, Redux Toolkit Query for state management, and Chart.js/Recharts for data visualization.

The dashboard architecture follows a modular, component-based approach where each widget is self-contained, handles its own data fetching, loading states, and error boundaries. The system implements responsive design using Material-UI's Grid system and breakpoints, ensuring optimal viewing experiences across desktop, tablet, and mobile devices.

## Architecture

### High-Level Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    React Frontend Layer                     │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │  Dashboard   │  │   Widgets    │  │    Charts    │      │
│  │    Pages     │  │  Components  │  │  (Recharts)  │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
└─────────────────────────────────────────────────────────────┘
                          │
                          │ RTK Query API Calls
                          ▼
┌─────────────────────────────────────────────────────────────┐
│                 Spring Boot Backend Layer                   │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │  Dashboard   │  │   Service    │  │  Repository  │      │
│  │ Controllers  │  │    Layer     │  │    Layer     │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
└─────────────────────────────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────────┐
│                      MariaDB Database                       │
│  member | loan | saving | transaction | accounting          │
└─────────────────────────────────────────────────────────────┘
```

### Component Hierarchy

```
DashboardPage (Role-specific)
├── FiscalPeriodHeader
├── OmniSearchBar
├── StatCard[] (KPI widgets)
├── QuickActionsPanel
│   ├── DepositModal
│   ├── LoanPaymentModal
│   └── WithdrawalModal
├── ChartWidgets[]
│   ├── MemberGrowthChart
│   ├── LoanPortfolioChart
│   ├── SavingsGrowthChart
│   └── RevenueT rendChart
├── RecentActivityTable
├── TrialBalanceWidget (Secretary only)
├── UnclassifiedTransactionAlert (Secretary only)
└── CashBoxTally (Officer only)
```


## Components and Interfaces

### Frontend Components

#### 1. DashboardPage Component

**Purpose**: Main container component that renders role-specific dashboard layouts

**Props**:
```typescript
interface DashboardPageProps {
  // No props - uses Redux state for user role
}
```

**State Management**:
- Uses `useSelector` to access `auth.user.role` from Redux store
- Uses RTK Query hooks for data fetching with automatic caching and refetching

**Responsibilities**:
- Determine user role and render appropriate widgets
- Manage responsive layout using Material-UI Grid
- Handle global dashboard error boundaries
- Coordinate widget refresh on user actions

#### 2. StatCard Component

**Purpose**: Reusable KPI display card

**Props**:
```typescript
interface StatCardProps {
  title: string;
  value: string | number;
  icon: ReactNode;
  color: string;
  trend?: {
    value: number;
    direction: 'up' | 'down';
  };
  onClick?: () => void;
}
```

**Features**:
- Material-UI Card with consistent styling
- Icon with colored background
- Optional trend indicator with percentage change
- Optional click handler for navigation

#### 3. ChartWidget Component

**Purpose**: Wrapper component for chart visualizations

**Props**:
```typescript
interface ChartWidgetProps {
  title: string;
  chartType: 'line' | 'bar' | 'pie' | 'doughnut';
  data: ChartData;
  isLoading: boolean;
  error?: string;
  height?: number;
  onRefresh?: () => void;
}
```

**Features**:
- Lazy loading with Suspense
- Loading skeleton during data fetch
- Error boundary with retry button
- Responsive chart sizing
- Tooltip on hover


#### 4. QuickActionsPanel Component

**Purpose**: Displays role-specific quick action buttons

**Props**:
```typescript
interface QuickActionsPanelProps {
  actions: QuickAction[];
  onActionClick: (action: QuickAction) => void;
}

interface QuickAction {
  id: string;
  title: string;
  description: string;
  icon: string;
  route?: string;
  modal?: 'deposit' | 'loan-payment' | 'withdrawal';
}
```

**Features**:
- Grid layout of action buttons
- Material-UI Icons dynamically loaded
- Opens modal or navigates to route
- Role-based filtering

#### 5. DepositModal Component

**Purpose**: Modal dialog for recording deposits

**Props**:
```typescript
interface DepositModalProps {
  open: boolean;
  onClose: () => void;
  onSuccess: () => void;
}
```

**Form Fields**:
- Member search/select (autocomplete)
- Amount (number input with validation)
- Deposit type (select: SHARE_CAPITAL | DEPOSIT)
- Description (text area)
- Transaction date (date picker)

**Validation**:
- Amount must be greater than 0
- Member must be selected
- Date cannot be future-dated

#### 6. RecentActivityTable Component

**Purpose**: Displays recent transactions in tabular format

**Props**:
```typescript
interface RecentActivityTableProps {
  limit?: number;
  memberId?: number; // Optional filter for member-specific view
}
```

**Columns**:
- Timestamp
- Member Name
- Transaction Type
- Amount
- Status

**Features**:
- Material-UI DataGrid
- Sortable columns
- Click row to view details
- Auto-refresh every 60 seconds


#### 7. OmniSearchBar Component

**Purpose**: Global search functionality for members and transactions

**Props**:
```typescript
interface OmniSearchBarProps {
  placeholder?: string;
}
```

**Features**:
- Material-UI Autocomplete
- Debounced search (300ms)
- Minimum 3 characters to trigger search
- Grouped results (Members | Transactions)
- Keyboard navigation support

#### 8. FiscalPeriodHeader Component

**Purpose**: Displays current fiscal period status

**Props**:
```typescript
interface FiscalPeriodHeaderProps {
  onStatusChange?: (status: 'OPEN' | 'CLOSED') => void;
}
```

**Features**:
- Shows fiscal period dates
- Status indicator (OPEN/CLOSED)
- Close period button (Secretary/President only)
- Warning if period is about to close

### Backend Components

#### 1. DashboardController

**Purpose**: REST API endpoints for dashboard data

**Endpoints**:
```java
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    
    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsDTO> getDashboardStats(
        @RequestParam(required = false) String role,
        @RequestParam(defaultValue = "month") String period
    );
    
    @GetMapping("/charts/member-growth")
    public ResponseEntity<ChartDataDTO> getMemberGrowthChart(
        @RequestParam String period
    );
    
    @GetMapping("/charts/loan-portfolio")
    public ResponseEntity<ChartDataDTO> getLoanPortfolioChart(
        @RequestParam String period
    );
    
    @GetMapping("/charts/revenue-trend")
    public ResponseEntity<ChartDataDTO> getRevenueTrendChart(
        @RequestParam(defaultValue = "12") int months
    );
    
    @GetMapping("/quick-actions")
    public ResponseEntity<List<QuickActionDTO>> getQuickActions(
        @RequestParam String role
    );
}
```


#### 2. DashboardService

**Purpose**: Business logic for dashboard data aggregation

**Methods**:
```java
@Service
public class DashboardService {
    
    public DashboardStatsDTO calculateDashboardStats(String role, String period);
    
    public ChartDataDTO getMemberGrowthData(String period);
    
    public ChartDataDTO getLoanPortfolioComposition();
    
    public ChartDataDTO getRevenueTrend(int months);
    
    public List<QuickActionDTO> getQuickActionsByRole(String role);
    
    public FinancialRatiosDTO calculateFinancialRatios();
    
    public List<RecentActivityDTO> getRecentActivities(int limit, Long memberId);
}
```

**Key Calculations**:
- Total cash balance: Sum of all account balances with type ASSET
- Active loan count: Count of loans with status ACTIVE
- Loan-to-savings ratio: Total outstanding loans / Total savings
- Default rate: (Defaulted loans / Total loans) × 100
- Monthly revenue: Sum of interest payments in current month

#### 3. DashboardRepository

**Purpose**: Custom queries for dashboard data

**Methods**:
```java
@Repository
public interface DashboardRepository {
    
    @Query("SELECT COUNT(m) FROM Member m WHERE m.dateRegist BETWEEN :start AND :end")
    Long countNewMembersInPeriod(@Param("start") LocalDate start, @Param("end") LocalDate end);
    
    @Query("SELECT SUM(l.amount) FROM Loan l WHERE l.status = 'ACTIVE'")
    BigDecimal getTotalActiveLoanAmount();
    
    @Query("SELECT SUM(s.balance) FROM SavingAccount s")
    BigDecimal getTotalSavingsBalance();
    
    @Query("SELECT l.loanType, COUNT(l), SUM(l.amount) FROM Loan l WHERE l.status = 'ACTIVE' GROUP BY l.loanType")
    List<Object[]> getLoanPortfolioComposition();
    
    @Query("SELECT MONTH(t.transDate), SUM(t.amount) FROM LedgerTransaction t " +
           "WHERE t.accountCode.accountType = 'REVENUE' AND t.transDate >= :startDate " +
           "GROUP BY MONTH(t.transDate) ORDER BY MONTH(t.transDate)")
    List<Object[]> getMonthlyRevenue(@Param("startDate") LocalDate startDate);
}
```


## Data Models

### Frontend TypeScript Interfaces

```typescript
// Dashboard Statistics
interface DashboardStats {
  totalMembers: number;
  activeLoans: number;
  totalSavings: number;
  monthlyRevenue: number;
  pendingLoanApplications?: number;
  pendingPayments?: number;
  cashBalance?: number;
  loanToSavingsRatio?: number;
  defaultRate?: number;
}

// Chart Data (compatible with Chart.js/Recharts)
interface ChartData {
  labels: string[];
  datasets: Array<{
    label: string;
    data: number[];
    backgroundColor?: string | string[];
    borderColor?: string;
    fill?: boolean;
  }>;
}

// Quick Action
interface QuickAction {
  id: string;
  title: string;
  description: string;
  icon: string;
  route?: string;
  permission: string;
  priority: number;
}

// Recent Activity
interface RecentActivity {
  transactionId: number;
  timestamp: string;
  memberName: string;
  type: 'DEPOSIT' | 'WITHDRAWAL' | 'LOAN_PAYMENT' | 'LOAN_DISBURSEMENT';
  amount: number;
  status: 'COMPLETED' | 'PENDING' | 'FAILED';
}

// Fiscal Period
interface FiscalPeriod {
  periodId: number;
  startDate: string;
  endDate: string;
  status: 'OPEN' | 'CLOSED';
  closedBy?: string;
  closedAt?: string;
}

// Financial Ratios
interface FinancialRatios {
  loanToSavingsRatio: number;
  defaultRate: number;
  liquidityRatio: number;
  capitalAdequacyRatio: number;
}

// Member Search Result
interface MemberSearchResult {
  memberId: number;
  name: string;
  idCard: string;
  photoPath?: string;
}

// Deposit Request
interface DepositRequest {
  memberId: number;
  amount: number;
  depositType: 'SHARE_CAPITAL' | 'DEPOSIT';
  description: string;
  transactionDate: string;
}

// Loan Payment Request
interface LoanPaymentRequest {
  loanId: number;
  amount: number;
  paymentDate: string;
  paymentMethod: 'CASH' | 'BANK_TRANSFER' | 'CHECK';
  reference?: string;
}

// Transaction Response
interface TransactionResponse {
  transactionId: string;
  success: boolean;
  message: string;
  receiptUrl?: string;
  newBalance?: number;
}
```


### Backend DTOs

```java
// Dashboard Statistics DTO
@Data
public class DashboardStatsDTO {
    private Long totalMembers;
    private Long activeLoans;
    private BigDecimal totalSavings;
    private BigDecimal monthlyRevenue;
    private Long pendingLoanApplications;
    private Long pendingPayments;
    private BigDecimal cashBalance;
    private BigDecimal loanToSavingsRatio;
    private BigDecimal defaultRate;
}

// Chart Data DTO
@Data
public class ChartDataDTO {
    private List<String> labels;
    private List<DatasetDTO> datasets;
    
    @Data
    public static class DatasetDTO {
        private String label;
        private List<BigDecimal> data;
        private String backgroundColor;
        private String borderColor;
        private Boolean fill;
    }
}

// Quick Action DTO
@Data
public class QuickActionDTO {
    private String id;
    private String title;
    private String description;
    private String icon;
    private String route;
    private String permission;
    private Integer priority;
}

// Recent Activity DTO
@Data
public class RecentActivityDTO {
    private Long transactionId;
    private LocalDateTime timestamp;
    private String memberName;
    private String type;
    private BigDecimal amount;
    private String status;
}

// Financial Ratios DTO
@Data
public class FinancialRatiosDTO {
    private BigDecimal loanToSavingsRatio;
    private BigDecimal defaultRate;
    private BigDecimal liquidityRatio;
    private BigDecimal capitalAdequacyRatio;
}
```


## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system-essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

### Property 1: Role-based widget visibility

*For any* user role and dashboard page render, the widgets displayed should exactly match the set of widgets configured for that role, with no widgets from other roles appearing.

**Validates: Requirements 1.1, 1.2, 1.3, 1.4, 2.1, 2.2, 2.3, 2.4, 3.1, 3.2, 3.3, 3.4, 4.1, 4.2, 4.3, 4.5**

### Property 2: Conditional alert rendering

*For any* trial balance widget, if the unclassified transaction count is greater than zero, then a warning badge should be visible on the widget.

**Validates: Requirements 2.5**

### Property 3: Payment reminder alerts

*For any* member with active loans, if any loan has a payment due date within 7 days from the current date, then a payment reminder alert should be displayed on the member's dashboard.

**Validates: Requirements 4.4**

### Property 4: Responsive layout adaptation

*For any* viewport width change, the dashboard grid layout should reflow to match the appropriate breakpoint configuration (4-column for desktop, 2-column for tablet, 1-column for mobile) without requiring a page reload.

**Validates: Requirements 5.1, 5.2, 5.3, 5.4, 5.5**

### Property 5: Cache invalidation on mutation

*For any* quick action that successfully modifies data (deposit, loan payment, withdrawal), the system should immediately invalidate the cache tags for affected widgets, triggering a refetch of their data.

**Validates: Requirements 6.3**

### Property 6: Stale data indication on error

*For any* widget whose API request fails, if cached data exists, the widget should display that cached data along with a visible stale data indicator.

**Validates: Requirements 6.4**

### Property 7: Exponential backoff retry

*For any* failed API request, the system should retry the request with exponential backoff delays (e.g., 1s, 2s, 4s) up to a maximum of 3 attempts before displaying an error.

**Validates: Requirements 6.5**

### Property 8: Chart tooltip interactivity

*For any* chart data point, when the user hovers over it, a tooltip should appear displaying the exact value and label for that data point.

**Validates: Requirements 7.2**

### Property 9: Loading state indication

*For any* chart widget in a loading state, a loading spinner should be visible within the chart container until data is successfully loaded or an error occurs.

**Validates: Requirements 7.5**

### Property 10: Search query threshold

*For any* search input in the omni-search bar, an API query should be triggered if and only if the input length is 3 or more characters.

**Validates: Requirements 8.2**

### Property 11: Widget error isolation

*For any* widget that encounters an API error or rendering error, the error should be contained within that widget's boundary, and all other widgets on the dashboard should continue to render and function normally.

**Validates: Requirements 9.1, 9.2, 9.3**

### Property 12: Error recovery UI

*For any* widget displaying an error state, a retry button should be present that, when clicked, attempts to reload the widget's data.

**Validates: Requirements 9.4**

### Property 13: Error logging

*For any* widget error (API failure or rendering exception), the error details should be logged to the browser console using console.error with sufficient context for debugging.

**Validates: Requirements 9.5**

### Property 14: Form validation before submission

*For any* quick action form (deposit, loan payment, withdrawal), when the submit button is clicked, the form should validate all inputs, and a POST request should be sent to the backend API if and only if all validations pass.

**Validates: Requirements 12.3**

### Property 15: Form error persistence

*For any* quick action form submission that fails validation or returns an error from the backend, the modal should remain open and display the validation errors without clearing the form inputs.

**Validates: Requirements 12.5**


## Error Handling

### Frontend Error Handling

#### 1. Error Boundaries

Each widget is wrapped in a React Error Boundary component that catches rendering errors:

```typescript
class WidgetErrorBoundary extends React.Component<Props, State> {
  componentDidCatch(error: Error, errorInfo: React.ErrorInfo) {
    console.error('Widget error:', error, errorInfo);
    // Log to error tracking service (e.g., Sentry)
  }
  
  render() {
    if (this.state.hasError) {
      return (
        <Box p={2}>
          <Alert severity="error">
            <AlertTitle>Widget Error</AlertTitle>
            Failed to load widget. <Button onClick={this.handleRetry}>Retry</Button>
          </Alert>
        </Box>
      );
    }
    return this.props.children;
  }
}
```

#### 2. API Error Handling

RTK Query automatically handles API errors with built-in retry logic:

```typescript
const dashboardApi = apiSlice.injectEndpoints({
  endpoints: (builder) => ({
    getDashboardStats: builder.query<DashboardStats, void>({
      query: () => '/dashboard/stats',
      // Retry failed requests with exponential backoff
      extraOptions: {
        maxRetries: 3,
        backoff: (attempt) => Math.min(1000 * 2 ** attempt, 30000),
      },
    }),
  }),
});
```

#### 3. Graceful Degradation

Widgets display cached data when API fails:

```typescript
const { data, error, isLoading } = useGetDashboardStatsQuery();

if (error && data) {
  // Show stale data with indicator
  return (
    <>
      <Chip label="Data may be outdated" color="warning" size="small" />
      <StatCard {...data} />
    </>
  );
}
```

### Backend Error Handling

#### 1. Global Exception Handler

```java
@ControllerAdvice
public class DashboardExceptionHandler {
    
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ErrorResponse> handleDatabaseError(DataAccessException ex) {
        log.error("Database error in dashboard", ex);
        return ResponseEntity.status(500)
            .body(new ErrorResponse("DATABASE_ERROR", "Failed to fetch dashboard data"));
    }
    
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleInvalidInput(IllegalArgumentException ex) {
        return ResponseEntity.badRequest()
            .body(new ErrorResponse("INVALID_INPUT", ex.getMessage()));
    }
}
```

#### 2. Null Safety

All service methods handle null/empty data gracefully:

```java
public DashboardStatsDTO calculateDashboardStats(String role) {
    DashboardStatsDTO stats = new DashboardStatsDTO();
    
    stats.setTotalMembers(memberRepository.count());
    stats.setActiveLoans(loanRepository.countByStatus(LoanStatus.ACTIVE));
    
    BigDecimal totalSavings = savingRepository.getTotalBalance();
    stats.setTotalSavings(totalSavings != null ? totalSavings : BigDecimal.ZERO);
    
    return stats;
}
```


## Testing Strategy

### Dual Testing Approach

The dashboard system will employ both unit testing and property-based testing to ensure comprehensive correctness:

- **Unit tests** verify specific examples, edge cases, and integration points
- **Property tests** verify universal properties that should hold across all inputs
- Together they provide comprehensive coverage: unit tests catch concrete bugs, property tests verify general correctness

### Unit Testing

#### Frontend Unit Tests (Jest + React Testing Library)

**Test Coverage**:
- Component rendering for each role
- User interactions (clicks, form submissions)
- Navigation behavior
- Modal open/close
- API integration with mocked responses
- Error boundary behavior

**Example Unit Tests**:
```typescript
describe('DashboardPage', () => {
  it('should render President dashboard with executive KPIs', () => {
    const { getByText } = render(<DashboardPage />, {
      preloadedState: { auth: { user: { role: 'ROLE_PRESIDENT' } } }
    });
    
    expect(getByText('Total Members')).toBeInTheDocument();
    expect(getByText('Active Loans')).toBeInTheDocument();
    expect(getByText('Total Savings')).toBeInTheDocument();
    expect(getByText('Monthly Revenue')).toBeInTheDocument();
  });
  
  it('should open deposit modal when quick action is clicked', () => {
    const { getByText, getByRole } = render(<DashboardPage />);
    
    fireEvent.click(getByText('Record Deposit'));
    
    expect(getByRole('dialog')).toBeInTheDocument();
    expect(getByText('Deposit Form')).toBeInTheDocument();
  });
  
  it('should handle widget API error gracefully', () => {
    server.use(
      rest.get('/api/dashboard/stats', (req, res, ctx) => {
        return res(ctx.status(500));
      })
    );
    
    const { getByText } = render(<DashboardPage />);
    
    expect(getByText('Failed to load widget')).toBeInTheDocument();
    expect(getByText('Retry')).toBeInTheDocument();
  });
});
```

#### Backend Unit Tests (JUnit + Mockito)

**Test Coverage**:
- Service layer calculations
- Repository queries
- DTO mappings
- Error handling
- Role-based data filtering

**Example Unit Tests**:
```java
@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {
    
    @Mock
    private MemberRepository memberRepository;
    
    @Mock
    private LoanRepository loanRepository;
    
    @InjectMocks
    private DashboardService dashboardService;
    
    @Test
    void shouldCalculateDashboardStatsCorrectly() {
        when(memberRepository.count()).thenReturn(150L);
        when(loanRepository.countByStatus(LoanStatus.ACTIVE)).thenReturn(45L);
        when(savingRepository.getTotalBalance()).thenReturn(new BigDecimal("2500000.00"));
        
        DashboardStatsDTO stats = dashboardService.calculateDashboardStats("ROLE_PRESIDENT");
        
        assertEquals(150L, stats.getTotalMembers());
        assertEquals(45L, stats.getActiveLoans());
        assertEquals(new BigDecimal("2500000.00"), stats.getTotalSavings());
    }
    
    @Test
    void shouldHandleNullSavingsBalanceGracefully() {
        when(savingRepository.getTotalBalance()).thenReturn(null);
        
        DashboardStatsDTO stats = dashboardService.calculateDashboardStats("ROLE_PRESIDENT");
        
        assertEquals(BigDecimal.ZERO, stats.getTotalSavings());
    }
}
```

### Property-Based Testing

#### Property Testing Library

**Frontend**: Use **fast-check** for JavaScript/TypeScript property-based testing
**Backend**: Use **jqwik** for Java property-based testing (already in dependencies)

#### Configuration

Each property-based test should run a minimum of 100 iterations to ensure thorough coverage of the input space.

#### Property Test Implementation

Each property-based test MUST be tagged with a comment explicitly referencing the correctness property in the design document using this exact format:

```
**Feature: role-based-dashboard-system, Property {number}: {property_text}**
```

**Frontend Property Tests (fast-check)**:

```typescript
import fc from 'fast-check';

describe('Dashboard Property Tests', () => {
  /**
   * Feature: role-based-dashboard-system, Property 1: Role-based widget visibility
   */
  it('should only display widgets configured for the user role', () => {
    fc.assert(
      fc.property(
        fc.constantFrom('ROLE_PRESIDENT', 'ROLE_SECRETARY', 'ROLE_OFFICER', 'ROLE_MEMBER'),
        (role) => {
          const { container } = render(<DashboardPage />, {
            preloadedState: { auth: { user: { role } } }
          });
          
          const expectedWidgets = getWidgetsForRole(role);
          const forbiddenWidgets = getAllWidgets().filter(w => !expectedWidgets.includes(w));
          
          expectedWidgets.forEach(widget => {
            expect(container.querySelector(`[data-widget="${widget}"]`)).toBeInTheDocument();
          });
          
          forbiddenWidgets.forEach(widget => {
            expect(container.querySelector(`[data-widget="${widget}"]`)).not.toBeInTheDocument();
          });
        }
      ),
      { numRuns: 100 }
    );
  });
  
  /**
   * Feature: role-based-dashboard-system, Property 2: Conditional alert rendering
   */
  it('should display warning badge when unclassified transactions exist', () => {
    fc.assert(
      fc.property(
        fc.integer({ min: 0, max: 100 }),
        (unclassifiedCount) => {
          const { container } = render(<TrialBalanceWidget />, {
            preloadedState: { 
              dashboard: { unclassifiedCount }
            }
          });
          
          const warningBadge = container.querySelector('[data-testid="warning-badge"]');
          
          if (unclassifiedCount > 0) {
            expect(warningBadge).toBeInTheDocument();
          } else {
            expect(warningBadge).not.toBeInTheDocument();
          }
        }
      ),
      { numRuns: 100 }
    );
  });
  
  /**
   * Feature: role-based-dashboard-system, Property 4: Responsive layout adaptation
   */
  it('should adapt grid layout based on viewport width', () => {
    fc.assert(
      fc.property(
        fc.integer({ min: 320, max: 2560 }),
        (viewportWidth) => {
          global.innerWidth = viewportWidth;
          window.dispatchEvent(new Event('resize'));
          
          const { container } = render(<DashboardPage />);
          const grid = container.querySelector('[data-testid="dashboard-grid"]');
          
          const expectedColumns = 
            viewportWidth >= 1200 ? 4 :
            viewportWidth >= 768 ? 2 : 1;
          
          const actualColumns = parseInt(
            window.getComputedStyle(grid).gridTemplateColumns.split(' ').length
          );
          
          expect(actualColumns).toBe(expectedColumns);
        }
      ),
      { numRuns: 100 }
    );
  });
  
  /**
   * Feature: role-based-dashboard-system, Property 10: Search query threshold
   */
  it('should only trigger search API when input length >= 3', () => {
    fc.assert(
      fc.property(
        fc.string({ minLength: 0, maxLength: 10 }),
        (searchQuery) => {
          const mockSearchApi = jest.fn();
          const { getByRole } = render(<OmniSearchBar onSearch={mockSearchApi} />);
          
          const searchInput = getByRole('textbox');
          fireEvent.change(searchInput, { target: { value: searchQuery } });
          
          // Wait for debounce
          jest.advanceTimersByTime(300);
          
          if (searchQuery.length >= 3) {
            expect(mockSearchApi).toHaveBeenCalledWith(searchQuery);
          } else {
            expect(mockSearchApi).not.toHaveBeenCalled();
          }
        }
      ),
      { numRuns: 100 }
    );
  });
});
```

**Backend Property Tests (jqwik)**:

```java
class DashboardServicePropertyTest {
    
    @Provide
    Arbitrary<String> roles() {
        return Arbitraries.of("ROLE_PRESIDENT", "ROLE_SECRETARY", "ROLE_OFFICER", "ROLE_MEMBER");
    }
    
    /**
     * Feature: role-based-dashboard-system, Property 1: Role-based widget visibility
     */
    @Property
    void quickActionsShouldMatchRolePermissions(@ForAll("roles") String role) {
        List<QuickActionDTO> actions = dashboardService.getQuickActionsByRole(role);
        
        // All returned actions should have permission matching or less restrictive than role
        actions.forEach(action -> {
            assertTrue(hasPermission(role, action.getPermission()));
        });
        
        // No actions from other roles should be included
        List<QuickActionDTO> allActions = getAllPossibleQuickActions();
        allActions.stream()
            .filter(action -> !hasPermission(role, action.getPermission()))
            .forEach(forbiddenAction -> {
                assertFalse(actions.contains(forbiddenAction));
            });
    }
    
    /**
     * Feature: role-based-dashboard-system, Property 7: Exponential backoff retry
     */
    @Property
    void apiRetryShouldUseExponentialBackoff(@ForAll @IntRange(min = 0, max = 2) int attemptNumber) {
        long delay = calculateRetryDelay(attemptNumber);
        long expectedDelay = (long) (1000 * Math.pow(2, attemptNumber));
        
        assertEquals(expectedDelay, delay);
    }
}
```

### Integration Testing

Integration tests verify end-to-end flows:

```java
@SpringBootTest
@AutoConfigureMockMvc
class DashboardIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    @WithMockUser(roles = "PRESIDENT")
    void shouldReturnExecutiveDashboardData() throws Exception {
        mockMvc.perform(get("/api/dashboard/stats")
                .param("role", "ROLE_PRESIDENT"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalMembers").exists())
            .andExpect(jsonPath("$.activeLoans").exists())
            .andExpect(jsonPath("$.totalSavings").exists())
            .andExpect(jsonPath("$.monthlyRevenue").exists());
    }
}
```

### Test Coverage Goals

- **Unit Test Coverage**: Minimum 80% line coverage
- **Property Test Coverage**: All 15 correctness properties must have corresponding property tests
- **Integration Test Coverage**: All API endpoints must have at least one integration test


## Performance Considerations

### Frontend Performance

#### 1. Code Splitting and Lazy Loading

Heavy chart components are lazy-loaded to reduce initial bundle size:

```typescript
const MemberGrowthChart = lazy(() => import('@/components/dashboard/charts/MemberGrowthChart'));
const LoanPortfolioChart = lazy(() => import('@/components/dashboard/charts/LoanPortfolioChart'));

// Usage with Suspense
<Suspense fallback={<LoadingSpinner />}>
  <MemberGrowthChart />
</Suspense>
```

#### 2. Memoization

Expensive calculations are memoized:

```typescript
const DashboardPage = () => {
  const stats = useGetDashboardStatsQuery();
  
  const formattedStats = useMemo(() => {
    return formatDashboardStats(stats.data);
  }, [stats.data]);
  
  return <StatCard {...formattedStats} />;
};
```

#### 3. Debouncing

Search input is debounced to reduce API calls:

```typescript
const debouncedSearch = useMemo(
  () => debounce((query: string) => {
    if (query.length >= 3) {
      searchMembers(query);
    }
  }, 300),
  []
);
```

#### 4. Virtual Scrolling

Large tables use virtual scrolling for performance:

```typescript
<DataGrid
  rows={transactions}
  columns={columns}
  virtualization
  pageSize={20}
/>
```

### Backend Performance

#### 1. Database Query Optimization

Indexed queries for dashboard data:

```sql
CREATE INDEX idx_loan_status ON loan(status);
CREATE INDEX idx_transaction_date ON transaction(trans_date);
CREATE INDEX idx_member_date_regist ON member(date_regist);
```

#### 2. Caching Strategy

Redis caching for frequently accessed dashboard data:

```java
@Cacheable(value = "dashboardStats", key = "#role")
public DashboardStatsDTO calculateDashboardStats(String role) {
    // Expensive calculation cached for 5 minutes
}
```

#### 3. Pagination

All list endpoints support pagination:

```java
@GetMapping("/recent-activities")
public Page<RecentActivityDTO> getRecentActivities(
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "10") int size
) {
    return dashboardService.getRecentActivities(PageRequest.of(page, size));
}
```

#### 4. Async Processing

Heavy calculations run asynchronously:

```java
@Async
public CompletableFuture<ChartDataDTO> calculateTrendData(String period) {
    // Long-running calculation
    return CompletableFuture.completedFuture(result);
}
```

### Performance Targets

- **Initial Page Load**: < 2 seconds
- **Widget Refresh**: < 500ms
- **Search Response**: < 300ms
- **Chart Rendering**: < 1 second
- **API Response Time**: < 200ms (p95)


## Security Considerations

### Authentication and Authorization

#### 1. JWT Token Validation

All dashboard API endpoints require valid JWT token:

```java
@RestController
@RequestMapping("/api/dashboard")
@PreAuthorize("isAuthenticated()")
public class DashboardController {
    // All endpoints require authentication
}
```

#### 2. Role-Based Access Control

Endpoints enforce role-based permissions:

```java
@GetMapping("/executive")
@PreAuthorize("hasRole('PRESIDENT')")
public ResponseEntity<ExecutiveDashboardDTO> getExecutiveDashboard() {
    // Only accessible to PRESIDENT role
}

@GetMapping("/secretary")
@PreAuthorize("hasAnyRole('SECRETARY', 'PRESIDENT')")
public ResponseEntity<SecretaryDashboardDTO> getSecretaryDashboard() {
    // Accessible to SECRETARY and PRESIDENT
}
```

#### 3. Data Filtering by Ownership

Members can only access their own data:

```java
public DashboardStatsDTO getMemberDashboard(Long userId) {
    Member member = memberRepository.findByUserId(userId)
        .orElseThrow(() -> new AccessDeniedException("Member not found"));
    
    // Return only this member's data
    return buildMemberDashboard(member);
}
```

### Input Validation

#### 1. Request Parameter Validation

```java
@GetMapping("/charts/member-growth")
public ResponseEntity<ChartDataDTO> getMemberGrowthChart(
    @RequestParam @Pattern(regexp = "^(week|month|quarter|year)$") String period
) {
    // Period parameter validated against allowed values
}
```

#### 2. DTO Validation

```java
@Data
public class DepositRequest {
    @NotNull
    @Positive
    private BigDecimal amount;
    
    @NotNull
    private Long memberId;
    
    @NotBlank
    private String depositType;
    
    @PastOrPresent
    private LocalDate transactionDate;
}
```

### Data Protection

#### 1. Sensitive Data Masking

Sensitive information is masked in logs:

```java
log.info("Dashboard accessed by user: {}", maskUsername(username));
```

#### 2. SQL Injection Prevention

All queries use parameterized statements via JPA:

```java
@Query("SELECT m FROM Member m WHERE m.name LIKE %:search%")
List<Member> searchMembers(@Param("search") String search);
```

#### 3. XSS Prevention

Frontend sanitizes user input:

```typescript
import DOMPurify from 'dompurify';

const sanitizedDescription = DOMPurify.sanitize(userInput);
```

### Audit Logging

All dashboard actions are logged:

```java
@Aspect
@Component
public class DashboardAuditAspect {
    
    @AfterReturning("@annotation(Audited)")
    public void logDashboardAccess(JoinPoint joinPoint) {
        String username = SecurityContextHolder.getContext()
            .getAuthentication().getName();
        String action = joinPoint.getSignature().getName();
        
        auditLog.info("User {} accessed dashboard action: {}", username, action);
    }
}
```

## Deployment Considerations

### Environment Configuration

Dashboard configuration varies by environment:

```yaml
# application-dev.yml
dashboard:
  refresh-interval: 30000  # 30 seconds for development
  cache-ttl: 60           # 1 minute cache
  
# application-prod.yml
dashboard:
  refresh-interval: 60000  # 60 seconds for production
  cache-ttl: 300          # 5 minute cache
```

### Database Migration

Flyway migration for dashboard-specific tables:

```sql
-- V10__Create_dashboard_cache_table.sql
CREATE TABLE dashboard_cache (
    cache_key VARCHAR(255) PRIMARY KEY,
    cache_value TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    INDEX idx_expires_at (expires_at)
);
```

### Monitoring

Dashboard-specific metrics:

```java
@Component
public class DashboardMetrics {
    
    private final MeterRegistry meterRegistry;
    
    public void recordDashboardLoad(String role, long durationMs) {
        meterRegistry.timer("dashboard.load.time", "role", role)
            .record(durationMs, TimeUnit.MILLISECONDS);
    }
    
    public void recordWidgetError(String widgetType) {
        meterRegistry.counter("dashboard.widget.error", "type", widgetType)
            .increment();
    }
}
```

## Future Enhancements

### Phase 2 Features

1. **Real-time Updates**: WebSocket integration for live dashboard updates
2. **Custom Dashboards**: Allow users to customize widget layout and visibility
3. **Advanced Analytics**: Machine learning-based forecasting and anomaly detection
4. **Mobile App**: Native mobile dashboard application
5. **Dashboard Templates**: Pre-configured dashboard templates for different use cases
6. **Widget Marketplace**: Community-contributed custom widgets
7. **Multi-language Support**: Internationalization for dashboard UI
8. **Dark Mode**: Theme switching for better accessibility

### Scalability Improvements

1. **Microservices Architecture**: Separate dashboard service for independent scaling
2. **GraphQL API**: More efficient data fetching with GraphQL
3. **Server-Side Rendering**: Improve initial load performance with SSR
4. **CDN Integration**: Serve static assets from CDN
5. **Database Read Replicas**: Separate read replicas for dashboard queries

---

**Related Documentation**:
- [Requirements Document](.kiro/specs/role-based-dashboard-system/requirements.md) - Detailed requirements
- [System Architecture](docs/architecture/system-design.md) - Overall system design
- [API Documentation](docs/api/rest-endpoints.md) - REST API reference
- [Testing Strategy](docs/reference/testing-strategy.md) - Testing approaches
