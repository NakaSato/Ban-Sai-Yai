# Implementation Plan

- [x] 1. Set up backend infrastructure for token management and rate limiting
  - Create RefreshToken entity with user relationship, expiration, and revocation fields
  - Create LoginAttempt entity for tracking failed attempts and lockout state
  - Add database repositories for RefreshToken and LoginAttempt
  - Add database migrations for refresh_tokens and login_attempts tables
  - Configure JWT properties for access and refresh token expiration times in application.yml
  - _Requirements: 2.1, 2.4, 4.1_

- [x] 1.1 Write property test for refresh token generation
  - **Property 3: Remember Me issues refresh token**
  - **Validates: Requirements 2.1**

- [x] 2. Implement TokenService for token lifecycle management
  - Create TokenService with methods for generating token pairs (access + refresh)
  - Implement token validation logic checking signature and expiration
  - Implement refresh token rotation (issue new refresh token on refresh)
  - Implement token revocation for logout
  - _Requirements: 2.1, 2.2, 2.3, 2.5, 8.1_

- [x] 2.1 Write property test for token refresh
  - **Property 4: Refresh token enables passwordless authentication**
  - **Validates: Requirements 2.2**

- [ ]* 2.2 Write property test for session-only tokens
  - **Property 5: Session-only login excludes refresh token**
  - **Validates: Requirements 2.3**

- [ ]* 2.3 Write property test for logout token invalidation
  - **Property 6: Logout invalidates all tokens**
  - **Validates: Requirements 2.5**

- [x] 3. Implement LoginAttemptService for rate limiting
  - Create LoginAttemptService with methods to record failed/successful attempts
  - Implement logic to check if username is currently blocked
  - Implement lockout time calculation and remaining time retrieval
  - Add automatic cleanup of expired lockout records
  - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.5_

- [ ]* 3.1 Write property test for rate limiting threshold
  - **Property 11: Rate limiting blocks after threshold**
  - **Validates: Requirements 4.1**

- [ ]* 3.2 Write property test for counter reset
  - **Property 12: Successful login resets rate limit counter**
  - **Validates: Requirements 4.4**

- [ ]* 3.3 Write property test for lockout time stability
  - **Property 13: Lockout time does not extend**
  - **Validates: Requirements 4.5**

- [x] 4. Update DTOs for enhanced authentication
  - Add rememberMe field to LoginRequest DTO
  - Add refreshToken and expiresIn fields to LoginResponse DTO
  - Update frontend TypeScript types to match new DTO structure
  - _Requirements: 2.1, 2.2_

- [x] 5. Enhance AuthController with new authentication features
  - Modify login endpoint to accept rememberMe parameter and use TokenService
  - Update login response to include refresh token when rememberMe is true
  - Enhance refresh endpoint with better error handling and token rotation via TokenService
  - Update logout endpoint to revoke refresh tokens via TokenService
  - Integrate LoginAttemptService to check blocks before authentication
  - Add lockout error response with remaining time when blocked
  - Return generic error messages for invalid credentials
  - _Requirements: 2.1, 2.2, 2.5, 4.1, 4.2, 5.1, 5.2_

- [ ]* 5.1 Write property test for generic credential errors
  - **Property 14: Invalid credentials return generic error**
  - **Validates: Requirements 5.1**

- [x] 6. Create frontend validation hook
  - Create useLoginValidation hook in frontend/src/hooks/
  - Implement username validation (minimum 3 characters, required)
  - Implement password validation (minimum 8 characters, required)
  - Return validation errors and overall form validity state
  - Support touched state to show errors only after user interaction
  - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5_

- [x] 6.1 Write unit tests for useLoginValidation hook
  - Test username validation (empty, short, valid)
  - Test password validation (empty, short, valid)
  - Test touched state behavior
  - Test error clearing on correction
  - _Requirements: 3.1, 3.2, 3.4_

- [x] 7. Create token refresh hook for automatic session management
  - Create useTokenRefresh hook in frontend/src/hooks/
  - Leverage existing tokenRefresh.ts utility functions
  - Implement token expiration monitoring using JWT decode
  - Trigger automatic refresh when token is within 5 minutes of expiry
  - Implement retry logic with exponential backoff (max 3 retries)
  - Handle refresh failures by redirecting to login page
  - Update stored tokens on successful refresh
  - _Requirements: 8.1, 8.2, 8.3, 8.4, 8.5_

- [ ]* 7.1 Write property test for refresh timing
  - **Property 19: Token refresh triggers before expiration**
  - **Validates: Requirements 8.1**

- [ ]* 7.2 Write property test for failed refresh redirect
  - **Property 20: Failed refresh redirects to login**
  - **Validates: Requirements 8.3**

