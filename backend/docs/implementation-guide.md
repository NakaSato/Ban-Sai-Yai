# Implementation Guide

## Overview

This guide provides step-by-step instructions for implementing the Ban Sai Yai Savings Group Financial Accounting System, including development setup, coding standards, and best practices.

## Development Setup

### 1. Prerequisites

#### Required Software
- **JDK 17**: OpenJDK or Oracle JDK
- **Maven 3.8+**: Build tool and dependency management
- **Git**: Version control
- **IDE**: IntelliJ IDEA or Eclipse (recommended)
- **Database**: MariaDB 10.6+ or MySQL 8.0+

#### Optional Tools
- **Docker**: For containerized development
- **Postman**: API testing
- **DBeaver**: Database management
- **Lombok**: Code generation

### 2. Project Setup

#### Clone and Build
```bash
# Clone repository
git clone https://github.com/organization/bansaiyai-system.git
cd bansaiyai-system

# Install dependencies
./mvnw clean install

# Run application
./mvnw spring-boot:run
```

#### IDE Configuration

##### IntelliJ IDEA
1. Open project as Maven project
2. Enable Lombok plugin: File → Settings → Plugins → Install Lombok
3. Configure code style: File → Settings → Editor → Code Style → Java
4. Set up code formatting: Import provided code style XML

##### Eclipse
1. Import as Maven project
2. Install Lombok via Marketplace
3. Configure annotation processing
4. Import code style preferences

## Project Structure

```
src/
├── main/
│   ├── java/
│   │   └── com/bansaiyai/bansaiyai/
│   │       ├── BansaiyaiApplication.java
│   │       ├── config/
│   │       │   ├── SecurityConfig.java
│   │       │   ├── JpaConfig.java
│   │       │   └── WebConfig.java
│   │       ├── controller/
│   │       │   ├── auth/
│   │       │   ├── member/
│   │       │   ├── loan/
│   │       │   ├── payment/
│   │       │   ├── dividend/
│   │       │   ├── report/
│   │       │   └── accounting/
│   │       ├── service/
│   │       │   ├── impl/
│   │       │   ├── MemberService.java
│   │       │   ├── LoanService.java
│   │       │   ├── PaymentService.java
│   │       │   ├── DividendService.java
│   │       │   ├── ReportService.java
│   │       │   └── AccountingService.java
│   │       ├── repository/
│   │       │   ├── MemberRepository.java
│   │       │   ├── LoanRepository.java
│   │       │   ├── PaymentRepository.java
│   │       │   ├── SavingAccountRepository.java
│   │       │   ├── DividendRepository.java
│   │       │   ├── TransactionRepository.java
│   │       │   └── AccountCodeRepository.java
│   │       ├── entity/
│   │       │   ├── Member.java
│   │       │   ├── User.java
│   │       │   ├── Loan.java
│   │       │   ├── Payment.java
│   │       │   ├── SavingAccount.java
│   │       │   ├── Dividend.java
│   │       │   ├── Transaction.java
│   │       │   └── AccountCode.java
│   │       ├── dto/
│   │       │   ├── request/
│   │       │   └── response/
│   │       ├── exception/
│   │       │   ├── GlobalExceptionHandler.java
│   │       │   ├── ValidationException.java
│   │       │   └── BusinessRuleException.java
│   │       ├── security/
│   │       │   ├── JwtTokenProvider.java
│   │       │   ├── UserDetailsServiceImpl.java
│   │       │   └── SecurityUtils.java
│   │       └── util/
│   │           ├── FileUtils.java
│   │           ├── DateUtils.java
│   │           ├── CalculationUtils.java
│   │           └── ValidationUtils.java
│   └── resources/
│       ├── application.yml
│       ├── application-dev.yml
│       ├── application-prod.yml
│       ├── application-test.yml
│       ├── db/migration/
│       ├── static/
│       └── templates/
└── test/
    ├── java/
    │   └── com/bansaiyai/bansaiyai/
    │       ├── controller/
    │       ├── service/
    │       ├── repository/
    │       └── integration/
    └── resources/
        ├── application-test.yml
        └── test-data/
```

