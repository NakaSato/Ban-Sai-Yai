# System Architecture & Design

## Overview

The Ban Sai Yai Savings Group system follows a **layered architecture pattern** using Spring Boot, replacing the original PHP-based web server architecture with a robust, enterprise-grade Java implementation.

## Architectural Layers

```
┌─────────────────────────────────────────────────────────────┐
│                    Presentation Layer                      │
│  ┌─────────────────┐  ┌─────────────────┐  ┌──────────────┐ │
│  │   Controllers   │  │   REST APIs     │  │   Web UI     │ │
│  │  (@Controller)  │  │(@RestController)│  │ (Thymeleaf)  │ │
│  └─────────────────┘  └─────────────────┘  └──────────────┘ │
└─────────────────────────────────────────────────────────────┘
                              │
┌─────────────────────────────────────────────────────────────┐
│                     Business Layer                         │
│  ┌─────────────────┐  ┌─────────────────┐  ┌──────────────┐ │
│  │    Services     │  │  Business Logic │  │   DTOs       │ │
│  │   (@Service)    │  │  Calculations   │  │ (Data Transfer│ │
│  │                 │  │ Validations     │  │  Objects)    │ │
│  └─────────────────┘  └─────────────────┘  └──────────────┘ │
└─────────────────────────────────────────────────────────────┘
                              │
┌─────────────────────────────────────────────────────────────┐
│                    Data Access Layer                       │
│  ┌─────────────────┐  ┌─────────────────┐  ┌──────────────┐ │
│  │  Repositories   │  │   JPA Entities  │  │   Database   │ │
│  │ (@Repository)   │  │  (@Entity)      │  │ (MariaDB)    │ │
│  │                 │  │                 │  │              │ │
│  └─────────────────┘  └─────────────────┘  └──────────────┘ │
└─────────────────────────────────────────────────────────────┘
```

## Core Components

### 1. Controller Layer (`@Controller` / `RestController`)

**Responsibility**: Handle HTTP requests and responses

**Key Controllers**:
- `MemberController` - Member registration and profile management
- `LoanController` - Loan requests and management
- `SavingsController` - Savings deposits and withdrawals
- `PaymentController` - Loan repayments and receipts
- `ReportController` - Financial reports generation
- `AuthController` - Authentication and authorization

**Example Structure**:
```java
@RestController
@RequestMapping("/api/members")
public class MemberController {
    
    @Autowired
    private MemberService memberService;
    
    @PostMapping("/register")
    public ResponseEntity<MemberDTO> registerMember(@RequestBody MemberDTO memberDTO) {
        return ResponseEntity.ok(memberService.createMember(memberDTO));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<MemberDTO> getMember(@PathVariable Long id) {
        return ResponseEntity.ok(memberService.getMember(id));
    }
}
```

### 2. Service Layer (`@Service`)

**Responsibility**: Contains business logic, calculations, and validations

**Key Services**:
- `MemberService` - Member registration logic
- `LoanService` - Loan processing and validation
- `SavingService` - Savings management
- `PaymentService` - Payment processing and receipt generation
- `ReportService` - Financial reporting
- `DividendService` - Dividend calculations
- `AccountingService` - General ledger management

**Business Logic Examples**:
- Interest calculations for loans
- Dividend distribution based on shares
- Loan eligibility validation
- Financial statement generation

### 3. Repository Layer (`@Repository`)

**Responsibility**: Data access and database operations

**Key Repositories**:
- `MemberRepository` - Member data operations
- `LoanRepository` - Loan data operations
- `SavingAccountRepository` - Savings operations
- `TransactionRepository` - Ledger transactions
- `AccountCodeRepository` - Chart of accounts

**Example Structure**:
```java
@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {
    
    List<Loan> findByStatus(LoanStatus status);
    
    List<Loan> findByMemberId(Long memberId);
    
    @Query("SELECT l FROM Loan l WHERE l.approvalDate BETWEEN :start AND :end")
    List<Loan> findLoansApprovedInPeriod(@Param("start") LocalDate start, 
                                         @Param("end") LocalDate end);
}
```

## Design Patterns

### 1. Repository Pattern
- Encapsulates data access logic
- Provides abstraction over database operations
- Facilitates unit testing with mocking

