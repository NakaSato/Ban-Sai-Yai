# Design Document

## Overview

The Dashboard Widgets feature implements role-specific, interactive components that provide financial officers, secretaries, presidents, and members with the tools and information they need for daily operations. This design builds upon the dashboard-data-pipeline infrastructure to create specialized widgets with embedded business logic, real-time calculations, modal interactions, and visual feedback mechanisms.

The architecture leverages the existing Spring Boot backend with MariaDB database and React/TypeScript frontend established in the data pipeline spec. Each widget is designed as a self-contained component that fetches its own data asynchronously, handles errors gracefully, and provides role-appropriate functionality.

Key design principles:
- **Widget autonomy**: Each widget manages its own state, data fetching, and error handling
- **Business logic encapsulation**: Complex calculations (PAR analysis, liquidity ratios) are implemented in dedicated service classes
- **Modal-based interactions**: Transaction entry uses modals to maintain context and prevent navigation
- **Real-time feedback**: Widgets update immediately after transactions without full page refresh
- **Progressive disclosure**: Complex features (cash reconciliation, PAR details) are revealed on demand

## Architecture

### High-Level Widget Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    Dashboard Layout                         │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │   Header     │  │ Fiscal Period│  │ Omni-Search  │      │
│  │  (Global)    │  │  Indicator   │  │     Bar      │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
│                                                             │
│  ┌──────────────────────────────────────────────────────┐  │
│  │           Role-Specific Widget Grid                  │  │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐          │  │
│  │  │ Widget 1 │  │ Widget 2 │  │ Widget 3 │          │  │
│  │  └──────────┘  └──────────┘  └──────────┘          │  │
│  └──────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

### Widget Data Flow

```
┌─────────────────────────────────────────────────────────────┐
│                    Widget Lifecycle                         │
│                                                             │
│  1. Mount → Display Skeleton                                │
│  2. Fetch Data → Show Loading Indicator                     │
│  3. Receive Data → Render Content                           │
│  4. User Interaction → Open Modal / Update State            │
│  5. Submit Transaction → POST to Backend                    │
│  6. Success → Refresh Widget Data                           │
│  7. Error → Display Error + Retry Option                    │
└─────────────────────────────────────────────────────────────┘
```

### Officer Dashboard Widget Layout

```
┌─────────────────────────────────────────────────────────────┐
│  Fiscal Period: August 2023 - OPEN    [Omni-Search Bar]    │
├─────────────────────────────────────────────────────────────┤
│  ┌──────────────────────┐  ┌──────────────────────┐        │
│  │  Teller Action Card  │  │   Cash Box Tally     │        │
│  │  [Member Search]     │  │   In: ฿50,000        │        │
│  │  Balance: ฿12,500    │  │   Out: ฿30,000       │        │
│  │  [Deposit] [Loan Pay]│  │   Net: ฿20,000       │        │
│  └──────────────────────┘  └──────────────────────┘        │
│  ┌──────────────────────────────────────────────────────┐  │
│  │           Transaction Feed                           │  │
│  │  10:30 - John Doe - Deposit - ฿1,000 [Receipt]      │  │
│  │  10:25 - Jane Smith - Loan Pay - ฿500 [Receipt]     │  │
│  └──────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

## Components and Interfaces

### Backend Components

#### 1. WidgetDataService

**Purpose**: Aggregate and prepare data for specific widgets

**Methods**:
```java
@Service
public class WidgetDataService {
    
    /**
     * Get member financial summary for Teller Action Card
     */
    public MemberFinancialSummaryDTO getMemberFinancialSummary(Long memberId);
    
    /**
     * Get cash box totals for current date
     */
    public CashBoxTallyDTO getCashBoxTally(LocalDate date);
    
    /**
     * Get recent transactions for Transaction Feed
     */
    public List<TransactionFeedItemDTO> getRecentTransactions(int limit);
    
    /**
     * Get trial balance status for Secretary
     */
    public TrialBalanceStatusDTO getTrialBalanceStatus();
    
    /**
     * Get unclassified transaction count
     */
    public Integer getUnclassifiedTransactionCount();
    
    /**
     * Get PAR analysis data for President
     */
    public PARAnalysisDTO getPARAnalysis();
    
    /**
     * Get liquidity ratio calculation
     */
    public LiquidityGaugeDTO getLiquidityGauge();
    
    /**
     * Get membership growth trend data
     */
    public MembershipGrowthDTO getMembershipGrowth(int months);
    
    /**
     * Get member passbook data
     */
    public DigitalPassbookDTO getDigitalPassbook(Long memberId);
    
    /**
     * Get member loan obligations
     */
    public LoanObligationDTO getLoanObligation(Long memberId);
    
    /**
     * Calculate dividend estimate for member
     */
    public DividendEstimateDTO getDividendEstimate(Long memberId);
}
```


#### 2. TransactionModalService

**Purpose**: Handle transaction submissions from modal dialogs

**Methods**:
```java
@Service
public class TransactionModalService {
    
    /**
     * Process deposit from Teller Action Card
     */
    @Transactional
    @Auditable
    public TransactionResult processDeposit(Long memberId, BigDecimal amount, Long userId);
    
    /**
     * Process loan payment with principal, interest, and fine breakdown
     */
    @Transactional
    @Auditable
    public TransactionResult processLoanPayment(
        Long loanId, 
        BigDecimal principal, 
        BigDecimal interest, 
        BigDecimal fine,
        Long userId
    );
    
    /**
     * Calculate minimum interest due for loan payment modal
     */
    public BigDecimal calculateMinimumInterestDue(Long loanId);
    
    /**
     * Validate transaction against fiscal period status
     */
    public ValidationResult validateTransactionAllowed();
}
```

**Key Logic**:
```java
public TransactionResult processDeposit(Long memberId, BigDecimal amount, Long userId) {
    // Validate fiscal period is open
    ValidationResult validation = validateTransactionAllowed();
    if (!validation.isValid()) {
        throw new FiscalPeriodClosedException("Cannot record transactions in closed period");
    }
    
    // Create transaction record
    SavingTransaction transaction = new SavingTransaction();
    transaction.setMemberId(memberId);
    transaction.setAmount(amount);
    transaction.setType(TransactionType.DEPOSIT);
    transaction.setTransDate(LocalDate.now());
    transaction.setUserId(userId);
    
    savingTransactionRepository.save(transaction);
    
    // Update member balance
    SavingAccount account = savingAccountRepository.findByMemberId(memberId)
        .orElseThrow(() -> new AccountNotFoundException("No account for member " + memberId));
    account.setBalance(account.getBalance().add(amount));
    savingAccountRepository.save(account);
    
    // Generate receipt
    String receiptId = receiptService.generateReceipt(transaction);
    
    return TransactionResult.success(transaction.getId(), receiptId);
}
```

#### 3. PARAnalysisService

**Purpose**: Calculate Portfolio at Risk metrics

**Methods**:
```java
@Service
public class PARAnalysisService {
    
    /**
     * Categorize all active loans by delinquency status
     */
    public PARAnalysisDTO analyzePAR();
    
    /**
     * Get detailed list of loans in a specific PAR category
     */
    public List<LoanDetailDTO> getLoansInCategory(PARCategory category);
    
    /**
     * Calculate days since last payment for a loan
     */
    private int calculateDaysSinceLastPayment(Loan loan);
    
    /**
     * Categorize loan based on days overdue
     */
    private PARCategory categorizeLoan(int daysSincePayment);
}
```

**PAR Calculation Logic**:
```java
public PARAnalysisDTO analyzePAR() {
    List<Loan> activeLoans = loanRepository.findByStatus(LoanStatus.ACTIVE);
    
    Map<PARCategory, List<LoanDetailDTO>> categorized = new EnumMap<>(PARCategory.class);
    for (PARCategory category : PARCategory.values()) {
        categorized.put(category, new ArrayList<>());
    }
    
    for (Loan loan : activeLoans) {
        int daysSincePayment = calculateDaysSinceLastPayment(loan);
        PARCategory category = categorizeLoan(daysSincePayment);
        
        LoanDetailDTO detail = new LoanDetailDTO();
        detail.setLoanId(loan.getId());
        detail.setMemberName(loan.getMember().getFullName());
        detail.setPrincipal(loan.getPrincipal());
        detail.setDaysOverdue(daysSincePayment);
        detail.setGuarantors(loan.getGuarantors().stream()
            .map(Guarantor::getName)
            .collect(Collectors.toList()));
        
        categorized.get(category).add(detail);
    }
    
    return new PARAnalysisDTO(categorized);
}

private PARCategory categorizeLoan(int daysSincePayment) {
    if (daysSincePayment <= 30) return PARCategory.STANDARD;
    if (daysSincePayment <= 60) return PARCategory.WATCH;
    if (daysSincePayment <= 90) return PARCategory.SUBSTANDARD;
    return PARCategory.LOSS;
}
```

#### 4. LiquidityCalculationService

**Purpose**: Calculate liquidity ratios and determine status zones

**Methods**:
```java
@Service
public class LiquidityCalculationService {
    
