# Implementation Plan

- [ ] 1. Backend: Create dashboard DTOs and data models
  - Create DashboardStatsDTO, ChartDataDTO, QuickActionDTO, RecentActivityDTO, FinancialRatiosDTO
  - Add validation annotations to DTOs
  - _Requirements: 1.1, 2.1, 3.1, 4.1_

- [ ] 2. Backend: Implement DashboardRepository with custom queries
  - Create custom query methods for member growth, loan portfolio composition, revenue trends
  - Add indexes for performance optimization
  - _Requirements: 1.2, 1.3, 1.4_

- [x] 3. Backend: Implement DashboardService business logic
  - Implement calculateDashboardStats method with role-based filtering
  - Implement chart data aggregation methods (member growth, loan portfolio, revenue trend)
  - Implement financial ratios calculation
  - Implement quick actions retrieval by role
  - _Requirements: 1.1, 1.2, 1.3, 1.4, 2.1, 2.2, 2.3, 2.4, 3.1, 3.2, 3.3, 3.4, 4.1, 4.2, 4.3, 4.5_

- [x] 3.1 Write property test for role-based quick actions
  - **Property 1: Role-based widget visibility**
  - **Validates: Requirements 1.1, 1.2, 1.3, 1.4, 2.1, 2.2, 2.3, 2.4, 3.1, 3.2, 3.3, 3.4, 4.1, 4.2, 4.3, 4.5**

- [ ]* 3.2 Write unit tests for DashboardService
  - Test calculateDashboardStats with different roles
  - Test null handling in aggregation methods
  - Test financial ratios calculation edge cases
  - _Requirements: 1.1, 1.4_

- [ ] 4. Backend: Implement DashboardController REST endpoints
  - Create GET /api/dashboard/stats endpoint with role parameter
  - Create GET /api/dashboard/charts/member-growth endpoint
  - Create GET /api/dashboard/charts/loan-portfolio endpoint
  - Create GET /api/dashboard/charts/revenue-trend endpoint
  - Create GET /api/dashboard/quick-actions endpoint
  - Add @PreAuthorize annotations for role-based access control
  - _Requirements: 1.1, 1.2, 1.3, 2.1, 2.2, 2.3, 2.4, 3.1, 3.2, 3.3, 3.4_

- [ ]* 4.1 Write integration tests for dashboard endpoints
  - Test each endpoint with different user roles
  - Test unauthorized access returns 403
  - Test invalid parameters return 400
  - _Requirements: 1.1, 2.1, 3.1_

- [ ] 5. Backend: Implement caching for dashboard data
  - Add Redis cache configuration
  - Add @Cacheable annotations to DashboardService methods
  - Configure cache TTL and eviction policies
  - _Requirements: 6.1, 6.2_

- [ ] 6. Backend: Implement trial balance and unclassified transactions endpoints
  - Create GET /api/dashboard/secretary/trial-balance endpoint
  - Create GET /api/dashboard/secretary/unclassified-count endpoint
  - Implement trial balance calculation logic
  - _Requirements: 2.2, 2.3_

- [ ]* 6.1 Write property test for conditional alert rendering
  - **Property 2: Conditional alert rendering**
  - **Validates: Requirements 2.5**

- [ ] 7. Backend: Implement officer-specific endpoints
  - Create GET /api/dashboard/officer/cash-box endpoint
  - Create GET /api/dashboard/officer/recent-transactions endpoint
  - Implement cash box tally calculation
  - _Requirements: 3.2, 3.4_

- [ ] 8. Backend: Implement member-specific endpoints
  - Create GET /api/dashboard/member/financials endpoint
  - Create GET /api/dashboard/member/loans endpoint
  - Create GET /api/dashboard/member/transactions endpoint
  - Implement payment reminder logic (loans due within 7 days)
  - _Requirements: 4.1, 4.2, 4.3, 4.4_

- [ ]* 8.1 Write property test for payment reminder alerts
  - **Property 3: Payment reminder alerts**
  - **Validates: Requirements 4.4**

- [ ] 9. Backend: Implement search functionality
  - Create GET /api/dashboard/members/search endpoint with query parameter
  - Implement member search with name and ID card matching
  - Add pagination support
  - _Requirements: 8.1, 8.2, 8.3, 8.4, 8.5_

- [ ]* 9.1 Write property test for search query threshold
  - **Property 10: Search query threshold**
  - **Validates: Requirements 8.2**

- [ ] 10. Backend: Implement quick action mutation endpoints
  - Create POST /api/dashboard/transactions/deposit endpoint
  - Create POST /api/dashboard/transactions/loan-payment endpoint
  - Create POST /api/dashboard/transactions/withdrawal endpoint
  - Add input validation
  - Implement cache invalidation on successful mutations
  - _Requirements: 12.2, 12.3, 12.4, 12.5_

- [ ]* 10.1 Write property test for form validation before submission
  - **Property 14: Form validation before submission**
  - **Validates: Requirements 12.3**

