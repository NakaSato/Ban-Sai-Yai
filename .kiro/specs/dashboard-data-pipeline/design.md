# Design Document

## Overview

The Dashboard Data Pipeline provides the technical infrastructure that powers the Ban Sai Yai Savings Group dashboard system. This design addresses three critical concerns: efficient data aggregation using materialized views, comprehensive security through role-based access control and audit logging, and optimal performance for users accessing the system over rural internet connections.

The architecture leverages the existing Spring Boot backend with MariaDB database and React/TypeScript frontend. The data pipeline implements a hybrid approach where historical data is pre-aggregated in summary tables (saving_forward, loan_forward) while current data is calculated in real-time. This strategy ensures fast query performance as the transaction dataset grows while maintaining data accuracy.

Key design principles:
- **Performance-first aggregation**: Use materialized views to avoid expensive full-table scans
- **Defense-in-depth security**: Enforce RBAC at multiple layers with comprehensive audit logging
- **Progressive enhancement**: Load critical UI structure first, then fetch data asynchronously
- **Mobile-first responsive design**: Optimize for touch interfaces and slow connections
- **Graceful degradation**: Handle errors without breaking the entire dashboard

## Architecture

### High-Level Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    React Frontend Layer                     │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │  Dashboard   │  │   Widgets    │  │    AJAX      │      │
│  │  Skeleton    │  │  (Async)     │  │   Loader     │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
└─────────────────────────────────────────────────────────────┘
                          │
                          │ Progressive Data Loading
                          ▼
┌─────────────────────────────────────────────────────────────┐
│                 Spring Boot Backend Layer                   │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │    RBAC      │  │  Aggregation │  │    Audit     │      │
│  │   Filter     │  │   Service    │  │   Logger     │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
└─────────────────────────────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────────┐
│                      MariaDB Database                       │
│  transaction | saving_forward | loan_forward | system_log   │
└─────────────────────────────────────────────────────────────┘
```

### Data Aggregation Flow

```
┌─────────────────────────────────────────────────────────────┐
│                   Monthly Closing Process                   │
│                                                             │
│  1. Calculate balances from transactions                    │
│  2. Create snapshot in saving_forward                       │
│  3. Create snapshot in loan_forward                         │
│  4. Mark fiscal period as CLOSED                            │
└─────────────────────────────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────────┐
│                  Real-Time Balance Query                    │
│                                                             │
│  Current Balance = Last Month Forward                       │
│                  + Sum(Transactions Since Closing)          │
└─────────────────────────────────────────────────────────────┘
```

### Security Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    Request Flow                             │
│                                                             │
│  HTTP Request                                               │
│       ↓                                                     │
│  Spring Security Filter                                     │
│       ↓                                                     │
│  Session Validation (user.user_level)                       │
│       ↓                                                     │
│  RBAC Authorization Check                                   │
│       ↓                                                     │
│  Controller Method                                          │
│       ↓                                                     │
│  Service Layer (Business Logic)                             │
│       ↓                                                     │
│  Audit Logger (for write operations)                        │
│       ↓                                                     │
│  Database Operation                                         │
└─────────────────────────────────────────────────────────────┘
```

## Components and Interfaces

### Backend Components

#### 1. DataAggregationService

**Purpose**: Calculates current balances using the hybrid approach

**Methods**:
```java
@Service
public class DataAggregationService {
    
    /**
     * Calculate current member balance using hybrid approach
     * Formula: Last Month Forward + Sum(Transactions Since Closing)
     */
    public BigDecimal getCurrentMemberBalance(Long memberId);
    
    /**
     * Calculate current loan balance with principal and interest breakdown
     */
    public LoanBalanceDTO getCurrentLoanBalance(Long loanId);
    
    /**
     * Get last closing date for the current fiscal period
     */
    public LocalDate getLastClosingDate();
    
    /**
     * Aggregate transactions since last closing for a member
     */
    public BigDecimal sumTransactionsSinceClosing(Long memberId, LocalDate closingDate);
    
    /**
     * Create monthly forward snapshots (called during period closing)
     */
    @Transactional
    public void createMonthlyForwardSnapshots(LocalDate closingDate);
}
```

**Key Logic**:
```java
public BigDecimal getCurrentMemberBalance(Long memberId) {
    LocalDate lastClosing = getLastClosingDate();
    
    // Get last month's forward balance
    BigDecimal forwardBalance = savingForwardRepository
        .findByMemberIdAndForwardDate(memberId, lastClosing)
        .map(SavingForward::getDepositForward)
        .orElse(BigDecimal.ZERO);
    
    // Sum transactions since closing
    BigDecimal recentTransactions = transactionRepository
        .sumTransactionsSinceDate(memberId, lastClosing);
    
    return forwardBalance.add(recentTransactions);
}
```

#### 2. RBACSecurityFilter

**Purpose**: Enforce role-based access control before rendering widgets

**Implementation**:
```java
@Component
public class RBACSecurityFilter extends OncePerRequestFilter {
    
    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        
        String requestPath = request.getRequestURI();
        HttpSession session = request.getSession(false);
        
        if (session == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        
        String userLevel = (String) session.getAttribute("user.user_level");
        
        if (!hasPermission(userLevel, requestPath)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        
        filterChain.doFilter(request, response);
    }
    
    private boolean hasPermission(String userLevel, String path) {
        // Check if user role has permission for requested resource
        return permissionMatrix.get(userLevel).contains(path);
    }
}
```