## Coding Standards

### 1. Java Coding Conventions

#### Naming Conventions
```java
// Classes: PascalCase
public class MemberService {
}

// Methods: camelCase
public Member registerMember(MemberDTO memberDTO) {
}

// Variables: camelCase
private String memberName;

// Constants: UPPER_SNAKE_CASE
public static final String MINIMUM_AGE_ERROR = "Member must be at least 18 years old";

// Packages: lowercase with dots
package com.bansaiyai.bansaiyai.service.impl;
```

#### Code Organization
```java
@Service
@Transactional
public class MemberServiceImpl implements MemberService {
    
    // Dependencies
    private final MemberRepository memberRepository;
    private final FileStorageService fileStorageService;
    
    // Constructor
    public MemberServiceImpl(MemberRepository memberRepository, 
                         FileStorageService fileStorageService) {
        this.memberRepository = memberRepository;
        this.fileStorageService = fileStorageService;
    }
    
    // Public methods
    @Override
    public Member registerMember(MemberDTO memberDTO) {
        // Implementation
    }
    
    // Private helper methods
    private void validateMemberData(MemberDTO memberDTO) {
        // Implementation
    }
}
```

### 2. Spring Boot Best Practices

#### Service Layer Pattern
```java
@Service
@Transactional
public class LoanServiceImpl implements LoanService {
    
    private final LoanRepository loanRepository;
    private final MemberRepository memberRepository;
    private final CalculationUtils calculationUtils;
    
    // Use constructor injection
    @Autowired
    public LoanServiceImpl(LoanRepository loanRepository,
                        MemberRepository memberRepository,
                        CalculationUtils calculationUtils) {
        this.loanRepository = loanRepository;
        this.memberRepository = memberRepository;
        this.calculationUtils = calculationUtils;
    }
    
    @Override
    @Transactional(readOnly = true)
    public Loan getLoanById(Long loanId) {
        return loanRepository.findById(loanId)
            .orElseThrow(() -> new ResourceNotFoundException("Loan not found"));
    }
    
    @Override
    public Loan createLoan(LoanApplicationDTO applicationDTO) {
        // Validate business rules
        validateLoanApplication(applicationDTO);
        
        // Create loan entity
        Loan loan = convertToEntity(applicationDTO);
        
        // Save and return
        return loanRepository.save(loan);
    }
}
```

#### Repository Pattern
```java
@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {
    
    // Custom query methods
    List<Loan> findByMemberIdOrderByCreatedDateDesc(Long memberId);
    
    @Query("SELECT l FROM Loan l WHERE l.status = :status AND l.createdDate >= :date")
    List<Loan> findByStatusAndCreatedDateAfter(@Param("status") LoanStatus status,
                                           @Param("date") LocalDateTime date);
    
    @Query(value = "SELECT COUNT(*) FROM loan WHERE member_id = :memberId AND status IN :statuses", 
           nativeQuery = true)
    long countByMemberIdAndStatuses(@Param("memberId") Long memberId,
                                   @Param("statuses") List<LoanStatus> statuses);
}
```

### 3. Error Handling

#### Custom Exceptions
```java
public class ValidationException extends RuntimeException {
    private final List<FieldError> fieldErrors;
    
    public ValidationException(String message) {
        super(message);
        this.fieldErrors = new ArrayList<>();
    }
    
    public ValidationException(String message, List<FieldError> fieldErrors) {
        super(message);
        this.fieldErrors = fieldErrors;
    }
}

public class BusinessRuleException extends RuntimeException {
    private final String businessRule;
    
    public BusinessRuleException(String businessRule, String message) {
        super(message);
        this.businessRule = businessRule;
    }
}
```

#### Global Exception Handler
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(
            ValidationException ex) {
        
        return ResponseEntity.badRequest()
            .body(ApiResponse.error(ex.getMessage()));
    }
    
    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessRuleException(
            BusinessRuleException ex) {
        
        return ResponseEntity.unprocessableEntity()
            .body(ApiResponse.error(ex.getMessage()));
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        
        List<ApiError> errors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(error -> new ApiError(
                error.getField(),
                error.getDefaultMessage(),
                error.getRejectedValue()
            ))
            .collect(Collectors.toList());
        
        return ResponseEntity.badRequest()
            .body(ApiResponse.error("Validation failed", errors));
    }
}
```

### 4. Testing Standards

#### Unit Test Structure
```java
@ExtendWith(MockitoExtension.class)
class LoanServiceTest {
    