    /**
     * Calculate current liquidity ratio
     */
    public LiquidityGaugeDTO calculateLiquidity();
    
    /**
     * Get total liquid assets (cash + bank)
     */
    private BigDecimal getTotalLiquidAssets();
    
    /**
     * Get total savings deposits
     */
    private BigDecimal getTotalSavingsDeposits();
    
    /**
     * Determine liquidity status zone
     */
    private LiquidityStatus determineStatus(BigDecimal ratio);
}
```

**Liquidity Calculation**:
```java
public LiquidityGaugeDTO calculateLiquidity() {
    BigDecimal liquidAssets = getTotalLiquidAssets();
    BigDecimal savingsDeposits = getTotalSavingsDeposits();
    
    if (savingsDeposits.compareTo(BigDecimal.ZERO) == 0) {
        throw new InsufficientDataException("No savings deposits to calculate liquidity");
    }
    
    BigDecimal ratio = liquidAssets
        .divide(savingsDeposits, 4, RoundingMode.HALF_UP)
        .multiply(new BigDecimal("100")); // Convert to percentage
    
    LiquidityStatus status = determineStatus(ratio);
    
    return new LiquidityGaugeDTO(liquidAssets, savingsDeposits, ratio, status);
}

private LiquidityStatus determineStatus(BigDecimal ratio) {
    if (ratio.compareTo(new BigDecimal("5")) < 0) return LiquidityStatus.CRISIS;
    if (ratio.compareTo(new BigDecimal("10")) < 0) return LiquidityStatus.CAUTION;
    if (ratio.compareTo(new BigDecimal("20")) <= 0) return LiquidityStatus.HEALTHY;
    return LiquidityStatus.INEFFICIENT;
}
```

#### 5. FiscalPeriodService

**Purpose**: Manage fiscal period status and transaction validation

**Methods**:
```java
@Service
public class FiscalPeriodService {
    
    /**
     * Get current fiscal period status
     */
    public FiscalPeriodDTO getCurrentFiscalPeriod();
    
    /**
     * Check if transactions are allowed
     */
    public boolean isTransactionAllowed();
    
    /**
     * Close current fiscal period
     */
    @Transactional
    public void closeFiscalPeriod(LocalDate closingDate);
    
    /**
     * Open new fiscal period
     */
    @Transactional
    public void openFiscalPeriod(LocalDate startDate);
}
```

#### 6. OmniSearchService

**Purpose**: Provide fast member search functionality

**Methods**:
```java
@Service
public class OmniSearchService {
    
