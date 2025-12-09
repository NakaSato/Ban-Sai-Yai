# Security & Authentication System

## Overview

The Ban Sai Yai Savings Group system implements **Role-Based Access Control (RBAC)** using **Spring Security** to ensure proper authentication and authorization for all system users. The security architecture follows the principle of least privilege, with each role having specific, well-defined permissions.

## Security Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    Spring Security Layer                    │
│                                                             │
│  ┌─────────────────┐  ┌─────────────────┐  ┌──────────────┐ │
│  │ Authentication  │  │  Authorization  │  │   Session    │ │
│  │   (UserDetails) │  │ (Role-Based)    │  │ Management   │ │
│  │                 │  │                 │  │              │ │
│  │ • User Login    │  │ • URL Security  │  │ • JWT Tokens │ │
│  │ • Password      │  │ • Method Security│  │ • Session    │ │
│  │   Encryption    │  │ • Role Hierarchy│  │   Timeout    │ │
│  │ • Token Mgmt    │  │ • Permission    │  │ • CSRF       │ │
│  │                 │  │   Checks        │  │   Protection │ │
│  └─────────────────┘  └─────────────────┘  └──────────────┘ │
└─────────────────────────────────────────────────────────────┘
                              │
┌─────────────────────────────────────────────────────────────┐
│                 User Role Hierarchy                         │
│                                                             │
│  ROLE_PRESIDENT (Executive Oversight)                       │
│  ├── ROLE_SECRETARY (Financial Management)                  │
│  │   ├── Access to all financial reports                   │
│  │   ├── Month-end closing capabilities                     │
│  │   └── Chart of Accounts management                      │
│  │                                                           │
│  ├── ROLE_OFFICER (Daily Operations)                        │
│  │   ├── Member registration                               │
│  │   ├── Payment processing                                │
│  │   └── Receipt generation                                │
│  │                                                           │
│  └── ROLE_MEMBER (Limited Access)                           │
│      ├── View personal data only                           │
│      ├── View own loan status                              │
│      └── View own savings balance                          │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

## User Roles & Permissions

### ROLE_PRESIDENT
**Description**: Executive oversight and final approval authority

**Permissions**:
- ✅ View all system dashboards and reports
- ✅ Approve or reject loan applications
- ✅ Access executive-level financial summaries
- ✅ Manage user roles and permissions
- ✅ View all member information
- ✅ Oversee dividend approvals
- ✅ System configuration management

**API Access**:
```
GET /api/dashboard/executive
PUT /api/loans/{id}/approve
PUT /api/loans/{id}/reject
GET /api/reports/summary
POST /api/users/roles
```

### ROLE_SECRETARY
**Description**: Financial management and accounting operations

**Permissions**:
- ✅ Access and generate all financial reports
- ✅ Perform month-end closing procedures
- ✅ Manage Chart of Accounts
- ✅ Calculate and process dividends
- ✅ View all financial transactions
- ✅ Generate balance sheets and P&L statements
- ✅ Export financial data

**API Access**:
```
GET /api/reports/financial
POST /api/accounting/close-month
GET /api/accounting/chart-of-accounts
POST /api/dividends/calculate
GET /api/transactions/all
```

### ROLE_OFFICER
**Description**: Daily operational tasks and member services

**Permissions**:
- ✅ Register new members
- ✅ Process loan applications
- ✅ Handle savings deposits and withdrawals
- ✅ Generate payment receipts
- ✅ View member profiles (non-sensitive data)
- ✅ Process loan repayments
- ✅ Upload member documents

**API Access**:
```
POST /api/members/register
POST /api/loans/apply
POST /api/savings/deposit
POST /api/payments/process
GET /api/members/profile/{id}
POST /api/receipts/generate
```

### ROLE_MEMBER
**Description**: Limited self-service access

**Permissions**:
- ✅ View own personal profile
- ✅ View own loan status and balance
- ✅ View own savings account balance
- ✅ View own transaction history
- ✅ Download own receipts
- ❌ Cannot view other members' data
- ❌ Cannot access financial reports
- ❌ Cannot perform administrative functions

**API Access**:
```
GET /api/members/me
GET /api/loans/my-loans
GET /api/savings/my-balance
GET /api/transactions/my-history
GET /api/receipts/my-receipts
```

## Spring Security Configuration