#### 3. AuditLoggingService

**Purpose**: Log all write operations to system_log table

**Methods**:
```java
@Service
public class AuditLoggingService {
    
    /**
     * Log a write operation with full context
     */
    @Async
    public void logWriteOperation(
        Long userId,
        String actionType,
        String oldValue,
        String newValue,
        String ipAddress
    );
    
    /**
     * Get recent audit log entries for President dashboard
     */
    public List<AuditLogDTO> getRecentAuditLogs(int limit);
    
    /**
     * Get audit logs filtered by user, action type, or date range
     */
    public List<AuditLogDTO> getAuditLogs(AuditLogFilter filter);
}
```

**Audit Log Capture**:
```java
@Aspect
@Component
public class AuditLoggingAspect {
    
    @Autowired
    private AuditLoggingService auditLoggingService;
    
    @AfterReturning(
        pointcut = "@annotation(Auditable)",
        returning = "result"
    )
    public void logAuditableOperation(JoinPoint joinPoint, Object result) {
        HttpServletRequest request = 
            ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
            .getRequest();
        
        Long userId = getCurrentUserId();
        String ipAddress = request.getRemoteAddr();
        String actionType = joinPoint.getSignature().getName();
        
        // Extract old and new values from method arguments and result
        String oldValue = extractOldValue(joinPoint.getArgs());
        String newValue = extractNewValue(result);
        
        auditLoggingService.logWriteOperation(
            userId, actionType, oldValue, newValue, ipAddress
        );
    }
}
```

#### 4. ProgressiveDataController

**Purpose**: Provide endpoints for asynchronous widget data loading

**Endpoints**:
```java
@RestController
@RequestMapping("/api/dashboard/data")
public class ProgressiveDataController {
    
    /**
     * Get critical widget data (loaded first)
     */
    @GetMapping("/critical")
    public ResponseEntity<CriticalDataDTO> getCriticalData(
        @RequestParam String role
    );
    
    /**
     * Get chart data (loaded after critical data)
     */
    @GetMapping("/charts/{chartType}")
    public ResponseEntity<ChartDataDTO> getChartData(
        @PathVariable String chartType,
        @RequestParam(required = false) String period
    );
    
    /**
     * Get non-critical widget data (loaded last)
     */
    @GetMapping("/secondary")
    public ResponseEntity<SecondaryDataDTO> getSecondaryData(
        @RequestParam String role
    );
}
```

#### 5. ErrorRecoveryService

**Purpose**: Handle database errors and provide fallback mechanisms

**Methods**:
```java
@Service
public class ErrorRecoveryService {
    
    /**
     * Attempt to calculate balance from raw transactions if forward table fails
     */
    public BigDecimal calculateBalanceFromRawTransactions(Long memberId);
    
    /**
     * Check data consistency between forward tables and transactions
     */
    public DataConsistencyReport checkDataConsistency();
    
    /**
     * Alert administrators of data inconsistencies
     */
    public void alertAdministrators(String errorMessage);
}
```

### Frontend Components

#### 1. DashboardSkeleton Component

**Purpose**: Render initial HTML structure immediately

**Props**:
```typescript
interface DashboardSkeletonProps {
  role: UserRole;
}
```

**Implementation**:
```typescript
export const DashboardSkeleton: React.FC<DashboardSkeletonProps> = ({ role }) => {
  const widgetLayout = getWidgetLayoutForRole(role);
  
  return (
    <Grid container spacing={3}>
      {widgetLayout.map((widget) => (
        <Grid item xs={12} md={6} lg={3} key={widget.id}>
          <Card>
            <CardContent>
              <Skeleton variant="text" width="60%" />
              <Skeleton variant="rectangular" height={100} />
            </CardContent>
          </Card>
        </Grid>
      ))}
    </Grid>
  );
};
```

#### 2. AsyncWidgetLoader Component

**Purpose**: Load widget data asynchronously with loading indicators

**Props**:
```typescript
interface AsyncWidgetLoaderProps {
  widgetId: string;
  priority: 'critical' | 'normal' | 'low';
  endpoint: string;
  renderWidget: (data: any) => React.ReactNode;
}
```

**Implementation**:
```typescript
export const AsyncWidgetLoader: React.FC<AsyncWidgetLoaderProps> = ({
  widgetId,
  priority,
  endpoint,
  renderWidget
}) => {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  
  useEffect(() => {
    const delay = priority === 'critical' ? 0 : 
                  priority === 'normal' ? 500 : 1000;
    
    const timer = setTimeout(() => {
      fetchWidgetData();
    }, delay);
    
    return () => clearTimeout(timer);
  }, []);
  
  const fetchWidgetData = async () => {
    try {
      const response = await fetch(endpoint);
      if (!response.ok) throw new Error('Failed to load widget');
      const json = await response.json();
      setData(json);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };
  
  if (loading) {
    return <CircularProgress />;
  }
  
  if (error) {
    return (
      <Alert severity="error">
        {error}
        <Button onClick={fetchWidgetData}>Retry</Button>
      </Alert>
    );
  }
  
  return renderWidget(data);
};
```

#### 3. ResponsiveWidgetGrid Component

**Purpose**: Implement Bootstrap-style responsive grid with breakpoints

**Props**:
```typescript
interface ResponsiveWidgetGridProps {
  widgets: WidgetConfig[];
}

interface WidgetConfig {
  id: string;
  component: React.ComponentType;
  breakpoints: {
    xs: number;  // Mobile (< 768px)
    md: number;  // Tablet (768px - 991px)
    lg: number;  // Desktop (>= 992px)
  };
}
```