    /**
     * Search members by ID, name, or national ID
     */
    public List<MemberSearchResultDTO> searchMembers(String query, int limit);
}
```

**Search Implementation**:
```java
public List<MemberSearchResultDTO> searchMembers(String query, int limit) {
    String searchPattern = "%" + query + "%";
    
    List<Member> results = memberRepository.findBySearchPattern(
        searchPattern, 
        PageRequest.of(0, limit)
    );
    
    return results.stream()
        .map(member -> new MemberSearchResultDTO(
            member.getId(),
            member.getFirstName(),
            member.getLastName(),
            member.getStatus()
        ))
        .collect(Collectors.toList());
}
```

### Frontend Components

#### 1. TellerActionCard Component

**Purpose**: Primary Officer interface for transaction entry

**Props**:
```typescript
interface TellerActionCardProps {
  onTransactionComplete: () => void;
}
```

**Implementation**:
```typescript
export const TellerActionCard: React.FC<TellerActionCardProps> = ({ 
  onTransactionComplete 
}) => {
  const [selectedMember, setSelectedMember] = useState<Member | null>(null);
  const [memberFinancials, setMemberFinancials] = useState<MemberFinancials | null>(null);
  const [depositModalOpen, setDepositModalOpen] = useState(false);
  const [loanPayModalOpen, setLoanPayModalOpen] = useState(false);
  
  const handleMemberSelect = async (memberId: number) => {
    const financials = await fetchMemberFinancials(memberId);
    setMemberFinancials(financials);
  };
  
  const handleDepositSubmit = async (amount: number) => {
    await submitDeposit(selectedMember!.id, amount);
    setDepositModalOpen(false);
    onTransactionComplete();
  };
  
  return (
    <Card>
      <CardContent>
        <Typography variant="h6">Teller Actions</Typography>
        
        <MemberAutocomplete 
          onSelect={handleMemberSelect}
        />
        
        {memberFinancials && (
          <>
            <Box sx={{ mt: 2 }}>
              <Typography>Balance: ฿{memberFinancials.balance}</Typography>
              <Typography>Loan: ฿{memberFinancials.loanPrincipal}</Typography>
              <Typography>Status: {memberFinancials.loanStatus}</Typography>
            </Box>
            
            <Box sx={{ mt: 2, display: 'flex', gap: 2 }}>
              <Button 
                variant="contained" 
                onClick={() => setDepositModalOpen(true)}
              >
                Deposit
              </Button>
              <Button 
                variant="contained" 
                onClick={() => setLoanPayModalOpen(true)}
                disabled={!memberFinancials.hasActiveLoan}
              >
                Loan Pay
              </Button>
            </Box>
          </>
        )}
      </CardContent>
      
      <DepositModal
        open={depositModalOpen}
        onClose={() => setDepositModalOpen(false)}
        onSubmit={handleDepositSubmit}
      />
      
      <LoanPaymentModal
        open={loanPayModalOpen}
        onClose={() => setLoanPayModalOpen(false)}
        loanId={memberFinancials?.loanId}
        onSubmit={handleLoanPaymentSubmit}
      />
    </Card>
  );
};
```


#### 2. CashBoxTally Component

**Purpose**: Real-time cash control with reconciliation

**Props**:
```typescript
interface CashBoxTallyProps {
  date: Date;
}
```

**Implementation**:
```typescript
export const CashBoxTally: React.FC<CashBoxTallyProps> = ({ date }) => {
  const [tally, setTally] = useState<CashBoxTallyData | null>(null);
  const [showReconciliation, setShowReconciliation] = useState(false);
  const [denominations, setDenominations] = useState<DenominationCount>({});
  
  useEffect(() => {
    fetchCashBoxTally(date).then(setTally);
  }, [date]);
  
  const calculatePhysicalTotal = () => {
    return Object.entries(denominations).reduce((total, [denom, count]) => {
      return total + (parseInt(denom) * count);
    }, 0);
  };
  
  const physicalTotal = calculatePhysicalTotal();
  const variance = tally ? physicalTotal - tally.netCash : 0;
  
  return (
    <Card>
      <CardContent>
        <Typography variant="h6">Cash Box Tally</Typography>
        
        {tally && (
          <>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', mt: 2 }}>
              <Box>
                <Typography color="success.main" variant="h4">
                  ฿{tally.totalIn.toLocaleString()}
                </Typography>
                <Typography variant="caption">Total In</Typography>
              </Box>
              
              <Box>
                <Typography color="error.main" variant="h4">
                  ฿{tally.totalOut.toLocaleString()}
                </Typography>
                <Typography variant="caption">Total Out</Typography>
              </Box>
            </Box>
            
            <Divider sx={{ my: 2 }} />
            
            <Typography variant="h5">
              Net Cash: ฿{tally.netCash.toLocaleString()}
            </Typography>
            
            <Button 
              sx={{ mt: 2 }}
              onClick={() => setShowReconciliation(!showReconciliation)}
            >
              {showReconciliation ? 'Hide' : 'Count Cash'}
            </Button>
            
            {showReconciliation && (
              <Box sx={{ mt: 2 }}>
                <Typography variant="subtitle2">Denomination Count</Typography>
                {[1000, 500, 100, 50, 20, 10, 5, 1].map(denom => (
                  <Box key={denom} sx={{ display: 'flex', gap: 2, mt: 1 }}>
                    <Typography sx={{ width: 80 }}>฿{denom}</Typography>
                    <TextField
                      type="number"
                      size="small"
                      value={denominations[denom] || 0}
                      onChange={(e) => setDenominations({
                        ...denominations,
                        [denom]: parseInt(e.target.value) || 0
                      })}
                    />
                  </Box>
                ))}
                
                <Box sx={{ mt: 2, p: 2, bgcolor: 'grey.100', borderRadius: 1 }}>
                  <Typography>Physical Total: ฿{physicalTotal.toLocaleString()}</Typography>
                  <Typography>Database Total: ฿{tally.netCash.toLocaleString()}</Typography>
                  <Typography 
                    color={variance === 0 ? 'success.main' : 'error.main'}
                    fontWeight="bold"
                  >
                    Variance: ฿{variance.toLocaleString()}
                  </Typography>
                </Box>
              </Box>
            )}
          </>
        )}
      </CardContent>
    </Card>
  );
};
```

#### 3. TransactionFeed Component

**Purpose**: Display recent transactions with receipt generation

**Props**:
```typescript
interface TransactionFeedProps {
  limit: number;
  refreshTrigger?: number;
}
```

**Implementation**:
```typescript
export const TransactionFeed: React.FC<TransactionFeedProps> = ({ 
  limit, 
  refreshTrigger 
}) => {
  const [transactions, setTransactions] = useState<TransactionFeedItem[]>([]);
  
  useEffect(() => {
    fetchRecentTransactions(limit).then(setTransactions);
  }, [limit, refreshTrigger]);
  
  const handleReceiptClick = (transactionId: number) => {
    window.open(`/api/receipts/${transactionId}/pdf`, '_blank');
  };
  
  return (
    <Card>
      <CardContent>
        <Typography variant="h6">Recent Transactions</Typography>
        
        <TableContainer sx={{ mt: 2 }}>
          <Table size="small">
            <TableHead>
              <TableRow>
                <TableCell>Time</TableCell>
                <TableCell>Member</TableCell>
                <TableCell>Type</TableCell>
                <TableCell align="right">Amount</TableCell>
                <TableCell align="center">Receipt</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {transactions.map((tx) => (
                <TableRow key={tx.id}>
                  <TableCell>{formatTime(tx.timestamp)}</TableCell>
                  <TableCell>{tx.memberName}</TableCell>
                  <TableCell>
                    <Chip 
                      label={tx.type} 
                      size="small"
                      color={getTransactionTypeColor(tx.type)}
                    />
                  </TableCell>
                  <TableCell align="right">
                    ฿{tx.amount.toLocaleString()}
                  </TableCell>
                  <TableCell align="center">
                    <IconButton 
                      size="small"
                      onClick={() => handleReceiptClick(tx.id)}
                    >
                      <ReceiptIcon />
                    </IconButton>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>
      </CardContent>
    </Card>
  );
};
```

#### 4. TrialBalanceWidget Component

**Purpose**: Visual indicator of accounting balance state

**Implementation**:
```typescript
export const TrialBalanceWidget: React.FC = () => {
  const [balance, setBalance] = useState<TrialBalanceStatus | null>(null);
  
  useEffect(() => {
    fetchTrialBalanceStatus().then(setBalance);
  }, []);
  
  if (!balance) return <CircularProgress />;
  
  const isBalanced = balance.debits === balance.credits;
  const imbalance = Math.abs(balance.debits - balance.credits);
  
  return (
    <Card>
      <CardContent>
        <Typography variant="h6">Trial Balance</Typography>
        
        <Box sx={{ mt: 3 }}>
          <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
            <Typography>Debits: ฿{balance.debits.toLocaleString()}</Typography>
            <Typography>Credits: ฿{balance.credits.toLocaleString()}</Typography>
          </Box>
          
          {isBalanced ? (
            <Box>
              <LinearProgress 
                variant="determinate" 
                value={100} 
                color="success"
                sx={{ height: 20, borderRadius: 1 }}
              />
              <Typography 
                color="success.main" 
                sx={{ mt: 1, textAlign: 'center' }}
              >
                ✓ Balanced
              </Typography>
            </Box>
          ) : (
            <Box>
              <Box sx={{ display: 'flex', height: 20 }}>
                <Box 
                  sx={{ 
                    flex: balance.debits, 
                    bgcolor: 'error.main',
                    borderRadius: '4px 0 0 4px'
                  }} 
                />
                <Box 
                  sx={{ 
                    flex: balance.credits, 
                    bgcolor: 'info.main',
                    borderRadius: '0 4px 4px 0'
                  }} 
                />
              </Box>
              <Typography 
                color="error.main" 
                sx={{ mt: 1, textAlign: 'center' }}
              >
                ⚠ Imbalance: ฿{imbalance.toLocaleString()}
              </Typography>
              <Alert severity="warning" sx={{ mt: 2 }}>
                Monthly report generation is disabled until balanced
              </Alert>
            </Box>
          )}
        </Box>
      </CardContent>
    </Card>
  );
};
```

#### 5. PARAnalysisWidget Component

**Purpose**: Portfolio at Risk visualization with drill-down

**Implementation**:
```typescript
export const PARAnalysisWidget: React.FC = () => {
  const [parData, setParData] = useState<PARAnalysisData | null>(null);
  const [selectedCategory, setSelectedCategory] = useState<PARCategory | null>(null);
  const [detailModalOpen, setDetailModalOpen] = useState(false);
  
  useEffect(() => {
    fetchPARAnalysis().then(setParData);
  }, []);
  
  const handleSegmentClick = (category: PARCategory) => {
    setSelectedCategory(category);
    setDetailModalOpen(true);
  };
  
  if (!parData) return <CircularProgress />;
  
  const chartData = {
    labels: ['Standard', 'Watch', 'Substandard', 'Loss'],
    datasets: [{
      data: [
        parData.standard.length,
        parData.watch.length,
        parData.substandard.length,
        parData.loss.length
      ],
      backgroundColor: ['#4caf50', '#ff9800', '#ff5722', '#f44336']
    }]
  };
  
  return (
    <Card>
      <CardContent>
        <Typography variant="h6">Portfolio at Risk (PAR)</Typography>
        
        <Box sx={{ mt: 2, height: 300 }}>
          <Doughnut 
            data={chartData}
            options={{
              onClick: (event, elements) => {
                if (elements.length > 0) {
                  const index = elements[0].index;
                  const categories: PARCategory[] = [
                    'STANDARD', 'WATCH', 'SUBSTANDARD', 'LOSS'
                  ];
                  handleSegmentClick(categories[index]);
                }
              }
            }}
          />
        </Box>
        
        <Box sx={{ mt: 2 }}>
          <Typography variant="caption" color="text.secondary">
            Click a segment to view loan details
          </Typography>
        </Box>
      </CardContent>
      
      <PARDetailModal
        open={detailModalOpen}
        onClose={() => setDetailModalOpen(false)}
        category={selectedCategory}
        loans={selectedCategory ? parData[selectedCategory.toLowerCase()] : []}
      />
    </Card>
  );
};
```

#### 6. LiquidityGauge Component

**Purpose**: Strategic liquidity monitoring

**Implementation**:
```typescript
export const LiquidityGauge: React.FC = () => {
  const [liquidity, setLiquidity] = useState<LiquidityData | null>(null);
  
  useEffect(() => {
    fetchLiquidityGauge().then(setLiquidity);
  }, []);
  
  if (!liquidity) return <CircularProgress />;
  
  const getStatusColor = (status: LiquidityStatus) => {
    switch (status) {
      case 'CRISIS': return '#f44336';
      case 'CAUTION': return '#ff9800';
      case 'HEALTHY': return '#4caf50';
      case 'INEFFICIENT': return '#2196f3';
    }
  };
  
  const getStatusMessage = (status: LiquidityStatus) => {
    switch (status) {
      case 'CRISIS': return 'Stop lending immediately';
      case 'CAUTION': return 'Monitor closely';
      case 'HEALTHY': return 'Optimal liquidity';
      case 'INEFFICIENT': return 'Encourage more borrowing';
    }
  };
  
  return (
    <Card>
      <CardContent>
        <Typography variant="h6">Liquidity Gauge</Typography>
        
        <Box sx={{ mt: 3, textAlign: 'center' }}>
          <Box 
            sx={{ 
              width: 200, 
              height: 200, 
              margin: '0 auto',
              position: 'relative'
            }}
          >
            <CircularProgress
              variant="determinate"
              value={Math.min(liquidity.ratio, 100)}
              size={200}
              thickness={8}
              sx={{ 
                color: getStatusColor(liquidity.status),
                position: 'absolute'
              }}
            />
            <Box
              sx={{
                position: 'absolute',
                top: '50%',
                left: '50%',
                transform: 'translate(-50%, -50%)',
                textAlign: 'center'
              }}
            >
              <Typography variant="h3">
                {liquidity.ratio.toFixed(1)}%
              </Typography>
              <Typography variant="caption" color="text.secondary">
                Liquidity Ratio
              </Typography>
            </Box>
          </Box>
          
          <Chip 
            label={liquidity.status}
            sx={{ 
              mt: 2,
              bgcolor: getStatusColor(liquidity.status),
              color: 'white'
            }}
          />
          
          <Typography sx={{ mt: 1 }} color="text.secondary">
            {getStatusMessage(liquidity.status)}
          </Typography>
        </Box>
        
        <Divider sx={{ my: 2 }} />
        
        <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
          <Box>
            <Typography variant="caption">Liquid Assets</Typography>
            <Typography variant="h6">
              ฿{liquidity.liquidAssets.toLocaleString()}
            </Typography>
          </Box>
          <Box>
            <Typography variant="caption">Total Deposits</Typography>
            <Typography variant="h6">
              ฿{liquidity.totalDeposits.toLocaleString()}
            </Typography>
          </Box>
        </Box>
      </CardContent>
    </Card>
  );
};
```


#### 7. DigitalPassbook Component

**Purpose**: Member savings transparency

**Implementation**:
```typescript
export const DigitalPassbook: React.FC<{ memberId: number }> = ({ memberId }) => {
  const [passbook, setPassbook] = useState<PassbookData | null>(null);
  
  useEffect(() => {
    fetchDigitalPassbook(memberId).then(setPassbook);
  }, [memberId]);
  
  if (!passbook) return <CircularProgress />;
  
  return (
    <Card>
      <CardContent>
        <Typography variant="h6" color="primary">
          Share Capital
        </Typography>
        
        <Typography variant="h3" sx={{ mt: 2, mb: 1 }}>
          ฿{passbook.balance.toLocaleString()}
        </Typography>
        
        <Typography variant="caption" color="text.secondary">
          Last activity: {formatDate(passbook.lastActivity)}
        </Typography>
        
        <Divider sx={{ my: 2 }} />
        
        <Box>
          <Typography variant="subtitle2" gutterBottom>
            Recent Transactions
          </Typography>
          {passbook.recentTransactions.map((tx) => (
            <Box 
              key={tx.id}
              sx={{ 
                display: 'flex', 
                justifyContent: 'space-between',
                py: 1,
                borderBottom: '1px solid',
                borderColor: 'divider'
              }}
            >
              <Typography variant="body2">{formatDate(tx.date)}</Typography>
              <Typography variant="body2" color={tx.amount > 0 ? 'success.main' : 'error.main'}>
                {tx.amount > 0 ? '+' : ''}฿{tx.amount.toLocaleString()}
              </Typography>
            </Box>
          ))}
        </Box>
      </CardContent>
    </Card>
  );
};
```

#### 8. FiscalPeriodIndicator Component

**Purpose**: Global header showing period status

**Implementation**:
```typescript
export const FiscalPeriodIndicator: React.FC = () => {
  const [period, setPeriod] = useState<FiscalPeriodData | null>(null);
  
  useEffect(() => {
    fetchCurrentFiscalPeriod().then(setPeriod);
    
    // Poll for updates every 30 seconds
    const interval = setInterval(() => {
      fetchCurrentFiscalPeriod().then(setPeriod);
    }, 30000);
    
    return () => clearInterval(interval);
  }, []);
  
  useEffect(() => {
    // Disable transaction buttons when period is closed
    if (period?.status === 'CLOSED') {
      document.querySelectorAll('[data-transaction-button]').forEach(button => {
        (button as HTMLButtonElement).disabled = true;
      });
    }
  }, [period]);
  
  if (!period) return null;
  
  return (
    <Chip
      label={`Period: ${period.month} ${period.year} - ${period.status}`}
      color={period.status === 'OPEN' ? 'success' : 'error'}
      sx={{ fontWeight: 'bold' }}
    />
  );
};
```

#### 9. OmniSearchBar Component

**Purpose**: Global member search

**Implementation**:
```typescript
export const OmniSearchBar: React.FC = () => {
  const [query, setQuery] = useState('');
  const [results, setResults] = useState<MemberSearchResult[]>([]);
  const [open, setOpen] = useState(false);
  const navigate = useNavigate();
  
  const handleSearch = useDebouncedCallback(async (searchQuery: string) => {
    if (searchQuery.length < 2) {
      setResults([]);
      return;
    }
    
    const members = await searchMembers(searchQuery, 5);
    setResults(members);
    setOpen(true);
  }, 300);
  
  const handleSelect = (memberId: number) => {
    navigate(`/members/${memberId}`);
    setQuery('');
    setOpen(false);
  };
  
  return (
    <Autocomplete
      freeSolo
      open={open}
      onClose={() => setOpen(false)}
      options={results}
      getOptionLabel={(option) => 
        typeof option === 'string' ? option : `${option.firstName} ${option.lastName}`
      }
      renderOption={(props, option) => (
        <Box component="li" {...props} onClick={() => handleSelect(option.id)}>
          <Box>
            <Typography variant="body2">
              {option.firstName} {option.lastName}
            </Typography>
            <Typography variant="caption" color="text.secondary">
              ID: {option.id} | Status: {option.status}
            </Typography>
          </Box>
        </Box>
      )}
      renderInput={(params) => (
        <TextField
          {...params}
          placeholder="Search members..."
          value={query}
          onChange={(e) => {
            setQuery(e.target.value);
            handleSearch(e.target.value);
          }}
          InputProps={{
            ...params.InputProps,
            startAdornment: <SearchIcon />
          }}
        />
      )}
    />
  );
};
```

## Data Models

### Backend DTOs

```java
// Member Financial Summary for Teller Action Card
@Data
public class MemberFinancialSummaryDTO {
    private Long memberId;
    private String memberName;
    private BigDecimal savingsBalance;
    private BigDecimal loanPrincipal;
    private String loanStatus;
    private boolean hasActiveLoan;
    private Long activeLoanId;
}

// Cash Box Tally
@Data
public class CashBoxTallyDTO {
    private LocalDate date;
    private BigDecimal totalIn;
    private BigDecimal totalOut;
    private BigDecimal netCash;
    private Map<String, BigDecimal> inBreakdown;  // deposit, repay_principal, etc.
    private Map<String, BigDecimal> outBreakdown; // withdraw, disburse_loan
}

// Transaction Feed Item
@Data
public class TransactionFeedItemDTO {
    private Long id;
    private LocalDateTime timestamp;
    private String memberName;
    private String transactionType;
    private BigDecimal amount;
    private String receiptId;
}

// Trial Balance Status
@Data
public class TrialBalanceStatusDTO {
    private BigDecimal totalDebits;
    private BigDecimal totalCredits;
    private boolean isBalanced;
    private BigDecimal imbalance;
    private LocalDate periodStart;
    private LocalDate periodEnd;
}

// PAR Analysis
@Data
public class PARAnalysisDTO {
    private List<LoanDetailDTO> standard;
    private List<LoanDetailDTO> watch;
    private List<LoanDetailDTO> substandard;
    private List<LoanDetailDTO> loss;
    private int totalLoans;
    private BigDecimal totalPrincipal;
}

@Data
public class LoanDetailDTO {
    private Long loanId;
    private String memberName;
    private BigDecimal principal;
    private int daysOverdue;
    private List<String> guarantors;
}

// Liquidity Gauge
@Data
public class LiquidityGaugeDTO {
    private BigDecimal liquidAssets;
    private BigDecimal totalDeposits;
    private BigDecimal ratio;
    private LiquidityStatus status;
}

public enum LiquidityStatus {
    CRISIS,    // < 5%
    CAUTION,   // 5-10%
    HEALTHY,   // 10-20%
    INEFFICIENT // > 20%
}

// Membership Growth
@Data
public class MembershipGrowthDTO {
    private List<MonthlyGrowthData> monthlyData;
}

@Data
public class MonthlyGrowthData {
    private String month;
    private int newMembers;
    private int resignations;
    private int netGrowth;
}

// Digital Passbook
@Data
public class DigitalPassbookDTO {
    private Long memberId;
    private BigDecimal balance;
    private LocalDate lastActivity;
    private List<PassbookTransactionDTO> recentTransactions;
}

// Loan Obligation
@Data
public class LoanObligationDTO {
    private Long loanId;
    private BigDecimal outstandingPrincipal;
    private LocalDate nextPaymentDue;
    private BigDecimal estimatedInterest;
    private boolean isOverdue;
}

// Dividend Estimate
@Data
public class DividendEstimateDTO {
    private BigDecimal totalShares;
    private BigDecimal projectedRate;
    private BigDecimal estimatedDividend;
    private String disclaimer;
}

// Fiscal Period
@Data
public class FiscalPeriodDTO {
    private String month;
    private int year;
    private FiscalPeriodStatus status;
    private LocalDate startDate;
    private LocalDate endDate;
}

public enum FiscalPeriodStatus {
    OPEN,
    CLOSED
}

// Member Search Result
@Data
public class MemberSearchResultDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String status;
}

// Transaction Result
@Data
public class TransactionResult {
    private boolean success;
    private Long transactionId;
    private String receiptId;
    private String errorMessage;
    
    public static TransactionResult success(Long transactionId, String receiptId) {
        TransactionResult result = new TransactionResult();
        result.setSuccess(true);
        result.setTransactionId(transactionId);
        result.setReceiptId(receiptId);
        return result;
    }
    
    public static TransactionResult error(String message) {
        TransactionResult result = new TransactionResult();
        result.setSuccess(false);
        result.setErrorMessage(message);
        return result;
    }
}
```

### Frontend TypeScript Interfaces

```typescript
interface MemberFinancials {
  memberId: number;
  memberName: string;
  balance: number;
  loanPrincipal: number;
  loanStatus: string;
  hasActiveLoan: boolean;
  loanId?: number;
}

interface CashBoxTallyData {
  date: string;
  totalIn: number;
  totalOut: number;
  netCash: number;
}

interface DenominationCount {
  [denomination: number]: number;
}

interface TransactionFeedItem {
  id: number;
  timestamp: string;
  memberName: string;
  type: string;
  amount: number;
}

interface TrialBalanceStatus {
  debits: number;
  credits: number;
  isBalanced: boolean;
}

interface PARAnalysisData {
  standard: LoanDetail[];
  watch: LoanDetail[];
  substandard: LoanDetail[];
  loss: LoanDetail[];
}

interface LoanDetail {
  loanId: number;
  memberName: string;
  principal: number;
  daysOverdue: number;
  guarantors: string[];
}

type PARCategory = 'STANDARD' | 'WATCH' | 'SUBSTANDARD' | 'LOSS';

interface LiquidityData {
  liquidAssets: number;
  totalDeposits: number;
  ratio: number;
  status: LiquidityStatus;
}

type LiquidityStatus = 'CRISIS' | 'CAUTION' | 'HEALTHY' | 'INEFFICIENT';

interface PassbookData {
  memberId: number;
  balance: number;
  lastActivity: string;
  recentTransactions: PassbookTransaction[];
}

interface PassbookTransaction {
  id: number;
  date: string;
  amount: number;
  type: string;
}

interface FiscalPeriodData {
  month: string;
  year: number;
  status: 'OPEN' | 'CLOSED';
}

interface MemberSearchResult {
  id: number;
  firstName: string;
  lastName: string;
  status: string;
}
```


## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system-essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

### Property 1: Member financial data fetch

*For any* member selected in the Teller Action Card, the widget should fetch and display the member's current savings balance, outstanding loan principal, and loan status

**Validates: Requirements 1.1**

### Property 2: Minimum interest calculation

*For any* loan payment submission, the calculated minimum interest due should equal the loan principal multiplied by the loan type's interest rate divided by the payment frequency

**Validates: Requirements 1.4**

### Property 3: Transaction form clearing

*For any* successful transaction submission, the form fields should be cleared and a success confirmation should be displayed

**Validates: Requirements 1.5**

### Property 4: Cash box total in calculation

*For any* set of transactions on a given date, the "Total In" should equal the sum of all deposits, repayments, and fees

**Validates: Requirements 2.1**

### Property 5: Cash box total out calculation

*For any* set of transactions on a given date, the "Total Out" should equal the sum of all withdrawals and loan disbursements

**Validates: Requirements 2.2**

### Property 6: Net cash calculation

*For any* cash box tally, the net cash should equal total in minus total out

**Validates: Requirements 2.3**

### Property 7: Physical cash variance calculation

*For any* set of denomination counts, the variance should equal the physical total (sum of denomination × count) minus the database net cash value

**Validates: Requirements 2.5**

### Property 8: Transaction feed ordering

*For any* set of transactions, the transaction feed should display them ordered by timestamp in descending order (most recent first)

**Validates: Requirements 3.1**

### Property 9: Transaction feed completeness

*For any* transaction displayed in the feed, it should include time, member name, transaction type, and amount

**Validates: Requirements 3.2**

### Property 10: Receipt icon presence

*For any* transaction in the feed, a receipt icon should be present

**Validates: Requirements 3.3**

### Property 11: New transaction at top

*For any* newly recorded transaction, it should appear at the top of the transaction feed after refresh

**Validates: Requirements 3.5**

### Property 12: Trial balance debit calculation

*For any* set of accounting entries in the active fiscal period, the sum of debits should equal the sum of all debit-type entries

**Validates: Requirements 4.1**

### Property 13: Trial balance credit calculation

*For any* set of accounting entries in the active fiscal period, the sum of credits should equal the sum of all credit-type entries

**Validates: Requirements 4.2**

### Property 14: Report generation blocking

*For any* unbalanced trial balance state (debits ≠ credits), the monthly report generation function should be disabled

**Validates: Requirements 4.5**

### Property 15: Unclassified transaction count

*For any* set of transactions, the unclassified count should equal the number of transactions where accounting code is null

**Validates: Requirements 5.1**

### Property 16: Unclassified warning display

*For any* unclassified transaction count greater than zero, a warning card should be displayed

**Validates: Requirements 5.2**

### Property 17: Income chart aggregation

*For any* set of accounting entries, the income chart should aggregate all entries from account code group 4xxx

**Validates: Requirements 6.1, 6.4**

### Property 18: Asset chart aggregation

*For any* set of accounting entries, the asset pie chart should aggregate all entries from account code group 1xxx

**Validates: Requirements 6.2, 6.5**

### Property 19: Chart data grouping

*For any* financial statement preview, the data should be aggregated by account type code groups

**Validates: Requirements 6.3**

### Property 20: PAR loan processing

*For any* set of active loans, the PAR analysis should process all loans and categorize each one

**Validates: Requirements 7.1**

### Property 21: PAR standard classification

*For any* loan where days since last payment is between 0 and 30, it should be classified as Standard

**Validates: Requirements 7.2**

### Property 22: PAR watch classification

*For any* loan where days since last payment is between 31 and 60, it should be classified as Watch

**Validates: Requirements 7.3**

### Property 23: PAR substandard classification

*For any* loan where days since last payment is between 61 and 90, it should be classified as Substandard

**Validates: Requirements 7.4**

### Property 24: PAR loss classification

*For any* loan where days since last payment is greater than 90, it should be classified as Loss

**Validates: Requirements 7.5**

### Property 25: Liquidity liquid assets calculation

*For any* set of account balances, the liquid assets should equal the sum of cash and bank balances

**Validates: Requirements 8.1**

### Property 26: Liquidity deposits calculation

*For any* set of member accounts, the total savings deposits should equal the sum of all member savings balances

**Validates: Requirements 8.2**

### Property 27: Liquidity ratio formula

*For any* liquidity calculation, the ratio should equal (liquid assets / total deposits) × 100

**Validates: Requirements 8.3**

### Property 28: Liquidity crisis status

*For any* liquidity ratio less than 5%, the status should be "Crisis" with red indicator

**Validates: Requirements 8.4**

### Property 29: Liquidity caution status

*For any* liquidity ratio between 5% and 10%, the status should be "Caution" with yellow indicator

**Validates: Requirements 8.5**

### Property 30: Liquidity healthy status

*For any* liquidity ratio between 10% and 20%, the status should be "Healthy" with green indicator

**Validates: Requirements 8.6**

### Property 31: Liquidity inefficient status

*For any* liquidity ratio greater than 20%, the status should be "Inefficient" with blue indicator

**Validates: Requirements 8.7**

### Property 32: Membership growth aggregation

*For any* set of members, the growth trend should aggregate new member counts by month

**Validates: Requirements 9.1, 9.2**

### Property 33: Resignation aggregation

*For any* set of members, the growth trend should aggregate resignation counts by month

**Validates: Requirements 9.3**

### Property 34: Digital passbook balance calculation

*For any* member, the passbook balance should equal the last month forward balance plus all current month deposits

**Validates: Requirements 10.1, 10.2**

### Property 35: Last activity timestamp

*For any* member passbook, the last activity timestamp should show the date of the most recent transaction

**Validates: Requirements 10.4**

### Property 36: Loan obligation display

*For any* member with an active loan, the loan obligation card should display outstanding principal, next payment due date, and estimated interest

**Validates: Requirements 11.1, 11.2, 11.3**

### Property 37: Overdue loan styling

*For any* loan where the next payment due date is in the past, the card border should be red

**Validates: Requirements 11.4**

### Property 38: Dividend calculation formula

*For any* member, the estimated dividend should equal total shares multiplied by projected dividend rate

**Validates: Requirements 12.3**

### Property 39: Dividend display completeness

*For any* dividend estimate display, it should include both the calculated amount and the disclaimer text

**Validates: Requirements 12.4**

### Property 40: Fiscal period data retrieval

*For any* dashboard load, the fiscal period indicator should query and display the current period status

**Validates: Requirements 13.1**

### Property 41: Transaction button disabling

*For any* closed fiscal period, all transaction buttons marked with data-transaction-button attribute should be disabled

**Validates: Requirements 13.4**

### Property 42: Fiscal period real-time update

*For any* fiscal period status change, the indicator should update without requiring a page refresh

**Validates: Requirements 13.5**

### Property 43: Omni-search query matching

*For any* search query, results should include members matching on member ID, first name, last name, or national ID

**Validates: Requirements 14.1**

### Property 44: Search result limit

*For any* search query, the number of results displayed should not exceed 5

**Validates: Requirements 14.2**

### Property 45: Search result completeness

*For any* search result, it should display the member's ID, full name, and status

**Validates: Requirements 14.3**

### Property 46: Widget error isolation

*For any* widget that fails to load, the error should be displayed only in that widget and all other widgets should continue rendering successfully

**Validates: Requirements 15.1, 15.3**

### Property 47: Error logging

*For any* widget error, the detailed error should be logged to the server console

**Validates: Requirements 15.2**

### Property 48: Retry button presence

*For any* widget displaying an error, a "Retry" button should be present

**Validates: Requirements 15.4**

### Property 49: Widget error recovery

*For any* widget error, the user should be able to retry without refreshing the entire page

**Validates: Requirements 15.5**


## Error Handling

### Backend Error Handling

#### 1. Transaction Validation Errors

**Strategy**: Validate business rules before processing transactions

```java
@Service
public class TransactionModalService {
    
    public TransactionResult processDeposit(Long memberId, BigDecimal amount, Long userId) {
        // Validate fiscal period
        if (!fiscalPeriodService.isTransactionAllowed()) {
            return TransactionResult.error("Cannot record transactions in closed period");
        }
        
        // Validate amount
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return TransactionResult.error("Amount must be greater than zero");
        }
        
        // Validate member exists and is active
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new MemberNotFoundException("Member not found"));
        
        if (!"ACTIVE".equals(member.getStatus())) {
            return TransactionResult.error("Cannot process transactions for inactive members");
        }
        
        try {
            // Process transaction
            // ...
            return TransactionResult.success(transaction.getId(), receiptId);
        } catch (Exception e) {
            log.error("Failed to process deposit for member {}", memberId, e);
            return TransactionResult.error("Transaction processing failed. Please try again.");
        }
    }
}
```

#### 2. Widget Data Calculation Errors

**Strategy**: Handle edge cases and provide fallback values

```java
@Service
public class LiquidityCalculationService {
    