### 2. DTO Pattern (Data Transfer Objects)
- Safe data transfer between layers
- Prevents exposing internal entity structure
- Enables API versioning flexibility

### 3. Service Layer Pattern
- Centralizes business logic
- Provides transaction management
- Enables separation of concerns

### 4. Factory Pattern
- Used for creating different types of reports
- Generates various receipt formats
- Creates different loan types

## Security Architecture

### Spring Security Implementation

```
┌─────────────────────────────────────────────────────────────┐
│                  Spring Security Layer                     │
│  ┌─────────────────┐  ┌─────────────────┐  ┌──────────────┐ │
│  │ Authentication  │  │  Authorization  │  │   JWT/Session│ │
│  │   (UserDetails) │  │ (Role-Based)    │  │   Management │ │
│  └─────────────────┘  └─────────────────┘  └──────────────┘ │
└─────────────────────────────────────────────────────────────┘
                              │
┌─────────────────────────────────────────────────────────────┐
│                 User Role Hierarchy                         │
│  ROLE_PRESIDENT (Executive)                                 │
│  ├── ROLE_SECRETARY (Financial Management)                  │
│  ├── ROLE_OFFICER (Daily Operations)                        │
│  └── ROLE_MEMBER (Limited Access)                           │
└─────────────────────────────────────────────────────────────┘
```

## Data Flow Architecture

### Typical Request Flow

```
1. HTTP Request → Controller
2. Controller → Service (with DTOs)
3. Service → Repository (with validation)
4. Repository → Database (JPA/Hibernate)
5. Database → Repository (Entity)
6. Repository → Service (Entity → DTO)
7. Service → Controller (DTO)
8. Controller → HTTP Response (JSON/HTML)
```

### Transaction Management

```java
@Service
@Transactional
public class LoanService {
    
    @Transactional
    public Loan approveLoan(Long loanId, String approvedBy) {
        // Update loan status
        // Create ledger transaction
        // Generate approval notification
        // All operations in single transaction
    }
}
```

## Integration Points

### 1. Database Integration
- **MariaDB** with **Spring Data JPA**
- **Hibernate** as ORM provider
- **Connection pooling** with HikariCP

### 2. Frontend Integration
- **Thymeleaf** for server-side rendering
- **Bootstrap + jQuery** for UI components
- **REST APIs** for AJAX operations

### 3. External Services
- **File Storage** for member photos and documents
- **Email Service** for notifications
- **PDF Generation** for receipts and reports

## Performance Considerations

### 1. Caching Strategy
- **Redis** for session management
- **Application-level caching** for frequently accessed data
- **Database query optimization** with proper indexing

### 2. Connection Management
- **Database connection pooling**
- **Lazy loading** for entity relationships
- **Batch processing** for bulk operations

### 3. Scalability
- **Stateless REST APIs** for horizontal scaling
- **Load balancing** support
- **Microservices-ready** architecture

## Error Handling Strategy

### Global Exception Handling
```java
@ControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex) {
        return ResponseEntity.badRequest()
            .body(new ErrorResponse(ex.getErrorCode(), ex.getMessage()));
    }
    
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(403)
            .body(new ErrorResponse("ACCESS_DENIED", "Insufficient permissions"));
    }
}
```

## Monitoring & Logging

### 1. Application Logging
- **SLF4J** with **Logback** for structured logging
- **Log levels**: ERROR, WARN, INFO, DEBUG, TRACE
- **Audit logging** for financial transactions

### 2. Performance Monitoring
- **Spring Boot Actuator** endpoints
- **Custom metrics** for business KPIs
- **Database performance monitoring**

## Deployment Architecture

### Development Environment
- **Embedded Tomcat** (Spring Boot default)
- **In-memory database** for testing (H2)
- **Hot reload** with Spring DevTools

### Production Environment
- **Standalone JAR** deployment
- **External MariaDB** server
- **Reverse proxy** with Nginx/Apache
- **Load balancer** for high availability

---

**Related Documentation**:
- [Database Schema](database-schema.md) - Complete entity mappings
- [Security Implementation](../security/authentication-authorization.md) - Detailed security setup
- [API Documentation](../api/rest-endpoints.md) - Complete API reference