**Implementation**:
```typescript
export const ResponsiveWidgetGrid: React.FC<ResponsiveWidgetGridProps> = ({ widgets }) => {
  return (
    <Grid container spacing={2}>
      {widgets.map((widget) => (
        <Grid 
          item 
          xs={widget.breakpoints.xs}
          md={widget.breakpoints.md}
          lg={widget.breakpoints.lg}
          key={widget.id}
        >
          <widget.component />
        </Grid>
      ))}
    </Grid>
  );
};
```

#### 4. TouchOptimizedButton Component

**Purpose**: Ensure all interactive elements meet 44px minimum touch target

**Props**:
```typescript
interface TouchOptimizedButtonProps extends ButtonProps {
  children: React.ReactNode;
}
```

**Implementation**:
```typescript
export const TouchOptimizedButton: React.FC<TouchOptimizedButtonProps> = ({
  children,
  ...props
}) => {
  return (
    <Button
      {...props}
      sx={{
        minHeight: '44px',
        minWidth: '44px',
        padding: '12px 16px',
        ...props.sx
      }}
    >
      {children}
    </Button>
  );
};
```

## Data Models

### Backend Entities

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
    
    @Column(name = "share_fwd", precision = 15, scale = 2)
    private BigDecimal shareForward;
    
    @Column(name = "deposit_fwd", precision = 15, scale = 2)
    private BigDecimal depositForward;
    
    @Column(name = "forward_date", nullable = false)
    private LocalDate forwardDate;
    
    // Getters and setters
}
```

#### LoanBalance Entity (loan_forward)

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
    
    // Getters and setters
}
```

#### SystemLog Entity

```java
@Entity
@Table(name = "system_log")
public class SystemLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long logId;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(nullable = false)
    private LocalDateTime timestamp;
    
    @Column(name = "ip_address", length = 45)
    private String ipAddress;
    
    @Column(name = "action_type", nullable = false, length = 100)
    private String actionType;
    
    @Column(name = "old_value", columnDefinition = "TEXT")
    private String oldValue;
    
    @Column(name = "new_value", columnDefinition = "TEXT")
    private String newValue;
    
    // Getters and setters
}
```

### DTOs

```java
// Balance Calculation DTO
@Data
public class CurrentBalanceDTO {
    private Long memberId;
    private BigDecimal forwardBalance;
    private BigDecimal recentTransactions;
    private BigDecimal currentBalance;
    private LocalDate lastClosingDate;
    private LocalDate calculatedAt;
}

// Audit Log DTO
@Data
public class AuditLogDTO {
    private Long logId;
    private String username;
    private LocalDateTime timestamp;
    private String ipAddress;
    private String actionType;
    private String oldValue;
    private String newValue;
}

// Data Consistency Report DTO
@Data
public class DataConsistencyReport {
    private boolean isConsistent;
    private List<String> inconsistencies;
    private LocalDateTime checkedAt;
}

// Progressive Loading DTOs
@Data
public class CriticalDataDTO {
    private BigDecimal cashBalance;
    private Integer activeMemberCount;
    private Integer pendingTransactionCount;
}

@Data
public class SecondaryDataDTO {
    private List<RecentActivityDTO> recentActivities;
    private Map<String, BigDecimal> monthlyTotals;
}
```

### Frontend TypeScript Interfaces

```typescript
// Widget Loading State
interface WidgetLoadingState {
  widgetId: string;
  status: 'idle' | 'loading' | 'success' | 'error';
  data: any | null;
  error: string | null;
  lastFetched: Date | null;
}

// Responsive Breakpoint Config
interface BreakpointConfig {
  xs: number;  // col-12 on mobile
  md: number;  // col-6 on tablet
  lg: number;  // col-3 on desktop
}

// Audit Log Entry
interface AuditLogEntry {
  logId: number;
  username: string;
  timestamp: string;
  ipAddress: string;
  actionType: string;
  oldValue: string;
  newValue: string;
}

// Balance Calculation
interface BalanceCalculation {
  memberId: number;
  forwardBalance: number;
  recentTransactions: number;
  currentBalance: number;
  lastClosingDate: string;
}
```

## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system-essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

### Property 1: Balance calculation formula

*For any* member with a forward balance F and a set of transactions T since the last closing date, the current balance should equal F + Σ(T)

**Validates: Requirements 1.1, 6.1**

### Property 2: Historical data uses summary tables

*For any* historical balance query for a date D that is before the last closing date, the system should query the summary table (saving_forward or loan_forward) rather than the raw transaction table

**Validates: Requirements 1.2, 1.5**

### Property 3: Monthly closing creates forward records

*For any* monthly closing operation on date D, the system should create new forward records in both saving_forward and loan_forward tables for all active members and loans

**Validates: Requirements 1.4**

### Property 4: Permission verification before rendering

*For any* user with role R requesting a widget W, the system should verify that R has permission to access W before rendering any data

**Validates: Requirements 2.1**

### Property 5: Role-based data filtering

*For any* widget response for a user with role R, the data elements included should only be those authorized for role R

**Validates: Requirements 2.4**

### Property 6: URL manipulation protection

*For any* dashboard URL request with role R, if R does not have permission for that dashboard, the system should return HTTP 403 and block access

**Validates: Requirements 2.5**