    public LiquidityGaugeDTO calculateLiquidity() {
        try {
            BigDecimal liquidAssets = getTotalLiquidAssets();
            BigDecimal savingsDeposits = getTotalSavingsDeposits();
            
            // Handle division by zero
            if (savingsDeposits.compareTo(BigDecimal.ZERO) == 0) {
                log.warn("No savings deposits found, returning default liquidity gauge");
                return LiquidityGaugeDTO.createDefault();
            }
            
            BigDecimal ratio = liquidAssets
                .divide(savingsDeposits, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));
            
            LiquidityStatus status = determineStatus(ratio);
            
            return new LiquidityGaugeDTO(liquidAssets, savingsDeposits, ratio, status);
            
        } catch (DataAccessException e) {
            log.error("Database error calculating liquidity", e);
            throw new WidgetDataException("Unable to calculate liquidity ratio");
        }
    }
}
```

#### 3. PAR Analysis Edge Cases

**Strategy**: Handle loans with missing payment data

```java
@Service
public class PARAnalysisService {
    
    private int calculateDaysSinceLastPayment(Loan loan) {
        LocalDate lastPaymentDate = loan.getLastPaymentDate();
        
        // Handle loans with no payments yet
        if (lastPaymentDate == null) {
            // Use loan disbursement date as reference
            lastPaymentDate = loan.getDisbursementDate();
            if (lastPaymentDate == null) {
                log.warn("Loan {} has no payment or disbursement date", loan.getId());
                return 0; // Treat as current
            }
        }
        
        return (int) ChronoUnit.DAYS.between(lastPaymentDate, LocalDate.now());
    }
}
```

#### 4. Search Query Sanitization

**Strategy**: Prevent SQL injection and handle special characters

```java
@Service
public class OmniSearchService {
    
