# Implementation Plan

- [x] 1. Database: Create RBAC schema and seed data
  - Create roles table with seed data for 4 roles (Officer, Secretary, President, Member)
  - Create permissions table with seed data for all permission slugs
  - Create role_permissions junction table with seed data mapping permissions to roles
  - Extend users table with role_id foreign key and status column
  - Create system_audit_log table with JSON columns for old/new values
  - Create cash_reconciliations table
  - Extend login_attempts table with locked_until column
  - Add indexes for performance optimization
  - _Requirements: 12.1, 12.2, 12.5_

- [x] 2. Backend: Create RBAC entity models
  - Create Role entity with ManyToMany relationship to Permission
  - Create Permission entity
  - Update User entity with ManyToOne relationship to Role and status field
  - Create AuditLog entity with JSON fields
  - Create CashReconciliation entity with status enum
  - Update LoginAttempt entity with lockout fields
  - _Requirements: 1.1, 12.2, 12.5_

- [x] 3. Backend: Create RBAC repositories
  - Create RoleRepository with findByRoleName method
  - Create PermissionRepository with findByPermSlug method
  - Create RolePermissionRepository for junction table operations
  - Create AuditLogRepository with custom queries for critical actions and violations
  - Create CashReconciliationRepository with date and status queries
  - Create GuarantorRepository with existsByLoanIdAndMemberId method
  - _Requirements: 2.1, 11.1, 12.3_

- [x] 4. Backend: Implement RolePermissionService
  - Implement getPermissionsForRole method with caching
  - Implement hasPermission method for permission checks
  - Implement addPermissionToRole method with cache invalidation
  - Implement removePermissionFromRole method with cache invalidation
  - Implement getRolePermissionMatrix method
  - Implement initializeDefaultPermissions method for startup
  - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5_

- [x] 4.1 Write property test for permission loading on login
  - **Property 2: Permission loading on login**
  - **Validates: Requirements 1.2**

- [x] 4.2 Write property test for permission propagation on addition
  - **Property 6: Immediate permission propagation on addition**
  - **Validates: Requirements 2.3**

- [x] 4.3 Write property test for permission revocation on removal
  - **Property 7: Immediate permission revocation on removal**
  - **Validates: Requirements 2.4**

- [x] 4.4 Write unit tests for RolePermissionService
  - Test getPermissionsForRole returns correct set
  - Test hasPermission with various role-permission combinations
  - Test cache invalidation on permission changes
  - _Requirements: 2.1, 2.2, 2.3, 2.4_


- [x] 5. Backend: Implement CustomPermissionEvaluator
  - Implement hasPermission method for Spring Security integration
  - Implement canApproveOwnTransaction method for SoD enforcement
  - Integrate with RolePermissionService for permission checks
  - _Requirements: 2.2, 15.3_

- [x] 5.1 Write property test for permission-based action authorization
  - **Property 5: Permission-based action authorization**
  - **Validates: Requirements 2.2**

- [x] 5.2 Write property test for self-approval denial
  - **Property 50: Self-approval denial**
  - **Validates: Requirements 15.3**

- [x] 6. Backend: Implement GuarantorAccessEvaluator
  - Implement canViewLoan method with borrower and guarantor checks
  - Implement getGuaranteedLoans method
  - Implement isActiveGuarantor method
  - _Requirements: 7.1, 7.2, 7.5_

- [x] 6.1 Write property test for guarantor relationship-based access
  - **Property 16: Guarantor relationship-based access**
  - **Validates: Requirements 7.1**

- [x] 6.2 Write property test for non-guarantor access denial
  - **Property 20: Non-guarantor access denial**
  - **Validates: Requirements 7.5**

- [x] 6.3 Write unit tests for GuarantorAccessEvaluator
  - Test canViewLoan for borrower
  - Test canViewLoan for guarantor
  - Test canViewLoan denial for non-related member
  - _Requirements: 7.1, 7.5_

