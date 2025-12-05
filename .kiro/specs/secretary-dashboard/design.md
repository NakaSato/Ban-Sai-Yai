# Design Document

## Overview

The Secretary Dashboard is an "Accounting Cockpit" designed to provide comprehensive financial control and oversight for the Ban Sai Yai Savings Group system. This dashboard serves as the central hub for the Secretary role, enabling real-time monitoring of accounting integrity, revenue/expense analysis, liquidity management, and financial report generation. The design follows a widget-based architecture where each widget is a self-contained component responsible for a specific aspect of financial management.

The dashboard enforces strict accounting controls by preventing month-end closing when the trial balance is unbalanced and provides actionable alerts for unclassified transactions. It integrates seamlessly with the existing Spring Boot backend and React/TypeScript frontend, leveraging Material-UI components and Redux Toolkit Query for state management.

## Architecture

### High-Level Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                  Secretary Dashboard Page                   │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │ Fiscal Period│  │  Summary     │  │  End of Month│      │
│  │   Header     │  │  Metrics     │  │    Button    │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
└─────────────────────────────────────────────────────────────┘
                              │
┌─────────────────────────────────────────────────────────────┐
│                    Widget Grid Layout                       │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │ Trial Balance│  │  Revenue &   │  │  Liquidity   │      │
│  │   Monitor    │  │   Expense    │  │  Management  │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
│  ┌──────────────┐  ┌──────────────┐                        │
│  │ Unclassified │  │    Report    │                        │
│  │ Transactions │  │  Generation  │                        │
│  └──────────────┘  └──────────────┘                        │
└─────────────────────────────────────────────────────────────┘
                              │
┌─────────────────────────────────────────────────────────────┐
│                  Redux Toolkit Query Layer                  │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │ dashboardApi │  │  Caching &   │  │  Auto-Refetch│      │
│  │  Endpoints   │  │ Invalidation │  │   Polling    │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
└─────────────────────────────────────────────────────────────┘
                              │
┌─────────────────────────────────────────────────────────────┐
│                    Spring Boot Backend                      │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │  Dashboard   │  │   Service    │  │  Repository  │      │
│  │  Controller  │  │    Layer     │  │    Layer     │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
└─────────────────────────────────────────────────────────────┘
                              │
┌─────────────────────────────────────────────────────────────┐
│                      MariaDB Database                       │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │ transaction  │  │  accounting  │  │system_config │      │
│  │    table     │  │    table     │  │    table     │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
└─────────────────────────────────────────────────────────────┘
```

### Component Hierarchy

```
SecretaryDashboardPage
├── FiscalPeriodHeader
│   ├── FiscalPeriodSelector
│   └── EndOfMonthButton
├── SummaryMetricsBar
│   ├── FiscalPeriodCard
│   ├── UnclassifiedCountCard
│   ├── TrialBalanceStatusCard
│   └── LiquidAssetsCard
├── TrialBalanceWidget
│   ├── BalanceIndicator
│   ├── DebitCreditDisplay
│   ├── VarianceDisplay
│   └── ActionButtons
├── RevenueExpenseWidget
│   ├── WaterfallChart
│   ├── CategoryBreakdown
│   └── NetProfitDisplay
├── LiquidityManagementWidget
│   ├── CashBankComparison
│   ├── LiquidityAlerts
│   └── BankTransactionActions
├── UnclassifiedTransactionAlert
│   ├── CountDisplay
│   └── NavigationButton
└── ReportGenerationHub
    ├── ReportButtonGrid
    └── ReportPreviewModal
```

## Components and Interfaces

### 1. SecretaryDashboardPage Component

**Purpose**: Main container component that orchestrates all widgets and manages dashboard-level state.

**Props**: None (uses Redux state and React Router)

**State**:
```typescript
interface SecretaryDashboardState {
  selectedFiscalPeriod: string;
  isEndOfMonthModalOpen: boolean;
  refreshInterval: number;
}
```

**Key Methods**:
- `handleFiscalPeriodChange(period: string): void` - Updates selected fiscal period
- `handleEndOfMonth(): void` - Initiates month-end closing process
- `handleRefreshAll(): void` - Manually refreshes all widgets

### 2. TrialBalanceWidget Component

**Purpose**: Displays trial balance status with visual indicators for balanced/unbalanced state.

**Props**:
```typescript
interface TrialBalanceWidgetProps {
  fiscalPeriod?: string;
  autoRefresh?: boolean;
  refreshInterval?: number;
}
```

**API Integration**:
```typescript
const { data, isLoading, error, refetch } = useGetTrialBalanceQuery();

