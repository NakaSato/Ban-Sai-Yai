# Implementation Plan

- [ ] 1. Set up backend widget data services
  - Create WidgetDataService with methods for all widget data aggregation
  - Implement getMemberFinancialSummary for Teller Action Card
  - Implement getCashBoxTally for cash control
  - Implement getRecentTransactions for Transaction Feed
  - _Requirements: 1.1, 2.1, 2.2, 2.3, 3.1, 3.2_

- [ ]* 1.1 Write property test for cash box total in calculation
  - **Property 4: Cash box total in calculation**
  - **Validates: Requirements 2.1**

- [ ]* 1.2 Write property test for cash box total out calculation
  - **Property 5: Cash box total out calculation**
  - **Validates: Requirements 2.2**

- [ ]* 1.3 Write property test for net cash calculation
  - **Property 6: Net cash calculation**
  - **Validates: Requirements 2.3**

- [ ]* 1.4 Write property test for transaction feed ordering
  - **Property 8: Transaction feed ordering**
  - **Validates: Requirements 3.1**

- [ ] 2. Implement transaction modal service
  - Create TransactionModalService for processing deposits and loan payments
  - Implement processDeposit with validation and audit logging
  - Implement processLoanPayment with principal, interest, and fine breakdown
  - Implement calculateMinimumInterestDue for loan payment validation
  - Add fiscal period validation before transaction processing
  - _Requirements: 1.4, 1.5, 13.4_

- [ ]* 2.1 Write property test for minimum interest calculation
  - **Property 2: Minimum interest calculation**
  - **Validates: Requirements 1.4**

- [ ]* 2.2 Write property test for transaction form clearing
  - **Property 3: Transaction form clearing**
  - **Validates: Requirements 1.5**

- [ ]* 2.3 Write unit tests for transaction validation
  - Test deposit with zero amount is rejected (example)
  - Test transaction in closed period is rejected (example)
  - Test transaction for inactive member is rejected (example)
  - _Requirements: 1.4, 13.4_

- [ ] 3. Implement PAR analysis service
  - Create PARAnalysisService with loan categorization logic
  - Implement analyzePAR to categorize all active loans
  - Implement calculateDaysSinceLastPayment with null handling
  - Implement categorizeLoan with 4-tier classification (Standard, Watch, Substandard, Loss)
  - Implement getLoansInCategory for drill-down details
  - _Requirements: 7.1, 7.2, 7.3, 7.4, 7.5_

- [ ]* 3.1 Write property test for PAR standard classification
  - **Property 21: PAR standard classification**
  - **Validates: Requirements 7.2**

- [ ]* 3.2 Write property test for PAR watch classification
  - **Property 22: PAR watch classification**
  - **Validates: Requirements 7.3**

- [ ]* 3.3 Write property test for PAR substandard classification
  - **Property 23: PAR substandard classification**
  - **Validates: Requirements 7.4**

- [ ]* 3.4 Write property test for PAR loss classification
  - **Property 24: PAR loss classification**
  - **Validates: Requirements 7.5**

- [ ]* 3.5 Write property test for PAR loan processing
  - **Property 20: PAR loan processing**
  - **Validates: Requirements 7.1**

- [ ] 4. Implement liquidity calculation service
  - Create LiquidityCalculationService with ratio calculation
  - Implement calculateLiquidity with liquid assets and deposits aggregation
  - Implement getTotalLiquidAssets (cash + bank balances)
  - Implement getTotalSavingsDeposits
  - Implement determineStatus with 4-zone classification (Crisis, Caution, Healthy, Inefficient)
  - Handle division by zero edge case
  - _Requirements: 8.1, 8.2, 8.3, 8.4, 8.5, 8.6, 8.7_

- [ ]* 4.1 Write property test for liquidity ratio formula
  - **Property 27: Liquidity ratio formula**
  - **Validates: Requirements 8.3**