    public List<MemberSearchResultDTO> searchMembers(String query, int limit) {
        // Sanitize input
        if (query == null || query.trim().isEmpty()) {
            return Collections.emptyList();
        }
        
        // Remove SQL wildcards and special characters
        String sanitized = query.replaceAll("[%_]", "");
        
        // Limit query length to prevent performance issues
        if (sanitized.length() > 50) {
            sanitized = sanitized.substring(0, 50);
        }
        
        String searchPattern = "%" + sanitized + "%";
        
        try {
            return memberRepository.findBySearchPattern(
                searchPattern, 
                PageRequest.of(0, limit)
            ).stream()
                .map(this::toSearchResultDTO)
                .collect(Collectors.toList());
        } catch (DataAccessException e) {
            log.error("Search query failed for: {}", sanitized, e);
            throw new SearchException("Search temporarily unavailable");
        }
    }
}
```

### Frontend Error Handling

#### 1. Widget Data Fetch Failures

**Strategy**: Display user-friendly errors with retry capability

```typescript
export const useWidgetData = <T>(endpoint: string) => {
  const [data, setData] = useState<T | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  
  const fetchData = useCallback(async () => {
    setLoading(true);
    setError(null);
    
    try {
      const response = await fetch(endpoint);
      
      if (!response.ok) {
        if (response.status === 403) {
          throw new Error('You do not have permission to view this data');
        }
        if (response.status === 404) {
          throw new Error('Data not found');
        }
        throw new Error('Failed to load data. Please try again.');
      }
      
      const json = await response.json();
      setData(json);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'An error occurred');
      console.error('Widget data fetch failed:', err);
    } finally {
      setLoading(false);
    }
  }, [endpoint]);
  
  useEffect(() => {
    fetchData();
  }, [fetchData]);
  
  return { data, loading, error, retry: fetchData };
};
```

#### 2. Transaction Submission Errors

**Strategy**: Show validation errors inline and allow correction

```typescript
export const DepositModal: React.FC<DepositModalProps> = ({
  open,
  onClose,
  onSubmit
}) => {
  const [amount, setAmount] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);
  
  const handleSubmit = async () => {
    setError(null);
    
    // Client-side validation
    const numAmount = parseFloat(amount);
    if (isNaN(numAmount) || numAmount <= 0) {
      setError('Please enter a valid amount greater than zero');
      return;
    }
    
    setSubmitting(true);
    
    try {
      const result = await submitDeposit(numAmount);
      
      if (!result.success) {
        setError(result.errorMessage || 'Transaction failed');
        return;
      }
      
      onSubmit(numAmount);
      setAmount('');
      onClose();
    } catch (err) {
      setError('Network error. Please check your connection and try again.');
    } finally {
      setSubmitting(false);
    }
  };
  
  return (
    <Dialog open={open} onClose={onClose}>
      <DialogTitle>Record Deposit</DialogTitle>
      <DialogContent>
        <TextField
          label="Amount"
          type="number"
          value={amount}
          onChange={(e) => setAmount(e.target.value)}
          error={!!error}
          helperText={error}
          fullWidth
          autoFocus
        />
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose} disabled={submitting}>
          Cancel
        </Button>
        <Button 
          onClick={handleSubmit} 
          variant="contained"
          disabled={submitting}
        >
          {submitting ? 'Processing...' : 'Submit'}
        </Button>
      </DialogActions>
    </Dialog>
  );
};
```

#### 3. Chart Rendering Errors

**Strategy**: Gracefully handle missing or invalid chart data

```typescript
export const PARAnalysisWidget: React.FC = () => {
  const { data, loading, error, retry } = useWidgetData<PARAnalysisData>(
    '/api/dashboard/widgets/par-analysis'
  );
  
  if (loading) {
    return (
      <Card>
        <CardContent>
          <Skeleton variant="circular" width={200} height={200} />
        </CardContent>
      </Card>
    );
  }
  
  if (error) {
    return (
      <Card>
        <CardContent>
          <Alert severity="error">
            <AlertTitle>Unable to Load PAR Analysis</AlertTitle>
            {error}
            <Button onClick={retry} sx={{ mt: 1 }}>
              Retry
            </Button>
          </Alert>
        </CardContent>
      </Card>
    );
  }
  
  if (!data || data.totalLoans === 0) {
    return (
      <Card>
        <CardContent>
          <Typography variant="h6">Portfolio at Risk (PAR)</Typography>
          <Alert severity="info" sx={{ mt: 2 }}>
            No active loans to analyze
          </Alert>
        </CardContent>
      </Card>
    );
  }
  
  // Render chart...
};
```

#### 4. Network Timeout Handling

**Strategy**: Implement timeout with user feedback

```typescript
const fetchWithTimeout = async (
  url: string,
  options: RequestInit = {},
  timeout: number = 10000
): Promise<Response> => {
  const controller = new AbortController();
  const timeoutId = setTimeout(() => controller.abort(), timeout);
  
  try {
    const response = await fetch(url, {
      ...options,
      signal: controller.signal
    });
    clearTimeout(timeoutId);
    return response;
  } catch (error) {
    clearTimeout(timeoutId);
    if (error.name === 'AbortError') {
      throw new Error('Request timed out. Please check your connection.');
    }
    throw error;
  }
};
```

## Testing Strategy

### Dual Testing Approach

The dashboard widgets will employ both unit testing and property-based testing:

- **Unit tests** verify specific examples, UI interactions, and edge cases
- **Property tests** verify universal properties that should hold across all inputs
- Together they provide comprehensive coverage: unit tests catch concrete bugs, property tests verify general correctness

### Unit Testing

#### Backend Unit Tests (JUnit + Mockito)

**Test Coverage**:
- Widget data aggregation with known inputs
- Transaction processing with validation
- PAR categorization for specific day ranges
- Liquidity status determination
- Search query handling
- Error scenarios and edge cases

**Example Unit Tests**:
```java
@ExtendWith(MockitoExtension.class)
class WidgetDataServiceTest {
    