- [x] 8. Update authentication Redux slice and API
  - Update authSlice to store refresh token in state
  - Update login mutation to send rememberMe parameter
  - Update authApi to handle new response structure with refreshToken
  - Update token storage to persist refresh token in localStorage when provided
  - Update logout action to call backend and clear all tokens
  - _Requirements: 2.1, 2.2, 2.5, 8.1, 8.2_

- [x] 9. Enhance LoginPage component with new UI features
  - Add password visibility toggle button with eye icon (using Material-UI IconButton and Visibility/VisibilityOff icons)
  - Implement toggle state management and icon switching
  - Add "Remember Me" checkbox to form (using Material-UI Checkbox and FormControlLabel)
  - Integrate useLoginValidation hook for real-time validation
  - Display inline validation errors below each field using TextField helperText prop
  - Disable submit button when validation errors exist
  - Clear errors when user starts typing (integrate with clearError action)
  - Update error handling to display lockout messages with countdown timer
  - Show success message briefly before redirect using Alert component
  - Update login mutation to send rememberMe parameter
  - Update setAuth dispatch to include refreshToken from response
  - _Requirements: 1.1, 1.2, 1.3, 2.1, 3.1, 3.2, 3.3, 3.4, 3.5, 5.1, 5.2, 5.3, 5.4, 5.5, 7.1, 7.2, 7.3, 7.4_

- [ ]* 9.1 Write property test for password visibility toggle
  - **Property 1: Password visibility toggle preserves value**
  - **Validates: Requirements 1.1, 1.2**

- [ ]* 9.2 Write property test for cursor position
  - **Property 2: Cursor position invariant during visibility toggle**
  - **Validates: Requirements 1.4**

- [ ]* 9.3 Write property test for submit button state
  - **Property 9: Validation errors disable submission**
  - **Validates: Requirements 3.5**

- [ ]* 9.4 Write property test for error clearing
  - **Property 15: Error clearing on user input**
  - **Validates: Requirements 5.5**

- [ ]* 9.5 Write property test for loading state
  - **Property 18: Loading state disables inputs**
  - **Validates: Requirements 7.2**

- [x] 10. Implement keyboard accessibility features in LoginPage
  - Ensure proper tab order for all form elements (username, password, remember me, submit)
  - Add Enter key handler to submit form from any input field
  - Add Escape key handler to clear error messages
  - Ensure visible focus indicators on all interactive elements (Material-UI default)
  - Add ARIA labels and descriptions for screen readers (aria-label, aria-describedby)
  - Update ARIA live regions for loading and error states (role="alert" on Alert component)
  - _Requirements: 6.1, 6.2, 6.3, 6.4, 6.5, 7.5_

- [ ]* 10.1 Write property test for keyboard navigation
  - **Property 16: Keyboard navigation order**
  - **Validates: Requirements 6.1, 6.2**

- [ ]* 10.2 Write property test for Enter key submission
  - **Property 17: Enter key submits form**
  - **Validates: Requirements 6.3**

- [x] 11. Verify mobile responsive design
  - Verify responsive CSS works for mobile viewport sizes (Material-UI Container already responsive)
  - Ensure touch targets are minimum 44x44 pixels (Material-UI components default to accessible sizes)
  - Test layout for portrait and landscape orientations
  - Verify no horizontal scrolling at all zoom levels
  - Ensure viewport meta tag exists in index.html for proper mobile rendering
  - _Requirements: 9.1, 9.2, 9.3, 9.4, 9.5_

- [ ]* 11.1 Write property test for touch target sizes
  - **Property 21: Touch targets meet minimum size**
  - **Validates: Requirements 9.5**

- [x] 12. Integrate useTokenRefresh hook in App component
  - Import useTokenRefresh hook in AppContent component
  - Initialize hook with default configuration (5 minute threshold, 3 max retries)
  - Ensure hook only runs when user is authenticated
  - Hook will automatically handle token refresh throughout application lifecycle
  - _Requirements: 8.1, 8.2, 8.5_

- [x] 13. Checkpoint - Ensure all tests pass
  - Run backend tests: `mvn test`
  - Run frontend tests: `cd frontend && npm test`
  - Ensure all tests pass, ask the user if questions arise.

- [ ]* 14. Add integration tests for complete authentication flows
  - Test complete login flow with valid credentials
  - Test login with Remember Me enabled and token refresh
  - Test rate limiting across multiple failed attempts
  - Test logout and token invalidation
  - Test keyboard navigation through entire form
  - Test mobile responsive behavior at different breakpoints
  - _Requirements: All_

- [x] 15. Final checkpoint - Ensure all tests pass
  - Run all backend tests: `mvn test`
  - Run all frontend tests: `cd frontend && npm test`
  - Ensure all tests pass, ask the user if questions arise.
