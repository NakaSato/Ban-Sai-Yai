# Sprint 6: Financial Reporting Service

## Overview

Sprint 6 implements **Financial Reporting Service** which generates comprehensive financial reports, balance sheets, and profit & loss statements. This sprint provides financial insights and regulatory reporting capabilities.

## Sprint Objectives

### Primary Goals
- ✅ Implement balance sheet generation
- ✅ Create profit & loss statements
- ✅ Set up loan portfolio reports
- ✅ Develop cash flow statements
- ✅ Implement export functionality

### Success Criteria
- Financial reports are accurate and balanced
- Reports can be generated for any date range
- Export to multiple formats (PDF, Excel, CSV)
- Regulatory compliance is maintained
- Report generation is performant

## Technical Implementation

### 1. Entity Classes

#### FinancialReport Entity
```java
@Entity
@Table(name = "financial_report")
public class FinancialReport {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reportId;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ReportType reportType;
    
    @Column(name = "report_name", nullable = false, length = 100)
    private String reportName;
    
    @Column(name = "period_start", nullable = false)
    private LocalDate periodStart;
    
    @Column(name = "period_end", nullable = false)
    private LocalDate periodEnd;
    
    @Column(name = "generated_date", nullable = false)
    private LocalDate generatedDate;
    
    @Column(name = "generated_by", nullable = false, length = 100)
    private String generatedBy;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private ReportStatus status;
    
    @Column(name = "file_path")
    private String filePath;
    
    @Column(name = "file_format", length = 10)
    private String fileFormat;
    
    @Column(name = "file_size")
    private Long fileSize;
    
    @Column(name = "generation_time_ms")
    private Long generationTimeMs;
    
    @Column(length = 1000)
    private String parameters;
    
    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    // Constructors, getters, setters
}
```

#### ReportCache Entity
```java
@Entity
@Table(name = "report_cache")
public class ReportCache {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cacheId;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ReportType reportType;
    
    @Column(name = "cache_key", nullable = false, length = 255)
    private String cacheKey;
    
    @Column(name = "report_data", columnDefinition = "TEXT")
    private String reportData;
    
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
    
    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    // Constructors, getters, setters
    
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
}
```

### 2. Enums

#### ReportType
```java
public enum ReportType {
    BALANCE_SHEET("Balance Sheet", "BS"),
    PROFIT_LOSS("Profit & Loss Statement", "PL"),
    CASH_FLOW("Cash Flow Statement", "CF"),
    LOAN_PORTFOLIO("Loan Portfolio", "LP"),
    MEMBER_STATISTICS("Member Statistics", "MS"),
    AGING_REPORT("Aging Report", "AR"),
    TRIAL_BALANCE("Trial Balance", "TB"),
    INCOME_EXPENSE("Income & Expense", "IE");
    
    private final String description;
    private final String prefix;
    
    ReportType(String description, String prefix) {
        this.description = description;
        this.prefix = prefix;
    }
    
    // Getters
}
```

#### ReportStatus
```java
public enum ReportStatus {
    GENERATING("Generating"),
    COMPLETED("Completed"),
    FAILED("Failed"),
    CANCELLED("Cancelled");
    
    private final String description;
    
    ReportStatus(String description) {
        this.description = description;
    }
    
    // Getters
}
```

### 3. Data Transfer Objects (DTOs)

#### BalanceSheetDTO
```java
public class BalanceSheetDTO {
    private LocalDate reportDate;
    private String organizationName;
    private String currency;
    
    private BalanceSheetSection assets;
    private BalanceSheetSection liabilities;
    private BalanceSheetSection equity;
    
    private BigDecimal totalAssets;
    private BigDecimal totalLiabilities;
    private BigDecimal totalEquity;
    
    private List<BalanceSheetItem> assetItems;
    private List<BalanceSheetItem> liabilityItems;
    private List<BalanceSheetItem> equityItems;
    
    // Constructors, getters, setters
}
```