- [ ]* 10.2 Write unit tests for mutation endpoints
  - Test successful deposit/payment/withdrawal
  - Test validation errors
  - Test cache invalidation
  - _Requirements: 12.3, 12.4, 12.5_

- [ ] 11. Checkpoint - Ensure all backend tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 12. Frontend: Create TypeScript interfaces for dashboard data
  - Create DashboardStats, ChartData, QuickAction, RecentActivity interfaces
  - Create DepositRequest, LoanPaymentRequest, TransactionResponse interfaces
  - Add to frontend/src/types/index.ts
  - _Requirements: 1.1, 2.1, 3.1, 4.1_

- [ ] 13. Frontend: Extend dashboardApi with new endpoints
  - Add getDashboardStats query
  - Add getMemberGrowthChart, getLoanPortfolioChart, getRevenueTrendChart queries
  - Add getTrialBalance, getUnclassifiedCount queries
  - Add getCashBox, getRecentTransactions queries
  - Add searchMembers query with debouncing
  - Add processDeposit, processLoanPayment, processWithdrawal mutations
  - Configure cache tags and invalidation
  - _Requirements: 1.1, 1.2, 1.3, 2.2, 2.3, 3.2, 3.4, 6.3, 8.2_

- [ ]* 13.1 Write property test for cache invalidation on mutation
  - **Property 5: Cache invalidation on mutation**
  - **Validates: Requirements 6.3**

- [ ] 14. Frontend: Create reusable StatCard component
  - Implement StatCard with title, value, icon, color, trend props
  - Add Material-UI Card styling
  - Add optional onClick handler for navigation
  - Add data-testid attributes for testing
  - _Requirements: 1.1, 2.1, 3.1, 4.1_

- [ ] 15. Frontend: Create ChartWidget wrapper component
  - Implement ChartWidget with title, chartType, data, isLoading, error props
  - Add Suspense and lazy loading support
  - Add loading skeleton
  - Add error boundary with retry button
  - Add responsive sizing
  - _Requirements: 1.2, 1.3, 7.1, 7.2, 7.3, 7.4, 7.5_

- [ ]* 15.1 Write property test for chart tooltip interactivity
  - **Property 8: Chart tooltip interactivity**
  - **Validates: Requirements 7.2**

- [ ]* 15.2 Write property test for loading state indication
  - **Property 9: Loading state indication**
  - **Validates: Requirements 7.5**

- [ ] 16. Frontend: Create specific chart components
  - Create MemberGrowthChart using Recharts LineChart
  - Create LoanPortfolioChart using Recharts PieChart
  - Create SavingsGrowthChart using Recharts AreaChart
  - Create RevenueTrendChart using Recharts BarChart
  - Add axis labels, legends, and tooltips
  - _Requirements: 1.2, 1.3, 4.5, 7.2, 7.3, 7.4_

- [ ] 17. Frontend: Create QuickActionsPanel component
  - Implement QuickActionsPanel with actions array prop
  - Add Material-UI Grid layout for action buttons
  - Add dynamic icon loading from Material-UI Icons
  - Add click handlers for modal opening or navigation
  - _Requirements: 2.4, 3.3, 12.1_

- [ ] 18. Frontend: Create DepositModal component
  - Implement modal with Material-UI Dialog
  - Add form with member autocomplete, amount, deposit type, description, date fields
  - Add react-hook-form for form management
  - Add yup validation schema
  - Integrate with processDeposit mutation
  - Handle success and error states
  - _Requirements: 12.2, 12.3, 12.4, 12.5_

- [ ]* 18.1 Write property test for form error persistence
  - **Property 15: Form error persistence**
  - **Validates: Requirements 12.5**

- [ ] 19. Frontend: Create LoanPaymentModal component
  - Implement modal with loan selection, amount, payment date, payment method fields
  - Add form validation
  - Integrate with processLoanPayment mutation
  - _Requirements: 12.2, 12.3, 12.4, 12.5_

- [ ] 20. Frontend: Create RecentActivityTable component
  - Implement table with Material-UI DataGrid
  - Add columns for timestamp, member name, type, amount, status
  - Add sorting and pagination
  - Add row click handler for navigation
  - Integrate with getRecentTransactions query
  - _Requirements: 3.2, 4.3_

- [ ] 21. Frontend: Create OmniSearchBar component
  - Implement search bar with Material-UI Autocomplete
  - Add debounced search (300ms delay)
  - Integrate with searchMembers query
  - Add grouped results display
  - Add keyboard navigation support
  - _Requirements: 8.1, 8.2, 8.3, 8.4, 8.5_

- [ ] 22. Frontend: Create FiscalPeriodHeader component
  - Implement header with fiscal period display
  - Add status indicator (OPEN/CLOSED)
  - Add close period button for Secretary/President roles
  - Integrate with getFiscalPeriod query
  - _Requirements: 2.1_