interface TrialBalanceResponse {
  totalDebits: number;
  totalCredits: number;
  variance: number;
  isBalanced: boolean;
  fiscalPeriod: string;
}
```

**Visual States**:
- **Balanced**: Green indicator, single progress bar at 100%
- **Unbalanced**: Red indicator, split progress bars showing debit/credit comparison
- **Loading**: Skeleton loader
- **Error**: Error message with retry button

### 3. RevenueExpenseWidget Component

**Purpose**: Visualizes income vs. expenses with category breakdowns and net profit calculation.

**Props**:
```typescript
interface RevenueExpenseWidgetProps {
  fiscalPeriod?: string;
  chartType?: 'waterfall' | 'stacked-bar';
}
```

**API Integration**:
```typescript
const { data } = useGetRevenueExpenseAnalysisQuery({ fiscalPeriod });

interface RevenueExpenseResponse {
  income: {
    interestOnLoans: number;
    entranceFees: number;
    fines: number;
    total: number;
  };
  expenses: {
    officeSupplies: number;
    utilities: number;
    committeeAllowances: number;
    total: number;
  };
  netProfit: number;
  fiscalPeriod: string;
}
```

**Chart Configuration**:
- Uses Recharts library for visualization
- Waterfall chart shows cumulative flow from income to expenses
- Stacked bar chart shows category comparisons
- Color coding: Green for income, Red for expenses, Blue for net profit

### 4. LiquidityManagementWidget Component

**Purpose**: Monitors cash in hand vs. bank deposits with threshold alerts.

**Props**:
```typescript
interface LiquidityManagementWidgetProps {
  cashThreshold?: number; // Default: 50000 THB
  lowCashThreshold?: number; // Default: 10000 THB
}
```

**API Integration**:
```typescript
const { data } = useGetLiquidityDataQuery();

interface LiquidityResponse {
  cashInHand: number;
  bankDeposits: number;
  totalLiquidAssets: number;
  alerts: Array<{
    type: 'HIGH_CASH' | 'LOW_CASH';
    message: string;
    threshold: number;
    currentValue: number;
  }>;
}
```

**Alert Logic**:
```typescript
if (cashInHand > cashThreshold) {
  showAlert('HIGH_CASH', 'Cash exceeds safety threshold. Consider bank deposit.');
}
if (cashInHand < lowCashThreshold) {
  showAlert('LOW_CASH', 'Low cash reserves. Consider bank withdrawal.');
}
```

### 5. ReportGenerationHub Component

**Purpose**: Provides one-click access to generate financial reports in PDF format.

**Props**:
```typescript
interface ReportGenerationHubProps {
  fiscalPeriod: string;
  isTrialBalanced: boolean;
}
```

**Report Types**:
```typescript
enum ReportType {
  TRIAL_BALANCE = 'trial-balance',
  INCOME_STATEMENT = 'income-statement',
  BALANCE_SHEET = 'balance-sheet',
}

interface ReportGenerationRequest {
  reportType: ReportType;
  fiscalPeriod: string;
  includeSignatureLines: boolean;
}
```

**API Integration**:
```typescript
const [generateReport] = useGenerateReportMutation();

const handleGenerateReport = async (reportType: ReportType) => {
  try {
    const response = await generateReport({
      reportType,
      fiscalPeriod: selectedFiscalPeriod,
      includeSignatureLines: true,
    }).unwrap();
    
    // Trigger PDF download
    downloadPDF(response.pdfUrl, response.filename);
  } catch (error) {
    showErrorNotification('Failed to generate report');
  }
};
```

### 6. EndOfMonthButton Component

**Purpose**: Initiates fiscal period closing with validation checks.

**Props**:
```typescript
interface EndOfMonthButtonProps {
  isTrialBalanced: boolean;
  fiscalPeriod: string;
  onSuccess: () => void;
}
```

**Validation Flow**:
```typescript
const handleEndOfMonth = async () => {
  // Pre-flight checks
  if (!isTrialBalanced) {
    showError('Cannot close period: Trial balance is unbalanced');
    return;
  }
  
  // Show confirmation dialog
  const confirmed = await showConfirmDialog({
    title: 'End of Month Closing',
    message: 'Are you sure you want to close the fiscal period? This action cannot be undone.',
    confirmText: 'Close Period',
    cancelText: 'Cancel',
  });
  
  if (!confirmed) return;
  
  // Execute closing
  try {
    await closeFiscalPeriod({ fiscalPeriod }).unwrap();
    showSuccess('Fiscal period closed successfully');
    onSuccess();
  } catch (error) {
    showError('Failed to close fiscal period');
  }
};
```

## Data Models

### Frontend Types

```typescript
// Dashboard Summary
interface DashboardSummary {
  fiscalPeriod: {
    month: string;
    year: number;
    status: 'OPEN' | 'CLOSED';
  };
  unclassifiedTransactionCount: number;
  trialBalanceStatus: 'BALANCED' | 'UNBALANCED';
  totalLiquidAssets: number;
}