- [x] 7. Backend: Implement AuditService
  - Implement logAction method with JSON serialization
  - Implement logAccessDenied method for 403 errors
  - Implement logRoleChange method
  - Implement getCriticalActions method (last 10 DELETE/OVERRIDE)
  - Implement getRoleViolations method
  - Implement getActivityHeatmap method
  - Add error handling for audit logging failures
  - _Requirements: 10.1, 10.4, 10.5, 11.1, 11.3, 11.4_

- [x] 7.1 Write property test for comprehensive CUD operation audit logging
  - **Property 30: Comprehensive CUD operation audit logging**
  - **Validates: Requirements 11.1**

- [x] 7.2 Write property test for audit log JSON format
  - **Property 34: Audit log JSON format for state changes**
  - **Validates: Requirements 11.5**

- [x] 7.3 Write unit tests for AuditService
  - Test logAction creates complete audit entry
  - Test JSON serialization of old/new values
  - Test getCriticalActions filtering
  - Test error handling for logging failures
  - _Requirements: 11.1, 11.5_

- [x] 8. Backend: Implement AuditAspect
  - Create @Audited annotation
  - Implement around advice for automatic audit logging
  - Capture method arguments and return values
  - Extract entity type and ID from method parameters
  - Integrate with AuditService
  - _Requirements: 11.1, 11.2_

- [x] 8.1 Write property test for loan approval audit with state capture
  - **Property 31: Loan approval audit with state capture**
  - **Validates: Requirements 11.2**

- [x] 9. Backend: Implement UserService
  - Implement createUser method with role assignment validation
  - Implement updateUserRole method with audit logging
  - Implement suspendUser method with status update
  - Implement deleteUser method with soft delete
  - Implement getAllUsers method
  - Implement validateRoleChange method
  - _Requirements: 1.1, 1.3, 13.2, 13.3, 13.4, 13.5_

- [x] 9.1 Write property test for single role assignment enforcement
  - **Property 1: Single role assignment enforcement**
  - **Validates: Requirements 1.1**

- [x] 9.2 Write property test for complete permission replacement on role change
  - **Property 3: Complete permission replacement on role change**
  - **Validates: Requirements 1.3**

- [x] 9.3 Write property test for user suspension status and login denial
  - **Property 41: User suspension status and login denial**
  - **Validates: Requirements 13.4**

- [x] 9.4 Write unit tests for UserService
  - Test createUser with valid and invalid roles
  - Test updateUserRole with audit logging
  - Test suspendUser updates status
  - Test deleteUser soft deletes record
  - _Requirements: 1.1, 1.3, 13.2, 13.3, 13.4, 13.5_

- [x] 10. Backend: Implement PasswordService
  - Implement validatePassword method with complexity rules
  - Implement hashPassword method using bcrypt cost factor 12
  - Implement verifyPassword method
  - _Requirements: 14.1, 14.2_

- [x] 10.1 Write property test for password complexity validation
  - **Property 43: Password complexity validation**
  - **Validates: Requirements 14.1**

- [x] 10.2 Write property test for password bcrypt hashing
  - **Property 44: Password bcrypt hashing**
  - **Validates: Requirements 14.2**

- [x] 12. Backend: Implement CashReconciliationService
  - Implement calculateDatabaseBalance method
  - Implement createReconciliation method with variance calculation
  - Implement hasVariance method
  - Implement approveDiscrepancy method with accounting entry
  - Implement rejectDiscrepancy method
  - Implement canCloseDay method
  - _Requirements: 8.2, 8.3, 8.4, 8.5, 9.3, 9.4, 9.5_

- [x] 12.1 Write property test for cash reconciliation variance calculation
  - **Property 21: Cash reconciliation variance calculation**
  - **Validates: Requirements 8.2**

- [x] 12.2 Write property test for day close authorization with zero variance
  - **Property 22: Day close authorization with zero variance**
  - **Validates: Requirements 8.3**

- [x] 12.3 Write property test for day close denial with non-zero variance
  - **Property 23: Day close denial with non-zero variance**
  - **Validates: Requirements 8.4**