### Property 7: Audit log creation for write operations

*For any* write operation (deposit, loan approval, withdrawal, etc.), the system should create an audit log entry containing user ID, timestamp, IP address, action type, old value, and new value

**Validates: Requirements 3.1, 3.2, 3.3, 3.5**

### Property 8: Audit log chronological ordering

*For any* set of audit log entries, when displayed in the System Activity widget, they should be ordered by timestamp in descending order (most recent first)

**Validates: Requirements 3.4**

### Property 9: Mobile viewport full-width widgets

*For any* viewport width W where W < 768 pixels, critical widgets (Cash Box, Member Search) should have Bootstrap class col-12 applied

**Validates: Requirements 4.1, 4.4**

### Property 10: Desktop viewport multi-column layout

*For any* viewport width W where W >= 992 pixels, widgets should have Bootstrap class col-lg-3 or similar multi-column classes applied

**Validates: Requirements 4.2, 4.5**

### Property 11: Touch target minimum size

*For any* interactive element (button, dropdown, link), the minimum height should be 44 pixels or greater

**Validates: Requirements 4.3**

### Property 12: Skeleton renders before data

*For any* dashboard page request, the HTML skeleton structure should render synchronously before any asynchronous data fetching begins

**Validates: Requirements 5.1**

### Property 13: Asynchronous widget data loading

*For any* widget on the dashboard, data should be fetched via AJAX after the skeleton renders, not during initial page load

**Validates: Requirements 5.2**

### Property 14: Chart data JSON format

*For any* chart data request, the response content-type should be application/json and contain structured data, not rendered HTML

**Validates: Requirements 5.3**

### Property 15: Priority-based loading order

*For any* set of widgets with different priority levels (critical, normal, low), critical widgets should initiate data loading before non-critical widgets

**Validates: Requirements 5.4**

### Property 16: Loading indicator visibility

*For any* widget in a loading state (data not yet fetched), a loading indicator should be visible to the user

**Validates: Requirements 5.5**

### Property 17: Multiple widget updates on related data change

*For any* loan payment transaction, both the loan balance widget and the cash box widget should reflect the updated values

**Validates: Requirements 7.3**

### Property 18: Currency precision

*For any* currency calculation result, the value should be rounded to exactly 2 decimal places

**Validates: Requirements 7.4**

### Property 19: Balance display completeness

*For any* member balance display, both share capital and deposit amounts should be included as separate fields

**Validates: Requirements 7.5**

### Property 20: Error message display on database failure

*For any* database query failure for a widget, the widget should display a user-friendly error message instead of crashing

**Validates: Requirements 8.1**

### Property 21: Fallback to raw transactions

*For any* balance calculation where forward table data is missing, the system should fall back to calculating the balance from raw transaction records

**Validates: Requirements 8.2**

### Property 22: Timeout error handling

*For any* data loading timeout, the system should log the error and display a retry option to the user

**Validates: Requirements 8.3**

### Property 23: Administrator alert on inconsistency

*For any* detected data inconsistency, the system should create an alert record in the audit log visible to administrators

**Validates: Requirements 8.4**

### Property 24: Widget error isolation

*For any* widget that fails to load, all other widgets on the same dashboard should continue to render and function normally

**Validates: Requirements 8.5**



## Error Handling

### Backend Error Handling

#### 1. Database Query Failures

**Strategy**: Implement fallback mechanisms and graceful degradation

```java
@Service
public class DataAggregationService {
    
    public BigDecimal getCurrentMemberBalance(Long memberId) {
        try {
            // Try to use forward table first
            return calculateBalanceFromForward(memberId);
        } catch (DataAccessException e) {
            log.warn("Forward table query failed, falling back to raw transactions", e);
            try {
                // Fallback to raw transaction calculation
                return calculateBalanceFromRawTransactions(memberId);
            } catch (DataAccessException fallbackError) {
                log.error("Both forward and raw transaction queries failed", fallbackError);
                throw new BalanceCalculationException(
                    "Unable to calculate balance for member " + memberId
                );
            }
        }
    }
}
```

#### 2. Missing Forward Data

**Strategy**: Detect missing data and calculate on-the-fly

```java
public BigDecimal calculateBalanceFromForward(Long memberId) {
    LocalDate lastClosing = getLastClosingDate();
    
    Optional<SavingForward> forward = savingForwardRepository
        .findByMemberIdAndForwardDate(memberId, lastClosing);
    
    if (forward.isEmpty()) {
        log.warn("No forward record found for member {} on {}, calculating from scratch", 
                 memberId, lastClosing);
        return calculateBalanceFromRawTransactions(memberId);
    }
    
    BigDecimal forwardBalance = forward.get().getDepositForward();
    BigDecimal recentTransactions = sumTransactionsSinceClosing(memberId, lastClosing);
    
    return forwardBalance.add(recentTransactions);
}
```

#### 3. Data Inconsistency Detection

**Strategy**: Validate data integrity and alert administrators