// Trial Balance
interface TrialBalance {
  totalDebits: number;
  totalCredits: number;
  variance: number;
  isBalanced: boolean;
  fiscalPeriod: string;
  lastUpdated: string;
}

// Revenue & Expense Analysis
interface RevenueExpenseAnalysis {
  income: {
    categories: Array<{
      code: string;
      name: string;
      amount: number;
    }>;
    total: number;
  };
  expenses: {
    categories: Array<{
      code: string;
      name: string;
      amount: number;
    }>;
    total: number;
  };
  netProfit: number;
  fiscalPeriod: string;
}

// Liquidity Data
interface LiquidityData {
  cashInHand: number;
  bankDeposits: number;
  totalLiquidAssets: number;
  alerts: Array<{
    type: 'HIGH_CASH' | 'LOW_CASH';
    message: string;
    threshold: number;
    currentValue: number;
    severity: 'INFO' | 'WARNING' | 'ERROR';
  }>;
  lastUpdated: string;
}

// Report Generation
interface ReportGenerationResponse {
  reportId: string;
  reportType: string;
  fiscalPeriod: string;
  generatedAt: string;
  pdfUrl: string;
  filename: string;
  fileSize: number;
}

// Fiscal Period
interface FiscalPeriod {
  id: string;
  month: string;
  year: number;
  status: 'OPEN' | 'CLOSED';
  openedDate: string;
  closedDate?: string;
  closedBy?: string;
}
```

### Backend DTOs

```java
// Trial Balance DTO
@Data
public class TrialBalanceDTO {
    private BigDecimal totalDebits;
    private BigDecimal totalCredits;
    private BigDecimal variance;
    private Boolean isBalanced;
    private String fiscalPeriod;
    private LocalDateTime lastUpdated;
}

// Revenue Expense Analysis DTO
@Data
public class RevenueExpenseAnalysisDTO {
    private IncomeData income;
    private ExpenseData expenses;
    private BigDecimal netProfit;
    private String fiscalPeriod;
    
    @Data
    public static class IncomeData {
        private List<CategoryAmount> categories;
        private BigDecimal total;
    }
    
    @Data
    public static class ExpenseData {
        private List<CategoryAmount> categories;
        private BigDecimal total;
    }
    
    @Data
    public static class CategoryAmount {
        private String code;
        private String name;
        private BigDecimal amount;
    }
}

// Liquidity Data DTO
@Data
public class LiquidityDTO {
    private BigDecimal cashInHand;
    private BigDecimal bankDeposits;
    private BigDecimal totalLiquidAssets;
    private List<LiquidityAlert> alerts;
    private LocalDateTime lastUpdated;
    
    @Data
    public static class LiquidityAlert {
        private AlertType type;
        private String message;
        private BigDecimal threshold;
        private BigDecimal currentValue;
        private AlertSeverity severity;
    }
    
    public enum AlertType {
        HIGH_CASH, LOW_CASH
    }
    
    public enum AlertSeverity {
        INFO, WARNING, ERROR
    }
}

// Report Generation Request DTO
@Data
public class ReportGenerationRequest {
    @NotNull
    private ReportType reportType;
    
    @NotNull
    private String fiscalPeriod;
    
    private Boolean includeSignatureLines = true;
    
    public enum ReportType {
        TRIAL_BALANCE,
        INCOME_STATEMENT,
        BALANCE_SHEET
    }
}

// Report Generation Response DTO
@Data
public class ReportGenerationResponse {
    private String reportId;
    private String reportType;
    private String fiscalPeriod;
    private LocalDateTime generatedAt;
    private String pdfUrl;
    private String filename;
    private Long fileSize;
}

// Fiscal Period Closing Request DTO
@Data
public class FiscalPeriodClosingRequest {
    @NotNull
    private String fiscalPeriod;
    
