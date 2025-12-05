# Implementation Plan

## Overview

This implementation plan outlines the tasks required to build the Secretary Dashboard feature for the Ban Sai Yai Savings Group system. The plan follows an incremental approach, building core functionality first, then adding widgets progressively, and finally implementing testing and polish.

## Task List

- [ ] 1. Set up backend API endpoints and service layer
  - Create SecretaryDashboardController with REST endpoints
  - Implement SecretaryDashboardService with business logic
  - Create DTOs for trial balance, revenue/expense, liquidity, and reports
  - _Requirements: 1.1, 1.2, 2.1, 2.2, 3.1, 3.2_

- [ ]* 1.1 Write property test for trial balance calculation
  - **Property 1: Trial Balance Calculation Accuracy**
  - **Validates: Requirements 1.1, 1.2**

- [ ]* 1.2 Write property test for variance calculation
  - **Property 2: Trial Balance Variance Calculation**
  - **Validates: Requirements 1.4, 1.5**

- [ ] 2. Implement trial balance calculation logic
  - Create database queries to sum debits and credits by fiscal period
  - Implement variance calculation and balanced status determination
  - Add caching layer for trial balance data
  - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5_

- [ ]* 2.1 Write property test for net profit calculation
  - **Property 7: Net Profit Calculation**
  - **Validates: Requirements 2.6**

- [ ] 3. Implement revenue and expense analysis
  - Create queries to aggregate transactions by account code ranges (4xxx for income, 5xxx for expenses)
  - Implement category breakdown logic for income and expense subcategories
  - Calculate net profit as income minus expenses
  - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.6_

- [ ]* 3.1 Write property test for liquid assets calculation
  - **Property 9: Total Liquid Assets Calculation**
  - **Validates: Requirements 3.8**

- [ ] 4. Implement liquidity management data retrieval
  - Create queries to fetch Cash in Hand (account 1010) and Bank Deposits (account 1020) balances
  - Implement threshold alert logic for high cash and low cash conditions
  - Calculate total liquid assets
  - _Requirements: 3.1, 3.2, 3.3, 3.8_

- [ ] 5. Implement report generation functionality
  - Create PDF generation service using iText or similar library
  - Implement trial balance report template with debits, credits, and account details
  - Implement income statement report template with revenue and expense categories
  - Implement balance sheet report template with assets, liabilities, and equity
  - Add fiscal period, generation date, and signature lines to all reports
  - _Requirements: 4.3, 4.4, 4.5, 4.6, 4.7_

- [ ]* 5.1 Write property test for report metadata completeness
  - **Property 10: Report Metadata Completeness**
  - **Validates: Requirements 4.6**

- [ ] 6. Implement fiscal period management
  - Create endpoint to retrieve current fiscal period status
  - Implement fiscal period closing logic with validation checks
  - Add database update to set fiscal period status to "Closed"
  - Implement transaction posting prevention for closed periods
  - _Requirements: 8.1, 8.2, 8.3, 8.5, 8.6_

- [ ]* 6.1 Write property test for month-end closing prevention
  - **Property 3: Month-End Closing Prevention**
  - **Validates: Requirements 1.6, 8.3**

- [ ]* 6.2 Write property test for fiscal period status update
  - **Property 18: Fiscal Period Status Update**
  - **Validates: Requirements 8.5**

- [ ]* 6.3 Write property test for transaction posting prevention
  - **Property 19: Transaction Posting Prevention**
  - **Validates: Requirements 8.6**

- [ ] 7. Create frontend Redux API slice for Secretary Dashboard
  - Add endpoints to dashboardApi for trial balance, revenue/expense, liquidity, and reports
  - Configure caching and auto-refetch policies
  - Add mutations for fiscal period closing and report generation
  - _Requirements: 1.1, 2.1, 3.1, 4.1, 8.1_

- [ ] 8. Build SecretaryDashboardPage component
  - Create main page component with layout grid
  - Implement fiscal period selector in header
  - Add End of Month button with validation
  - Set up auto-refresh polling every 30 seconds
  - _Requirements: 5.1, 8.1, 9.1, 7.1_

- [ ]* 8.1 Write property test for API polling interval
  - **Property 15: API Polling Interval**
  - **Validates: Requirements 7.1**

- [ ] 9. Build TrialBalanceWidget component
  - Create widget container with loading and error states
  - Implement balanced/unbalanced visual indicators
  - Display total debits, total credits, and variance
  - Add "View Journal Entries" button when unbalanced
  - Show warning message when unbalanced
  - _Requirements: 1.4, 1.5, 1.6, 1.7, 1.8, 6.1_

- [ ]* 9.1 Write property test for trial balance display completeness
  - **Property 4: Trial Balance Display Completeness**
  - **Validates: Requirements 1.8**

- [ ]* 9.2 Write property test for imbalance action button display
  - **Property 14: Imbalance Action Button Display**
  - **Validates: Requirements 6.1**

- [ ] 10. Build RevenueExpenseWidget component
  - Create widget with waterfall or stacked bar chart using Recharts
  - Display income categories (interest, fees, fines) and expense categories (supplies, utilities, allowances)
  - Calculate and display net profit
  - Show warning indicator when net profit is negative
  - Implement drill-down navigation to transaction details
  - _Requirements: 2.3, 2.4, 2.5, 2.6, 2.7, 2.8_

- [ ]* 10.1 Write property test for negative profit warning
  - **Property 8: Negative Profit Warning**
  - **Validates: Requirements 2.7**