#### ProfitLossDTO
```java
public class ProfitLossDTO {
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private String organizationName;
    private String currency;
    
    private ProfitLossSection revenues;
    private ProfitLossSection expenses;
    
    private BigDecimal totalRevenues;
    private BigDecimal totalExpenses;
    private BigDecimal netIncome;
    
    private List<ProfitLossItem> revenueItems;
    private List<ProfitLossItem> expenseItems;
    
    // Constructors, getters, setters
}
```

#### ReportRequestDTO
```java
public class ReportRequestDTO {
    
    @NotNull(message = "Report type is required")
    private ReportType reportType;
    
    @NotNull(message = "Period start is required")
    private LocalDate periodStart;
    
    @NotNull(message = "Period end is required")
    private LocalDate periodEnd;
    
    private String reportFormat = "PDF"; // PDF, EXCEL, CSV
    
    private List<String> includeSections;
    
    private Map<String, Object> parameters;
    
    private Boolean cacheResult = true;
    
    private Integer timeoutMinutes = 30;
    
    // Constructors, getters, setters
}
```

### 4. Repository Layer

#### FinancialReportRepository
```java
@Repository
public interface FinancialReportRepository extends JpaRepository<FinancialReport, Long> {
    
    List<FinancialReport> findByReportTypeOrderByGeneratedDateDesc(ReportType reportType);
    
    @Query("SELECT fr FROM FinancialReport fr WHERE fr.reportType = :reportType " +
           "AND fr.periodStart = :periodStart AND fr.periodEnd = :periodEnd")
    List<FinancialReport> findByPeriod(@Param("reportType") ReportType reportType,
                                     @Param("periodStart") LocalDate periodStart,
                                     @Param("periodEnd") LocalDate periodEnd);
    
    @Query("SELECT fr FROM FinancialReport fr WHERE fr.generatedBy = :username ORDER BY fr.generatedDate DESC")
    List<FinancialReport> findByGeneratedByOrderByGeneratedDateDesc(@Param("username") String username);
    
    @Query("SELECT COUNT(fr) FROM FinancialReport fr WHERE fr.status = :status AND fr.createdAt BETWEEN :startDate AND :endDate")
    long countByStatusAndDateRange(@Param("status") ReportStatus status,
                                   @Param("startDate") LocalDateTime startDate,
                                   @Param("endDate") LocalDateTime endDate);
}
```

#### ReportCacheRepository
```java
@Repository
public interface ReportCacheRepository extends JpaRepository<ReportCache, Long> {
    
    Optional<ReportCache> findByReportTypeAndCacheKey(ReportType reportType, String cacheKey);
    
    @Query("DELETE FROM ReportCache rc WHERE rc.expiresAt < :now")
    void deleteExpired(@Param("now") LocalDateTime now);
    
    @Query("SELECT COUNT(rc) FROM ReportCache rc WHERE rc.isExpired() = true")
    long countExpired();
}
```

### 5. Service Layer