    private String closedBy;
    private String notes;
}
```

## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system-essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

### Property 1: Trial Balance Calculation Accuracy
*For any* set of transactions in the active fiscal period, the sum of all debit amounts should equal the calculated total debits, and the sum of all credit amounts should equal the calculated total credits.
**Validates: Requirements 1.1, 1.2**

### Property 2: Trial Balance Variance Calculation
*For any* trial balance state, the variance should equal the absolute difference between total debits and total credits, and isBalanced should be true if and only if variance equals zero.
**Validates: Requirements 1.4, 1.5**

### Property 3: Month-End Closing Prevention
*For any* trial balance state where isBalanced is false, the End of Month button should be disabled and the closing operation should be rejected if attempted.
**Validates: Requirements 1.6, 8.3**

### Property 4: Trial Balance Display Completeness
*For any* trial balance data, the widget display should contain total debits, total credits, and variance values.
**Validates: Requirements 1.8**

### Property 5: Income Account Filtering
*For any* set of transactions, when aggregating income, only transactions linked to account codes in the 4xxx range should be included in the income total.
**Validates: Requirements 2.1**

### Property 6: Expense Account Filtering
*For any* set of transactions, when aggregating expenses, only transactions linked to account codes in the 5xxx range should be included in the expense total.
**Validates: Requirements 2.2**

### Property 7: Net Profit Calculation
*For any* revenue and expense values, the calculated net profit should equal total income minus total expenses.
**Validates: Requirements 2.6**

### Property 8: Negative Profit Warning
*For any* revenue and expense analysis where net profit is negative, the widget should display a warning indicator.
**Validates: Requirements 2.7**

### Property 9: Total Liquid Assets Calculation
*For any* cash in hand and bank deposit values, the total liquid assets should equal their sum.
**Validates: Requirements 3.8**

### Property 10: Report Metadata Completeness
*For any* generated financial report, the PDF should contain the fiscal period, generation date, and signature lines.
**Validates: Requirements 4.6**

### Property 11: Report Generation Blocking
*For any* trial balance state where isBalanced is false, all report generation buttons should be disabled and display an explanatory message.
**Validates: Requirements 4.8**

### Property 12: Summary Metrics Display
*For any* dashboard state, summary cards should display the count of unclassified transactions, trial balance status, and total liquid assets.
**Validates: Requirements 5.2, 5.3, 5.4**

### Property 13: Problem Metric Highlighting
*For any* summary metric that indicates a problem (unbalanced trial balance, unclassified transactions > 0), the corresponding card should be highlighted with warning or error styling.
**Validates: Requirements 5.5**

### Property 14: Imbalance Action Button Display
*For any* trial balance state where isBalanced is false, the trial balance widget should display a "View Journal Entries" button.
**Validates: Requirements 6.1**

### Property 15: API Polling Interval
*For any* dashboard session, API polling should occur at 30-second intervals to check for data updates.
**Validates: Requirements 7.1**

### Property 16: Stale Data Indicator
*For any* widget where the backend API is unavailable or returns an error, a "Data may be stale" indicator should be displayed.
**Validates: Requirements 7.5**

### Property 17: End of Month Button State
*For any* trial balance state, the End of Month button should be enabled if and only if isBalanced is true.
**Validates: Requirements 8.2, 8.3**

### Property 18: Fiscal Period Status Update
*For any* successful End of Month operation, the fiscal period status in the database should be updated to "Closed".
**Validates: Requirements 8.5**

### Property 19: Transaction Posting Prevention
*For any* fiscal period with status "Closed", all transaction posting operations by Officers should be rejected.
**Validates: Requirements 8.6**

### Property 20: Fiscal Period Filter Application
*For any* fiscal period selection, all dashboard widgets should reload with data filtered to the selected period.
**Validates: Requirements 9.2**

### Property 21: Closed Period Interaction Restriction
*For any* closed fiscal period being viewed, all action buttons for journal entries and report generation should be disabled.
**Validates: Requirements 9.4**

### Property 22: Widget Error Display
*For any* widget that fails to load data, an error message should be displayed within the widget container.
**Validates: Requirements 10.1**

### Property 23: Error Notification Display
*For any* failed operation (report generation, journal entry submission), a notification with the error message should be displayed.
**Validates: Requirements 10.2, 10.3**

### Property 24: Error Logging
*For any* backend API error, the error details should be logged to the browser console.
**Validates: Requirements 10.4**

### Property 25: Error Recovery UI
*For any* network error, a retry button should be displayed allowing the user to attempt the operation again.
**Validates: Requirements 10.5**



## Error Handling

### Frontend Error Handling Strategy

**Widget-Level Error Boundaries**:
```typescript
class WidgetErrorBoundary extends React.Component<Props, State> {
  componentDidCatch(error: Error, errorInfo: React.ErrorInfo) {
    console.error('Widget error:', error, errorInfo);
    logErrorToService(error, errorInfo);
  }
  
