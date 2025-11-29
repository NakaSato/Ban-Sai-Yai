# Testing Strategy Reference

## Overview

This document outlines the comprehensive testing strategy for the Ban Sai Yai Savings Group Financial Accounting System, covering unit testing, integration testing, and end-to-end testing approaches.

## Testing Pyramid

### Unit Tests (70%)
- **Purpose**: Test individual components in isolation
- **Tools**: JUnit 5, Mockito, AssertJ
- **Coverage Target**: 80% minimum
- **Scope**: Service layer, Repository layer, Utility classes

### Integration Tests (20%)
- **Purpose**: Test component interactions
- **Tools**: Spring Boot Test, TestContainers, H2 Database
- **Coverage Target**: Key workflows
- **Scope**: API endpoints, Database operations, External integrations

### End-to-End Tests (10%)
- **Purpose**: Test complete user workflows
- **Tools**: Selenium, Cypress, Postman/Newman
- **Coverage Target**: Critical business paths
- **Scope**: User journeys, API workflows

## Unit Testing Strategy

### Service Layer Testing

#### Example: MemberService Test
```java
@ExtendWith(MockitoExtension.class)
class MemberServiceTest {
    
    @Mock
    private MemberRepository memberRepository;
    
    @Mock
    private FileStorageService fileStorageService;
    
    @Mock
    private NotificationService notificationService;
    
    @InjectMocks
    private MemberService memberService;
    
    @Test
    @DisplayName("Should register new member successfully")
    void testRegisterMember_Success() {
        // Given
        MemberDTO memberDTO = createValidMemberDTO();
        Member savedMember = createSavedMember();
        
        when(memberRepository.existsByIdCard(memberDTO.getIdCard())).thenReturn(false);
        when(memberRepository.save(any(Member.class))).thenReturn(savedMember);
        when(fileStorageService.storeFile(any())).thenReturn("photo.jpg");
        
        // When
        Member result = memberService.registerMember(memberDTO, mockMultipartFile());
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(memberDTO.getName());
        assertThat(result.getIdCard()).isEqualTo(memberDTO.getIdCard());
        verify(memberRepository).save(any(Member.class));
        verify(notificationService).notifyMemberRegistration(any());
    }
    
    @Test
    @DisplayName("Should throw exception for duplicate ID card")
    void testRegisterMember_DuplicateIdCard_ThrowsException() {
        // Given
        MemberDTO memberDTO = createValidMemberDTO();
        
        when(memberRepository.existsByIdCard(memberDTO.getIdCard())).thenReturn(true);
        
        // When & Then
        assertThrows(ValidationException.class, 
            () -> memberService.registerMember(memberDTO, mockMultipartFile()));
    }
}
```

#### Loan Service Testing
```java
@ExtendWith(MockitoExtension.class)
class LoanServiceTest {
    
    @Mock
    private LoanRepository loanRepository;
    
    @Mock
    private MemberRepository memberRepository;
    
    @Mock
    private CollateralService collateralService;
    
    @InjectMocks
    private LoanService loanService;
    
    @Test
    @DisplayName("Should calculate loan installment correctly")
    void testCalculateLoanInstallment_Success() {
        // Given
        BigDecimal principal = new BigDecimal("100000");
        BigDecimal annualRate = new BigDecimal("12"); // 12% per year
        Integer termMonths = 12;
        
        // When
        BigDecimal installment = loanService.calculateMonthlyInstallment(principal, annualRate, termMonths);
        
        // Then
        // Expected calculation: (100000 * 0.12/12) / (1 - (1 + 0.12/12)^-12)
        BigDecimal expected = new BigDecimal("8884.88");
        assertThat(installment).isEqualByComparingTo(expected, within(new BigDecimal("0.01")));
    }
    
    @Test
    @DisplayName("Should validate loan eligibility")
    void testValidateLoanEligibility_EligibleMember() {
        // Given
        Member member = createEligibleMember();
        
        when(memberRepository.findById(anyLong())).thenReturn(Optional.of(member));
        when(loanRepository.countActiveLoans(anyLong())).thenReturn(1);
        
        // When & Then
        assertDoesNotThrow(() -> loanService.validateLoanEligibility(1L, new BigDecimal("50000")));
    }
}
```

### Repository Layer Testing