    @Mock
    private MemberRepository memberRepository;
    
    @Mock
    private SavingAccountRepository savingAccountRepository;
    
    @Mock
    private LoanRepository loanRepository;
    
    @InjectMocks
    private WidgetDataService widgetDataService;
    
    @Test
    void shouldCalculateCashBoxTallyCorrectly() {
        LocalDate date = LocalDate.of(2023, 9, 15);
        
        List<SavingTransaction> transactions = Arrays.asList(
            createDeposit(1000.00),
            createDeposit(500.00),
            createWithdrawal(300.00)
        );
        
        when(transactionRepository.findByTransDate(date))
            .thenReturn(transactions);
        
        CashBoxTallyDTO result = widgetDataService.getCashBoxTally(date);
        
        assertEquals(new BigDecimal("1500.00"), result.getTotalIn());
        assertEquals(new BigDecimal("300.00"), result.getTotalOut());
        assertEquals(new BigDecimal("1200.00"), result.getNetCash());
    }
    
    @Test
    void shouldCategorizeLoanAsStandard() {
        Loan loan = createLoan();
        loan.setLastPaymentDate(LocalDate.now().minusDays(15));
        
        PARCategory category = parAnalysisService.categorizeLoan(
            calculateDaysSinceLastPayment(loan)
        );
        
        assertEquals(PARCategory.STANDARD, category);
    }
    