    // Mocks
    @Mock
    private LoanRepository loanRepository;
    
    @Mock
    private MemberRepository memberRepository;
    
    @InjectMocks
    private LoanServiceImpl loanService;
    
    // Test data
    private Loan testLoan;
    private Member testMember;
    
    @BeforeEach
    void setUp() {
        testMember = TestDataFactory.createTestMember();
        testLoan = TestDataFactory.createTestLoan();
    }
    
    @Test
    @DisplayName("Should create loan successfully")
    void testCreateLoan_Success() {
        // Given
        LoanApplicationDTO applicationDTO = createValidLoanApplicationDTO();
        
        when(memberRepository.findById(anyLong())).thenReturn(Optional.of(testMember));
        when(loanRepository.save(any(Loan.class))).thenReturn(testLoan);
        
        // When
        Loan result = loanService.createLoan(applicationDTO);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getMember()).isEqualTo(testMember);
        verify(loanRepository).save(any(Loan.class));
    }
    
    @Test
    @DisplayName("Should throw exception for invalid member")
    void testCreateLoan_InvalidMember_ThrowsException() {
        // Given
        LoanApplicationDTO applicationDTO = createValidLoanApplicationDTO();
        
        when(memberRepository.findById(anyLong())).thenReturn(Optional.empty());
        
        // When & Then
        assertThrows(ResourceNotFoundException.class, 
            () -> loanService.createLoan(applicationDTO));
    }
}
```

#### Integration Test Structure
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class LoanControllerIntegrationTest {
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Autowired
    private LoanRepository loanRepository;
    
    @Autowired
    private MemberRepository memberRepository;
    
    @Test
    @DisplayName("Should create loan via API")
    void testCreateLoan_Success() {
        // Given
        Member member = memberRepository.save(TestDataFactory.createTestMember());
        LoanApplicationDTO applicationDTO = createValidLoanApplicationDTO();
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(getAuthToken("OFFICER"));
        
        HttpEntity<LoanApplicationDTO> entity = new HttpEntity<>(applicationDTO, headers);
        
        // When
        ResponseEntity<ApiResponse> response = restTemplate.postForEntity(
            "/api/loans", entity, ApiResponse.class);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(loanRepository.count()).isEqualTo(1);
    }
}
```

## Database Implementation

### 1. Entity Design

#### Base Entity
```java
@MappedSuperclass
public abstract class BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    @LastModifiedDate
    private LocalDateTime updatedAt;
    
    @Column(name = "created_by")
    private String createdBy;
    
    @Column(name = "updated_by")
    private String updatedBy;
    
    // Getters and setters
}
```

#### Entity Example
```java
@Entity
@Table(name = "member")
@EntityListeners(AuditingEntityListener.class)
public class Member extends BaseEntity {
    
    @Column(name = "member_id", unique = true, nullable = false)
    private String memberId;
    
    @Column(name = "name", nullable = false, length = 100)
    private String name;
    
    @Column(name = "id_card", unique = true, nullable = false, length = 13)
    private String idCard;
    
    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;
    
    @Column(name = "address", nullable = false, length = 200)
    private String address;
    
    @Column(name = "phone", nullable = false, length = 20)
    private String phone;
    
    @Column(name = "email", length = 100)
    private String email;
    
    @Column(name = "photo_path")
    private String photoPath;
    
    @Column(name = "registration_date", nullable = false)
    private LocalDate registrationDate;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    // Relationships
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Loan> loans;
    
    @OneToOne(mappedBy = "member", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private SavingAccount savingAccount;
    
    // Getters and setters with business logic
    public int getAge() {
        return Period.between(dateOfBirth, LocalDate.now()).getYears();
    }
    
    public boolean isEligibleForLoan() {
        return isActive && getAge() >= 18 && 
               registrationDate.isBefore(LocalDate.now().minusMonths(6));
    }
}
```