- [ ]* 4.2 Write property test for liquidity status classification
  - **Property 28-31: Liquidity crisis/caution/healthy/inefficient status**
  - **Validates: Requirements 8.4, 8.5, 8.6, 8.7**

- [ ]* 4.3 Write unit tests for liquidity edge cases
  - Test zero deposits returns default gauge (example)
  - Test crisis status displays red indicator (example)
  - Test healthy status displays green indicator (example)
  - _Requirements: 8.4, 8.5, 8.6, 8.7_

- [ ] 5. Implement fiscal period service
  - Create FiscalPeriodService for period management
  - Implement getCurrentFiscalPeriod to query system configuration
  - Implement isTransactionAllowed to validate period status
  - Implement closeFiscalPeriod and openFiscalPeriod methods
  - _Requirements: 13.1, 13.4_

- [ ]* 5.1 Write property test for fiscal period data retrieval
  - **Property 40: Fiscal period data retrieval**
  - **Validates: Requirements 13.1**

- [ ] 6. Implement omni-search service
  - Create OmniSearchService for member search
  - Implement searchMembers with query sanitization
  - Add SQL injection prevention
  - Limit results to 5 members
  - Search across member ID, first name, last name, and national ID
  - _Requirements: 14.1, 14.2, 14.3_

- [ ]* 6.1 Write property test for search result limit
  - **Property 44: Search result limit**
  - **Validates: Requirements 14.2**

- [ ]* 6.2 Write property test for search result completeness
  - **Property 45: Search result completeness**
  - **Validates: Requirements 14.3**

- [ ]* 6.3 Write unit tests for search functionality
  - Test search by member ID returns correct member (example)
  - Test search by name returns matching members (example)
  - Test empty query returns empty results (edge-case)
  - _Requirements: 14.1, 14.5_

- [ ] 7. Implement Secretary dashboard widget services
  - Add getTrialBalanceStatus to WidgetDataService
  - Implement trial balance calculation (sum debits, sum credits)
  - Add getUnclassifiedTransactionCount
  - Implement getFinancialStatementPreviews with income and asset aggregation
  - Filter accounting entries by code groups (4xxx for income, 1xxx for assets)
  - _Requirements: 4.1, 4.2, 5.1, 6.1, 6.2, 6.3, 6.4, 6.5_

- [ ]* 7.1 Write property test for trial balance calculations
  - **Property 12-13: Trial balance debit and credit calculation**
  - **Validates: Requirements 4.1, 4.2**

- [ ]* 7.2 Write property test for unclassified transaction count
  - **Property 15: Unclassified transaction count**
  - **Validates: Requirements 5.1**

- [ ]* 7.3 Write property test for chart data aggregation
  - **Property 17-19: Income/asset chart aggregation and grouping**
  - **Validates: Requirements 6.1, 6.2, 6.3, 6.4, 6.5**

- [ ] 8. Implement President dashboard widget services
  - Add getMembershipGrowth to WidgetDataService
  - Implement monthly aggregation of new members and resignations
  - Query member table by registration date
  - _Requirements: 9.1, 9.2, 9.3_

- [ ]* 8.1 Write property test for membership growth aggregation
  - **Property 32-33: Membership growth and resignation aggregation**
  - **Validates: Requirements 9.1, 9.2, 9.3**

- [ ] 9. Implement Member dashboard widget services
  - Add getDigitalPassbook to WidgetDataService
  - Implement balance calculation (forward + current month deposits)
  - Add getLoanObligation with principal, due date, and interest
  - Implement getDividendEstimate with shares × rate calculation
  - _Requirements: 10.1, 10.2, 10.4, 11.1, 11.2, 11.3, 12.1, 12.2, 12.3, 12.4_

- [ ]* 9.1 Write property test for digital passbook balance
  - **Property 34: Digital passbook balance calculation**
  - **Validates: Requirements 10.1, 10.2**

- [ ]* 9.2 Write property test for dividend calculation
  - **Property 38: Dividend calculation formula**
  - **Validates: Requirements 12.3**