    @Test
    void shouldCategorizeLoanAsLoss() {
        Loan loan = createLoan();
        loan.setLastPaymentDate(LocalDate.now().minusDays(120));
        
        PARCategory category = parAnalysisService.categorizeLoan(
            calculateDaysSinceLastPayment(loan)
        );
        
        assertEquals(PARCategory.LOSS, category);
    }
    
    @Test
    void shouldReturnCrisisStatusForLowLiquidity() {
        BigDecimal ratio = new BigDecimal("3.5");
        
        LiquidityStatus status = liquidityCalculationService.determineStatus(ratio);
        
        assertEquals(LiquidityStatus.CRISIS, status);
    }
}
```

#### Frontend Unit Tests (Jest + React Testing Library)

**Test Coverage**:
- Widget rendering with mock data
- Modal opening and closing
- Form submission and validation
- Error display and retry functionality
- Chart rendering with various data sets

**Example Unit Tests**:
```typescript
describe('TellerActionCard', () => {
  it('should fetch member financials when member is selected', async () => {
    const mockFinancials = {
      memberId: 1,
      memberName: 'John Doe',
      balance: 10000,
      loanPrincipal: 5000,
      loanStatus: 'ACTIVE',
      hasActiveLoan: true
    };
    
    server.use(
      rest.get('/api/dashboard/widgets/member-financials/1', (req, res, ctx) => {
        return res(ctx.json(mockFinancials));
      })
    );
    
    const { getByText, findByText } = render(<TellerActionCard />);
    
    // Select member
    const autocomplete = screen.getByRole('combobox');
    fireEvent.change(autocomplete, { target: { value: 'John' } });
    fireEvent.click(await findByText('John Doe'));
    
    // Verify financials are displayed
    expect(await findByText('Balance: ฿10,000')).toBeInTheDocument();
    expect(await findByText('Loan: ฿5,000')).toBeInTheDocument();
  });
  
  it('should open deposit modal when deposit button clicked', () => {
    const { getByText } = render(<TellerActionCard />);
    
    // Assume member is already selected
    const depositButton = getByText('Deposit');
    fireEvent.click(depositButton);
    
    expect(screen.getByRole('dialog')).toBeInTheDocument();
    expect(screen.getByLabelText('Amount')).toBeInTheDocument();
  });
});