  render() {
    if (this.state.hasError) {
      return (
        <WidgetErrorDisplay
          message="Failed to load widget"
          onRetry={() => this.setState({ hasError: false })}
        />
      );
    }
    return this.props.children;
  }
}
```

**API Error Handling**:
```typescript
// RTK Query error handling
const { data, error, isLoading } = useGetTrialBalanceQuery();

if (error) {
  if ('status' in error) {
    // HTTP error
    const statusCode = error.status;
    if (statusCode === 403) {
      return <AccessDeniedMessage />;
    } else if (statusCode === 500) {
      return <ServerErrorMessage onRetry={refetch} />;
    }
  } else {
    // Network error
    return <NetworkErrorMessage onRetry={refetch} />;
  }
}
```

**Validation Error Display**:
```typescript
interface ValidationError {
  field: string;
  message: string;
}

const handleSubmitError = (error: any) => {
  if (error.data?.validationErrors) {
    const errors: ValidationError[] = error.data.validationErrors;
    errors.forEach(err => {
      setFieldError(err.field, err.message);
    });
  } else {
    showNotification({
      type: 'error',
      message: error.data?.message || 'Operation failed',
    });
  }
};
```

### Backend Error Handling

**Controller Exception Handling**:
```java
@RestControllerAdvice
public class DashboardExceptionHandler {
    
    @ExceptionHandler(TrialBalanceException.class)
    public ResponseEntity<ErrorResponse> handleTrialBalanceException(
            TrialBalanceException ex) {
        return ResponseEntity.badRequest()
            .body(new ErrorResponse(
                "TRIAL_BALANCE_ERROR",
                ex.getMessage()
            ));
    }
    
    @ExceptionHandler(FiscalPeriodClosedException.class)
    public ResponseEntity<ErrorResponse> handleFiscalPeriodClosed(
            FiscalPeriodClosedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(new ErrorResponse(
                "FISCAL_PERIOD_CLOSED",
                "Cannot perform operation: Fiscal period is closed"
            ));
    }
    
    @ExceptionHandler(ReportGenerationException.class)
    public ResponseEntity<ErrorResponse> handleReportGenerationException(
            ReportGenerationException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ErrorResponse(
                "REPORT_GENERATION_FAILED",
                ex.getMessage()
            ));
    }
}
```

**Service Layer Validation**:
```java
@Service
public class SecretaryDashboardService {
    
    public void closeFiscalPeriod(String fiscalPeriod) {
        // Validate trial balance
        TrialBalanceDTO trialBalance = getTrialBalance(fiscalPeriod);
        if (!trialBalance.getIsBalanced()) {
            throw new TrialBalanceException(
                "Cannot close fiscal period: Trial balance is unbalanced. " +
                "Variance: " + trialBalance.getVariance()
            );
        }
        
        // Validate no unclassified transactions
        long unclassifiedCount = getUnclassifiedTransactionCount(fiscalPeriod);
        if (unclassifiedCount > 0) {
            throw new UnclassifiedTransactionsException(
                "Cannot close fiscal period: " + unclassifiedCount + 
                " unclassified transactions remain"
            );
        }
        
        // Proceed with closing
        updateFiscalPeriodStatus(fiscalPeriod, FiscalPeriodStatus.CLOSED);
    }
}
```

## Testing Strategy

### Unit Testing

**Component Unit Tests**:
```typescript
describe('TrialBalanceWidget', () => {
  it('should display balanced state when debits equal credits', () => {
    const mockData = {
      totalDebits: 10000,
      totalCredits: 10000,
      variance: 0,
      isBalanced: true,
      fiscalPeriod: 'January 2024',
    };
    
    render(<TrialBalanceWidget />, {
      preloadedState: {
        dashboardApi: {
          queries: {
            'getTrialBalance(undefined)': {
              data: mockData,
              status: 'fulfilled',
            },
          },
        },
      },
    });
    
    expect(screen.getByText('Books are Balanced')).toBeInTheDocument();
    expect(screen.getByText('100% Balanced')).toBeInTheDocument();
  });
  
  it('should display imbalance alert when debits do not equal credits', () => {
    const mockData = {
      totalDebits: 10000,
      totalCredits: 9500,
      variance: 500,
      isBalanced: false,
      fiscalPeriod: 'January 2024',
    };
    
    render(<TrialBalanceWidget />, {
      preloadedState: {
        dashboardApi: {
          queries: {
            'getTrialBalance(undefined)': {
              data: mockData,
              status: 'fulfilled',
            },
          },
        },
      },
    });
    
    expect(screen.getByText('Books are Unbalanced')).toBeInTheDocument();
    expect(screen.getByText(/฿500.00/)).toBeInTheDocument();
  });
});
```

**Service Unit Tests**:
```java
@SpringBootTest
class SecretaryDashboardServiceTest {
    