- [x] 12.4 Write unit tests for CashReconciliationService
  - Test variance calculation accuracy
  - Test day close allowed with zero variance
  - Test day close prevented with variance
  - Test discrepancy approval creates accounting entry
  - _Requirements: 8.2, 8.3, 8.4, 9.3_

- [x] 13. Backend: Implement TransactionService with SoD enforcement
  - Extend transaction creation to record creator user ID
  - Implement canVoid method with 24-hour time check
  - Implement approval queue logic
  - Implement approval method with self-approval check
  - Record approver user ID separately from creator
  - _Requirements: 3.3, 3.4, 15.1, 15.2, 15.3, 15.4, 15.5_

- [x] 13.1 Write property test for time-based void authorization
  - **Property 10: Time-based void authorization for recent transactions**
  - **Validates: Requirements 3.3**

- [x] 13.2 Write property test for time-based void denial
  - **Property 11: Time-based void denial for old transactions**
  - **Validates: Requirements 3.4**

- [x] 13.3 Write property test for transaction creator tracking
  - **Property 48: Transaction creator tracking**
  - **Validates: Requirements 15.1**

- [x] 13.4 Write property test for different-user approval authorization
  - **Property 51: Different-user approval authorization**
  - **Validates: Requirements 15.4**

- [x] 14. Checkpoint - Ensure all backend service tests pass
  - Fixed compilation errors in DashboardServiceTest
  - All backend services compile successfully
  - Ready for integration testing phase

- [x] 15. Backend: Configure Spring Security with JWT
  - Create JwtAuthenticationFilter
  - Implement UserDetailsServiceImpl loading user with role and permissions
  - Configure SecurityFilterChain with role-based endpoint protection
  - Add JWT token generation with role claims
  - Add JWT token validation
  - _Requirements: 1.2, 1.4_

- [x] 15.1 Write property test for authorization based on current role only
  - **Property 4: Authorization based on current role only**
  - **Validates: Requirements 1.4**

- [x] 16. Backend: Create SecurityExceptionHandler
  - Implement handleAccessDenied for 403 errors with audit logging
  - Implement handleInsufficientAuth for 401 errors
  - Implement handleAccountLocked for lockout errors
  - Create custom exception classes
  - _Requirements: 10.4, 14.4_

- [x] 16.1 Write property test for unauthorized access attempt logging
  - **Property 29: Unauthorized access attempt logging**
  - **Validates: Requirements 10.4**

- [x] 17. Backend: Create RBAC REST endpoints
  - Create GET /api/roles endpoint
  - Create GET /api/permissions endpoint
  - Create GET /api/roles/{id}/permissions endpoint
  - Create POST /api/roles/{id}/permissions endpoint with @PreAuthorize
  - Create DELETE /api/roles/{id}/permissions/{permId} endpoint
  - _Requirements: 2.1, 2.3, 2.4_

- [x] 18. Backend: Create User Management REST endpoints
  - Create GET /api/admin/users endpoint with @PreAuthorize('ROLE_PRESIDENT')
  - Create POST /api/admin/users endpoint with validation
  - Create PUT /api/admin/users/{id}/role endpoint
  - Create PUT /api/admin/users/{id}/suspend endpoint
  - Create DELETE /api/admin/users/{id} endpoint (soft delete)
  - _Requirements: 13.1, 13.2, 13.3, 13.4, 13.5_

- [x] 18.1 Write property test for user creation required fields validation
  - **Property 39: User creation required fields validation**
  - **Validates: Requirements 13.2**

- [x] 18.2 Write integration tests for user management endpoints
  - Test President can access user management
  - Test other roles cannot access user management
  - Test user creation validation
  - Test role update with audit logging
  - Test user suspension and deletion
  - Test password complexity validation
  - _Requirements: 13.1, 13.2, 13.3_

- [ ] 19. Backend: Create Audit Dashboard REST endpoints
  - Create GET /api/audit/critical-actions endpoint
  - Create GET /api/audit/role-violations endpoint
  - Create GET /api/audit/activity-heatmap endpoint
  - Create GET /api/audit/off-hours-alerts endpoint
  - Add @PreAuthorize('ROLE_PRESIDENT') to all endpoints
  - _Requirements: 10.1, 10.2, 10.3, 10.4, 10.5_

