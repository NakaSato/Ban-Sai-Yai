# Login Portal Improvements - Design Document

## Overview

This design document outlines the technical approach for enhancing the Bansaiyai Financial System login portal. The improvements focus on security, user experience, and accessibility while maintaining compatibility with the existing Spring Boot backend and React frontend architecture.

The current implementation uses JWT-based authentication with a basic username/password form. The enhanced version will add:
- Password visibility toggle for better user experience
- "Remember Me" functionality with refresh tokens
- Real-time input validation with user feedback
- Rate limiting to prevent brute force attacks
- Automatic token refresh for seamless sessions
- Enhanced error handling and user feedback
- Full keyboard accessibility
- Mobile-responsive design

## Architecture

### High-Level Architecture

The login portal improvements follow a layered architecture:

```
┌─────────────────────────────────────────┐
│         React Frontend (UI Layer)       │
│  - LoginPage Component                  │
│  - Form Validation Hooks                │
│  - Token Management                     │
└──────────────┬──────────────────────────┘
               │ HTTPS/REST
┌──────────────▼──────────────────────────┐
│      Spring Boot Backend (API Layer)    │
│  - AuthController                       │
│  - Rate Limiting Filter                 │
│  - JWT Token Service                    │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│       Data Layer                        │
│  - User Repository                      │
│  - Token Store (Redis/In-Memory)        │
│  - Rate Limit Store                     │
└─────────────────────────────────────────┘
```

### Component Interaction Flow

1. **Login Flow:**
   - User enters credentials → Frontend validates → Backend authenticates → Issues tokens → Frontend stores tokens → Redirects to dashboard

2. **Remember Me Flow:**
   - User checks "Remember Me" → Backend issues refresh token → Frontend stores in localStorage → On return, auto-authenticates using refresh token

3. **Token Refresh Flow:**
   - Frontend detects token expiring → Requests new token with refresh token → Backend validates and issues new token → Frontend updates stored token

4. **Rate Limiting Flow:**
   - Failed login → Backend increments counter → After threshold, blocks attempts → Returns lockout error → Frontend displays countdown

## Components and Interfaces

### Frontend Components

#### 1. Enhanced LoginPage Component

**Location:** `frontend/src/pages/auth/LoginPage.tsx`

**Responsibilities:**
- Render login form with all interactive elements
- Manage form state and validation
- Handle authentication flow
- Display loading states and errors
- Manage "Remember Me" preference

**Key Methods:**
- `handleSubmit()`: Process form submission
- `handleInputChange()`: Update form state and trigger validation
- `togglePasswordVisibility()`: Show/hide password
- `clearError()`: Remove error messages

#### 2. useLoginValidation Hook

**Location:** `frontend/src/hooks/useLoginValidation.ts`

**Responsibilities:**
- Validate username and password inputs
- Provide real-time validation feedback
- Determine form submission eligibility

**Interface:**
```typescript
interface ValidationResult {
  isValid: boolean;
  errors: {
    username?: string;
    password?: string;
  };
}

function useLoginValidation(
  username: string,
  password: string,
  touched: { username: boolean; password: boolean }
): ValidationResult
```

#### 3. useTokenRefresh Hook

**Location:** `frontend/src/hooks/useTokenRefresh.ts`

**Responsibilities:**
- Monitor token expiration
- Automatically refresh tokens before expiry
- Handle refresh failures
- Redirect to login on session expiry

**Interface:**
```typescript
interface TokenRefreshConfig {
  refreshThresholdMinutes: number;
  maxRetries: number;
}

function useTokenRefresh(config: TokenRefreshConfig): {
  isRefreshing: boolean;
  lastRefreshTime: Date | null;
}
```

### Backend Components

#### 1. Enhanced AuthController

**Location:** `src/main/java/com/bansaiyai/bansaiyai/controller/AuthController.java`

**New/Modified Endpoints:**
- `POST /auth/login` - Enhanced to support "Remember Me"
- `POST /auth/refresh` - Enhanced with better error handling
- `POST /auth/logout` - Enhanced to invalidate refresh tokens

**Request/Response Models:**
```java
// Enhanced LoginRequest
class LoginRequest {
  String username;
  String password;
  boolean rememberMe;
}

// Enhanced LoginResponse
class LoginResponse {
  String accessToken;
  String refreshToken; // Only if rememberMe = true
  long expiresIn;
  String tokenType;
  UserInfo user;
}
```

#### 2. RateLimitingFilter

**Location:** `src/main/java/com/bansaiyai/bansaiyai/security/RateLimitingFilter.java`

**Responsibilities:**
- Track failed login attempts per username
- Enforce lockout after threshold exceeded
- Reset counters on successful login
- Provide lockout time remaining