describe('CashBoxTally', () => {
  it('should calculate variance correctly', async () => {
    const mockTally = {
      date: '2023-09-15',
      totalIn: 50000,
      totalOut: 30000,
      netCash: 20000
    };
    
    server.use(
      rest.get('/api/dashboard/widgets/cash-box-tally', (req, res, ctx) => {
        return res(ctx.json(mockTally));
      })
    );
    
    const { getByText, getByLabelText } = render(
      <CashBoxTally date={new Date('2023-09-15')} />
    );
    
    // Wait for data to load
    await waitFor(() => {
      expect(getByText('฿20,000')).toBeInTheDocument();
    });
    
    // Click "Count Cash"
    fireEvent.click(getByText('Count Cash'));
    
    // Enter denominations
    fireEvent.change(getByLabelText('฿1000'), { target: { value: '15' } });
    fireEvent.change(getByLabelText('฿500'), { target: { value: '10' } });
    
    // Verify variance calculation
    // Physical: (15 * 1000) + (10 * 500) = 20000
    // Variance: 20000 - 20000 = 0
    expect(getByText('Variance: ฿0')).toBeInTheDocument();
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
**Feature: dashboard-widgets, Property {number}: {property_text}**
```

**Backend Property Tests (jqwik)**:

```java
class WidgetPropertyTest {
    
    @Provide
    Arbitrary<List<SavingTransaction>> transactions() {
        return Arbitraries.integers().between(0, 100)
            .flatMap(count -> 
                Arbitraries.of(
                    createDeposit(),
                    createWithdrawal(),
                    createRepayment()
                ).list().ofSize(count)
            );
    }
    
    @Provide
    Arbitrary<Loan> loans() {
        return Arbitraries.integers().between(0, 365)
            .map(daysOverdue -> {
                Loan loan = createLoan();
                loan.setLastPaymentDate(LocalDate.now().minusDays(daysOverdue));
                return loan;
            });
    }
    
    /**
     * Feature: dashboard-widgets, Property 4: Cash box total in calculation
     */
    @Property
    void totalInShouldEqualSumOfIncomingTransactions(
        @ForAll("transactions") List<SavingTransaction> transactions
    ) {
        LocalDate date = LocalDate.now();
        transactions.forEach(t -> t.setTransDate(date));
        
        // Calculate expected
        BigDecimal expected = transactions.stream()
            .filter(t -> isIncomingType(t.getType()))
            .map(SavingTransaction::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Test
        when(transactionRepository.findByTransDate(date)).thenReturn(transactions);
        CashBoxTallyDTO result = widgetDataService.getCashBoxTally(date);
        
        assertEquals(expected, result.getTotalIn());
    }
    
    /**
     * Feature: dashboard-widgets, Property 6: Net cash calculation
     */
    @Property
    void netCashShouldEqualTotalInMinusTotalOut(
        @ForAll("transactions") List<SavingTransaction> transactions
    ) {
        LocalDate date = LocalDate.now();
        transactions.forEach(t -> t.setTransDate(date));
        
        when(transactionRepository.findByTransDate(date)).thenReturn(transactions);
        CashBoxTallyDTO result = widgetDataService.getCashBoxTally(date);
        
        BigDecimal expectedNet = result.getTotalIn().subtract(result.getTotalOut());
        
        assertEquals(expectedNet, result.getNetCash());
    }
    
    /**
     * Feature: dashboard-widgets, Property 21: PAR standard classification
     */
    @Property
    void loansShouldBeClassifiedAsStandardWhen0To30DaysOverdue(
        @ForAll @IntRange(min = 0, max = 30) int daysOverdue
    ) {
        Loan loan = createLoan();
        loan.setLastPaymentDate(LocalDate.now().minusDays(daysOverdue));
        
        int daysSince = parAnalysisService.calculateDaysSinceLastPayment(loan);
        PARCategory category = parAnalysisService.categorizeLoan(daysSince);
        
        assertEquals(PARCategory.STANDARD, category);
    }
    
    /**
     * Feature: dashboard-widgets, Property 22: PAR watch classification
     */
    @Property
    void loansShouldBeClassifiedAsWatchWhen31To60DaysOverdue(
        @ForAll @IntRange(min = 31, max = 60) int daysOverdue
    ) {
        Loan loan = createLoan();
        loan.setLastPaymentDate(LocalDate.now().minusDays(daysOverdue));
        
        int daysSince = parAnalysisService.calculateDaysSinceLastPayment(loan);
        PARCategory category = parAnalysisService.categorizeLoan(daysSince);
        
        assertEquals(PARCategory.WATCH, category);
    }
    
    /**
     * Feature: dashboard-widgets, Property 27: Liquidity ratio formula
     */
    @Property
    void liquidityRatioShouldEqualLiquidAssetsDividedByDepositsTimesHundred(
        @ForAll @BigRange(min = "1000", max = "1000000") BigDecimal liquidAssets,
        @ForAll @BigRange(min = "1000", max = "1000000") BigDecimal deposits
    ) {
        when(accountingRepository.sumCashAndBankBalances()).thenReturn(liquidAssets);
        when(savingAccountRepository.sumAllBalances()).thenReturn(deposits);
        
        LiquidityGaugeDTO result = liquidityCalculationService.calculateLiquidity();
        
        BigDecimal expectedRatio = liquidAssets
            .divide(deposits, 4, RoundingMode.HALF_UP)
            .multiply(new BigDecimal("100"));
        
        assertEquals(expectedRatio, result.getRatio());
    }
    
    /**
     * Feature: dashboard-widgets, Property 28-31: Liquidity status classification
     */
    @Property
    void liquidityStatusShouldMatchRatioRange(
        @ForAll @BigRange(min = "0", max = "100") BigDecimal ratio
    ) {
        LiquidityStatus status = liquidityCalculationService.determineStatus(ratio);
        
        if (ratio.compareTo(new BigDecimal("5")) < 0) {
            assertEquals(LiquidityStatus.CRISIS, status);
        } else if (ratio.compareTo(new BigDecimal("10")) < 0) {
            assertEquals(LiquidityStatus.CAUTION, status);
        } else if (ratio.compareTo(new BigDecimal("20")) <= 0) {
            assertEquals(LiquidityStatus.HEALTHY, status);
        } else {
            assertEquals(LiquidityStatus.INEFFICIENT, status);
        }
    }
    
    /**
     * Feature: dashboard-widgets, Property 38: Dividend calculation formula
     */
    @Property
    void dividendShouldEqualSharesTimesRate(
        @ForAll @BigRange(min = "0", max = "100000") BigDecimal shares,
        @ForAll @BigRange(min = "0.01", max = "0.20") BigDecimal rate
    ) {
        Member member = createMember();
        when(savingAccountRepository.findByMemberId(member.getId()))
            .thenReturn(Optional.of(createAccountWithShares(shares)));
        when(systemConfigRepository.getProjectedDividendRate())
            .thenReturn(rate);
        
        DividendEstimateDTO result = widgetDataService.getDividendEstimate(member.getId());
        
        BigDecimal expected = shares.multiply(rate).setScale(2, RoundingMode.HALF_UP);
        
        assertEquals(expected, result.getEstimatedDividend());
    }
    
    /**
     * Feature: dashboard-widgets, Property 44: Search result limit
     */
    @Property
    void searchResultsShouldNeverExceedLimit(
        @ForAll String query,
        @ForAll @IntRange(min = 1, max = 10) int limit
    ) {
        List<MemberSearchResultDTO> results = omniSearchService.searchMembers(query, limit);
        
        assertTrue(results.size() <= limit);
    }
}
```

**Frontend Property Tests (fast-check)**:

```typescript
import fc from 'fast-check';

describe('Dashboard Widgets Property Tests', () => {
  /**
   * Feature: dashboard-widgets, Property 7: Physical cash variance calculation
   */
  it('should calculate variance as physical total minus database total', () => {
    fc.assert(
      fc.property(
        fc.record({
          1000: fc.integer({ min: 0, max: 100 }),
          500: fc.integer({ min: 0, max: 100 }),
          100: fc.integer({ min: 0, max: 100 }),
          50: fc.integer({ min: 0, max: 100 }),
          20: fc.integer({ min: 0, max: 100 })
        }),
        fc.integer({ min: 0, max: 100000 }),
        (denominations, databaseTotal) => {
          const physicalTotal = Object.entries(denominations).reduce(
            (sum, [denom, count]) => sum + (parseInt(denom) * count),
            0
          );
          
          const expectedVariance = physicalTotal - databaseTotal;
          
          const { getByText } = render(
            <CashBoxTally 
              date={new Date()} 
              tally={{ totalIn: 0, totalOut: 0, netCash: databaseTotal }}
              denominations={denominations}
            />
          );
          
          expect(getByText(`Variance: ฿${expectedVariance.toLocaleString()}`))
            .toBeInTheDocument();
        }
      ),
      { numRuns: 100 }
    );
  });
  
  /**
   * Feature: dashboard-widgets, Property 8: Transaction feed ordering
   */
  it('should display transactions in descending timestamp order', () => {
    fc.assert(
      fc.property(
        fc.array(
          fc.record({
            id: fc.integer(),
            timestamp: fc.date(),
            memberName: fc.string(),
            type: fc.constantFrom('DEPOSIT', 'WITHDRAWAL', 'LOAN_PAY'),
            amount: fc.integer({ min: 1, max: 100000 })
          }),
          { minLength: 2, maxLength: 20 }
        ),
        (transactions) => {
          const { container } = render(
            <TransactionFeed transactions={transactions} />
          );
          
          const displayedTimestamps = Array.from(
            container.querySelectorAll('[data-timestamp]')
          ).map(el => new Date(el.getAttribute('data-timestamp')!));
          
          // Verify descending order
          for (let i = 0; i < displayedTimestamps.length - 1; i++) {
            expect(displayedTimestamps[i].getTime())
              .toBeGreaterThanOrEqual(displayedTimestamps[i + 1].getTime());
          }
        }
      ),
      { numRuns: 100 }
    );
  });
  
  /**
   * Feature: dashboard-widgets, Property 34: Digital passbook balance calculation
   */
  it('should calculate balance as forward plus current month deposits', () => {
    fc.assert(
      fc.property(
        fc.integer({ min: 0, max: 100000 }),
        fc.array(fc.integer({ min: 1, max: 10000 }), { maxLength: 20 }),
        (forwardBalance, deposits) => {
          const expectedBalance = forwardBalance + deposits.reduce((a, b) => a + b, 0);
          
          const passbook = {
            memberId: 1,
            forwardBalance,
            currentMonthDeposits: deposits,
            balance: expectedBalance
          };
          
          const { getByText } = render(<DigitalPassbook data={passbook} />);
          
          expect(getByText(`฿${expectedBalance.toLocaleString()}`))
            .toBeInTheDocument();
        }
      ),
      { numRuns: 100 }
    );
  });
  
  /**
   * Feature: dashboard-widgets, Property 46: Widget error isolation
   */
  it('should continue rendering other widgets when one fails', () => {
    fc.assert(
      fc.property(
        fc.integer({ min: 0, max: 4 }), // Index of widget to fail
        fc.array(fc.string(), { minLength: 5, maxLength: 5 }), // Widget IDs
        async (failIndex, widgetIds) => {
          server.use(
            rest.get('/api/dashboard/widgets/:widgetId', (req, res, ctx) => {
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
          
          await waitFor(() => {
            // Failed widget shows error
            const failedWidget = container.querySelector(
              `[data-widget-id="${widgetIds[failIndex]}"]`
            );
            expect(failedWidget).toHaveTextContent(/error/i);
            
            // Other widgets loaded successfully
            widgetIds.forEach((id, index) => {
              if (index !== failIndex) {
                const widget = container.querySelector(`[data-widget-id="${id}"]`);
                expect(widget).toHaveAttribute('data-loaded', 'true');
              }
            });
          });
        }
      ),
      { numRuns: 100 }
    );
  });
  
  /**
   * Feature: dashboard-widgets, Property 41: Transaction button disabling
   */
  it('should disable all transaction buttons when period is closed', () => {
    fc.assert(
      fc.property(
        fc.constantFrom('OPEN', 'CLOSED'),
        (periodStatus) => {
          const { container } = render(
            <OfficerDashboard fiscalPeriodStatus={periodStatus} />
          );
          
          const transactionButtons = container.querySelectorAll(
            '[data-transaction-button]'
          );
          
          transactionButtons.forEach(button => {
            if (periodStatus === 'CLOSED') {
              expect(button).toBeDisabled();
            } else {
              expect(button).not.toBeDisabled();
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

Integration tests verify end-to-end widget functionality including database interactions:

```java
@SpringBootTest
@AutoConfigureMockMvc
class DashboardWidgetIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private MemberRepository memberRepository;
    
    @Autowired
    private SavingTransactionRepository transactionRepository;
    
    @Test
    @WithMockUser(roles = "OFFICER")
    void shouldProcessDepositAndUpdateCashBoxTally() throws Exception {
        // Setup
        Member member = createAndSaveMember();
        LocalDate today = LocalDate.now();
        
        // Record deposit
        mockMvc.perform(post("/api/dashboard/transactions/deposit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.format(
                    "{\"memberId\": %d, \"amount\": 1000.00}",
                    member.getId()
                )))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
        
        // Verify cash box tally updated
        mockMvc.perform(get("/api/dashboard/widgets/cash-box-tally")
                .param("date", today.toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalIn").value(1000.00));
    }
    
    @Test
    @WithMockUser(roles = "PRESIDENT")
    void shouldCalculatePARAnalysisCorrectly() throws Exception {
        // Setup loans with different overdue statuses
        createLoanWithPaymentDate(LocalDate.now().minusDays(15)); // Standard
        createLoanWithPaymentDate(LocalDate.now().minusDays(45)); // Watch
        createLoanWithPaymentDate(LocalDate.now().minusDays(75)); // Substandard
        createLoanWithPaymentDate(LocalDate.now().minusDays(120)); // Loss
        
        // Test
        mockMvc.perform(get("/api/dashboard/widgets/par-analysis"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.standard.length()").value(1))
            .andExpect(jsonPath("$.watch.length()").value(1))
            .andExpect(jsonPath("$.substandard.length()").value(1))
            .andExpect(jsonPath("$.loss.length()").value(1));
    }
    
    @Test
    @WithMockUser(roles = "SECRETARY")
    void shouldBlockReportGenerationWhenUnbalanced() throws Exception {
        // Create unbalanced accounting entries
        createAccountingEntry("DEBIT", new BigDecimal("10000"));
        createAccountingEntry("CREDIT", new BigDecimal("9500"));
        
        // Verify trial balance shows unbalanced
        mockMvc.perform(get("/api/dashboard/widgets/trial-balance"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.isBalanced").value(false));
        
        // Verify report generation is blocked
        mockMvc.perform(post("/api/reports/monthly"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value(containsString("unbalanced")));
    }
}
```

---

**Related Documentation**:
- [Dashboard Data Pipeline](../dashboard-data-pipeline/design.md) - Infrastructure and data aggregation
- [Role-Based Dashboard System](../role-based-dashboard-system/design.md) - Dashboard layout and routing
- [Database Schema](../../docs/architecture/database-schema.md) - Entity relationships
- [System Design](../../docs/architecture/system-design.md) - Overall architecture