### 2. Database Migration

#### Flyway Migration Example
```sql
-- V1__Create_member_table.sql
CREATE TABLE member (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id VARCHAR(20) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    id_card VARCHAR(13) UNIQUE NOT NULL,
    date_of_birth DATE NOT NULL,
    address VARCHAR(200) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    email VARCHAR(100),
    photo_path VARCHAR(500),
    registration_date DATE NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    INDEX idx_member_id (member_id),
    INDEX idx_id_card (id_card),
    INDEX idx_registration_date (registration_date)
);

-- V2__Create_loan_table.sql
CREATE TABLE loan (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    loan_number VARCHAR(50) UNIQUE NOT NULL,
    member_id BIGINT NOT NULL,
    loan_type ENUM('PERSONAL', 'BUSINESS', 'EMERGENCY', 'EDUCATION', 'HOUSING') NOT NULL,
    principal_amount DECIMAL(15,2) NOT NULL,
    interest_rate DECIMAL(5,2) NOT NULL,
    term_months INT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status ENUM('PENDING', 'APPROVED', 'REJECTED', 'ACTIVE', 'COMPLETED', 'DEFAULTED') NOT NULL,
    purpose VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    FOREIGN KEY (member_id) REFERENCES member(id),
    INDEX idx_member_id (member_id),
    INDEX idx_loan_number (loan_number),
    INDEX idx_status (status)
);
```

## Security Implementation

### 1. JWT Authentication

#### JWT Token Provider
```java
@Component
public class JwtTokenProvider {
    
    @Value("${app.jwt.secret}")
    private String jwtSecret;
    
    @Value("${app.jwt.expiration}")
    private int jwtExpirationInMs;
    
    public String generateToken(Authentication authentication) {
        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();
        
        return Jwts.builder()
            .setSubject(userPrincipal.getUsername())
            .claim("userId", userPrincipal.getUserId())
            .claim("role", userPrincipal.getAuthorities().iterator().next().getAuthority())
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationInMs))
            .signWith(SignatureAlgorithm.HS512, jwtSecret)
            .compact();
    }
    
    public String getUsernameFromToken(String token) {
        return Jwts.parser()
            .setSigningKey(jwtSecret)
            .parseClaimsJws(token)
            .getBody()
            .getSubject();
    }
    
    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }
}
```

#### Security Configuration
```java
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    
    @Autowired
    private UserDetailsServiceImpl userDetailsService;
    
    @Autowired
    private JwtAuthenticationEntryPoint unauthorizedHandler;
    
    @Bean
    public JwtTokenProvider tokenProvider() {
        return new JwtTokenProvider();
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.cors().and().csrf().disable()
            .exceptionHandling().authenticationEntryPoint(unauthorizedHandler).and()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/public/**").permitAll()
                .requestMatchers("/api/actuator/health").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/members/search").permitAll()
                .requestMatchers("/api/members/**").hasAnyRole("OFFICER", "SECRETARY", "PRESIDENT")
                .requestMatchers("/api/loans/**").hasAnyRole("OFFICER", "SECRETARY", "PRESIDENT")
                .requestMatchers("/api/payments/**").hasAnyRole("OFFICER", "SECRETARY", "PRESIDENT")
                .requestMatchers("/api/dividends/**").hasAnyRole("SECRETARY", "PRESIDENT")
                .requestMatchers("/api/reports/**").hasAnyRole("SECRETARY", "PRESIDENT")
                .requestMatchers("/api/accounting/**").hasAnyRole("SECRETARY", "PRESIDENT")
                .anyRequest().authenticated()
            );
        
        http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
    
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter();
    }
}
```

### 2. Method-Level Security