- [ ] 11. Build LiquidityManagementWidget component
  - Create comparative display for Cash in Hand vs Bank Deposits
  - Implement threshold alerts for high cash (>50000 THB) and low cash (<10000 THB)
  - Add action buttons for "Make Bank Deposit" and "Make Bank Withdrawal"
  - Display total liquid assets
  - _Requirements: 3.3, 3.4, 3.5, 3.6, 3.7, 3.8_

- [ ] 12. Build UnclassifiedTransactionAlert component
  - Display count of unclassified transactions
  - Show success state when count is zero
  - Show warning state with count when transactions exist
  - Add "Classify Transactions" button that navigates to Journal Entry screen with filter
  - _Requirements: 6.2, 6.4_

- [ ] 13. Build ReportGenerationHub component
  - Create grid layout with report generation buttons
  - Implement PDF download trigger for each report type
  - Disable buttons when trial balance is unbalanced
  - Show explanatory message when buttons are disabled
  - Handle report generation errors with notifications
  - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.5, 4.7, 4.8_

- [ ]* 13.1 Write property test for report generation blocking
  - **Property 11: Report Generation Blocking**
  - **Validates: Requirements 4.8**

- [ ] 14. Build SummaryMetricsBar component
  - Create summary cards for fiscal period, unclassified count, trial balance status, and liquid assets
  - Implement warning/error highlighting for problem metrics
  - _Requirements: 5.1, 5.2, 5.3, 5.4, 5.5_

- [ ]* 14.1 Write property test for summary metrics display
  - **Property 12: Summary Metrics Display**
  - **Validates: Requirements 5.2, 5.3, 5.4**

- [ ]* 14.2 Write property test for problem metric highlighting
  - **Property 13: Problem Metric Highlighting**
  - **Validates: Requirements 5.5**

- [ ] 15. Implement End of Month closing flow
  - Create confirmation dialog with warning message
  - Implement validation checks (trial balance balanced, no unclassified transactions)
  - Call backend API to close fiscal period
  - Show success notification and update dashboard
  - Handle errors with appropriate messages
  - _Requirements: 8.2, 8.3, 8.4, 8.5, 8.7_

- [ ]* 15.1 Write property test for End of Month button state
  - **Property 17: End of Month Button State**
  - **Validates: Requirements 8.2, 8.3**

- [ ] 16. Implement fiscal period selector and historical viewing
  - Create dropdown to select previous fiscal periods
  - Reload all widgets with filtered data when period changes
  - Display read-only indicator for closed periods
  - Disable action buttons when viewing closed periods
  - Restore functionality when switching back to current period
  - _Requirements: 9.1, 9.2, 9.3, 9.4, 9.5_

- [ ]* 16.1 Write property test for fiscal period filter application
  - **Property 20: Fiscal Period Filter Application**
  - **Validates: Requirements 9.2**

- [ ]* 16.2 Write property test for closed period interaction restriction
  - **Property 21: Closed Period Interaction Restriction**
  - **Validates: Requirements 9.4**

- [ ] 17. Implement comprehensive error handling
  - Add error boundaries for each widget
  - Display error messages within widget containers when data fails to load
  - Show notifications for operation failures (report generation, fiscal period closing)
  - Log all errors to browser console
  - Add retry buttons for network errors
  - Display "Data may be stale" indicator when API is unavailable
  - _Requirements: 7.5, 10.1, 10.2, 10.3, 10.4, 10.5_

- [ ]* 17.1 Write property test for widget error display
  - **Property 22: Widget Error Display**
  - **Validates: Requirements 10.1**

- [ ]* 17.2 Write property test for stale data indicator
  - **Property 16: Stale Data Indicator**
  - **Validates: Requirements 7.5**

- [ ]* 17.3 Write property test for error recovery UI
  - **Property 25: Error Recovery UI**
  - **Validates: Requirements 10.5**

- [ ] 18. Add role-based access control
  - Protect Secretary Dashboard route with RoleBasedRoute component
  - Add @PreAuthorize annotations to backend endpoints
  - Implement 403 error handling for unauthorized access
  - _Requirements: 15.1, 15.2, 15.3_

- [ ] 19. Implement performance optimizations
  - Add React.memo to widget components
  - Configure RTK Query caching with appropriate TTLs
  - Lazy load chart components with Suspense
  - Add database indices for fiscal period and account code queries
  - Implement Redis caching for trial balance and revenue/expense data
  - _Requirements: 17.1, 17.2, 17.3_

- [ ] 20. Add audit logging for critical operations
  - Log fiscal period closing events with user ID and timestamp
  - Log report generation events
  - Log failed End of Month attempts
  - _Requirements: Security and compliance_

- [ ]* 21. Write integration tests for backend endpoints
  - Test trial balance endpoint returns correct data
  - Test revenue/expense endpoint aggregates correctly
  - Test liquidity endpoint retrieves correct account balances
  - Test report generation endpoints create valid PDFs
  - Test fiscal period closing endpoint validates and updates status
  - Test closed period prevents transaction posting

- [ ]* 22. Write end-to-end tests for dashboard workflows
  - Test complete dashboard load and widget display
  - Test End of Month closing flow with validation
  - Test report generation and download
  - Test fiscal period selector and historical viewing
  - Test error handling and recovery

- [ ] 23. Checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 24. Final polish and documentation
  - Add loading skeletons for all widgets
  - Ensure consistent styling across all components
  - Add tooltips for buttons and icons
  - Update API documentation with new endpoints
  - Create user guide for Secretary Dashboard features
  - _Requirements: All_
