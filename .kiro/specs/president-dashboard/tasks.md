# Implementation Plan: President Dashboard

- [ ] 1. Create backend DTOs for President Dashboard widgets
  - Create data transfer objects for all four widgets
  - Include PARAnalysisDTO, PARMemberDetailDTO, CapitalGrowthDTO, LoanApprovalItemDTO, LoanApprovalDecisionRequest, LoanApprovalResponseDTO, and DividendProjectionDTO
  - Add necessary nested classes (GuarantorInfo, MonthlyDataPoint)
  - _Requirements: 1.1, 1.2, 1.3, 1.4, 2.1, 2.2, 3.1, 3.3, 3.4, 3.5, 4.1, 4.2_

- [ ] 2. Implement PAR (Portfolio At Risk) analysis service methods
  - Add calculatePARAnalysis() method to DashboardService
  - Implement loan classification logic based on days overdue (Current: 0 days, Watchlist: 1-30 days, Substandard: 31-90 days, Default: >90 days)
  - Calculate PAR metrics including counts and amounts for each category
  - Add getPARMemberDetails() method to retrieve detailed member and guarantor information for a specific PAR category
  - _Requirements: 1.2, 1.3, 1.4, 1.5_

- [ ]* 2.1 Write property test for PAR classification consistency
  - **Property 1: PAR Classification Consistency**
  - **Validates: Requirements 1.2**

- [ ]* 2.2 Write property test for PAR segment totals
  - **Property 2: PAR Segment Totals Match Portfolio**
  - **Validates: Requirements 1.1, 1.2**

- [ ]* 2.3 Write property test for guarantor information completeness
  - **Property 3: Guarantor Information Completeness**
  - **Validates: Requirements 1.4**

- [ ] 3. Implement capital growth and membership trends service methods
  - Add getCapitalGrowthTrends() method to DashboardService
  - Query savings balances and retained earnings over specified time period
  - Query membership counts and changes (new members, resignations) by month
  - Aggregate data into monthly data points with both capital and membership metrics
  - Calculate growth rates for assets and membership
  - _Requirements: 2.1, 2.2, 2.5_

- [ ]* 3.1 Write property test for capital growth calculation accuracy
  - **Property 4: Capital Growth Calculation Accuracy**
  - **Validates: Requirements 2.1, 2.5**

- [ ]* 3.2 Write property test for membership trend consistency
  - **Property 5: Membership Trend Consistency**
  - **Validates: Requirements 2.2**

- [ ] 4. Implement loan approval queue service methods
  - Add getPendingLoanApplications() method to DashboardService
  - Query all loans with PENDING status
  - Calculate debt-to-savings ratio for each applicant
  - Identify high-risk applications based on debt ratios and guarantor leverage
  - Add approveLoanApplication() method to handle loan approval workflow
  - Add rejectLoanApplication() method to handle loan rejection workflow
  - Update loan status, metadata, and timestamps appropriately
  - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5_

- [ ]* 4.1 Write property test for loan approval queue filtering
  - **Property 6: Loan Approval Queue Filtering**
  - **Validates: Requirements 3.1**

- [ ]* 4.2 Write property test for debt-to-savings ratio calculation
  - **Property 7: Debt-to-Savings Ratio Calculation**
  - **Validates: Requirements 3.1, 3.3**

- [ ]* 4.3 Write property test for risk flag accuracy
  - **Property 8: Risk Flag Accuracy**
  - **Validates: Requirements 3.3**

- [ ]* 4.4 Write property test for loan approval state transition
  - **Property 9: Loan Approval State Transition**
  - **Validates: Requirements 3.4**

- [ ]* 4.5 Write property test for loan rejection state transition
  - **Property 10: Loan Rejection State Transition**
  - **Validates: Requirements 3.5**

- [ ] 5. Implement dividend projection service method
  - Add calculateDividendProjection() method to DashboardService
  - Query total income, total expenses, and statutory reserves from accounting records
  - Query total shares outstanding from member records
  - Calculate estimated dividend per share using formula: (income - expenses - reserves) / shares
  - Handle cases where data is unavailable or insufficient
  - Calculate dividend rate percentage
  - _Requirements: 4.1, 4.2, 4.3, 4.4_

- [ ]* 5.1 Write property test for dividend calculation formula
  - **Property 11: Dividend Calculation Formula**
  - **Validates: Requirements 4.2**

- [ ]* 5.2 Write property test for dividend projection data availability
  - **Property 12: Dividend Projection Data Availability**
  - **Validates: Requirements 4.3**

- [ ] 6. Add repository query methods
  - Add custom query methods to LoanRepository for PAR analysis and pending loans
  - Add custom query methods to MemberRepository for membership trends
  - Add custom query methods to SavingRepository for capital growth data
  - Add custom query methods to AccountingRepository for dividend calculations
  - Implement efficient queries with proper indexing
  - _Requirements: 1.2, 1.3, 2.1, 2.2, 3.1, 4.2_