#### FinancialReportService
```java
@Service
@Transactional
public class FinancialReportService {
    
    @Autowired
    private FinancialReportRepository reportRepository;
    
    @Autowired
    private ReportCacheRepository cacheRepository;
    
    @Autowired
    private TransactionService transactionService;
    
    @Autowired
    private LoanRepository loanRepository;
    
    @Autowired
    private SavingAccountRepository savingAccountRepository;
    
    @Autowired
    private ReportGeneratorService reportGeneratorService;
    
    @Autowired
    private FileStorageService fileStorageService;
    
    @Autowired
    private AsyncReportGenerator asyncReportGenerator;
    
    public FinancialReport generateReport(ReportRequestDTO reportRequest, String username) {
        // Check cache first
        if (reportRequest.getCacheResult()) {
            Optional<ReportCache> cached = checkCache(reportRequest);
            if (cached.isPresent() && !cached.get().isExpired()) {
                return createReportFromCache(cached.get(), reportRequest, username);
            }
        }
        
        // Create report record
        FinancialReport report = createReportRecord(reportRequest, username);
        reportRepository.save(report);
        
        try {
            // Generate report based on type
            switch (reportRequest.getReportType()) {
                case BALANCE_SHEET:
                    return generateBalanceSheetReport(report, reportRequest);
                case PROFIT_LOSS:
                    return generateProfitLossReport(report, reportRequest);
                case LOAN_PORTFOLIO:
                    return generateLoanPortfolioReport(report, reportRequest);
                case CASH_FLOW:
                    return generateCashFlowReport(report, reportRequest);
                default:
                    throw new ValidationException("Unsupported report type");
            }
        } catch (Exception e) {
            report.setStatus(ReportStatus.FAILED);
            reportRepository.save(report);
            throw new ReportGenerationException("Failed to generate report", e);
        }
    }
    
    public CompletableFuture<FinancialReport> generateReportAsync(ReportRequestDTO reportRequest, String username) {
        return CompletableFuture.supplyAsync(() -> {
            return generateReport(reportRequest, username);
        }, asyncReportGenerator.getExecutor());
    }
    
    @Transactional(readOnly = true)
    public byte[] downloadReport(Long reportId) throws IOException {
        FinancialReport report = reportRepository.findById(reportId)
            .orElseThrow(() -> new ResourceNotFoundException("Report not found"));
        
        if (report.getFilePath() == null) {
            throw new ResourceNotFoundException("Report file not available");
        }
        
        return fileStorageService.getFile(report.getFilePath());
    }
    
    @Transactional(readOnly = true)
    public List<FinancialReport> getReportHistory(ReportType reportType, int limit) {
        return reportRepository.findByReportTypeOrderByGeneratedDateDesc(reportType)
            .stream()
            .limit(limit)
            .collect(Collectors.toList());
    }
    
    private FinancialReport generateBalanceSheetReport(FinancialReport report, ReportRequestDTO request) {
        BalanceSheetDTO balanceSheet = calculateBalanceSheet(request.getPeriodStart(), request.getPeriodEnd());
        
        // Generate PDF/Excel file
        byte[] reportBytes = reportGeneratorService.generateBalanceSheet(balanceSheet, request.getReportFormat());
        
        // Save file
        String fileName = generateFileName(request.getReportType(), request.getPeriodEnd(), request.getReportFormat());
        String filePath = fileStorageService.saveFile(reportBytes, "reports/" + fileName);
        
        // Update report record
        report.setFilePath(filePath);
        report.setFileSize((long) reportBytes.length);
        report.setFileFormat(request.getReportFormat());
        report.setStatus(ReportStatus.COMPLETED);
        report.setGenerationTimeMs(System.currentTimeMillis() - report.getCreatedAt().toEpochSecond(ZoneOffset.UTC) * 1000);
        
        // Cache result
        cacheReport(request, balanceSheet);
        
        return reportRepository.save(report);
    }
    
    private FinancialReport generateProfitLossReport(FinancialReport report, ReportRequestDTO request) {
        ProfitLossDTO profitLoss = calculateProfitLoss(request.getPeriodStart(), request.getPeriodEnd());
        
        // Generate PDF/Excel file
        byte[] reportBytes = reportGeneratorService.generateProfitLoss(profitLoss, request.getReportFormat());
        
        // Save file
        String fileName = generateFileName(request.getReportType(), request.getPeriodEnd(), request.getReportFormat());
        String filePath = fileStorageService.saveFile(reportBytes, "reports/" + fileName);
        
        // Update report record
        report.setFilePath(filePath);
        report.setFileSize((long) reportBytes.length);
        report.setFileFormat(request.getReportFormat());
        report.setStatus(ReportStatus.COMPLETED);
        report.setGenerationTimeMs(System.currentTimeMillis() - report.getCreatedAt().toEpochSecond(ZoneOffset.UTC) * 1000);
        
        // Cache result
        cacheReport(request, profitLoss);
        
        return reportRepository.save(report);
    }
    
    private BalanceSheetDTO calculateBalanceSheet(LocalDate startDate, LocalDate endDate) {
        BalanceSheetDTO balanceSheet = new BalanceSheetDTO();
        balanceSheet.setReportDate(endDate);
        balanceSheet.setOrganizationName("Ban Sai Yai Savings Group");
        balanceSheet.setCurrency("THB");
        
        // Calculate assets
        BigDecimal totalAssets = BigDecimal.ZERO;
        List<BalanceSheetItem> assetItems = new ArrayList<>();
        
        // Cash and cash equivalents
        BigDecimal cashBalance = transactionService.getCashBalance(endDate);
        assetItems.add(new BalanceSheetItem("1001", "Cash", cashBalance, "CURRENT_ASSET"));
        totalAssets = totalAssets.add(cashBalance);
        
        // Loans receivable
        BigDecimal loansReceivable = loanRepository.getOutstandingPrincipal(endDate);
        assetItems.add(new BalanceSheetItem("1002", "Loans Receivable", loansReceivable, "CURRENT_ASSET"));
        totalAssets = totalAssets.add(loansReceivable);
        
        // Calculate liabilities
        BigDecimal totalLiabilities = BigDecimal.ZERO;
        List<BalanceSheetItem> liabilityItems = new ArrayList<>();
        
        // Savings deposits
        BigDecimal savingsDeposits = savingAccountRepository.getTotalSavings();
        liabilityItems.add(new BalanceSheetItem("2001", "Savings Deposits", savingsDeposits, "CURRENT_LIABILITY"));
        totalLiabilities = totalLiabilities.add(savingsDeposits);
        
        // Calculate equity
        BigDecimal totalEquity = totalAssets.subtract(totalLiabilities);
        List<BalanceSheetItem> equityItems = new ArrayList<>();
        
        // Share capital
        BigDecimal shareCapital = savingAccountRepository.getTotalShareCapital();
        equityItems.add(new BalanceSheetItem("3001", "Share Capital", shareCapital, "EQUITY"));
        
        // Retained earnings
        BigDecimal retainedEarnings = transactionService.getRetainedEarnings(startDate, endDate);
        equityItems.add(new BalanceSheetItem("3002", "Retained Earnings", retainedEarnings, "EQUITY"));
        
        balanceSheet.setTotalAssets(totalAssets);
        balanceSheet.setTotalLiabilities(totalLiabilities);
        balanceSheet.setTotalEquity(totalEquity);
        balanceSheet.setAssetItems(assetItems);
        balanceSheet.setLiabilityItems(liabilityItems);
        balanceSheet.setEquityItems(equityItems);
        
        return balanceSheet;
    }
    
    private ProfitLossDTO calculateProfitLoss(LocalDate startDate, LocalDate endDate) {
        ProfitLossDTO profitLoss = new ProfitLossDTO();
        profitLoss.setPeriodStart(startDate);
        profitLoss.setPeriodEnd(endDate);
        profitLoss.setOrganizationName("Ban Sai Yai Savings Group");
        profitLoss.setCurrency("THB");
        
        // Calculate revenues
        BigDecimal totalRevenues = BigDecimal.ZERO;
        List<ProfitLossItem> revenueItems = new ArrayList<>();
        
        // Interest income from loans
        BigDecimal interestIncome = transactionService.getInterestIncome(startDate, endDate);
        revenueItems.add(new ProfitLossItem("4001", "Interest Income", interestIncome, "REVENUE"));
        totalRevenues = totalRevenues.add(interestIncome);
        
        // Calculate expenses
        BigDecimal totalExpenses = BigDecimal.ZERO;
        List<ProfitLossItem> expenseItems = new ArrayList<>();
        
        // Interest expense on savings
        BigDecimal interestExpense = transactionService.getInterestExpense(startDate, endDate);
        expenseItems.add(new ProfitLossItem("5001", "Interest Expense", interestExpense, "EXPENSE"));
        totalExpenses = totalExpenses.add(interestExpense);
        
        // Operating expenses
        BigDecimal operatingExpenses = transactionService.getOperatingExpenses(startDate, endDate);
        expenseItems.add(new ProfitLossItem("5002", "Operating Expenses", operatingExpenses, "EXPENSE"));
        totalExpenses = totalExpenses.add(operatingExpenses);
        
        BigDecimal netIncome = totalRevenues.subtract(totalExpenses);
        
        profitLoss.setTotalRevenues(totalRevenues);
        profitLoss.setTotalExpenses(totalExpenses);
        profitLoss.setNetIncome(netIncome);
        profitLoss.setRevenueItems(revenueItems);
        profitLoss.setExpenseItems(expenseItems);
        
        return profitLoss;
    }
    
    private Optional<ReportCache> checkCache(ReportRequestDTO request) {
        String cacheKey = generateCacheKey(request);
        return cacheRepository.findByReportTypeAndCacheKey(request.getReportType(), cacheKey);
    }
    
    private void cacheReport(ReportRequestDTO request, Object reportData) {
        try {
            String cacheKey = generateCacheKey(request);
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonData = objectMapper.writeValueAsString(reportData);
            
            ReportCache cache = new ReportCache();
            cache.setReportType(request.getReportType());
            cache.setCacheKey(cacheKey);
            cache.setReportData(jsonData);
            cache.setExpiresAt(LocalDateTime.now().plusHours(24)); // Cache for 24 hours
            
            cacheRepository.save(cache);
        } catch (Exception e) {
            log.warn("Failed to cache report", e);
        }
    }
    
    private String generateCacheKey(ReportRequestDTO request) {
        return String.format("%s_%s_%s_%s", 
            request.getReportType(), 
            request.getPeriodStart(), 
            request.getPeriodEnd(),
            request.getReportFormat());
    }
    
    private String generateFileName(ReportType reportType, LocalDate endDate, String format) {
        String dateStr = endDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return String.format("%s_%s.%s", reportType.getPrefix(), dateStr, format.toLowerCase());
    }
    
    private FinancialReport createReportFromCache(ReportCache cache, ReportRequestDTO request, String username) {
        FinancialReport report = new FinancialReport();
        report.setReportType(request.getReportType());
        report.setReportName(request.getReportType().getDescription());
        report.setPeriodStart(request.getPeriodStart());
        report.setPeriodEnd(request.getPeriodEnd());
        report.setGeneratedDate(LocalDate.now());
        report.setGeneratedBy(username);
        report.setStatus(ReportStatus.COMPLETED);
        report.setFileFormat(request.getReportFormat());
        report.setParameters(request.getParameters().toString());
        
        return reportRepository.save(report);
    }
    
    private FinancialReport createReportRecord(ReportRequestDTO request, String username) {
        FinancialReport report = new FinancialReport();
        report.setReportType(request.getReportType());
        report.setReportName(request.getReportType().getDescription());
        report.setPeriodStart(request.getPeriodStart());
        report.setPeriodEnd(request.getPeriodEnd());
        report.setGeneratedDate(LocalDate.now());
        report.setGeneratedBy(username);
        report.setStatus(ReportStatus.GENERATING);
        report.setFileFormat(request.getReportFormat());
        report.setParameters(request.getParameters().toString());
        
        return report;
    }
}
```