### Security Configuration Class

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    
    @Autowired
    private CustomUserDetailsService userDetailsService;
    
    @Autowired
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Public endpoints
                .requestMatchers("/api/auth/login").permitAll()
                .requestMatchers("/api/auth/refresh").permitAll()
                .requestMatchers("/error").permitAll()
                
                // Role-based endpoints
                .requestMatchers("/api/dashboard/executive/**")
                    .hasRole("PRESIDENT")
                .requestMatchers("/api/reports/financial/**")
                    .hasAnyRole("SECRETARY", "PRESIDENT")
                .requestMatchers("/api/accounting/**")
                    .hasAnyRole("SECRETARY", "PRESIDENT")
                .requestMatchers("/api/dividends/**")
                    .hasAnyRole("SECRETARY", "PRESIDENT")
                .requestMatchers("/api/loans/approve/**")
                    .hasAnyRole("PRESIDENT")
                .requestMatchers("/api/members/register")
                    .hasAnyRole("OFFICER", "SECRETARY", "PRESIDENT")
                .requestMatchers("/api/savings/**")
                    .hasAnyRole("OFFICER", "SECRETARY", "PRESIDENT")
                .requestMatchers("/api/payments/**")
                    .hasAnyRole("OFFICER", "SECRETARY", "PRESIDENT")
                
                // Member-specific endpoints
                .requestMatchers("/api/members/me").hasRole("MEMBER")
                .requestMatchers("/api/loans/my-loans").hasRole("MEMBER")
                .requestMatchers("/api/savings/my-balance").hasRole("MEMBER")
                .requestMatchers("/api/transactions/my-history").hasRole("MEMBER")
                
                // Admin endpoints
                .requestMatchers("/api/admin/**")
                    .hasAnyRole("SECRETARY", "PRESIDENT")
                
                // All other requests require authentication
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter(), 
                UsernamePasswordAuthenticationFilter.class)
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(jwtAuthenticationEntryPoint));
        
        return http.build();
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
    
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
    
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter();
    }
}
```

### Custom UserDetailsService

```java
@Service
public class CustomUserDetailsService implements UserDetailsService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Override
    public UserDetails loadUserByUsername(String username) 
            throws UsernameNotFoundException {
        
        UserEntity user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException(
                "User not found with username: " + username));
        
        return User.builder()
            .username(user.getUsername())
            .password(user.getPassword())
            .authorities(getAuthorities(user.getRole()))
            .accountExpired(false)
            .accountLocked(false)
            .credentialsExpired(false)
            .disabled(false)
            .build();
    }
    
    private Collection<? extends GrantedAuthority> getAuthorities(UserRole role) {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }
}
```

## JWT Token Management

### JWT Utility Class

```java
@Component
public class JwtTokenUtil {
    
    @Value("${jwt.secret}")
    private String secret;
    
    @Value("${jwt.expiration}")
    private Long expiration;
    
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", userDetails.getAuthorities());
        return createToken(claims, userDetails.getUsername());
    }
    
    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
            .setClaims(claims)
            .setSubject(subject)
            .setIssuedAt(new Date(System.currentTimeMillis()))
            .setExpiration(new Date(System.currentTimeMillis() + expiration))
            .signWith(SignatureAlgorithm.HS512, secret)
            .compact();
    }
    
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = getUsernameFromToken(token);
        return (username.equals(userDetails.getUsername()) 
                && !isTokenExpired(token));
    }
    
    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }
    
    public List<String> getRolesFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        return claims.get("roles", List.class);
    }
    
    private Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }
}
```

### JWT Authentication Filter

```java
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    
    @Autowired
    private CustomUserDetailsService userDetailsService;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain chain) throws ServletException, IOException {
        
        final String requestTokenHeader = request.getHeader("Authorization");
        
        String username = null;
        String jwtToken = null;
        
        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
            jwtToken = requestTokenHeader.substring(7);
            try {
                username = jwtTokenUtil.getUsernameFromToken(jwtToken);
            } catch (IllegalArgumentException e) {
                logger.error("Unable to get JWT Token", e);
            } catch (ExpiredJwtException e) {
                logger.error("JWT Token has expired", e);
            }
        }
        
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
            
            if (jwtTokenUtil.validateToken(jwtToken, userDetails)) {
                UsernamePasswordAuthenticationToken authToken = 
                    new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        
        chain.doFilter(request, response);
    }
}
```

## Method-Level Security

### Service Layer Security

```java
@Service
@PreAuthorize("hasAnyRole('OFFICER', 'SECRETARY', 'PRESIDENT')")
public class MemberService {
    
    @PreAuthorize("hasRole('OFFICER') or #memberId == authentication.principal.memberId")
    public MemberDTO getMember(Long memberId) {
        // Implementation
    }
    
    @PreAuthorize("hasAnyRole('SECRETARY', 'PRESIDENT')")
    public List<MemberDTO> getAllMembers() {
        // Implementation
    }
    
    @PostAuthorize("hasRole('OFFICER') or returnObject.memberId == authentication.principal.memberId")
    public MemberDTO findMemberById(Long memberId) {
        // Implementation
    }
}

@Service
public class LoanService {
    
    @PreAuthorize("hasAnyRole('OFFICER', 'SECRETARY', 'PRESIDENT')")
    public Loan applyLoan(LoanDTO loanDTO) {
        // Implementation
    }
    
    @PreAuthorize("hasRole('PRESIDENT')")
    public Loan approveLoan(Long loanId, String approvedBy) {
        // Implementation
    }
    