    @Autowired
    private SecretaryDashboardService dashboardService;
    
    @MockBean
    private TransactionRepository transactionRepository;
    
    @MockBean
    private AccountingRepository accountingRepository;
    
    @Test
    void testGetTrialBalance_Balanced() {
        // Arrange
        String fiscalPeriod = "2024-01";
        when(transactionRepository.sumDebitsByFiscalPeriod(fiscalPeriod))
            .thenReturn(new BigDecimal("10000.00"));
        when(transactionRepository.sumCreditsByFiscalPeriod(fiscalPeriod))
            .thenReturn(new BigDecimal("10000.00"));
        
        // Act
        TrialBalanceDTO result = dashboardService.getTrialBalance(fiscalPeriod);
        
        // Assert
        assertEquals(new BigDecimal("10000.00"), result.getTotalDebits());
        assertEquals(new BigDecimal("10000.00"), result.getTotalCredits());
        assertEquals(BigDecimal.ZERO, result.getVariance());
        assertTrue(result.getIsBalanced());
    }
    
    @Test
    void testCloseFiscalPeriod_ThrowsException_WhenUnbalanced() {
        // Arrange
        String fiscalPeriod = "2024-01";
        when(transactionRepository.sumDebitsByFiscalPeriod(fiscalPeriod))
            .thenReturn(new BigDecimal("10000.00"));
        when(transactionRepository.sumCreditsByFiscalPeriod(fiscalPeriod))
            .thenReturn(new BigDecimal("9500.00"));
        
        // Act & Assert
        assertThrows(TrialBalanceException.class, () -> {
            dashboardService.closeFiscalPeriod(fiscalPeriod);
        });
    }
}
```

### Property-Based Testing

The property-based testing approach will use **fast-check** for TypeScript/JavaScript and **jqwik** for Java. Each property test should run a minimum of 100 iterations to ensure comprehensive coverage.

**Frontend Property Tests**:
```typescript
import fc from 'fast-check';

describe('Trial Balance Properties', () => {
  it('Property 1: Trial balance variance equals absolute difference', () => {
    fc.assert(
      fc.property(
        fc.float({ min: 0, max: 1000000 }), // totalDebits
        fc.float({ min: 0, max: 1000000 }), // totalCredits
        (debits, credits) => {
          const variance = Math.abs(debits - credits);
          const isBalanced = variance < 0.01; // Account for floating point
          
          const result = calculateTrialBalance(debits, credits);
          
          expect(Math.abs(result.variance - variance)).toBeLessThan(0.01);
          expect(result.isBalanced).toBe(isBalanced);
        }
      ),
      { numRuns: 100 }
    );
  });
  
  it('Property 9: Total liquid assets equals sum of cash and bank', () => {
    fc.assert(
      fc.property(
        fc.float({ min: 0, max: 1000000 }), // cashInHand
        fc.float({ min: 0, max: 1000000 }), // bankDeposits
        (cash, bank) => {
          const expected = cash + bank;
          const result = calculateTotalLiquidAssets(cash, bank);
          
          expect(Math.abs(result - expected)).toBeLessThan(0.01);
        }
      ),
      { numRuns: 100 }
    );
  });
});
```

**Backend Property Tests**:
```java
@PropertyTest
class SecretaryDashboardPropertyTest {
    
    @Property
    void property1_trialBalanceVarianceEqualsAbsoluteDifference(
            @ForAll @BigRange(min = "0", max = "1000000") BigDecimal debits,
            @ForAll @BigRange(min = "0", max = "1000000") BigDecimal credits) {
        
        // Calculate expected variance
        BigDecimal expectedVariance = debits.subtract(credits).abs();
        boolean expectedBalanced = expectedVariance.compareTo(BigDecimal.ZERO) == 0;
        
        // Create trial balance
        TrialBalanceDTO result = new TrialBalanceDTO();
        result.setTotalDebits(debits);
        result.setTotalCredits(credits);
        result.setVariance(debits.subtract(credits));
        result.setIsBalanced(result.getVariance().compareTo(BigDecimal.ZERO) == 0);
        
        // Verify property
        assertEquals(expectedVariance, result.getVariance().abs());
        assertEquals(expectedBalanced, result.getIsBalanced());
    }
    