#### ReportGeneratorService
```java
@Service
public class ReportGeneratorService {
    
    @Autowired
    private PDFReportGenerator pdfGenerator;
    
    @Autowired
    private ExcelReportGenerator excelGenerator;
    
    @Autowired
    private CSVReportGenerator csvGenerator;
    
    public byte[] generateBalanceSheet(BalanceSheetDTO balanceSheet, String format) {
        switch (format.toUpperCase()) {
            case "PDF":
                return pdfGenerator.generateBalanceSheet(balanceSheet);
            case "EXCEL":
                return excelGenerator.generateBalanceSheet(balanceSheet);
            case "CSV":
                return csvGenerator.generateBalanceSheet(balanceSheet);
            default:
                throw new ValidationException("Unsupported report format: " + format);
        }
    }
    
    public byte[] generateProfitLoss(ProfitLossDTO profitLoss, String format) {
        switch (format.toUpperCase()) {
            case "PDF":
                return pdfGenerator.generateProfitLoss(profitLoss);
            case "EXCEL":
                return excelGenerator.generateProfitLoss(profitLoss);
            case "CSV":
                return csvGenerator.generateProfitLoss(profitLoss);
            default:
                throw new ValidationException("Unsupported report format: " + format);
        }
    }
    
    public byte[] generateLoanPortfolio(LoanPortfolioDTO portfolio, String format) {
        switch (format.toUpperCase()) {
            case "PDF":
                return pdfGenerator.generateLoanPortfolio(portfolio);
            case "EXCEL":
                return excelGenerator.generateLoanPortfolio(portfolio);
            case "CSV":
                return csvGenerator.generateLoanPortfolio(portfolio);
            default:
                throw new ValidationException("Unsupported report format: " + format);
        }
    }
}
```