#### Custom Security Annotations
```java
@Component("memberSecurity")
public class MemberSecurity {
    
    public boolean canViewMemberDetails(Long memberId, Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        // Admin can view all members
        if (userDetails.hasRole("SECRETARY") || userDetails.hasRole("PRESIDENT")) {
            return true;
        }
        
        // Officers can view members they manage
        if (userDetails.hasRole("OFFICER")) {
            return canManageMember(memberId, userDetails.getUserId());
        }
        
        // Members can only view their own details
        if (userDetails.hasRole("MEMBER")) {
            return memberId.equals(userDetails.getMemberId());
        }
        
        return false;
    }
    
    private boolean canManageMember(Long memberId, Long officerId) {
        // Business logic to check if officer can manage this member
        return true; // Simplified for example
    }
}
```

## API Implementation

### 1. REST Controller Pattern

#### Base Controller
```java
@RestController
@RequestMapping("/api/members")
@Validated
@Slf4j
public class MemberController {
    
    private final MemberService memberService;
    
    @Autowired
    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }
    
    @PostMapping
    @PreAuthorize("hasRole('OFFICER')")
    public ResponseEntity<ApiResponse<Member>> createMember(
            @Valid @RequestBody MemberDTO memberDTO,
            Authentication authentication) {
        
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        try {
            Member member = memberService.registerMember(memberDTO, userDetails.getUserId());
            
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(member, "Member created successfully"));
                
        } catch (ValidationException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @GetMapping("/{memberId}")
    @PreAuthorize("@memberSecurity.canViewMemberDetails(#memberId, authentication)")
    public ResponseEntity<ApiResponse<Member>> getMember(@PathVariable Long memberId) {
        
        Member member = memberService.getMemberById(memberId);
        
        return ResponseEntity.ok(ApiResponse.success(member));
    }
    
    @GetMapping
    @PreAuthorize("hasAnyRole('OFFICER', 'SECRETARY', 'PRESIDENT')")
    public ResponseEntity<ApiResponse<List<Member>>> getMembers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        Pageable pageable = PageRequest.of(page, size, 
            Sort.by(Sort.Direction.fromString(sortDir), sortBy));
        
        Page<Member> members = memberService.getAllMembers(pageable);
        
        return ResponseEntity.ok(ApiResponse.success(members.getContent()));
    }
}
```

### 2. DTO Pattern

#### Request DTO
```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberDTO {
    
    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;
    
    @NotBlank(message = "ID card is required")
    @Pattern(regexp = "^\\d{13}$", message = "ID card must be exactly 13 digits")
    private String idCard;
    
    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;
    
    @NotBlank(message = "Address is required")
    @Size(min = 10, max = 200, message = "Address must be between 10 and 200 characters")
    private String address;
    
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[0-9]{9,10}$", message = "Phone number must be 9-10 digits")
    private String phone;
    
    @Email(message = "Invalid email format")
    private String email;
}
```

#### Response DTO
```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberResponseDTO {
    
    private Long memberId;
    private String name;
    private String idCard;
    private LocalDate dateOfBirth;
    private Integer age;
    private String address;
    private String phone;
    private String email;
    private String photoUrl;
    private LocalDate registrationDate;
    private Boolean isActive;
    
    public static MemberResponseDTO fromEntity(Member member) {
        return MemberResponseDTO.builder()
            .memberId(member.getMemberId())
            .name(member.getName())
            .idCard(maskIdCard(member.getIdCard()))
            .dateOfBirth(member.getDateOfBirth())
            .age(member.getAge())
            .address(member.getAddress())
            .phone(member.getPhone())
            .email(member.getEmail())
            .photoUrl(member.getPhotoPath())
            .registrationDate(member.getRegistrationDate())
            .isActive(member.getIsActive())
            .build();
    }
    
    private static String maskIdCard(String idCard) {
        if (idCard == null || idCard.length() < 13) return idCard;
        return idCard.substring(0, 3) + "XXXXX" + idCard.substring(8);
    }
}
```

## Business Logic Implementation

### 1. Service Layer Example