#### Member Repository Test
```java
@DataJpaTest
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.datasource.url=jdbc:h2:mem:testdb"
})
class MemberRepositoryTest {
    
    @Autowired
    private MemberRepository memberRepository;
    
    @Test
    @DisplayName("Should save and retrieve member")
    void testSaveAndFindMember_Success() {
        // Given
        Member member = Member.builder()
            .name("Test Member")
            .idCard("1234567890123")
            .address("Test Address")
            .phone("0812345678")
            .registrationDate(LocalDate.now())
            .isActive(true)
            .build();
        
        // When
        Member saved = memberRepository.save(member);
        Optional<Member> found = memberRepository.findById(saved.getMemberId());
        
        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Test Member");
        assertThat(found.get().getIdCard()).isEqualTo("1234567890123");
    }
    
    @Test
    @DisplayName("Should find member by ID card")
    void testFindByIdCard_Success() {
        // Given
        Member member = createTestMember();
        memberRepository.save(member);
        
        // When
        Optional<Member> found = memberRepository.findByIdCard("1234567890123");
        
        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Test Member");
    }
    
    @Test
    @DisplayName("Should check ID card existence")
    void testExistsByIdCard_Success() {
        // Given
        Member member = createTestMember();
        memberRepository.save(member);
        
        // When
        boolean exists = memberRepository.existsByIdCard("1234567890123");
        boolean notExists = memberRepository.existsByIdCard("9876543210987");
        
        // Then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }
}
```

## Integration Testing Strategy

### API Integration Testing