- [ ] 19.1 Write property test for off-hours activity security alert
  - **Property 28: Off-hours activity security alert**
  - **Validates: Requirements 10.3**

- [ ] 20. Backend: Create Cash Reconciliation REST endpoints
  - Create POST /api/cash-reconciliation endpoint for Officer
  - Create GET /api/cash-reconciliation/pending endpoint for Secretary
  - Create POST /api/cash-reconciliation/{id}/approve endpoint for Secretary
  - Create POST /api/cash-reconciliation/{id}/reject endpoint for Secretary
  - Create GET /api/cash-reconciliation/can-close-day endpoint
  - _Requirements: 8.1, 8.2, 8.3, 8.4, 8.5, 9.1, 9.2, 9.4, 9.5_

- [ ] 20.1 Write property test for variance escalation to Secretary
  - **Property 24: Variance escalation to Secretary**
  - **Validates: Requirements 8.5, 9.1**

- [ ] 20.2 Write integration tests for cash reconciliation flow
  - Test Officer creates reconciliation with variance
  - Test Officer cannot close day with variance
  - Test Secretary approves discrepancy
  - Test day can close after approval
  - _Requirements: 8.2, 8.3, 8.4, 9.3, 9.4_

- [ ] 21. Backend: Create Guarantor access endpoints
  - Create GET /api/members/{id}/guaranteed-loans endpoint
  - Create GET /api/loans/{id} endpoint with guarantor access check
  - Integrate GuarantorAccessEvaluator into loan access control
  - _Requirements: 7.1, 7.2, 7.5_

- [ ] 21.1 Write property test for guaranteed loans widget accuracy
  - **Property 17: Guaranteed loans widget accuracy**
  - **Validates: Requirements 7.2**

- [ ] 21.2 Write property test for guarantor access revocation on loan completion
  - **Property 19: Guarantor access revocation on loan completion**
  - **Validates: Requirements 7.4**

- [ ] 22. Backend: Add role-based access control to existing endpoints
  - Add @PreAuthorize annotations to transaction endpoints
  - Add @PreAuthorize annotations to loan endpoints
  - Add @PreAuthorize annotations to accounting endpoints
  - Add @PreAuthorize annotations to member endpoints
  - Implement member data isolation in member endpoints
  - _Requirements: 3.1, 3.5, 4.1, 4.2, 4.3, 4.4, 4.5, 5.1, 5.3, 5.4, 6.1, 6.2, 6.4, 6.5_

- [ ] 22.1 Write property test for member data isolation
  - **Property 13: Member data isolation**
  - **Validates: Requirements 6.1, 6.2**

- [ ] 22.2 Write integration tests for role-based endpoint access
  - Test Officer can create transactions
  - Test Officer cannot edit general ledger
  - Test Secretary can edit chart of accounts
  - Test Secretary cannot create transactions
  - Test President can approve loans
  - Test President cannot create transactions
  - Test Member can only view own data
  - _Requirements: 3.1, 3.5, 4.2, 4.3, 5.2, 5.4, 6.1, 6.2_

- [ ] 23. Checkpoint - Ensure all backend integration tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 24. Frontend: Create TypeScript interfaces for RBAC
  - Create Role interface
  - Create Permission interface
  - Create User interface with role and permissions
  - Create AuditLog interface
  - Create CashReconciliation interface
  - Create CreateUserRequest and UpdateRoleRequest interfaces
  - Add to frontend/src/types/index.ts
  - _Requirements: 1.1, 11.1, 13.2, 13.3_

- [ ] 25. Frontend: Extend authSlice with role and permissions
  - Add role and permissions to user state
  - Update login action to store role and permissions from JWT
  - Add hasPermission selector
  - Add hasRole selector
  - _Requirements: 1.2, 2.2_