    @PreAuthorize("hasRole('MEMBER') and @loanSecurity.isOwner(#loanId, authentication.principal.username)")
    public LoanDTO getMyLoan(Long loanId) {
        // Implementation
    }
}
```

### Custom Security Expressions

```java
@Component("loanSecurity")
public class LoanSecurity {
    
    public boolean isOwner(Long loanId, String username) {
        UserEntity user = userRepository.findByUsername(username).orElse(null);
        if (user == null || user.getMember() == null) {
            return false;
        }
        
        Loan loan = loanRepository.findById(loanId).orElse(null);
        return loan != null && loan.getMember().getMemberId().equals(user.getMember().getMemberId());
    }
    
    public boolean canViewMemberDetails(Long memberId, Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        UserEntity currentUser = userDetails.getUser();
        
        // Users can view their own details
        if (currentUser.getMember() != null && 
            currentUser.getMember().getMemberId().equals(memberId)) {
            return true;
        }
        
        // Officers, Secretary, President can view any member
        return currentUser.getRole() != UserRole.ROLE_MEMBER;
    }
}
```

## Authentication Endpoints

### Login Controller

```java
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    
    @Autowired
    private CustomUserDetailsService userDetailsService;
    
    @PostMapping("/login")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody LoginRequest loginRequest) {
        
        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsername(), 
                    loginRequest.getPassword()
                )
            );
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Incorrect username or password", e);
        }
        
        final UserDetails userDetails = userDetailsService
            .loadUserByUsername(loginRequest.getUsername());
        
        final String jwt = jwtTokenUtil.generateToken(userDetails);
        
        return ResponseEntity.ok(new JwtResponse(jwt, userDetails.getUsername(), 
            userDetails.getAuthorities().iterator().next().getAuthority()));
    }
    
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshAuthenticationToken(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            
            String username = jwtTokenUtil.getUsernameFromToken(token);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            
            if (jwtTokenUtil.validateToken(token, userDetails)) {
                String newToken = jwtTokenUtil.generateToken(userDetails);
                return ResponseEntity.ok(new JwtResponse(newToken, username, 
                    userDetails.getAuthorities().iterator().next().getAuthority()));
            }
        }
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}
```

## Security Best Practices

### 1. Password Security
- **BCrypt** encryption with strength 12
- Password complexity requirements
- Password history tracking
- Account lockout after failed attempts

### 2. Session Management
- JWT tokens with configurable expiration
- Refresh token mechanism
- Secure token storage (HttpOnly cookies)
- Token revocation on logout

### 3. API Security
- HTTPS enforcement in production
- CORS configuration
- Rate limiting to prevent brute force
- Input validation and sanitization

### 4. Audit Logging
```java
@Aspect
@Component
@Slf4j
public class SecurityAuditAspect {
    
    @AfterReturning(pointcut = "@annotation(auditable)", returning = "result")
    public void auditAfterReturning(JoinPoint joinPoint, Auditable auditable, Object result) {
        String action = auditable.action();
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        
        log.info("AUDIT: User {} performed {} action on {}", 
                username, action, joinPoint.getSignature().getName());
    }
    
    @AfterThrowing(pointcut = "@annotation(auditable)", throwing = "exception")
    public void auditAfterThrowing(JoinPoint joinPoint, Auditable auditable, Exception exception) {
        String action = auditable.action();
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        
        log.error("AUDIT: User {} failed {} action on {} - Error: {}", 
                username, action, joinPoint.getSignature().getName(), exception.getMessage());
    }
}
```

## Testing Security

### Security Test Configuration

```java
@TestPropertySource(properties = {
    "jwt.secret=testSecretKeyForTesting12345678901234567890",
    "jwt.expiration=86400000" // 24 hours
})
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class SecurityTestConfig {
    
    @TestConfiguration
    static class TestSecurityConfig {
        
        @Bean
        public SecurityFilterChain testFilterChain(HttpSecurity http) throws Exception {
            http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
            return http.build();
        }
    }
}
```

### Integration Tests

```java
@SpringBootTest
@AutoConfigureMockMvc
public class SecurityIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    
    @Test
    public void testAccessWithPresidentRole() throws Exception {
        String token = generateTestToken("ROLE_PRESIDENT");
        
        mockMvc.perform(get("/api/dashboard/executive")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }
    
    @Test
    public void testAccessDeniedForMemberRole() throws Exception {
        String token = generateTestToken("ROLE_MEMBER");
        
        mockMvc.perform(get("/api/dashboard/executive")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }
    
    private String generateTestToken(String role) {
        UserDetails userDetails = User.builder()
            .username("testuser")
            .password("password")
            .authorities("ROLE_" + role)
            .build();
        
        return jwtTokenUtil.generateToken(userDetails);
    }
}
```

---

**Related Documentation**:
- [System Design](../architecture/system-design.md) - Overall security architecture
- [API Documentation](../api/rest-endpoints.md) - Secured endpoint definitions
- [Testing Strategy](../testing/unit-integration.md) - Security testing approaches