- [ ]* 9.3 Write unit tests for member widgets
  - Test passbook with no transactions shows forward balance (edge-case)
  - Test loan obligation with no active loan shows message (edge-case)
  - Test overdue loan displays red border (example)
  - _Requirements: 10.5, 11.4, 11.5_

- [ ] 10. Create backend REST endpoints for widgets
  - Add DashboardWidgetController with endpoints for all widgets
  - Implement GET /api/dashboard/widgets/member-financials/{memberId}
  - Implement GET /api/dashboard/widgets/cash-box-tally
  - Implement GET /api/dashboard/widgets/recent-transactions
  - Implement GET /api/dashboard/widgets/trial-balance
  - Implement GET /api/dashboard/widgets/unclassified-count
  - Implement GET /api/dashboard/widgets/financial-previews
  - Implement GET /api/dashboard/widgets/par-analysis
  - Implement GET /api/dashboard/widgets/liquidity-gauge
  - Implement GET /api/dashboard/widgets/membership-growth
  - Implement GET /api/dashboard/widgets/passbook/{memberId}
  - Implement GET /api/dashboard/widgets/loan-obligation/{memberId}
  - Implement GET /api/dashboard/widgets/dividend-estimate/{memberId}
  - Implement GET /api/dashboard/widgets/fiscal-period
  - Implement GET /api/dashboard/widgets/search-members
  - Add RBAC annotations to restrict access by role
  - _Requirements: All widget requirements_

- [ ] 11. Implement transaction submission endpoints
  - Add POST /api/dashboard/transactions/deposit endpoint
  - Add POST /api/dashboard/transactions/loan-payment endpoint
  - Implement request validation
  - Return TransactionResult with success/error status
  - _Requirements: 1.2, 1.3, 1.4, 1.5_

- [ ] 12. Checkpoint - Ensure backend tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 13. Create TellerActionCard component
  - Implement React component with member autocomplete
  - Add AJAX fetch for member financial summary on selection
  - Display balance, loan principal, and loan status
  - Add Deposit and Loan Pay buttons
  - Implement modal opening logic
  - _Requirements: 1.1, 1.2, 1.3_

- [ ]* 13.1 Write property test for member financial data fetch
  - **Property 1: Member financial data fetch**
  - **Validates: Requirements 1.1**

- [ ]* 13.2 Write unit tests for Teller Action Card
  - Test deposit button opens modal (example)
  - Test loan pay button opens modal (example)
  - Test member selection fetches financials (example)
  - _Requirements: 1.1, 1.2, 1.3_

- [ ] 14. Create DepositModal and LoanPaymentModal components
  - Implement DepositModal with amount input field
  - Implement LoanPaymentModal with principal, interest, and fine fields
  - Add client-side validation
  - Implement form submission with error handling
  - Display success confirmation on completion
  - Clear form after successful submission
  - _Requirements: 1.2, 1.3, 1.4, 1.5_

- [ ]* 14.1 Write unit tests for modals
  - Test deposit modal validates positive amounts (example)
  - Test loan payment calculates minimum interest (example)
  - Test form clears after successful submission (example)
  - _Requirements: 1.4, 1.5_

- [ ] 15. Create CashBoxTally component
  - Implement component with Total In, Total Out, and Net Cash display
  - Add "Count Cash" button to reveal denomination entry
  - Implement denomination input fields (1000, 500, 100, 50, 20, 10, 5, 1)
  - Calculate physical total from denominations
  - Display variance (physical - database)
  - Color-code variance (green for zero, red for non-zero)
  - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5_

- [ ]* 15.1 Write property test for variance calculation
  - **Property 7: Physical cash variance calculation**
  - **Validates: Requirements 2.5**

- [ ]* 15.2 Write unit tests for Cash Box Tally
  - Test count cash reveals denomination section (example)
  - Test variance calculation with sample denominations (example)
  - _Requirements: 2.4, 2.5_

