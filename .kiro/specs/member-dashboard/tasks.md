# Implementation Plan

- [ ] 1. Create backend DTOs and entities for member dashboard
  - Create `MemberDashboardDTO` to aggregate all dashboard data
  - Create `DigitalPassbookDTO` and `WelfareStatusDTO` for passbook widget
  - Create `TransactionDTO` for transaction timeline
  - Create `LoanSimulationRequestDTO` and `LoanSimulationResultDTO` with `AmortizationEntryDTO`
  - Create `GuarantorObligationDTO` for guarantor tracking
  - Create `MemberEligibilityDTO` for loan eligibility checks
  - Create `WelfareFund` entity with JPA annotations
  - _Requirements: 1.1, 1.2, 1.3, 1.4, 2.2, 3.2, 3.3, 4.2_

- [ ] 2. Implement MemberDashboardService with core business logic
  - [ ] 2.1 Implement passbook summary aggregation
    - Create method to fetch member's total savings from SavingAccount
    - Create method to calculate outstanding loan (principal + accrued interest)
    - Create method to fetch welfare fund status
    - Aggregate data into DigitalPassbookDTO
    - _Requirements: 1.2, 1.3, 1.4_

  - [ ]* 2.2 Write property test for passbook balance consistency
    - **Property 1: Passbook balance consistency**
    - **Validates: Requirements 1.2**

  - [ ]* 2.3 Write property test for outstanding loan calculation
    - **Property 2: Outstanding loan calculation accuracy**
    - **Validates: Requirements 1.3**

  - [ ] 2.4 Implement transaction timeline retrieval
    - Create method to fetch paginated transactions for member
    - Sort transactions in reverse chronological order
    - Map SavingTransaction entities to TransactionDTO
    - Include officer name from teller_id
    - _Requirements: 2.1, 2.2, 2.3_

  - [ ]* 2.5 Write property test for transaction timeline completeness
    - **Property 3: Transaction timeline completeness**
    - **Validates: Requirements 2.1, 2.3**

  - [ ]* 2.6 Write property test for transaction timeline ordering
    - **Property 4: Transaction timeline ordering**
    - **Validates: Requirements 2.1**

  - [ ]* 2.7 Write property test for transaction officer attribution
    - **Property 5: Transaction officer attribution**
    - **Validates: Requirements 2.2**

- [ ] 3. Implement LoanSimulatorService with calculation logic
  - [ ] 3.1 Implement loan simulation calculation
    - Create method to calculate monthly installment using reducing balance formula
    - Create method to calculate total interest payable
    - Create method to generate amortization schedule
    - Fetch interest rate from LoanType enum
    - Validate input parameters (amount, term within loan type limits)
    - _Requirements: 3.2, 3.3, 3.4, 3.5_

  - [ ]* 3.2 Write property test for loan simulation calculation accuracy
    - **Property 6: Loan simulation calculation accuracy**
    - **Validates: Requirements 3.2, 3.3**

  - [ ]* 3.3 Write property test for interest rate consistency
    - **Property 7: Loan simulation interest rate consistency**
    - **Validates: Requirements 3.4**

  - [ ]* 3.4 Write property test for loan simulation validation
    - **Property 8: Loan simulation validation**
    - **Validates: Requirements 3.5**

  - [ ] 3.5 Implement member loan eligibility check
    - Create method to check if member is eligible for loans
    - Calculate maximum loan amount based on share capital
    - Return eligibility status with reason if not eligible
    - _Requirements: 3.1_

- [ ] 4. Implement GuarantorService for obligation tracking
  - [ ] 4.1 Implement guarantor obligations retrieval
    - Create method to fetch all loans guaranteed by member
    - Calculate days overdue for each loan
    - Determine loan status (CURRENT, OVERDUE, DEFAULTED)
    - Map to GuarantorObligationDTO with borrower details
    - _Requirements: 4.1, 4.2, 4.3_

  - [ ]* 4.2 Write property test for guarantor obligation completeness
    - **Property 9: Guarantor obligation completeness**
    - **Validates: Requirements 4.1**

  - [ ]* 4.3 Write property test for guarantor obligation status accuracy
    - **Property 10: Guarantor obligation status accuracy**
    - **Validates: Requirements 4.3**

  - [ ] 4.4 Implement guarantor notification system
    - Create method to detect missed payments
    - Create notification trigger for guarantors
    - Store notification in database or send via notification service
    - _Requirements: 4.4_

  - [ ]* 4.5 Write property test for guarantor notification trigger
    - **Property 11: Guarantor notification trigger**
    - **Validates: Requirements 4.4**

  - [ ] 4.6 Implement guarantor obligation removal logic
    - Create method to filter out fully repaid loans
    - Ensure only active obligations are returned
    - _Requirements: 4.5_

  - [ ]* 4.7 Write property test for guarantor obligation removal
    - **Property 12: Guarantor obligation removal**
    - **Validates: Requirements 4.5**

- [ ] 5. Create MemberDashboardController with REST endpoints
  - Create `GET /api/member-dashboard/{memberId}` endpoint for complete dashboard
  - Create `GET /api/member-dashboard/{memberId}/passbook` endpoint
  - Create `GET /api/member-dashboard/{memberId}/transactions` endpoint with pagination
  - Create `POST /api/member-dashboard/{memberId}/loan-simulation` endpoint
  - Create `GET /api/member-dashboard/{memberId}/guarantor-obligations` endpoint
  - Create `GET /api/member-dashboard/{memberId}/loan-eligibility` endpoint
  - Add authorization checks to ensure members can only access their own dashboard
  - Add validation for request parameters
  - Add error handling with appropriate HTTP status codes
  - _Requirements: 1.1, 1.2, 1.3, 1.4, 2.1, 2.2, 3.1, 3.2, 3.3, 4.1, 4.2_