    @Property
    void property7_netProfitEqualsIncomeMinusExpenses(
            @ForAll @BigRange(min = "0", max = "1000000") BigDecimal income,
            @ForAll @BigRange(min = "0", max = "1000000") BigDecimal expenses) {
        
        BigDecimal expectedNetProfit = income.subtract(expenses);
        
        RevenueExpenseAnalysisDTO result = new RevenueExpenseAnalysisDTO();
        result.getIncome().setTotal(income);
        result.getExpenses().setTotal(expenses);
        result.setNetProfit(income.subtract(expenses));
        
        assertEquals(expectedNetProfit, result.getNetProfit());
    }
}
```

### Integration Testing

**Dashboard API Integration Tests**:
```java
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class SecretaryDashboardIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private TransactionRepository transactionRepository;
    
    @Test
    @WithMockUser(roles = "SECRETARY")
    void testGetTrialBalance_ReturnsCorrectData() throws Exception {
        // Arrange: Create test transactions
        createTestTransactions();
        
        // Act & Assert
        mockMvc.perform(get("/api/dashboard/secretary/trial-balance"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalDebits").exists())
            .andExpect(jsonPath("$.totalCredits").exists())
            .andExpect(jsonPath("$.variance").exists())
            .andExpect(jsonPath("$.isBalanced").isBoolean());
    }
    
    @Test
    @WithMockUser(roles = "SECRETARY")
    void testCloseFiscalPeriod_FailsWhenUnbalanced() throws Exception {
        // Arrange: Create unbalanced transactions
        createUnbalancedTransactions();
        
        // Act & Assert
        mockMvc.perform(post("/api/dashboard/secretary/close-fiscal-period")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"fiscalPeriod\":\"2024-01\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errorCode").value("TRIAL_BALANCE_ERROR"));
    }
}
```

### End-to-End Testing

**Cypress E2E Tests**:
```typescript
describe('Secretary Dashboard E2E', () => {
  beforeEach(() => {
    cy.login('secretary@test.com', 'password');
    cy.visit('/dashboard/secretary');
  });
  
  it('should display trial balance widget with correct data', () => {
    cy.get('[data-testid="trial-balance-widget"]').should('be.visible');
    cy.get('[data-testid="total-debits"]').should('contain', '฿');
    cy.get('[data-testid="total-credits"]').should('contain', '฿');
  });
  
  it('should prevent month-end closing when trial balance is unbalanced', () => {
    // Assuming test data has unbalanced trial balance
    cy.get('[data-testid="end-of-month-button"]').should('be.disabled');
    cy.get('[data-testid="end-of-month-button"]').trigger('mouseover');
    cy.get('[data-testid="tooltip"]').should('contain', 'Trial balance must be balanced');
  });
  
  it('should generate trial balance report when balanced', () => {
    // Assuming test data has balanced trial balance
    cy.get('[data-testid="generate-trial-balance-report"]').click();
    cy.get('[data-testid="report-generation-success"]').should('be.visible');
  });
});
```

## Performance Considerations

### Frontend Optimization

**1. Component Memoization**:
```typescript
const TrialBalanceWidget = React.memo(({ fiscalPeriod }: Props) => {
  // Component implementation
}, (prevProps, nextProps) => {
  return prevProps.fiscalPeriod === nextProps.fiscalPeriod;
});
```

**2. Data Caching with RTK Query**:
```typescript
export const dashboardApi = apiSlice.injectEndpoints({
  endpoints: (builder) => ({
    getTrialBalance: builder.query<TrialBalanceResponse, void>({
      query: () => '/dashboard/secretary/trial-balance',
      providesTags: ['TrialBalance'],
      // Cache for 30 seconds
      keepUnusedDataFor: 30,
    }),
  }),
});
```

**3. Lazy Loading Charts**:
```typescript
const RevenueExpenseChart = lazy(() => import('./RevenueExpenseChart'));

<Suspense fallback={<ChartSkeleton />}>
  <RevenueExpenseChart data={data} />
</Suspense>
```

### Backend Optimization

**1. Database Query Optimization**:
```java
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
           "WHERE t.type = 'DEBIT' AND t.fiscalPeriod = :fiscalPeriod")
    BigDecimal sumDebitsByFiscalPeriod(@Param("fiscalPeriod") String fiscalPeriod);
    
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
           "WHERE t.type = 'CREDIT' AND t.fiscalPeriod = :fiscalPeriod")
    BigDecimal sumCreditsByFiscalPeriod(@Param("fiscalPeriod") String fiscalPeriod);
}
```

**2. Caching with Redis**:
```java
@Service
public class SecretaryDashboardService {
    