- [ ] 16. Create TransactionFeed component
  - Implement component displaying 10 most recent transactions
  - Fetch data from backend endpoint
  - Display time, member name, type, and amount for each transaction
  - Add receipt icon for each transaction
  - Implement receipt PDF generation on icon click (window.open)
  - Add refresh mechanism triggered by transaction completion
  - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5_

- [ ]* 16.1 Write property test for transaction feed completeness
  - **Property 9: Transaction feed completeness**
  - **Validates: Requirements 3.2**

- [ ]* 16.2 Write property test for receipt icon presence
  - **Property 10: Receipt icon presence**
  - **Validates: Requirements 3.3**

- [ ] 17. Create TrialBalanceWidget component
  - Implement component with debit and credit sum display
  - Show single green progress bar when balanced
  - Show split red/blue progress bar when unbalanced
  - Display imbalance amount
  - Show warning alert when unbalanced
  - Disable report generation button when unbalanced
  - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.5_

- [ ]* 17.1 Write property test for report generation blocking
  - **Property 14: Report generation blocking**
  - **Validates: Requirements 4.5**

- [ ]* 17.2 Write unit tests for Trial Balance Widget
  - Test balanced state shows green bar (example)
  - Test unbalanced state shows split bar (example)
  - Test report button disabled when unbalanced (example)
  - _Requirements: 4.3, 4.4, 4.5_

- [ ] 18. Create UnclassifiedTransactionAlert component
  - Implement warning card displaying unclassified count
  - Add click handler to navigate to Journal Entry screen
  - Pre-filter Journal Entry view to show only unclassified transactions
  - Display success message when count is zero
  - _Requirements: 5.1, 5.2, 5.3, 5.4, 5.5_

- [ ]* 18.1 Write property test for unclassified warning display
  - **Property 16: Unclassified warning display**
  - **Validates: Requirements 5.2**

- [ ] 19. Create FinancialStatementPreviews component
  - Implement bar chart for income vs expenses
  - Implement pie chart for asset distribution
  - Use Chart.js or Recharts for visualizations
  - Fetch aggregated data from backend
  - _Requirements: 6.1, 6.2_

- [ ] 20. Create PARAnalysisWidget component
  - Implement doughnut chart with 4 segments (Standard, Watch, Substandard, Loss)
  - Color-code segments (green, yellow, orange, red)
  - Add click handler to open detail modal
  - Create PARDetailModal showing member list with guarantors
  - _Requirements: 7.1, 7.2, 7.3, 7.4, 7.5, 7.6, 7.7_

- [ ]* 20.1 Write unit tests for PAR Analysis Widget
  - Test chart renders with correct segments (example)
  - Test clicking segment opens modal (example)
  - Test modal displays member details (example)
  - _Requirements: 7.6, 7.7_

- [ ] 21. Create LiquidityGauge component
  - Implement circular gauge visualization
  - Display ratio percentage in center
  - Color-code gauge by status (red, yellow, green, blue)
  - Display status chip (Crisis, Caution, Healthy, Inefficient)
  - Show liquid assets and total deposits breakdown
  - Display status-specific message
  - _Requirements: 8.1, 8.2, 8.3, 8.4, 8.5, 8.6, 8.7_

- [ ]* 21.1 Write unit tests for Liquidity Gauge
  - Test crisis status displays red gauge (example)
  - Test healthy status displays green gauge (example)
  - Test inefficient status displays blue gauge (example)
  - _Requirements: 8.4, 8.5, 8.6, 8.7_

- [ ] 22. Create MembershipGrowthTrend component
  - Implement line chart with two lines (new members, resignations)
  - Use different colors for each line (green for new, red for resignations)
  - Fetch monthly aggregated data from backend
  - Display month labels on x-axis
  - _Requirements: 9.1, 9.2, 9.3, 9.4_