### 6. Controller Layer

#### FinancialReportController
```java
@RestController
@RequestMapping("/api/reports/financial")
@Validated
public class FinancialReportController {
    
    @Autowired
    private FinancialReportService reportService;
    
    @PostMapping("/generate")
    @PreAuthorize("hasAnyRole('SECRETARY', 'PRESIDENT')")
    public ResponseEntity<ApiResponse<FinancialReport>> generateReport(
            @Valid @RequestBody ReportRequestDTO reportRequest,
            Authentication authentication) {
        
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String username = userDetails.getUsername();
        
        FinancialReport report = reportService.generateReport(reportRequest, username);
        
        return ResponseEntity.ok(ApiResponse.success(report, "Report generation initiated"));
    }
    
    @PostMapping("/generate-async")
    @PreAuthorize("hasAnyRole('SECRETARY', 'PRESIDENT')")
    public ResponseEntity<ApiResponse<String>> generateReportAsync(
            @Valid @RequestBody ReportRequestDTO reportRequest,
            Authentication authentication) {
        
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String username = userDetails.getUsername();
        
        CompletableFuture<FinancialReport> future = reportService.generateReportAsync(reportRequest, username);
        
        return ResponseEntity.ok(ApiResponse.success("Report generation started", "Report is being generated asynchronously"));
    }
    
    @GetMapping("/download/{reportId}")
    @PreAuthorize("hasAnyRole('SECRETARY', 'PRESIDENT')")
    public ResponseEntity<byte[]> downloadReport(@PathVariable Long reportId) throws IOException {
        
        byte[] reportBytes = reportService.downloadReport(reportId);
        
        FinancialReport report = reportService.getReportById(reportId);
        String filename = "report_" + report.getReportId() + "." + report.getFileFormat().toLowerCase();
        
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
            .header(HttpHeaders.CONTENT_TYPE, getContentType(report.getFileFormat()))
            .body(reportBytes);
    }
    
    @GetMapping("/history")
    @PreAuthorize("hasAnyRole('SECRETARY', 'PRESIDENT')")
    public ResponseEntity<ApiResponse<List<FinancialReport>>> getReportHistory(
            @RequestParam(required = false) ReportType reportType,
            @RequestParam(defaultValue = "10") int limit) {
        
        List<FinancialReport> reports;
        if (reportType != null) {
            reports = reportService.getReportHistory(reportType, limit);
        } else {
            reports = reportService.getAllReports(limit);
        }
        
        return ResponseEntity.ok(ApiResponse.success(reports));
    }
    
    @GetMapping("/balance-sheet")
    @PreAuthorize("hasAnyRole('SECRETARY', 'PRESIDENT')")
    public ResponseEntity<ApiResponse<BalanceSheetDTO>> getBalanceSheet(
            @RequestParam LocalDate asOfDate) {
        
        BalanceSheetDTO balanceSheet = reportService.getBalanceSheet(asOfDate);
        
        return ResponseEntity.ok(ApiResponse.success(balanceSheet));
    }
    
    @GetMapping("/profit-loss")
    @PreAuthorize("hasAnyRole('SECRETARY', 'PRESIDENT')")
    public ResponseEntity<ApiResponse<ProfitLossDTO>> getProfitLoss(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        
        ProfitLossDTO profitLoss = reportService.getProfitLoss(startDate, endDate);
        
        return ResponseEntity.ok(ApiResponse.success(profitLoss));
    }
    
    private String getContentType(String format) {
        switch (format.toUpperCase()) {
            case "PDF":
                return "application/pdf";
            case "EXCEL":
                return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "CSV":
                return "text/csv";
            default:
                return "application/octet-stream";
        }
    }
}
```