**Configuration:**
```java
class RateLimitConfig {
  int maxAttempts = 5;
  int windowMinutes = 15;
  int lockoutMinutes = 15;
}
```

#### 3. TokenService

**Location:** `src/main/java/com/bansaiyai/bansaiyai/service/TokenService.java`

**Responsibilities:**
- Generate access tokens (short-lived, 15 minutes)
- Generate refresh tokens (long-lived, 7 days)
- Validate and refresh tokens
- Revoke tokens on logout

**Interface:**
```java
interface TokenService {
  TokenPair generateTokens(UserPrincipal user, boolean rememberMe);
  String refreshAccessToken(String refreshToken);
  void revokeRefreshToken(String refreshToken);
  boolean validateToken(String token);
}
```

#### 4. LoginAttemptService

**Location:** `src/main/java/com/bansaiyai/bansaiyai/service/LoginAttemptService.java`

**Responsibilities:**
- Record failed login attempts
- Check if username is locked
- Calculate remaining lockout time
- Reset attempts on success

**Interface:**
```java
interface LoginAttemptService {
  void recordFailedAttempt(String username);
  void recordSuccessfulAttempt(String username);
  boolean isBlocked(String username);
  long getRemainingLockoutSeconds(String username);
}
```

## Data Models

### Frontend Models

```typescript
// Login form state
interface LoginFormData {
  username: string;
  password: string;
  rememberMe: boolean;
}

// Validation state
interface ValidationState {
  username: {
    error: string | null;
    touched: boolean;
  };
  password: {
    error: string | null;
    touched: boolean;
  };
}

// Auth state
interface AuthState {
  user: UserInfo | null;
  accessToken: string | null;
  refreshToken: string | null;
  isLoading: boolean;
  error: string | null;
  isAuthenticated: boolean;
}

// Token storage
interface TokenStorage {
  accessToken: string;
  refreshToken?: string;
  expiresAt: number;
}
```

### Backend Models

```java
// Login attempt tracking
class LoginAttempt {
  String username;
  int failedAttempts;
  LocalDateTime firstAttemptTime;
  LocalDateTime lockoutUntil;
}

// Refresh token entity
@Entity
class RefreshToken {
  @Id
  Long id;
  
  String token;
  
  @ManyToOne
  User user;
  
  LocalDateTime expiresAt;
  LocalDateTime createdAt;
  boolean revoked;
}

// Token pair
class TokenPair {
  String accessToken;
  String refreshToken;
  long accessTokenExpiresIn;
  long refreshTokenExpiresIn;
}
```


## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system-essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

### Property 1: Password visibility toggle preserves value

*For any* password input value, toggling visibility should change the input type between "password" and "text" while preserving the exact password value.

**Validates: Requirements 1.1, 1.2**

### Property 2: Cursor position invariant during visibility toggle

*For any* cursor position in the password field, toggling password visibility should maintain the same cursor position.

**Validates: Requirements 1.4**

### Property 3: Remember Me issues refresh token

*For any* successful login with rememberMe=true, the authentication response should include a non-null refresh token with expiration greater than the access token.

**Validates: Requirements 2.1**

### Property 4: Refresh token enables passwordless authentication

*For any* valid refresh token, the authentication system should successfully authenticate the user and issue a new access token without requiring username or password.

**Validates: Requirements 2.2**

### Property 5: Session-only login excludes refresh token

*For any* successful login with rememberMe=false, the authentication response should not include a refresh token.

**Validates: Requirements 2.3**

### Property 6: Logout invalidates all tokens

*For any* authenticated session with access and refresh tokens, calling logout should invalidate both tokens such that subsequent use of either token fails authentication.

**Validates: Requirements 2.5**

### Property 7: Username validation rejects short inputs

*For any* username string with length less than 3 characters, the input validator should return an error indicating minimum length requirement.

**Validates: Requirements 3.1**

### Property 8: Password validation rejects short inputs

*For any* password string with length less than 8 characters, the input validator should return an error indicating minimum length requirement.

**Validates: Requirements 3.2**

### Property 9: Validation errors disable submission

*For any* form state containing one or more validation errors, the submit button should be disabled.

**Validates: Requirements 3.5**

### Property 10: Validation error clearing on correction

*For any* input field with a validation error, updating the field with a valid value should immediately clear the error for that field.

**Validates: Requirements 3.4**

### Property 11: Rate limiting blocks after threshold

*For any* username with 5 consecutive failed login attempts within a 15-minute window, the 6th attempt should be blocked and return a lockout error.

**Validates: Requirements 4.1**

### Property 12: Successful login resets rate limit counter

*For any* username with N failed attempts (where N < 5), a successful login should reset the failed attempt counter to 0.