- [ ] 23. Frontend: Create TrialBalanceWidget component
  - Implement widget displaying total debits and credits
  - Add conditional warning badge when unclassified count > 0
  - Integrate with getTrialBalance and getUnclassifiedCount queries
  - _Requirements: 2.2, 2.3, 2.5_

- [ ] 24. Frontend: Create CashBoxTally component
  - Implement widget displaying total in, total out, net cash
  - Add date display
  - Integrate with getCashBox query
  - _Requirements: 3.4_

- [ ] 25. Frontend: Create WidgetErrorBoundary component
  - Implement React Error Boundary class component
  - Add error logging to console
  - Add error UI with retry button
  - Ensure errors are isolated to individual widgets
  - _Requirements: 9.1, 9.2, 9.3, 9.4, 9.5_

- [ ]* 25.1 Write property test for widget error isolation
  - **Property 11: Widget error isolation**
  - **Validates: Requirements 9.1, 9.2, 9.3**

- [ ]* 25.2 Write property test for error recovery UI
  - **Property 12: Error recovery UI**
  - **Validates: Requirements 9.4**

- [ ]* 25.3 Write property test for error logging
  - **Property 13: Error logging**
  - **Validates: Requirements 9.5**

- [ ] 26. Frontend: Implement role-based DashboardPage components
  - Create PresidentDashboardPage with executive KPIs and charts
  - Create SecretaryDashboardPage with financial management widgets
  - Create OfficerDashboardPage with operational widgets
  - Create MemberDashboardPage with personal financial information
  - Add responsive Grid layout with Material-UI breakpoints
  - _Requirements: 1.1, 1.2, 1.3, 1.4, 2.1, 2.2, 2.3, 2.4, 3.1, 3.2, 3.3, 3.4, 4.1, 4.2, 4.3, 4.4, 4.5_

- [ ]* 26.1 Write property test for responsive layout adaptation
  - **Property 4: Responsive layout adaptation**
  - **Validates: Requirements 5.1, 5.2, 5.3, 5.4, 5.5**

- [ ] 27. Frontend: Implement auto-refresh functionality
  - Add useEffect hook with 60-second interval for data refetch
  - Add cleanup on component unmount
  - Add manual refresh button
  - _Requirements: 6.1, 6.2_

- [ ] 28. Frontend: Implement error handling with stale data indication
  - Add stale data indicator chip when API fails but cached data exists
  - Add retry logic with exponential backoff in RTK Query configuration
  - _Requirements: 6.4, 6.5_

- [ ]* 28.1 Write property test for stale data indication on error
  - **Property 6: Stale data indication on error**
  - **Validates: Requirements 6.4**

- [ ]* 28.2 Write property test for exponential backoff retry
  - **Property 7: Exponential backoff retry**
  - **Validates: Requirements 6.5**

- [ ] 29. Frontend: Add routing for role-based dashboards
  - Update App.tsx to route /dashboard to appropriate role-based component
  - Add role-based route guards
  - _Requirements: 1.1, 2.1, 3.1, 4.1_

- [ ]* 29.1 Write unit tests for role-based dashboard rendering
  - Test President sees executive widgets
  - Test Secretary sees financial management widgets
  - Test Officer sees operational widgets
  - Test Member sees personal financial widgets
  - _Requirements: 1.1, 2.1, 3.1, 4.1_

- [ ] 30. Frontend: Implement export functionality (PDF/Excel)
  - Add export button in dashboard header for President role
  - Integrate jsPDF library for PDF generation
  - Integrate xlsx library for Excel generation
  - Implement export logic to capture dashboard data
  - Trigger browser download on export complete
  - _Requirements: 10.1, 10.2, 10.3, 10.4, 10.5_

- [ ]* 30.1 Write unit tests for export functionality
  - Test PDF export generates file
  - Test Excel export generates file
  - Test export button only visible to President
  - _Requirements: 10.1, 10.2, 10.3, 10.4, 10.5_

- [ ] 31. Checkpoint - Ensure all frontend tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 32. Integration: Test end-to-end dashboard flows
  - Test President dashboard loads with all widgets
  - Test Secretary can record deposit via quick action
  - Test Officer can view recent transactions
  - Test Member can view personal loans and payment reminders
  - Test responsive layout on different viewport sizes
  - Test error handling and recovery
  - _Requirements: All_

- [ ]* 32.1 Write end-to-end tests for critical user flows
  - Test complete deposit flow from dashboard
  - Test complete loan payment flow from dashboard
  - Test search and navigation flow
  - _Requirements: 12.2, 12.3, 12.4_

- [ ] 33. Documentation: Update API documentation
  - Document all new dashboard endpoints in docs/api/rest-endpoints.md
  - Add request/response examples
  - Document authentication and authorization requirements
  - _Requirements: All_

- [ ] 34. Documentation: Create dashboard user guide
  - Document dashboard features for each role
  - Add screenshots of dashboard layouts
  - Document quick actions and workflows
  - _Requirements: All_

- [ ] 35. Final Checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.