```java
@Service
public class DataConsistencyService {
    
    @Scheduled(cron = "0 0 2 * * *") // Run at 2 AM daily
    public void checkDataConsistency() {
        List<String> inconsistencies = new ArrayList<>();
        
        // Check if forward balances match calculated balances
        List<Member> activeMembers = memberRepository.findByStatus("ACTIVE");
        
        for (Member member : activeMembers) {
            BigDecimal forwardBalance = getForwardBalance(member.getId());
            BigDecimal calculatedBalance = calculateBalanceFromRawTransactions(member.getId());
            
            if (forwardBalance.compareTo(calculatedBalance) != 0) {
                String message = String.format(
                    "Balance mismatch for member %d: forward=%s, calculated=%s",
                    member.getId(), forwardBalance, calculatedBalance
                );
                inconsistencies.add(message);
                log.error(message);
            }
        }
        
        if (!inconsistencies.isEmpty()) {
            alertAdministrators(inconsistencies);
        }
    }
    
    private void alertAdministrators(List<String> inconsistencies) {
        auditLoggingService.logSystemAlert(
            "DATA_INCONSISTENCY",
            String.join("\n", inconsistencies)
        );
    }
}
```

#### 4. Timeout Handling

**Strategy**: Set reasonable timeouts and provide retry mechanisms

```java
@Configuration
public class DatabaseConfig {
    
    @Bean
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(username);
        config.setPassword(password);
        
        // Set connection timeout to 10 seconds
        config.setConnectionTimeout(10000);
        
        // Set query timeout to 30 seconds
        config.setValidationTimeout(30000);
        
        return new HikariDataSource(config);
    }
}
```

### Frontend Error Handling

#### 1. Widget Error Boundaries

**Strategy**: Isolate widget errors to prevent dashboard-wide failures

```typescript
class WidgetErrorBoundary extends React.Component<Props, State> {
  state = { hasError: false, error: null };
  
  static getDerivedStateFromError(error: Error) {
    return { hasError: true, error };
  }
  
  componentDidCatch(error: Error, errorInfo: React.ErrorInfo) {
    console.error('Widget error:', error, errorInfo);
    // Log to error tracking service
  }
  
  handleRetry = () => {
    this.setState({ hasError: false, error: null });
  };
  
  render() {
    if (this.state.hasError) {
      return (
        <Card>
          <CardContent>
            <Alert severity="error">
              <AlertTitle>Widget Error</AlertTitle>
              Failed to load widget data.
              <Button onClick={this.handleRetry} sx={{ mt: 1 }}>
                Retry
              </Button>
            </Alert>
          </CardContent>
        </Card>
      );
    }
    
    return this.props.children;
  }
}
```

#### 2. Network Error Handling

**Strategy**: Implement retry logic with exponential backoff

```typescript
async function fetchWithRetry(
  url: string,
  options: RequestInit = {},
  maxRetries: number = 3
): Promise<Response> {
  let lastError: Error;
  
  for (let attempt = 0; attempt < maxRetries; attempt++) {
    try {
      const response = await fetch(url, options);
      
      if (!response.ok) {
        throw new Error(`HTTP ${response.status}: ${response.statusText}`);
      }
      
      return response;
    } catch (error) {
      lastError = error as Error;
      
      if (attempt < maxRetries - 1) {
        // Exponential backoff: 1s, 2s, 4s
        const delay = Math.pow(2, attempt) * 1000;
        await new Promise(resolve => setTimeout(resolve, delay));
      }
    }
  }
  
  throw lastError!;
}
```

#### 3. Permission Denied Handling

**Strategy**: Redirect to appropriate dashboard or show access denied message

```typescript
async function fetchWidgetData(widgetId: string): Promise<any> {
  try {
    const response = await fetch(`/api/dashboard/data/${widgetId}`);
    
    if (response.status === 403) {
      // Permission denied
      showAccessDeniedMessage();
      return null;
    }
    
    if (response.status === 401) {
      // Session expired
      redirectToLogin();
      return null;
    }
    
    return await response.json();
  } catch (error) {
    console.error('Failed to fetch widget data:', error);
    throw error;
  }
}
```

## Testing Strategy

### Dual Testing Approach

The dashboard data pipeline will employ both unit testing and property-based testing to ensure comprehensive correctness:

- **Unit tests** verify specific examples, edge cases, and integration points between components
- **Property tests** verify universal properties that should hold across all inputs
- Together they provide comprehensive coverage: unit tests catch concrete bugs, property tests verify general correctness

### Unit Testing

#### Backend Unit Tests (JUnit + Mockito)

**Test Coverage**:
- Balance calculation with known inputs
- Forward table creation during monthly closing
- RBAC filter behavior with specific roles
- Audit log entry creation
- Error handling and fallback mechanisms
- Data consistency checks