**Validates: Requirements 4.4**

### Property 13: Lockout time does not extend

*For any* locked username, additional login attempts during the lockout period should not extend the lockout expiration time.

**Validates: Requirements 4.5**

### Property 14: Invalid credentials return generic error

*For any* combination of invalid username and password, the authentication error message should not reveal which credential was incorrect.

**Validates: Requirements 5.1**

### Property 15: Error clearing on user input

*For any* displayed error message, typing in any form field should clear the error message.

**Validates: Requirements 5.5**

### Property 16: Keyboard navigation order

*For any* focusable element in the login form, pressing Tab should move focus to the next element in the defined tab order, and Shift+Tab should move to the previous element.

**Validates: Requirements 6.1, 6.2**

### Property 17: Enter key submits form

*For any* input field in the login form, pressing Enter should trigger form submission if validation passes.

**Validates: Requirements 6.3**

### Property 18: Loading state disables inputs

*For any* form submission in progress, all input fields and the submit button should be disabled.

**Validates: Requirements 7.2**

### Property 19: Token refresh triggers before expiration

*For any* JWT token with expiration time T, when current time reaches T - 5 minutes, the session manager should automatically initiate token refresh.

**Validates: Requirements 8.1**

### Property 20: Failed refresh redirects to login

*For any* token refresh attempt that fails with an invalid refresh token error, the session manager should redirect the user to the login page.

**Validates: Requirements 8.3**

### Property 21: Touch targets meet minimum size

*For any* interactive element (button, input, checkbox) in the login portal, the touch target size should be at least 44x44 pixels.

**Validates: Requirements 9.5**

## Error Handling

### Frontend Error Handling

**Validation Errors:**
- Display inline error messages below each field
- Use red color and error icon for visual indication
- Clear errors immediately when user corrects input
- Prevent form submission when errors exist

**Authentication Errors:**
- Generic error for invalid credentials: "Invalid username or password"
- Specific error for lockout: "Account temporarily locked. Try again in X minutes"
- Network error: "Connection failed. Please check your internet and try again"
- Server error: "Something went wrong. Please try again later"

**Token Refresh Errors:**
- Silent retry for network errors (up to 3 attempts)
- Redirect to login for invalid/expired refresh tokens
- Show notification if refresh fails during active use

### Backend Error Handling

**Authentication Failures:**
- Return 401 Unauthorized for invalid credentials
- Return 423 Locked for rate-limited accounts
- Return 500 Internal Server Error for unexpected failures
- Log all authentication failures with username and timestamp

**Rate Limiting:**
- Track attempts in memory or Redis for scalability
- Clean up expired lockout records periodically
- Return remaining lockout time in error response

**Token Operations:**
- Return 401 for expired or invalid tokens
- Return 403 for revoked tokens
- Log token refresh failures for security monitoring

## Testing Strategy

### Unit Testing

**Frontend Unit Tests:**
- Test form validation logic with various inputs
- Test password visibility toggle functionality
- Test error message display and clearing
- Test keyboard navigation handlers
- Test token storage and retrieval
- Test responsive layout breakpoints

**Backend Unit Tests:**
- Test rate limiting logic with various attempt patterns
- Test token generation and validation
- Test refresh token lifecycle
- Test authentication with valid/invalid credentials
- Test lockout time calculations

### Property-Based Testing

The correctness properties defined above will be implemented using property-based testing frameworks:

**Frontend:** Use `fast-check` library for TypeScript/React
**Backend:** Use `jqwik` library for Java/Spring Boot

Each property-based test should:
- Run a minimum of 100 iterations with random inputs
- Include a comment referencing the specific correctness property
- Use the format: `**Feature: login-portal-improvements, Property {number}: {property_text}**`
- Generate realistic test data (valid usernames, passwords, tokens, etc.)

**Example Property Test Structure:**

```typescript
// Frontend example
/**
 * Feature: login-portal-improvements, Property 1: Password visibility toggle preserves value
 * Validates: Requirements 1.1, 1.2
 */
test('password visibility toggle preserves value', () => {
  fc.assert(
    fc.property(fc.string(), (password) => {
      // Test implementation
    }),
    { numRuns: 100 }
  );
});
```

```java
// Backend example
/**
 * Feature: login-portal-improvements, Property 11: Rate limiting blocks after threshold
 * Validates: Requirements 4.1
 */
@Property(tries = 100)
void rateLimitingBlocksAfterThreshold(@ForAll String username) {
  // Test implementation
}
```

### Integration Testing

**End-to-End Scenarios:**
- Complete login flow with valid credentials
- Login flow with "Remember Me" enabled
- Login flow with rate limiting triggered
- Token refresh during active session
- Logout and token invalidation
- Mobile responsive behavior