- [ ]* 6. Write integration tests for MemberDashboardController
  - Test complete dashboard data retrieval
  - Test passbook endpoint with various member states
  - Test transaction timeline pagination
  - Test loan simulation with valid and invalid inputs
  - Test guarantor obligations endpoint
  - Test authorization (members can only access own dashboard)
  - Test error responses (404 for invalid member, 403 for unauthorized)
  - _Requirements: All_

- [ ] 7. Create database migration for WelfareFund entity
  - Create Flyway migration script to create welfare_fund table
  - Add foreign key constraint to member table
  - Add indexes for performance (member_id, is_eligible)
  - Create WelfareFundRepository interface
  - _Requirements: 1.4_

- [ ] 8. Create frontend API slice for member dashboard
  - Create `memberDashboardApi.ts` in `frontend/src/store/api/`
  - Define RTK Query endpoints for all dashboard APIs
  - Add TypeScript interfaces for request/response types
  - Configure cache invalidation tags
  - _Requirements: All_

- [ ] 9. Implement DigitalPassbookWidget component
  - [ ] 9.1 Create DigitalPassbookWidget component
    - Create component with card-based layout
    - Display member name and photo
    - Display total savings, outstanding loan, welfare status
    - Implement show/hide toggle for sensitive information
    - Add loading and error states
    - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5_

  - [ ]* 9.2 Write property test for privacy toggle state preservation
    - **Property 13: Privacy toggle state preservation**
    - **Validates: Requirements 1.5**

  - [ ]* 9.3 Write component tests for DigitalPassbookWidget
    - Test rendering with mock data
    - Test privacy toggle interaction
    - Test loading state
    - Test error state
    - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5_

- [ ] 10. Implement TransactionTimelineWidget component
  - Create TransactionTimelineWidget component
  - Display transactions in reverse chronological order
  - Show transaction date, type, amount, officer name
  - Implement infinite scroll or pagination for loading more transactions
  - Add filtering by date range and transaction type
  - Add loading and error states
  - _Requirements: 2.1, 2.2, 2.3, 2.4_

- [ ]* 11. Write component tests for TransactionTimelineWidget
  - Test rendering transaction list
  - Test pagination/infinite scroll
  - Test filtering functionality
  - Test empty state
  - Test loading state
  - _Requirements: 2.1, 2.2, 2.3, 2.4_

- [ ] 12. Implement LoanSimulatorWidget component
  - Create LoanSimulatorWidget component
  - Create form with loan type selector, amount input, term input
  - Implement form validation (min/max amounts, term limits)
  - Display calculation results (monthly installment, total interest)
  - Display amortization schedule table
  - Show member eligibility status
  - Add loading state during calculation
  - Add error handling for validation errors
  - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5_

- [ ]* 13. Write component tests for LoanSimulatorWidget
  - Test form rendering
  - Test input validation
  - Test calculation display
  - Test eligibility display
  - Test error handling
  - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5_

- [ ] 14. Implement GuarantorObligationsWidget component
  - Create GuarantorObligationsWidget component
  - Display list of guaranteed loans
  - Show borrower name, photo, loan amount, status
  - Highlight overdue loans with alert styling
  - Show days overdue for overdue loans
  - Add click handler to view loan details
  - Add empty state when no obligations
  - _Requirements: 4.1, 4.2, 4.3, 4.4_

- [ ]* 15. Write component tests for GuarantorObligationsWidget
  - Test rendering obligations list
  - Test overdue loan highlighting
  - Test empty state
  - Test click interactions
  - _Requirements: 4.1, 4.2, 4.3, 4.4_

- [ ] 16. Create MemberDashboardPage container component
  - Create MemberDashboardPage component
  - Integrate all four widgets (Passbook, Timeline, Simulator, Guarantor)
  - Fetch dashboard data using RTK Query hooks
  - Implement responsive layout for mobile-first design
  - Add error boundary for widget failures
  - Add loading skeleton for initial load
  - Handle authentication and authorization
  - _Requirements: All_

- [ ]* 17. Write integration tests for MemberDashboardPage
  - Test complete page rendering with all widgets
  - Test data loading from API
  - Test error handling
  - Test responsive layout
  - _Requirements: All_

- [ ] 18. Add routing and navigation for member dashboard
  - Add route `/member-dashboard` to React Router configuration
  - Add navigation link in member menu
  - Implement role-based access (ROLE_MEMBER only)
  - Add redirect for unauthorized users
  - _Requirements: All_

- [ ] 19. Implement mobile-responsive styling
  - Create CSS/Tailwind styles for mobile-first design
  - Ensure widgets stack vertically on mobile
  - Optimize touch interactions for mobile devices
  - Test on various screen sizes (mobile, tablet, desktop)
  - _Requirements: All_

- [ ] 20. Add database indexes for performance optimization
  - Create index on saving_transaction(saving_account_id, transaction_date DESC)
  - Create index on guarantor(member_id, loan_id)
  - Create index on loan(status, maturity_date)
  - Test query performance with large datasets
  - _Requirements: 2.1, 4.1_

- [ ] 21. Checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.