- [ ] 26. Frontend: Create rbacApi with RTK Query
  - Add getRoles query
  - Add getPermissions query
  - Add getRolePermissions query
  - Add addPermissionToRole mutation
  - Add removePermissionFromRole mutation
  - Add getUsers query
  - Add createUser mutation
  - Add updateUserRole mutation
  - Add suspendUser mutation
  - Add deleteUser mutation
  - _Requirements: 2.1, 2.3, 2.4, 13.1, 13.2, 13.3, 13.4, 13.5_

- [ ] 27. Frontend: Create RoleBasedRoute component
  - Implement route protection based on allowed roles
  - Add redirect to login if not authenticated
  - Add redirect to unauthorized page if wrong role
  - Add fallback prop for custom unauthorized UI
  - _Requirements: 1.4, 6.5_

- [ ] 27.1 Write unit tests for RoleBasedRoute
  - Test renders children when user has allowed role
  - Test redirects when user lacks allowed role
  - Test redirects to login when not authenticated
  - _Requirements: 1.4_

- [ ] 28. Frontend: Create PermissionGuard component
  - Implement conditional rendering based on permission
  - Add fallback prop for alternative UI
  - Use hasPermission selector from authSlice
  - _Requirements: 2.2_

- [ ] 28.1 Write unit tests for PermissionGuard
  - Test renders children when user has permission
  - Test does not render when user lacks permission
  - Test renders fallback when provided
  - _Requirements: 2.2_

- [ ] 29. Frontend: Create UserManagement component
  - Implement user list table with role and status columns
  - Add create user form with validation
  - Add edit role dialog
  - Add suspend/activate user buttons
  - Add soft delete user button with confirmation
  - Integrate with rbacApi
  - _Requirements: 13.1, 13.2, 13.3, 13.4, 13.5_

- [ ] 29.1 Write unit tests for UserManagement component
  - Test user list displays correctly
  - Test create user form validation
  - Test role update triggers API call
  - Test suspend user updates status
  - _Requirements: 13.1, 13.2, 13.3, 13.4_

- [ ] 30. Frontend: Create AuditDashboard component
  - Create CriticalActionsLog widget
  - Create StaffActivityHeatmap widget
  - Create RoleViolationsTable widget
  - Create SecurityAlertsPanel widget
  - Integrate with audit API endpoints
  - Add date range filters
  - _Requirements: 10.1, 10.2, 10.3, 10.4, 10.5_

- [ ] 30.1 Write unit tests for AuditDashboard
  - Test critical actions widget displays last 10 actions
  - Test role violations table displays 403 errors
  - Test security alerts show off-hours activities
  - _Requirements: 10.1, 10.4, 10.5_

- [ ] 31. Frontend: Create CashReconciliation component
  - Create physical cash count input form
  - Display database balance
  - Display calculated variance
  - Add close day button (disabled if variance exists)
  - Add notes field for Officer
  - Show escalation message when variance exists
  - _Requirements: 8.1, 8.2, 8.3, 8.4, 8.5_

- [ ] 31.1 Write unit tests for CashReconciliation component
  - Test variance calculation display
  - Test close day button disabled with variance
  - Test close day button enabled with zero variance
  - _Requirements: 8.2, 8.3, 8.4_

- [ ] 32. Frontend: Create DiscrepancyApproval component for Secretary
  - Display pending reconciliations list
  - Show variance amount and Officer notes
  - Add approve button with accounting entry confirmation
  - Add reject button with reason field
  - Integrate with cash reconciliation API
  - _Requirements: 9.1, 9.2, 9.3, 9.4, 9.5_

- [ ] 32.1 Write unit tests for DiscrepancyApproval component
  - Test pending reconciliations display
  - Test approve triggers API call
  - Test reject requires reason
  - _Requirements: 9.2, 9.3, 9.5_

- [ ] 33. Frontend: Create GuaranteedLoans widget
  - Display list of loans user has guaranteed
  - Show borrower name and loan amount
  - Add visual indicator (green/red) for repayment status
  - Add click handler to view loan details
  - Integrate with guarantor API
  - _Requirements: 7.2, 7.3_