**API Integration Tests:**
- Test authentication endpoints with various payloads
- Test rate limiting across multiple requests
- Test token refresh endpoint
- Test logout endpoint

### Accessibility Testing

- Keyboard navigation through entire form
- Screen reader compatibility (ARIA labels and live regions)
- Focus indicators visibility
- Color contrast ratios
- Touch target sizes on mobile

## Security Considerations

### Password Security

- Never log or display passwords in plain text (except when visibility toggle is active)
- Use HTTPS for all authentication requests
- Implement password strength requirements (minimum 8 characters)
- Consider adding password strength indicator in future iteration

### Token Security

- Use short-lived access tokens (15 minutes)
- Store refresh tokens securely (httpOnly cookies or secure localStorage)
- **Implement token rotation on refresh:**
  - When a refresh token is used, immediately invalidate it
  - Issue a new refresh token along with the new access token
  - Store both old and new refresh token hashes in database with timestamps
  - Implement grace period (30 seconds) to handle race conditions from concurrent requests
  - If an already-used refresh token is presented, revoke all tokens for that user (potential token theft)
  - Log all token rotation events for security auditing
- Revoke all tokens on logout
- Validate token signatures and expiration on every request
- **Token Storage Strategy:**
  - Access tokens: Store in memory (Redux state) for automatic cleanup on browser close
  - Refresh tokens: Store in localStorage only when "Remember Me" is checked
  - Never store tokens in cookies without httpOnly flag
  - Clear all stored tokens on logout or security events

### Rate Limiting

- **Per-Username Rate Limiting:**
  - Track failed attempts per username in Redis or in-memory store
  - 5 failed attempts within 15-minute window triggers lockout
  - Lockout duration: 15 minutes from first failed attempt
  - Reset counter on successful login
- **IP-Based Rate Limiting (Additional Layer):**
  - Track failed attempts per IP address
  - 20 failed attempts from same IP within 15 minutes triggers IP lockout
  - IP lockout duration: 30 minutes
  - Prevents distributed attacks targeting multiple usernames
  - Use X-Forwarded-For header when behind proxy/load balancer
  - Whitelist internal IPs and trusted networks
- **Implementation Details:**
  - Use Redis sorted sets for efficient time-window queries
  - Key format: `rate_limit:username:{username}` and `rate_limit:ip:{ip}`
  - Store attempt timestamps as scores in sorted set
  - Periodically clean up expired entries (TTL on keys)
- Use exponential backoff for lockout periods (future enhancement: 15min → 30min → 1hr)
- Log suspicious activity for security monitoring (multiple usernames from same IP, rapid attempts)

### Error Messages

- Never reveal whether username or password was incorrect
- Provide specific errors only for non-security issues (lockout, network)
- Log detailed errors server-side for debugging
- Sanitize all error messages before displaying to users

## Performance Considerations

### Frontend Performance

- Debounce validation to avoid excessive re-renders
- Lazy load authentication API calls
- Minimize bundle size by code splitting
- Use React.memo for form components
- Implement virtual scrolling for mobile keyboards

### Backend Performance

- Use in-memory cache (Redis) for rate limiting data
- Index refresh tokens by user ID for fast lookup
- Implement connection pooling for database
- Use async processing for non-critical operations (logging)
- Set appropriate cache TTLs for token validation

### Network Optimization

- Compress API responses
- Implement request caching where appropriate
- Use HTTP/2 for multiplexing
- Minimize payload sizes
- Implement retry logic with exponential backoff

## Deployment Considerations

### Configuration

**Frontend Environment Variables:**
```
VITE_API_BASE_URL=https://api.bansaiyai.com
VITE_TOKEN_REFRESH_THRESHOLD_MINUTES=5
VITE_MAX_REFRESH_RETRIES=3
```

**Backend Application Properties:**
```yaml
auth:
  jwt:
    access-token-expiration: 900000  # 15 minutes
    refresh-token-expiration: 604800000  # 7 days
  rate-limit:
    max-attempts: 5
    window-minutes: 15
    lockout-minutes: 15
```

### Database Migrations

- Add `refresh_tokens` table with indexes on user_id and token
- Add `login_attempts` table with indexes on username and timestamp
- Implement cleanup job for expired tokens and old attempts

### Monitoring

- Track login success/failure rates
- Monitor rate limiting triggers
- Alert on unusual authentication patterns
- Track token refresh success rates
- Monitor API response times

## Future Enhancements

- Multi-factor authentication (MFA)
- Social login (Google, Facebook)
- Biometric authentication for mobile
- Password strength indicator
- Account recovery via email/SMS
- Login history and device management
- Passwordless authentication (magic links)