## Business Rules & Validation

### Report Generation Rules
1. **Period Validation**: End date must be after start date
2. **Date Range**: Maximum 12 months per report
3. **User Authorization**: Only Secretary and President can generate reports
4. **Data Integrity**: Reports must balance (assets = liabilities + equity)
5. **Currency Consistency**: All amounts in base currency (THB)

### Caching Rules
1. **Cache Duration**: 24 hours for most reports
2. **Cache Key**: Based on report type and parameters
3. **Cache Cleanup**: Automatic cleanup of expired entries
4. **Cache Invalidation**: Manual invalidation available for admins

### Export Rules
1. **Format Support**: PDF, Excel, CSV formats
2. **File Size Limits**: Maximum file size based on format
3. **Retention Policy**: Reports retained for 2 years
4. **Access Control**: Only authorized users can download

## Testing Strategy

### Unit Tests
```java
@ExtendWith(MockitoExtension.class)
class FinancialReportServiceTest {
    
    @Mock
    private FinancialReportRepository reportRepository;
    
    @Mock
    private TransactionService transactionService;
    
    @Mock
    private ReportGeneratorService reportGeneratorService;
    
    @InjectMocks
    private FinancialReportService reportService;
    
    @Test
    void testGenerateBalanceSheetReport_Success() {
        // Given
        ReportRequestDTO request = createValidBalanceSheetRequest();
        FinancialReport report = createTestFinancialReport();
        BalanceSheetDTO balanceSheet = createTestBalanceSheet();
        
        when(reportRepository.save(any(FinancialReport.class))).thenReturn(report);
        when(transactionService.getCashBalance(any())).thenReturn(BigDecimal.valueOf(1000000));
        when(reportGeneratorService.generateBalanceSheet(any(), any())).thenReturn(new byte[0]);
        
        // When
        FinancialReport result = reportService.generateReport(request, "testuser");
        
        // Then
        assertThat(result.getReportType()).isEqualTo(ReportType.BALANCE_SHEET);
        assertThat(result.getStatus()).isEqualTo(ReportStatus.COMPLETED);
        verify(reportRepository).save(any(FinancialReport.class));
    }
    
    @Test
    void testGenerateReport_InvalidPeriod_ThrowsException() {
        // Given
        ReportRequestDTO request = createInvalidPeriodRequest();
        
        // When & Then
        assertThrows(ValidationException.class, () -> reportService.generateReport(request, "testuser"));
    }
}
```

---

**Related Documentation**:
- [Database Schema](../architecture/database-schema.md) - Entity definitions
- [API Documentation](../api/rest-endpoints.md) - Endpoint details
- [Report Generation](../reference/report-generation.md) - Detailed report generation