- [ ] 7. Checkpoint - Ensure all backend tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 8. Add President Dashboard controller endpoints
  - Add GET /api/dashboard/president/par-analysis endpoint with @PreAuthorize("hasRole('PRESIDENT')")
  - Add GET /api/dashboard/president/par-details endpoint with category parameter
  - Add GET /api/dashboard/president/capital-growth endpoint with months parameter
  - Add GET /api/dashboard/president/loan-approval-queue endpoint
  - Add POST /api/dashboard/president/loan-approval-queue/{loanId}/approve endpoint
  - Add POST /api/dashboard/president/loan-approval-queue/{loanId}/reject endpoint
  - Add GET /api/dashboard/president/dividend-projection endpoint
  - Wire endpoints to DashboardService methods
  - Add proper error handling and validation
  - _Requirements: 1.1, 1.2, 1.3, 2.1, 2.2, 3.1, 3.4, 3.5, 4.1_

- [ ]* 8.1 Write unit tests for controller endpoints
  - Test authorization (verify 403 for non-PRESIDENT users)
  - Test request/response mapping
  - Test error responses for invalid inputs
  - _Requirements: 1.1, 2.1, 3.1, 4.1_

- [ ] 9. Create frontend API slice for President Dashboard
  - Add presidentDashboardApi endpoints to RTK Query
  - Define useGetPARAnalysisQuery hook
  - Define useGetPARDetailsQuery hook
  - Define useGetCapitalGrowthQuery hook
  - Define useGetLoanApprovalQueueQuery hook
  - Define useApproveLoanMutation hook
  - Define useRejectLoanMutation hook
  - Define useGetDividendProjectionQuery hook
  - Configure cache invalidation for mutations
  - _Requirements: 1.1, 1.3, 2.1, 3.1, 3.4, 3.5, 4.1_

- [ ] 10. Create PAR Radar Widget component
  - Create PARRadarWidget.tsx component
  - Implement donut chart visualization using chart library (e.g., Recharts)
  - Display four segments: Current (green), Watchlist (yellow), Substandard (orange), Default (red)
  - Add click handlers for segment selection
  - Display PAR metrics (counts and amounts)
  - Handle loading and error states
  - _Requirements: 1.1, 1.2_

- [ ] 11. Create PAR Details Modal component
  - Create PARDetailsModal.tsx component
  - Display table of members in selected PAR category
  - Show member name, loan amount, days overdue, and guarantor information
  - Add sorting and filtering capabilities
  - Handle empty state when no members in category
  - _Requirements: 1.3, 1.4_

- [ ] 12. Create Capital Growth Widget component
  - Create CapitalGrowthWidget.tsx component
  - Implement dual-axis line chart using chart library
  - Display total assets (savings + retained earnings) on primary axis
  - Display membership count on secondary axis
  - Show 12-month rolling period by default
  - Add time period selector (6 months, 12 months, 24 months)
  - Display growth rate indicators
  - Handle loading and error states
  - _Requirements: 2.1, 2.2, 2.3_

- [ ] 13. Create Loan Approval Queue Widget component
  - Create LoanApprovalQueueWidget.tsx component
  - Display table of pending loan applications
  - Show applicant name, loan amount, purpose, debt-to-savings ratio, guarantor status
  - Display risk level indicators (warning icons for high-risk loans)
  - Add approve and reject action buttons
  - Implement approval modal with notes input
  - Implement rejection modal with reason input
  - Handle loading and error states
  - Show success/error notifications after actions
  - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5_

- [ ] 14. Create Dividend Projection Widget component
  - Create DividendProjectionWidget.tsx component
  - Display estimated dividend per share prominently
  - Show calculation breakdown (income, expenses, reserves, shares)
  - Display dividend rate percentage
  - Show projection period
  - Handle insufficient data state with appropriate message
  - Add tooltip explanations for financial terms
  - _Requirements: 4.1, 4.2, 4.3_

- [ ] 15. Create President Dashboard Page component
  - Create PresidentDashboardPage.tsx component
  - Integrate all four widget components
  - Implement responsive grid layout
  - Add page header with title and description
  - Add refresh functionality for all widgets
  - Implement role-based access control (redirect if not PRESIDENT)
  - Handle overall loading and error states
  - _Requirements: 1.1, 2.1, 3.1, 4.1_

- [ ]* 15.1 Write unit tests for President Dashboard widgets
  - Test widget rendering with mock data
  - Test user interactions (button clicks, segment selection)
  - Test conditional rendering (loading, error, empty states)
  - _Requirements: 1.1, 2.1, 3.1, 4.1_

- [ ] 16. Add routing for President Dashboard
  - Add route for /dashboard/president in React Router configuration
  - Add RoleBasedRoute wrapper to enforce PRESIDENT role
  - Add navigation link in main navigation menu (visible only to PRESIDENT role)
  - Update navigation menu to highlight active dashboard route
  - _Requirements: 1.1, 2.1, 3.1, 4.1_

- [ ] 17. Final Checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.