#### REST Controller Test
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class MemberControllerIntegrationTest {
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Autowired
    private MemberRepository memberRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Test
    @DisplayName("Should create new member via API")
    void testCreateMember_Success() {
        // Given
        MemberDTO memberDTO = createValidMemberDTO();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(getAuthToken("OFFICER"));
        
        HttpEntity<MemberDTO> entity = new HttpEntity<>(memberDTO, headers);
        
        // When
        ResponseEntity<ApiResponse> response = restTemplate.postForEntity(
            "/api/members", entity, ApiResponse.class);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(memberRepository.count()).isEqualTo(1);
        
        Member saved = memberRepository.findAll().get(0);
        assertThat(saved.getName()).isEqualTo(memberDTO.getName());
    }
    
    @Test
    @DisplayName("Should reject unauthorized access")
    void testCreateMember_Unauthorized_ThrowsException() {
        // Given
        MemberDTO memberDTO = createValidMemberDTO();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        // No authorization header
        
        HttpEntity<MemberDTO> entity = new HttpEntity<>(memberDTO, headers);
        
        // When
        ResponseEntity<ApiResponse> response = restTemplate.postForEntity(
            "/api/members", entity, ApiResponse.class);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
```

### Database Integration Testing

#### Transaction Management Test
```java
@SpringBootTest
@Transactional
class TransactionIntegrationTest {
    
    @Autowired
    private LoanService loanService;
    
    @Autowired
    private PaymentService paymentService;
    
    @Autowired
    private LoanRepository loanRepository;
    
    @Test
    @DisplayName("Should process loan payment with transaction rollback on error")
    void testProcessPayment_TransactionRollback_Success() {
        // Given
        Loan loan = createTestLoan();
        loanRepository.save(loan);
        
        BigDecimal paymentAmount = new BigDecimal("5000");
        
        // Mock payment processor to throw exception
        // This should cause transaction rollback
        
        // When & Then
        assertThrows(PaymentProcessingException.class, 
            () -> paymentService.processPayment(loan.getLoanId(), paymentAmount));
        
        // Verify transaction was rolled back
        Loan updatedLoan = loanRepository.findById(loan.getLoanId()).orElse(null);
        assertThat(updatedLoan.getOutstandingBalance())
            .isEqualTo(loan.getOutstandingBalance()); // Should be unchanged
    }
}
```

## End-to-End Testing Strategy

### UI Automation Testing

#### Selenium Test Example
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class MemberRegistrationE2ETest {
    
    private WebDriver driver;
    
    @BeforeEach
    void setUp() {
        driver = new ChromeDriver();
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
    }
    
    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
    
    @Test
    @DisplayName("Should complete member registration workflow")
    void testMemberRegistrationWorkflow_Success() {
        // Given
        driver.get("http://localhost:8080/members/register");
        
        // When
        driver.findElement(By.id("name")).sendKeys("John Doe");
        driver.findElement(By.id("idCard")).sendKeys("1234567890123");
        driver.findElement(By.id("address")).sendKeys("123 Test Street");
        driver.findElement(By.id("phone")).sendKeys("0812345678");
        
        WebElement fileInput = driver.findElement(By.id("photo"));
        fileInput.sendKeys("/path/to/test/photo.jpg");
        
        driver.findElement(By.id("submitBtn")).click();
        
        // Then
        WebDriverWait wait = new WebDriverWait(driver, 10);
        WebElement successMessage = wait.until(
            ExpectedConditions.visibilityOfElementLocated(By.className("success-message")));
        
        assertThat(successMessage.getText()).contains("Member registered successfully");
    }
}
```

### API Workflow Testing

#### Postman/Newman Collection
```json
{
  "info": {
    "name": "Ban Sai Yai API Tests",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "Complete Loan Workflow",
      "item": [
        {
          "name": "Login as Officer",
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Content-Type",
                "value": "application/json"
              }
            ],
            "body": {
              "mode": "raw",
              "raw": "{\"username\":\"officer\",\"password\":\"password\"}"
            },
            "url": {
              "raw": "{{baseUrl}}/api/auth/login",
              "host": ["{{baseUrl}}"],
              "path": ["api", "auth", "login"]
            }
          }
        },
        {
          "name": "Register Member",
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Content-Type",
                "value": "application/json"
              },
              {
                "key": "Authorization",
                "value": "Bearer {{accessToken}}"
              }
            ],
            "body": {
              "mode": "raw",
              "raw": "{\"name\":\"Test Member\",\"idCard\":\"1234567890123\",\"address\":\"Test Address\",\"phone\":\"0812345678\"}"
            },
            "url": {
              "raw": "{{baseUrl}}/api/members",
              "host": ["{{baseUrl}}"],
              "path": ["api", "members"]
            }
          }
        }
      ]
    }
  ]
}
```

## Performance Testing Strategy

### Load Testing with JMeter

#### JMeter Test Plan
```xml
<?xml version="1.0" encoding="UTF-8"?>
<jmeterTestPlan version="1.2" properties="5.0" jmeter="5.4.1">
  <hashTree>
    <TestPlan guiclass="TestPlanGui" testclass="TestPlan" testname="Ban Sai Yai Load Test">
      <stringProp name="TestPlan.comments">API Load Testing</stringProp>
      <boolProp name="TestPlan.functional_mode">false</boolProp>
      <boolProp name="TestPlan.serialize_threadgroups">false</boolProp>
    </TestPlan>
    
    <hashTree>
      <ThreadGroup guiclass="ThreadGroupGui" testclass="ThreadGroup" testname="API Users">
        <stringProp name="ThreadGroup.num_threads">100</stringProp>
        <stringProp name="ThreadGroup.ramp_time">60</stringProp>
        <stringProp name="ThreadGroup.duration">300</stringProp>
      </ThreadGroup>
      
      <hashTree>
        <HTTPSamplerProxy guiclass="HttpTestSampleGui" testclass="HTTPSamplerProxy" testname="Get Members">
          <stringProp name="HTTPSampler.domain">localhost</stringProp>
          <stringProp name="HTTPSampler.port">8080</stringProp>
          <stringProp name="HTTPSampler.path">/api/members</stringProp>
          <stringProp name="HTTPSampler.method">GET</stringProp>
        </HTTPSamplerProxy>
      </hashTree>
    </hashTree>
  </hashTree>
</jmeterTestPlan>
```

## Test Data Management

### Test Data Factory
```java
@Component
public class TestDataFactory {
    
    private static final Faker faker = new Faker();
    
    public static Member createTestMember() {
        return Member.builder()
            .name(faker.name().fullName())
            .idCard(faker.number().digits(13))
            .address(faker.address().fullAddress())
            .phone(faker.phoneNumber().phoneNumber())
            .email(faker.internet().emailAddress())
            .registrationDate(LocalDate.now())
            .isActive(true)
            .build();
    }
    
    public static Loan createTestLoan() {
        return Loan.builder()
            .loanType(LoanType.PERSONAL)
            .principalAmount(new BigDecimal("50000"))
            .interestRate(new BigDecimal("12.5"))
            .termMonths(12)
            .startDate(LocalDate.now())
            .endDate(LocalDate.now().plusMonths(12))
            .status(LoanStatus.APPROVED)
            .build();
    }
    
    public static SavingAccount createTestSavingAccount() {
        return SavingAccount.builder()
            .shareCapital(new BigDecimal("10000"))
            .deposit(new BigDecimal("5000"))
            .balance(new BigDecimal("15000"))
            .build();
    }
}
```

### Test Container Configuration
```java
@TestConfiguration
public class TestContainerConfig {
    
    @Bean
    @Primary
    public DataSource dataSource() {
        MariaDBContainer<?> mariaDB = new MariaDBContainer<>("mariadb:10.6")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");
        
        mariaDB.start();
        
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(mariaDB.getJdbcUrl());
        config.setUsername(mariaDB.getUsername());
        config.setPassword(mariaDB.getPassword());
        
        return new HikariDataSource(config);
    }
}
```

## Code Coverage

### JaCoCo Configuration
```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.7</version>
    <executions>
        <execution>
            <goals>
                <goal>prepare-agent</goal>
            </goals>
        </execution>
        <execution>
            <id>report</id>
            <phase>test</phase>
            <goals>
                <goal>report</goal>
            </goals>
        </execution>
        <execution>
            <id>check</id>
            <goals>
                <goal>check</goal>
            </goals>
            <configuration>
                <rules>
                    <rule>
                        <element>BUNDLE</element>
                        <limits>
                            <limit>
                                <counter>INSTRUCTION</counter>
                                <value>COVEREDRATIO</value>
                                <minimum>0.80</minimum>
                            </limit>
                        </limits>
                    </rule>
                </rules>
            </configuration>
        </execution>
    </executions>
</plugin>
```

## Continuous Testing

### GitHub Actions CI/CD
```yaml
name: Test and Build

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    
    services:
      mysql:
        image: mariadb:10.6
        env:
          MYSQL_ROOT_PASSWORD: password
          MYSQL_DATABASE: testdb
        options: >-
          --health-cmd="mysqladmin ping"
          --health-interval=10s
          --health-timeout=5s
          --health-retries=3
    
    steps:
    - uses: actions/checkout@v2
    
    - name: Set up JDK 17
      uses: actions/setup-java@v2
      with:
        java-version: '17'
        distribution: 'temurin'
    
    - name: Run tests
      run: ./mvnw test
    
    - name: Generate test report
      run: ./mvnw jacoco:report
    
    - name: Upload coverage to Codecov
      uses: codecov/codecov-action@v1
      with:
        file: ./target/site/jacoco/jacoco.xml
```

## Test Environment Management

### Test Profiles
```properties
# application-test.properties
spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1
spring.datasource.driver-class-name=org.h2.Driver
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true

# Disable email in tests
spring.mail.host=localhost
spring.mail.port=2525

# Test file storage
app.upload.location=/tmp/test-uploads

# Test security
app.jwt.secret=test-secret-key-for-testing-only
app.jwt.expiration=60000
```

### Test Utilities
```java
@Component
public class TestUtils {
    
    public static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public static <T> T fromJsonString(String json, Class<T> clazz) {
        try {
            return new ObjectMapper().readValue(json, clazz);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public static MockMultipartFile createMockFile(String filename, String content) {
        return new MockMultipartFile(
            "file", 
            filename, 
            MediaType.IMAGE_JPEG_VALUE, 
            content.getBytes()
        );
    }
    
    public static String generateTestToken(String role) {
        return Jwts.builder()
            .setSubject("testuser")
            .claim("role", role)
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + 60000))
            .signWith(SignatureAlgorithm.HS512, "test-secret")
            .compact();
    }
}
```

## Test Reporting

### Test Results Dashboard
- **Unit Tests**: JUnit XML reports
- **Integration Tests**: Spring Boot Test reports
- **Coverage**: JaCoCo HTML reports
- **Performance**: JMeter HTML reports
- **API Tests**: Postman/Newman HTML reports

### Quality Gates
- **Unit Test Coverage**: Minimum 80%
- **Integration Tests**: All critical paths covered
- **Performance**: Response time < 2 seconds under normal load
- **Security**: No critical vulnerabilities
- **Code Quality**: SonarQube quality gate passed

---

**Related Documentation**:
- [Business Rules](business-rules.md) - Business validation rules
- [Data Validation](data-validation.md) - Input validation framework
- [API Documentation](../api/rest-endpoints.md) - API specifications