- [ ] 33.1 Write property test for loan status visual indicator
  - **Property 18: Loan status visual indicator**
  - **Validates: Requirements 7.3**

- [ ] 33.2 Write unit tests for GuaranteedLoans widget
  - Test displays all guaranteed loans
  - Test green indicator for current loans
  - Test red indicator for delinquent loans
  - _Requirements: 7.2, 7.3_

- [ ] 34. Frontend: Update routing with role-based protection
  - Wrap admin routes with RoleBasedRoute for ROLE_PRESIDENT
  - Wrap accounting routes with RoleBasedRoute for ROLE_SECRETARY
  - Wrap transaction routes with RoleBasedRoute for ROLE_OFFICER
  - Wrap member profile routes with member data isolation
  - Add unauthorized page
  - _Requirements: 1.4, 3.1, 4.1, 5.1, 6.1, 6.5_

- [ ] 35. Frontend: Add permission-based UI elements
  - Wrap loan approve button with PermissionGuard
  - Wrap transaction void button with PermissionGuard
  - Wrap accounting edit buttons with PermissionGuard
  - Wrap user management link with PermissionGuard
  - Hide/show navigation items based on role
  - _Requirements: 2.2, 3.3, 4.2, 5.2, 13.1_

- [ ] 36. Frontend: Implement authorization error handling
  - Add API interceptor for 403 errors
  - Display user-friendly error messages
  - Add redirect to login on 401 errors
  - Show lockout message on account locked error
  - _Requirements: 10.4, 14.4_

- [ ] 37. Frontend: Add audit logging indicators
  - Show "This action will be audited" message on sensitive operations
  - Display last action timestamp in user menu
  - Add audit trail link for users to view their own actions
  - _Requirements: 11.1_

- [ ] 38. Checkpoint - Ensure all frontend tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 39. Integration: Test complete RBAC flows end-to-end
  - Test Officer creates transaction, President approves
  - Test Secretary cannot create transaction
  - Test President cannot create transaction (ghost transaction prevention)
  - Test Member can only view own data
  - Test Guarantor can view guaranteed loan
  - Test Non-guarantor cannot view loan
  - Test Cash reconciliation with variance escalation
  - Test Role change revokes old permissions
  - Test Account lockout after failed logins
  - Test Audit logs created for all CUD operations
  - _Requirements: All_

- [ ] 39.1 Write end-to-end tests for critical security flows
  - Test complete transaction approval flow with SoD
  - Test complete cash reconciliation flow
  - Test complete role change flow with permission updates
  - Test complete guarantor access flow
  - _Requirements: 3.2, 8.2, 8.3, 9.3, 9.4, 15.3, 15.4_

- [ ] 40. Documentation: Update API documentation
  - Document all RBAC endpoints in docs/api/rest-endpoints.md
  - Document permission slugs and their meanings
  - Document role-permission matrix
  - Add authentication and authorization examples
  - Document audit log structure
  - _Requirements: All_

- [ ] 41. Documentation: Create RBAC security guide
  - Document security principles and design decisions
  - Document role descriptions and responsibilities
  - Document permission management procedures
  - Document audit log review procedures
  - Document incident response procedures
  - _Requirements: All_

- [ ] 42. Documentation: Create user guides for each role
  - Create Officer user guide with transaction and reconciliation workflows
  - Create Secretary user guide with accounting and discrepancy approval workflows
  - Create President user guide with approval and audit dashboard workflows
  - Create Member user guide with profile and guaranteed loans access
  - _Requirements: All_

- [ ] 43. Security: Perform security audit
  - Review all @PreAuthorize annotations for correctness
  - Verify SQL injection prevention in all queries
  - Verify password hashing implementation
  - Verify audit log integrity and immutability
  - Verify session management and JWT expiration
  - Test for privilege escalation vulnerabilities
  - _Requirements: All_

- [ ] 44. Final Checkpoint - Ensure all tests pass and security audit complete
  - Ensure all tests pass, ask the user if questions arise.