#### Loan Calculation Service
```java
@Service
@Transactional
public class LoanCalculationServiceImpl implements LoanCalculationService {
    
    @Override
    public BigDecimal calculateMonthlyInstallment(BigDecimal principal, 
                                         BigDecimal annualRate, 
                                         Integer termMonths) {
        
        // Convert annual rate to monthly rate
        BigDecimal monthlyRate = annualRate.divide(BigDecimal.valueOf(1200), 
            8, RoundingMode.HALF_UP);
        
        // Calculate installment using reducing balance formula
        BigDecimal denominator = BigDecimal.ONE.subtract(
            BigDecimal.ONE.add(monthlyRate).pow(-termMonths));
        
        return principal.multiply(monthlyRate).divide(denominator, 
            2, RoundingMode.HALF_UP);
    }
    
    @Override
    public LoanSchedule generateLoanSchedule(Loan loan) {
        List<Installment> installments = new ArrayList<>();
        
        BigDecimal outstandingBalance = loan.getPrincipalAmount();
        BigDecimal monthlyRate = loan.getInterestRate().divide(BigDecimal.valueOf(1200), 
            8, RoundingMode.HALF_UP);
        
        BigDecimal monthlyInstallment = calculateMonthlyInstallment(
            loan.getPrincipalAmount(), loan.getInterestRate(), loan.getTermMonths());
        
        for (int month = 1; month <= loan.getTermMonths(); month++) {
            BigDecimal interestPayment = outstandingBalance.multiply(monthlyRate)
                .setScale(2, RoundingMode.HALF_UP);
            
            BigDecimal principalPayment = monthlyInstallment.subtract(interestPayment);
            
            installments.add(Installment.builder()
                .installmentNumber(month)
                .dueDate(loan.getStartDate().plusMonths(month))
                .principalAmount(principalPayment)
                .interestAmount(interestPayment)
                .totalAmount(monthlyInstallment)
                .outstandingBalance(outstandingBalance.subtract(principalPayment))
                .build());
            
            outstandingBalance = outstandingBalance.subtract(principalPayment);
        }
        
        return new LoanSchedule(installments);
    }
}
```

### 2. Validation Service

#### Business Rule Validation
```java
@Service
public class ValidationServiceImpl implements ValidationService {
    
    @Override
    public void validateLoanApplication(LoanApplicationDTO applicationDTO) {
        // Age validation
        Member member = memberRepository.findById(applicationDTO.getMemberId())
            .orElseThrow(() -> new ResourceNotFoundException("Member not found"));
        
        if (member.getAge() < 18) {
            throw new ValidationException("Member must be at least 18 years old");
        }
        
        if (member.getRegistrationDate().isAfter(LocalDate.now().minusMonths(6))) {
            throw new ValidationException("Member must be active for at least 6 months");
        }
        
        // Loan-to-share ratio validation
        SavingAccount account = savingAccountRepository.findByMemberId(member.getMemberId())
            .orElseThrow(() -> new ResourceNotFoundException("Saving account not found"));
        
        BigDecimal maxLoanAmount = account.getShareCapital().multiply(BigDecimal.valueOf(5));
        
        if (applicationDTO.getLoanAmount().compareTo(maxLoanAmount) > 0) {
            throw new ValidationException(
                "Loan amount cannot exceed 5 times share capital");
        }
        
        // Existing loans validation
        long activeLoansCount = loanRepository.countActiveLoans(member.getMemberId());
        
        if (activeLoansCount >= 2) {
            throw new ValidationException("Member cannot have more than 2 active loans");
        }
    }
}
```

## Configuration Management

### 1. Application Properties

#### Multi-Profile Configuration
```yaml
# application.yml
spring:
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:dev}
  
  application:
    name: bansaiyai
    
  datasource:
    url: ${DB_URL:jdbc:mysql://localhost:3306/bansaiyai_db}
    username: ${DB_USERNAME:bansaiyai_user}
    password: ${DB_PASSWORD:password}
    driver-class-name: com.mysql.cj.jdbc.Driver
    
  jpa:
    hibernate:
      ddl-auto: ${DDL_AUTO:validate}
    show-sql: ${SHOW_SQL:false}
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQL8Dialect
        
  servlet:
    multipart:
      max-file-size: ${MAX_FILE_SIZE:10MB}
      max-request-size: ${MAX_REQUEST_SIZE:10MB}
      
server:
  port: ${SERVER_PORT:8080}
  servlet:
    context-path: /api
    
app:
  upload:
    location: ${UPLOAD_LOCATION:/opt/bansaiyai/uploads}
  jwt:
    secret: ${JWT_SECRET:default-secret-key}
    expiration: ${JWT_EXPIRATION:86400000}
    
logging:
  level:
    com.bansaiyai: ${LOG_LEVEL:INFO}
    org.springframework.security: WARN
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
```