    @Cacheable(value = "trialBalance", key = "#fiscalPeriod")
    public TrialBalanceDTO getTrialBalance(String fiscalPeriod) {
        // Expensive calculation
        BigDecimal totalDebits = transactionRepository
            .sumDebitsByFiscalPeriod(fiscalPeriod);
        BigDecimal totalCredits = transactionRepository
            .sumCreditsByFiscalPeriod(fiscalPeriod);
        
        return buildTrialBalanceDTO(totalDebits, totalCredits, fiscalPeriod);
    }
    
    @CacheEvict(value = "trialBalance", key = "#fiscalPeriod")
    public void invalidateTrialBalanceCache(String fiscalPeriod) {
        // Called when transactions are posted
    }
}
```

**3. Batch Processing for Reports**:
```java
@Service
public class ReportGenerationService {
    
    @Async
    public CompletableFuture<ReportGenerationResponse> generateReport(
            ReportGenerationRequest request) {
        
        // Generate report asynchronously
        byte[] pdfBytes = generatePDF(request);
        String filename = saveToStorage(pdfBytes, request);
        
        return CompletableFuture.completedFuture(
            new ReportGenerationResponse(filename, pdfBytes.length)
        );
    }
}
```

## Security Considerations

### Role-Based Access Control

**Frontend Route Protection**:
```typescript
<Route
  path="/dashboard/secretary"
  element={
    <RoleBasedRoute requiredRole="ROLE_SECRETARY">
      <SecretaryDashboardPage />
    </RoleBasedRoute>
  }
/>
```

**Backend Endpoint Security**:
```java
@RestController
@RequestMapping("/api/dashboard/secretary")
@PreAuthorize("hasRole('SECRETARY')")
public class SecretaryDashboardController {
    
    @GetMapping("/trial-balance")
    public ResponseEntity<TrialBalanceDTO> getTrialBalance() {
        // Only accessible to users with SECRETARY role
        return ResponseEntity.ok(dashboardService.getTrialBalance());
    }
    
    @PostMapping("/close-fiscal-period")
    @PreAuthorize("hasRole('SECRETARY') and hasAuthority('CLOSE_FISCAL_PERIOD')")
    public ResponseEntity<Void> closeFiscalPeriod(
            @RequestBody FiscalPeriodClosingRequest request) {
        // Requires both SECRETARY role and specific permission
        dashboardService.closeFiscalPeriod(request.getFiscalPeriod());
        return ResponseEntity.ok().build();
    }
}
```

### Audit Logging

**Fiscal Period Closing Audit**:
```java
@Service
public class SecretaryDashboardService {
    
    @Autowired
    private AuditLogRepository auditLogRepository;
    
    @Transactional
    public void closeFiscalPeriod(String fiscalPeriod) {
        // Validate and close period
        updateFiscalPeriodStatus(fiscalPeriod, FiscalPeriodStatus.CLOSED);
        
        // Create audit log
        AuditLog auditLog = new AuditLog();
        auditLog.setAction("CLOSE_FISCAL_PERIOD");
        auditLog.setEntityType("FISCAL_PERIOD");
        auditLog.setEntityId(fiscalPeriod);
        auditLog.setUserId(getCurrentUserId());
        auditLog.setTimestamp(LocalDateTime.now());
        auditLog.setDetails("Fiscal period " + fiscalPeriod + " closed");
        
        auditLogRepository.save(auditLog);
    }
}
```

## Deployment Considerations

### Environment Configuration

**Frontend Environment Variables**:
```env
VITE_API_BASE_URL=https://api.bansaiyai.com
VITE_DASHBOARD_REFRESH_INTERVAL=30000
VITE_REPORT_DOWNLOAD_TIMEOUT=60000
```

**Backend Application Properties**:
```yaml
dashboard:
  secretary:
    trial-balance:
      cache-ttl: 300 # 5 minutes
    reports:
      storage-path: /var/reports
      max-file-size: 10485760 # 10MB
    fiscal-period:
      auto-close-enabled: false
```

### Monitoring and Alerting

**Key Metrics to Monitor**:
- Trial balance calculation time
- Report generation success rate
- Dashboard widget load times
- API response times
- Cache hit rates

**Alert Conditions**:
- Trial balance remains unbalanced for > 24 hours
- Report generation failure rate > 5%
- Dashboard API response time > 2 seconds
- Unclassified transaction count > 100