**Example Unit Tests**:
```java
@ExtendWith(MockitoExtension.class)
class DataAggregationServiceTest {
    
    @Mock
    private SavingForwardRepository savingForwardRepository;
    
    @Mock
    private TransactionRepository transactionRepository;
    
    @InjectMocks
    private DataAggregationService dataAggregationService;
    
    @Test
    void shouldCalculateBalanceUsingForwardPlusTransactions() {
        Long memberId = 1L;
        LocalDate closingDate = LocalDate.of(2023, 8, 31);
        BigDecimal forwardBalance = new BigDecimal("10000.00");
        BigDecimal recentTransactions = new BigDecimal("500.00");
        
        SavingForward forward = new SavingForward();
        forward.setDepositForward(forwardBalance);
        
        when(savingForwardRepository.findByMemberIdAndForwardDate(memberId, closingDate))
            .thenReturn(Optional.of(forward));
        when(transactionRepository.sumTransactionsSinceDate(memberId, closingDate))
            .thenReturn(recentTransactions);
        
        BigDecimal result = dataAggregationService.getCurrentMemberBalance(memberId);
        
        assertEquals(new BigDecimal("10500.00"), result);
    }
    
    @Test
    void shouldFallbackToRawTransactionsWhenForwardMissing() {
        Long memberId = 1L;
        LocalDate closingDate = LocalDate.of(2023, 8, 31);
        BigDecimal rawBalance = new BigDecimal("8000.00");
        
        when(savingForwardRepository.findByMemberIdAndForwardDate(memberId, closingDate))
            .thenReturn(Optional.empty());
        when(transactionRepository.calculateBalanceFromAllTransactions(memberId))
            .thenReturn(rawBalance);
        
        BigDecimal result = dataAggregationService.getCurrentMemberBalance(memberId);
        
        assertEquals(rawBalance, result);
    }
    
    @Test
    void shouldCreateForwardRecordsForAllActiveMembers() {
        LocalDate closingDate = LocalDate.of(2023, 9, 30);
        List<Member> activeMembers = Arrays.asList(
            createMember(1L), createMember(2L), createMember(3L)
        );
        
        when(memberRepository.findByStatus("ACTIVE")).thenReturn(activeMembers);
        
        dataAggregationService.createMonthlyForwardSnapshots(closingDate);
        
        verify(savingForwardRepository, times(3)).save(any(SavingForward.class));
        verify(loanForwardRepository, atLeastOnce()).save(any(LoanBalance.class));
    }
}
```

#### Frontend Unit Tests (Jest + React Testing Library)

**Test Coverage**:
- Skeleton rendering before data load
- Async widget data fetching
- Loading indicator display
- Error boundary behavior
- Responsive grid layout
- Touch target sizing

**Example Unit Tests**:
```typescript
describe('AsyncWidgetLoader', () => {
  it('should display loading indicator while fetching data', () => {
    const { getByRole } = render(
      <AsyncWidgetLoader
        widgetId="cash-box"
        priority="critical"
        endpoint="/api/dashboard/data/cash-box"
        renderWidget={(data) => <div>{data.value}</div>}
      />
    );
    
    expect(getByRole('progressbar')).toBeInTheDocument();
  });
  
  it('should render widget after data loads successfully', async () => {
    server.use(
      rest.get('/api/dashboard/data/cash-box', (req, res, ctx) => {
        return res(ctx.json({ value: '10000.00' }));
      })
    );
    
    const { findByText } = render(
      <AsyncWidgetLoader
        widgetId="cash-box"
        priority="critical"
        endpoint="/api/dashboard/data/cash-box"
        renderWidget={(data) => <div>{data.value}</div>}
      />
    );
    
    expect(await findByText('10000.00')).toBeInTheDocument();
  });
  
  it('should display error message and retry button on failure', async () => {
    server.use(
      rest.get('/api/dashboard/data/cash-box', (req, res, ctx) => {
        return res(ctx.status(500));
      })
    );
    
    const { findByText, getByText } = render(
      <AsyncWidgetLoader
        widgetId="cash-box"
        priority="critical"
        endpoint="/api/dashboard/data/cash-box"
        renderWidget={(data) => <div>{data.value}</div>}
      />
    );
    
    expect(await findByText(/Failed to load widget/i)).toBeInTheDocument();
    expect(getByText('Retry')).toBeInTheDocument();
  });
});

describe('ResponsiveWidgetGrid', () => {
  it('should apply col-12 class on mobile viewport', () => {
    global.innerWidth = 375;
    
    const widgets = [
      {
        id: 'cash-box',
        component: () => <div>Cash Box</div>,
        breakpoints: { xs: 12, md: 6, lg: 3 }
      }
    ];
    
    const { container } = render(<ResponsiveWidgetGrid widgets={widgets} />);
    const gridItem = container.querySelector('[data-widget-id="cash-box"]');
    
    expect(gridItem).toHaveClass('MuiGrid-grid-xs-12');
  });
  
  it('should apply col-lg-3 class on desktop viewport', () => {
    global.innerWidth = 1920;
    
    const widgets = [
      {
        id: 'cash-box',
        component: () => <div>Cash Box</div>,
        breakpoints: { xs: 12, md: 6, lg: 3 }
      }
    ];
    
    const { container } = render(<ResponsiveWidgetGrid widgets={widgets} />);
    const gridItem = container.querySelector('[data-widget-id="cash-box"]');
    
    expect(gridItem).toHaveClass('MuiGrid-grid-lg-3');
  });
});
```

### Property-Based Testing

#### Property Testing Library

**Frontend**: Use **fast-check** for JavaScript/TypeScript property-based testing
**Backend**: Use **jqwik** for Java property-based testing

#### Configuration

Each property-based test should run a minimum of 100 iterations to ensure thorough coverage of the input space.

#### Property Test Implementation

Each property-based test MUST be tagged with a comment explicitly referencing the correctness property in the design document using this exact format:

```
**Feature: dashboard-data-pipeline, Property {number}: {property_text}**
```

**Backend Property Tests (jqwik)**:

```java
class DataAggregationPropertyTest {
    
    @Provide
    Arbitrary<Member> members() {
        return Arbitraries.integers().between(1, 1000)
            .map(id -> createMember(id.longValue()));
    }
    
    @Provide
    Arbitrary<BigDecimal> balances() {
        return Arbitraries.bigDecimals()
            .between(BigDecimal.ZERO, new BigDecimal("1000000.00"))
            .ofScale(2);
    }
    
    @Provide
    Arbitrary<List<Transaction>> transactions() {
        return Arbitraries.integers().between(0, 100)
            .flatMap(count -> 
                Arbitraries.of(createTransaction())
                    .list().ofSize(count)
            );
    }
    
    /**
     * Feature: dashboard-data-pipeline, Property 1: Balance calculation formula
     */
    @Property
    void currentBalanceShouldEqualForwardPlusTransactions(
        @ForAll("members") Member member,
        @ForAll("balances") BigDecimal forwardBalance,
        @ForAll("transactions") List<Transaction> transactions
    ) {
        // Setup
        LocalDate closingDate = LocalDate.now().minusMonths(1);
        savingForwardRepository.save(
            createForward(member.getId(), forwardBalance, closingDate)
        );
        transactions.forEach(t -> {
            t.setMemberId(member.getId());
            t.setTransDate(closingDate.plusDays(1));
            transactionRepository.save(t);
        });
        
        // Calculate expected
        BigDecimal transactionSum = transactions.stream()
            .map(Transaction::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal expected = forwardBalance.add(transactionSum);
        
        // Test
        BigDecimal actual = dataAggregationService.getCurrentMemberBalance(member.getId());
        
        assertEquals(expected, actual);
    }
    
    /**
     * Feature: dashboard-data-pipeline, Property 3: Monthly closing creates forward records
     */
    @Property
    void monthlyClosingShouldCreateForwardRecordsForAllActiveMembers(
        @ForAll("members") List<Member> members
    ) {
        // Setup
        members.forEach(m -> {
            m.setStatus("ACTIVE");
            memberRepository.save(m);
        });
        
        LocalDate closingDate = LocalDate.now();
        
        // Execute
        dataAggregationService.createMonthlyForwardSnapshots(closingDate);
        
        // Verify
        for (Member member : members) {
            Optional<SavingForward> forward = savingForwardRepository
                .findByMemberIdAndForwardDate(member.getId(), closingDate);
            assertTrue(forward.isPresent(), 
                "Forward record should exist for member " + member.getId());
        }
    }
    
    /**
     * Feature: dashboard-data-pipeline, Property 18: Currency precision
     */
    @Property
    void allCurrencyCalculationsShouldHaveTwoDecimalPlaces(
        @ForAll("balances") BigDecimal amount1,
        @ForAll("balances") BigDecimal amount2
    ) {
        BigDecimal result = amount1.add(amount2);
        
        assertEquals(2, result.scale());
    }
    
    /**
     * Feature: dashboard-data-pipeline, Property 21: Fallback to raw transactions
     */
    @Property
    void shouldFallbackToRawTransactionsWhenForwardMissing(
        @ForAll("members") Member member,
        @ForAll("transactions") List<Transaction> transactions
    ) {
        // Setup: No forward record exists
        LocalDate closingDate = LocalDate.now().minusMonths(1);
        transactions.forEach(t -> {
            t.setMemberId(member.getId());
            transactionRepository.save(t);
        });
        
        // Calculate expected from raw transactions
        BigDecimal expected = transactions.stream()
            .map(Transaction::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Test
        BigDecimal actual = dataAggregationService.getCurrentMemberBalance(member.getId());
        
        assertEquals(expected, actual);
    }
}
```

**Frontend Property Tests (fast-check)**:

```typescript
import fc from 'fast-check';

describe('Dashboard Data Pipeline Property Tests', () => {
  /**
   * Feature: dashboard-data-pipeline, Property 9: Mobile viewport full-width widgets
   */
  it('should apply col-12 to critical widgets on mobile viewports', () => {
    fc.assert(
      fc.property(
        fc.integer({ min: 320, max: 767 }), // Mobile viewport widths
        (viewportWidth) => {
          global.innerWidth = viewportWidth;
          
          const criticalWidgets = ['cash-box', 'member-search'];
          const { container } = render(<DashboardPage role="OFFICER" />);
          
          criticalWidgets.forEach(widgetId => {
            const widget = container.querySelector(`[data-widget-id="${widgetId}"]`);
            expect(widget).toHaveClass('MuiGrid-grid-xs-12');
          });
        }
      ),
      { numRuns: 100 }
    );
  });
  
  /**
   * Feature: dashboard-data-pipeline, Property 10: Desktop viewport multi-column layout
   */
  it('should apply multi-column classes on desktop viewports', () => {
    fc.assert(
      fc.property(
        fc.integer({ min: 992, max: 2560 }), // Desktop viewport widths
        (viewportWidth) => {
          global.innerWidth = viewportWidth;
          
          const { container } = render(<DashboardPage role="OFFICER" />);
          const widgets = container.querySelectorAll('[data-widget-id]');
          
          widgets.forEach(widget => {
            const hasMultiColumn = 
              widget.classList.contains('MuiGrid-grid-lg-3') ||
              widget.classList.contains('MuiGrid-grid-lg-4') ||
              widget.classList.contains('MuiGrid-grid-lg-6');
            
            expect(hasMultiColumn).toBe(true);
          });
        }
      ),
      { numRuns: 100 }
    );
  });
  
  /**
   * Feature: dashboard-data-pipeline, Property 11: Touch target minimum size
   */
  it('should ensure all interactive elements have minimum 44px height', () => {
    fc.assert(
      fc.property(
        fc.constantFrom('OFFICER', 'SECRETARY', 'PRESIDENT', 'MEMBER'),
        (role) => {
          const { container } = render(<DashboardPage role={role} />);
          
          const interactiveElements = container.querySelectorAll(
            'button, a, input, select, [role="button"]'
          );
          
          interactiveElements.forEach(element => {
            const height = element.getBoundingClientRect().height;
            expect(height).toBeGreaterThanOrEqual(44);
          });
        }
      ),
      { numRuns: 100 }
    );
  });
  
  /**
   * Feature: dashboard-data-pipeline, Property 15: Priority-based loading order
   */
  it('should load critical widgets before non-critical widgets', () => {
    fc.assert(
      fc.property(
        fc.array(fc.record({
          id: fc.string(),
          priority: fc.constantFrom('critical', 'normal', 'low')
        }), { minLength: 3, maxLength: 10 }),
        (widgets) => {
          const loadOrder: string[] = [];
          
          const mockFetch = jest.fn((url) => {
            const widgetId = url.split('/').pop();
            loadOrder.push(widgetId);
            return Promise.resolve({ json: () => Promise.resolve({}) });
          });
          
          global.fetch = mockFetch;
          
          render(<DashboardWithWidgets widgets={widgets} />);
          
          // Wait for all fetches
          jest.runAllTimers();
          
          // Verify critical widgets loaded first
          const criticalWidgets = widgets
            .filter(w => w.priority === 'critical')
            .map(w => w.id);
          
          const firstCriticalIndex = Math.min(
            ...criticalWidgets.map(id => loadOrder.indexOf(id))
          );
          
          const firstNonCriticalIndex = Math.min(
            ...widgets
              .filter(w => w.priority !== 'critical')
              .map(w => loadOrder.indexOf(w.id))
          );
          
          expect(firstCriticalIndex).toBeLessThan(firstNonCriticalIndex);
        }
      ),
      { numRuns: 100 }
    );
  });
  
  /**
   * Feature: dashboard-data-pipeline, Property 24: Widget error isolation
   */
  it('should continue rendering other widgets when one fails', () => {
    fc.assert(
      fc.property(
        fc.integer({ min: 0, max: 9 }), // Index of widget to fail
        fc.array(fc.string(), { minLength: 5, maxLength: 10 }), // Widget IDs
        (failIndex, widgetIds) => {
          server.use(
            rest.get('/api/dashboard/data/:widgetId', (req, res, ctx) => {
              const index = widgetIds.indexOf(req.params.widgetId as string);
              if (index === failIndex) {
                return res(ctx.status(500));
              }
              return res(ctx.json({ data: 'success' }));
            })
          );
          
          const { container } = render(
            <DashboardWithWidgets widgetIds={widgetIds} />
          );
          
          // Wait for all widgets to load
          await waitFor(() => {
            const loadedWidgets = container.querySelectorAll('[data-loaded="true"]');
            expect(loadedWidgets.length).toBe(widgetIds.length - 1);
          });
          
          // Verify failed widget shows error
          const failedWidget = container.querySelector(
            `[data-widget-id="${widgetIds[failIndex]}"]`
          );
          expect(failedWidget).toHaveTextContent(/error/i);
          
          // Verify other widgets loaded successfully
          widgetIds.forEach((id, index) => {
            if (index !== failIndex) {
              const widget = container.querySelector(`[data-widget-id="${id}"]`);
              expect(widget).toHaveAttribute('data-loaded', 'true');
            }
          });
        }
      ),
      { numRuns: 100 }
    );
  });
});
```

### Integration Testing

Integration tests verify end-to-end flows including database interactions:

```java
@SpringBootTest
@AutoConfigureMockMvc
class DashboardDataPipelineIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private MemberRepository memberRepository;
    
    @Autowired
    private SavingForwardRepository savingForwardRepository;
    
    @Test
    @WithMockUser(roles = "OFFICER")
    void shouldCalculateBalanceUsingHybridApproach() throws Exception {
        // Setup test data
        Member member = createAndSaveMember();
        LocalDate closingDate = LocalDate.now().minusMonths(1);
        SavingForward forward = createForward(member.getId(), new BigDecimal("5000.00"), closingDate);
        savingForwardRepository.save(forward);
        
        // Perform request
        mockMvc.perform(get("/api/dashboard/data/balance")
                .param("memberId", member.getId().toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.currentBalance").value("5000.00"))
            .andExpect(jsonPath("$.forwardBalance").value("5000.00"));
    }
    
    @Test
    @WithMockUser(roles = "MEMBER")
    void shouldBlockAccessToOfficerWidgets() throws Exception {
        mockMvc.perform(get("/api/dashboard/data/cash-box"))
            .andExpect(status().isForbidden());
    }
    
    @Test
    @WithMockUser(roles = "OFFICER")
    void shouldLogAuditEntryForWriteOperation() throws Exception {
        mockMvc.perform(post("/api/transactions/deposit")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"memberId\": 1, \"amount\": 1000.00}"))
            .andExpect(status().isOk());
        
        // Verify audit log was created
        List<SystemLog> logs = systemLogRepository.findByActionType("DEPOSIT");
        assertFalse(logs.isEmpty());
        assertEquals("DEPOSIT", logs.get(0).getActionType());
    }
}
```

---

**Related Documentation**:
- [Database Schema](../../docs/architecture/database-schema.md) - Complete entity mappings
- [System Design](../../docs/architecture/system-design.md) - Overall architecture
- [Role-Based Dashboard System](../role-based-dashboard-system/design.md) - Dashboard component design
- [Dashboard Widgets](../dashboard-widgets/design.md) - Widget specifications