### 2. Bean Configuration

#### Utility Beans
```java
@Configuration
public class ApplicationConfig {
    
    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration()
            .setMatchingStrategy(MatchingStrategies.STRICT)
            .setSkipNullEnabled(true);
        return modelMapper;
    }
    
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return objectMapper;
    }
    
    @Bean
    public BigDecimalCalculator bigDecimalCalculator() {
        return new BigDecimalCalculator();
    }
    
    @Bean
    public FileUtils fileUtils(@Value("${app.upload.location}") String uploadLocation) {
        return new FileUtils(uploadLocation);
    }
}
```

## Implementation Checklist

### Sprint 1: Member Registration
- [ ] Implement Member entity with JPA annotations
- [ ] Create MemberRepository with custom queries
- [ ] Implement MemberService with validation logic
- [ ] Create MemberController with REST endpoints
- [ ] Implement file upload functionality
- [ ] Add unit tests for service layer
- [ ] Add integration tests for API endpoints
- [ ] Add database migration scripts

### Sprint 2: Savings Service
- [ ] Implement SavingAccount entity
- [ ] Create SavingAccountRepository
- [ ] Implement SavingService with interest calculation
- [ ] Create SavingController with deposit/withdrawal endpoints
- [ ] Implement transaction logging
- [ ] Add balance validation logic
- [ ] Create transaction history reports

### Sprint 3: Loan Management
- [ ] Implement Loan entity with relationships
- [ ] Create LoanRepository with complex queries
- [ ] Implement LoanService with business rules
- [ ] Create LoanController with CRUD operations
- [ ] Implement loan eligibility validation
- [ ] Create loan schedule generation
- [ ] Add collateral and guarantor management

### Sprint 4: Repayment & Receipts
- [ ] Implement Payment entity
- [ ] Create PaymentRepository
- [ ] Implement PaymentService with calculation logic
- [ ] Create PaymentController for processing payments
- [ ] Implement PDF receipt generation
- [ ] Add penalty calculation for late payments
- [ ] Create payment history reports

### Sprint 5: Approval Workflow
- [ ] Implement approval status management
- [ ] Create ApprovalService with workflow logic
- [ ] Implement multi-level approval process
- [ ] Create ApprovalController with approval endpoints
- [ ] Add notification system for approvals
- [ ] Implement approval history tracking
- [ ] Add role-based approval authority

### Sprint 6: Financial Reporting
- [ ] Implement report generation service
- [ ] Create balance sheet calculations
- [ ] Implement profit & loss statements
- [ ] Create trial balance generation
- [ ] Add export functionality (PDF, Excel)
- [ ] Implement report caching
- [ ] Create scheduled report generation

### Sprint 7: Dividend Calculation
- [ ] Implement dividend calculation logic
- [ ] Create profit distribution algorithm
- [ ] Implement tax calculation for dividends
- [ ] Create DividendService with validation
- [ ] Add dividend approval workflow
- [ ] Implement dividend payment processing
- [ ] Create dividend history reports

### Sprint 8: Accounting & GL
- [ ] Implement Chart of Accounts management
- [ ] Create journal entry processing
- [ ] Implement double-entry bookkeeping
- [ ] Create trial balance verification
- [ ] Implement month-end closing procedures
- [ ] Add accounting report generation
- [ ] Create audit trail functionality

---

**Related Documentation**:
- [Architecture Overview](architecture/system-design.md) - System design and patterns
- [Database Schema](architecture/database-schema.md) - Database design and entities
- [API Documentation](api/rest-endpoints.md) - API specifications
- [Testing Strategy](reference/testing-strategy.md) - Testing guidelines and examples
- [Deployment Guide](reference/deployment-guide.md) - Production deployment instructions