- [ ] 23. Create DigitalPassbook component
  - Implement card displaying member balance
  - Show balance in large prominent font
  - Display "Last activity" timestamp
  - Show recent transactions list
  - Handle no-transaction edge case with appropriate message
  - _Requirements: 10.1, 10.2, 10.3, 10.4, 10.5_

- [ ]* 23.1 Write property test for last activity timestamp
  - **Property 35: Last activity timestamp**
  - **Validates: Requirements 10.4**

- [ ] 24. Create LoanObligationCard component
  - Implement card displaying outstanding principal
  - Show next payment due date
  - Display estimated interest due
  - Add conditional red border styling for overdue loans
  - Handle no-loan edge case with appropriate message
  - _Requirements: 11.1, 11.2, 11.3, 11.4, 11.5_

- [ ]* 24.1 Write property test for overdue loan styling
  - **Property 37: Overdue loan styling**
  - **Validates: Requirements 11.4**

- [ ] 25. Create DividendEstimator component
  - Implement card displaying estimated dividend
  - Show calculation breakdown (shares × rate)
  - Display disclaimer text
  - Fetch projected rate from system configuration
  - _Requirements: 12.1, 12.2, 12.3, 12.4, 12.5_

- [ ]* 25.1 Write property test for dividend display completeness
  - **Property 39: Dividend display completeness**
  - **Validates: Requirements 12.4**

- [ ] 26. Create FiscalPeriodIndicator component
  - Implement header badge showing period status
  - Display green badge for OPEN, red badge for CLOSED
  - Add polling mechanism to check for status changes every 30 seconds
  - Implement JavaScript to disable transaction buttons when closed
  - Update display without page refresh on status change
  - _Requirements: 13.1, 13.2, 13.3, 13.4, 13.5_

- [ ]* 26.1 Write property test for transaction button disabling
  - **Property 41: Transaction button disabling**
  - **Validates: Requirements 13.4**

- [ ]* 26.2 Write property test for real-time update
  - **Property 42: Fiscal period real-time update**
  - **Validates: Requirements 13.5**

- [ ] 27. Create OmniSearchBar component
  - Implement autocomplete search input
  - Add debounced search (300ms delay)
  - Display dropdown with member results
  - Show member ID, full name, and status for each result
  - Limit results to 5 members
  - Implement navigation to member profile on selection
  - Handle no-results edge case
  - _Requirements: 14.1, 14.2, 14.3, 14.4, 14.5_

- [ ]* 27.1 Write property test for omni-search query matching
  - **Property 43: Omni-search query matching**
  - **Validates: Requirements 14.1**

- [ ] 28. Implement widget error handling
  - Create useWidgetData custom hook with error handling
  - Implement error boundaries for each widget
  - Display user-friendly error messages
  - Add retry buttons to error displays
  - Ensure widget errors don't crash entire dashboard
  - Log errors to console for debugging
  - _Requirements: 15.1, 15.2, 15.3, 15.4, 15.5_

- [ ]* 28.1 Write property test for widget error isolation
  - **Property 46: Widget error isolation**
  - **Validates: Requirements 15.1, 15.3**

- [ ]* 28.2 Write property test for error logging
  - **Property 47: Error logging**
  - **Validates: Requirements 15.2**

- [ ]* 28.3 Write property test for retry button presence
  - **Property 48: Retry button presence**
  - **Validates: Requirements 15.4**

- [ ] 29. Integrate widgets into role-based dashboards
  - Add TellerActionCard, CashBoxTally, and TransactionFeed to Officer dashboard
  - Add TrialBalanceWidget, UnclassifiedTransactionAlert, and FinancialStatementPreviews to Secretary dashboard
  - Add PARAnalysisWidget, LiquidityGauge, and MembershipGrowthTrend to President dashboard
  - Add DigitalPassbook, LoanObligationCard, and DividendEstimator to Member dashboard
  - Add FiscalPeriodIndicator and OmniSearchBar to all dashboard headers
  - Configure responsive grid layout for each role
  - _Requirements: All requirements_

- [ ] 30. Checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.
