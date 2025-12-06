# Design Document

## Overview

The RBAC Security System provides comprehensive role-based access control for the Ban Sai Yai Savings Group Financial Accounting System. The design implements a hierarchical RBAC model with four static roles (Officer, Secretary, President, Member), granular permission management, relationship-based access control for guarantors, segregation of duties enforcement, and comprehensive audit logging.

The system leverages the existing Java/Spring Boot backend with Spring Security for authentication and authorization, MariaDB for data persistence, and integrates with the React/TypeScript frontend for role-based UI rendering. The architecture follows security best practices including password hashing with bcrypt, brute-force protection, audit trails, and the principle of least privilege.

Key design principles:
- **Single Role Assignment**: Each user has exactly one primary role for clear accountability
- **Granular Permissions**: Fine-grained permissions mapped to roles through a flexible matrix
- **Separation of Duties**: Critical operations require multiple users (e.g., Officer creates, Secretary approves)
- **Relationship-Based Access**: Guarantors can view loans they guarantee beyond standard role permissions
- **Comprehensive Auditing**: All security-relevant actions are logged with rollback capability
- **Defense in Depth**: Multiple layers of security from database constraints to UI validation

## Architecture

### High-Level Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    React Frontend Layer                     │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │ Role-Based   │  │  Permission  │  │    Audit     │      │
│  │   Routes     │  │   Guards     │  │  Dashboard   │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
└─────────────────────────────────────────────────────────────┘
                          │
                          │ JWT with Role Claims
                          ▼
┌─────────────────────────────────────────────────────────────┐
│              Spring Security Filter Chain                   │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │     JWT      │  │    RBAC      │  │    Audit     │      │
│  │    Filter    │  │   Filter     │  │   Aspect     │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
└─────────────────────────────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────────┐
│                 Spring Boot Backend Layer                   │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │    RBAC      │  │  Permission  │  │    Audit     │      │
│  │  Service     │  │   Service    │  │   Service    │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
└─────────────────────────────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────────┐
│                      MariaDB Database                       │
│  roles | permissions | role_permissions | users | audit     │
└─────────────────────────────────────────────────────────────┘
```

### Component Hierarchy

```
Security Layer
├── Authentication
│   ├── JwtAuthenticationFilter
│   ├── UserDetailsServiceImpl
│   └── LoginAttemptService
├── Authorization
│   ├── RolePermissionService
│   ├── PermissionEvaluator
│   └── GuarantorAccessEvaluator
├── Audit
│   ├── AuditAspect
│   ├── AuditService
│   └── AuditRepository
└── User Management
    ├── UserService
    ├── RoleService
    └── PasswordService
```


## Components and Interfaces

### Backend Components

#### 1. RolePermissionService

**Purpose**: Manages role-permission mappings and permission checks

**Methods**:
```java
@Service
public class RolePermissionService {
    
    public Set<String> getPermissionsForRole(String roleName);
    
    public boolean hasPermission(User user, String permissionSlug);
    
    public void addPermissionToRole(String roleName, String permissionSlug);
    
    public void removePermissionFromRole(String roleName, String permissionSlug);
    
    public Map<String, Set<String>> getRolePermissionMatrix();
    
    public void initializeDefaultPermissions();
}
```

**Key Logic**:
- Caches role-permission mappings for performance
- Invalidates cache when permissions are modified
- Loads permissions on application startup

#### 2. PermissionEvaluator

**Purpose**: Custom Spring Security permission evaluator for method-level security

**Methods**:
```java
@Component
public class CustomPermissionEvaluator implements PermissionEvaluator {
    
    @Override
    public boolean hasPermission(Authentication auth, Object targetDomainObject, Object permission);
    
    @Override
    public boolean hasPermission(Authentication auth, Serializable targetId, String targetType, Object permission);
    
    public boolean canApproveOwnTransaction(User user, Transaction transaction);
}
```

**Usage in Controllers**:
```java
@PreAuthorize("hasPermission(null, 'loan.approve')")
@PostMapping("/loans/{id}/approve")
public ResponseEntity<?> approveLoan(@PathVariable Long id);
```

#### 3. GuarantorAccessEvaluator

**Purpose**: Implements relationship-based access control for guarantors

**Methods**:
```java
@Service
public class GuarantorAccessEvaluator {
    
    public boolean canViewLoan(User user, Long loanId);
    
    public List<Loan> getGuaranteedLoans(Long memberId);
    
    public boolean isActiveGuarantor(Long memberId, Long loanId);
}
```

**Key Logic**:
```java
public boolean canViewLoan(User user, Long loanId) {
    Loan loan = loanRepository.findById(loanId).orElse(null);
    if (loan == null) return false;
    
    // Standard access: Is the user the borrower?
    if (user.getMember().getId().equals(loan.getBorrower().getId())) {
        return true;
    }
    
    // Relationship access: Is the user an active guarantor?
    boolean isGuarantor = guarantorRepository.existsByLoanIdAndMemberId(
        loanId, user.getMember().getId()
    );
    
    return isGuarantor;
}
```


#### 4. AuditService

**Purpose**: Manages audit logging for security-relevant actions

**Methods**:
```java
@Service
public class AuditService {
    
    public void logAction(User user, String action, String entityType, Long entityId, 
                         Object oldValues, Object newValues);
    
    public void logAccessDenied(User user, String resource, String permission);
    
    public void logRoleChange(User targetUser, String oldRole, String newRole, User admin);
    
    public List<AuditLog> getCriticalActions(int limit);
    
    public List<AuditLog> getRoleViolations(LocalDateTime since);
    
    public Map<String, Integer> getActivityHeatmap(LocalDateTime since);
}
```

**Audit Log Structure**:
```java
@Entity
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long logId;
    
    @ManyToOne
    private User user;
    
    private String action;
    private String entityType;
    private Long entityId;
    private String ipAddress;
    
    @Column(columnDefinition = "JSON")
    private String oldValues;
    
    @Column(columnDefinition = "JSON")
    private String newValues;
    
    private LocalDateTime timestamp;
}
```

#### 5. AuditAspect

**Purpose**: AOP aspect for automatic audit logging

**Implementation**:
```java
@Aspect
@Component
public class AuditAspect {
    
    @Autowired
    private AuditService auditService;
    
    @Around("@annotation(Audited)")
    public Object auditMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        // Capture method arguments before execution
        Object[] args = joinPoint.getArgs();
        Object oldState = captureState(args);
        
        // Execute the method
        Object result = joinPoint.proceed();
        
        // Capture state after execution
        Object newState = captureState(result);
        
        // Log the action
        User currentUser = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        auditService.logAction(currentUser, joinPoint.getSignature().getName(), 
                              getEntityType(args), getEntityId(args), oldState, newState);
        
        return result;
    }
}
```

**Usage**:
```java
@Audited
@PostMapping("/loans/{id}/approve")
public ResponseEntity<?> approveLoan(@PathVariable Long id, @RequestBody ApprovalRequest request) {
    // Method automatically audited
}
```


#### 6. CashReconciliationService

**Purpose**: Manages end-of-day cash reconciliation with variance detection

**Methods**:
```java
@Service
public class CashReconciliationService {
    
    public BigDecimal calculateDatabaseBalance(LocalDate date);
    
    public CashReconciliation createReconciliation(User officer, BigDecimal physicalCount);
    
    public boolean hasVariance(CashReconciliation reconciliation);
    
    public void approveDiscrepancy(User secretary, Long reconciliationId, String notes);
    
    public void rejectDiscrepancy(User secretary, Long reconciliationId, String reason);
    
    public boolean canCloseDay(LocalDate date);
}
```

**Reconciliation Logic**:
```java
public CashReconciliation createReconciliation(User officer, BigDecimal physicalCount) {
    LocalDate today = LocalDate.now();
    BigDecimal databaseBalance = calculateDatabaseBalance(today);
    BigDecimal variance = physicalCount.subtract(databaseBalance);
    
    CashReconciliation reconciliation = new CashReconciliation();
    reconciliation.setDate(today);
    reconciliation.setOfficer(officer);
    reconciliation.setPhysicalCount(physicalCount);
    reconciliation.setDatabaseBalance(databaseBalance);
    reconciliation.setVariance(variance);
    reconciliation.setStatus(variance.compareTo(BigDecimal.ZERO) == 0 ? 
                            ReconciliationStatus.APPROVED : ReconciliationStatus.PENDING);
    
    return cashReconciliationRepository.save(reconciliation);
}
```

#### 7. UserService

**Purpose**: Manages user accounts and role assignments

**Methods**:
```java
@Service
public class UserService {
    
    public User createUser(String username, String password, Long memberId, String roleName);
    
    public void updateUserRole(Long userId, String newRoleName, User admin);
    
    public void suspendUser(Long userId, User admin);
    
    public void deleteUser(Long userId, User admin);
    
    public List<User> getAllUsers();
    
    public boolean validateRoleChange(User user, String newRole);
}
```

**Role Change Logic**:
```java
@Transactional
public void updateUserRole(Long userId, String newRoleName, User admin) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new EntityNotFoundException("User not found"));
    
    String oldRole = user.getRole().getRoleName();
    
    Role newRole = roleRepository.findByRoleName(newRoleName)
        .orElseThrow(() -> new IllegalArgumentException("Invalid role"));
    
    user.setRole(newRole);
    userRepository.save(user);
    
    // Log the role change
    auditService.logRoleChange(user, oldRole, newRoleName, admin);
    
    // Invalidate user's sessions
    sessionRegistry.expireUserSessions(user.getUsername());
}
```


#### 8. LoginAttemptService

**Purpose**: Prevents brute-force attacks through account lockout

**Methods**:
```java
@Service
public class LoginAttemptService {
    
    public void loginSucceeded(String username);
    
    public void loginFailed(String username);
    
    public boolean isBlocked(String username);
    
    public int getRemainingAttempts(String username);
    
    public LocalDateTime getLockoutExpiry(String username);
}
```

**Implementation**:
```java
private static final int MAX_ATTEMPTS = 5;
private static final int LOCKOUT_DURATION_MINUTES = 30;
private static final int ATTEMPT_WINDOW_MINUTES = 15;

public void loginFailed(String username) {
    LoginAttempt attempt = loginAttemptRepository.findByUsername(username)
        .orElse(new LoginAttempt(username));
    
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime windowStart = now.minusMinutes(ATTEMPT_WINDOW_MINUTES);
    
    // Count recent failed attempts
    int recentAttempts = attempt.getFailedAttempts().stream()
        .filter(timestamp -> timestamp.isAfter(windowStart))
        .count();
    
    attempt.addFailedAttempt(now);
    
    if (recentAttempts + 1 >= MAX_ATTEMPTS) {
        attempt.setLockedUntil(now.plusMinutes(LOCKOUT_DURATION_MINUTES));
    }
    
    loginAttemptRepository.save(attempt);
}

public boolean isBlocked(String username) {
    return loginAttemptRepository.findByUsername(username)
        .map(attempt -> attempt.getLockedUntil() != null && 
                       attempt.getLockedUntil().isAfter(LocalDateTime.now()))
        .orElse(false);
}
```

### Frontend Components

#### 1. RoleBasedRoute Component

**Purpose**: Protects routes based on user role

**Implementation**:
```typescript
interface RoleBasedRouteProps {
  allowedRoles: string[];
  children: ReactNode;
  fallback?: ReactNode;
}

const RoleBasedRoute: React.FC<RoleBasedRouteProps> = ({ 
  allowedRoles, 
  children, 
  fallback 
}) => {
  const user = useSelector((state: RootState) => state.auth.user);
  
  if (!user) {
    return <Navigate to="/login" />;
  }
  
  if (!allowedRoles.includes(user.role)) {
    return fallback || <Navigate to="/unauthorized" />;
  }
  
  return <>{children}</>;
};
```

**Usage**:
```typescript
<Route path="/admin/users" element={
  <RoleBasedRoute allowedRoles={['ROLE_PRESIDENT']}>
    <UserManagementPage />
  </RoleBasedRoute>
} />
```


#### 2. PermissionGuard Component

**Purpose**: Conditionally renders UI elements based on permissions

**Implementation**:
```typescript
interface PermissionGuardProps {
  permission: string;
  children: ReactNode;
  fallback?: ReactNode;
}

const PermissionGuard: React.FC<PermissionGuardProps> = ({ 
  permission, 
  children, 
  fallback 
}) => {
  const user = useSelector((state: RootState) => state.auth.user);
  const hasPermission = user?.permissions?.includes(permission) || false;
  
  return hasPermission ? <>{children}</> : <>{fallback}</>;
};
```

**Usage**:
```typescript
<PermissionGuard permission="loan.approve">
  <Button onClick={handleApproveLoan}>Approve Loan</Button>
</PermissionGuard>
```

#### 3. AuditDashboard Component

**Purpose**: Displays audit logs and security alerts for President

**Props**:
```typescript
interface AuditDashboardProps {
  // No props - fetches data based on current user
}
```

**Features**:
- Critical Actions Log widget
- Staff Activity Heatmap
- Role Violation Attempts table
- Security alerts for off-hours activity

#### 4. UserManagement Component

**Purpose**: Administrative interface for managing users and roles

**Features**:
- User list with role and status
- Create user form
- Edit user role
- Suspend/activate user
- Soft delete user

#### 5. CashReconciliation Component

**Purpose**: End-of-day cash reconciliation interface for Officers

**Features**:
- Physical cash count input
- Database balance display
- Variance calculation
- Close day button (disabled if variance exists)
- Escalation to Secretary if variance


## Data Models

### Database Schema

#### Roles Table
```sql
CREATE TABLE roles (
    role_id INT PRIMARY KEY AUTO_INCREMENT,
    role_name VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Seed data
INSERT INTO roles (role_name, description) VALUES
('ROLE_OFFICER', 'Operational user responsible for cash handling'),
('ROLE_SECRETARY', 'Auditor/Controller responsible for accounting accuracy'),
('ROLE_PRESIDENT', 'Approver/Executive with super admin visibility'),
('ROLE_MEMBER', 'End user with restricted access to personal data');
```

#### Permissions Table
```sql
CREATE TABLE permissions (
    perm_id INT PRIMARY KEY AUTO_INCREMENT,
    perm_slug VARCHAR(50) NOT NULL UNIQUE,
    module VARCHAR(50) NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_module (module)
);

-- Seed data (sample permissions)
INSERT INTO permissions (perm_slug, module, description) VALUES
('member.view', 'Member Management', 'View member profiles'),
('member.create', 'Member Management', 'Register new members'),
('member.edit', 'Member Management', 'Edit member information'),
('transaction.create', 'Transactions', 'Create deposits and withdrawals'),
('transaction.view', 'Transactions', 'View transaction history'),
('transaction.void', 'Transactions', 'Void transactions'),
('loan.view', 'Loans', 'View loan details'),
('loan.approve', 'Loans', 'Approve loan applications'),
('accounting.view', 'Accounting', 'View general ledger'),
('accounting.edit', 'Accounting', 'Edit chart of accounts'),
('accounting.post', 'Accounting', 'Post journal entries'),
('accounting.close', 'Accounting', 'Close accounting periods'),
('report.operational', 'Reporting', 'View operational reports'),
('report.financial', 'Reporting', 'View financial statements'),
('audit.view', 'System', 'View audit logs'),
('system.manage_users', 'System', 'Manage user accounts'),
('system.set_dividend', 'System', 'Set dividend rate');
```

#### Role_Permissions Table
```sql
CREATE TABLE role_permissions (
    role_id INT NOT NULL,
    perm_id INT NOT NULL,
    PRIMARY KEY (role_id, perm_id),
    FOREIGN KEY (role_id) REFERENCES roles(role_id) ON DELETE CASCADE,
    FOREIGN KEY (perm_id) REFERENCES permissions(perm_id) ON DELETE CASCADE
);

-- Seed data (Officer permissions)
INSERT INTO role_permissions (role_id, perm_id)
SELECT r.role_id, p.perm_id
FROM roles r, permissions p
WHERE r.role_name = 'ROLE_OFFICER'
AND p.perm_slug IN ('member.view', 'member.create', 'transaction.create', 
                    'transaction.view', 'transaction.void', 'loan.view', 
                    'report.operational');

-- Seed data (Secretary permissions)
INSERT INTO role_permissions (role_id, perm_id)
SELECT r.role_id, p.perm_id
FROM roles r, permissions p
WHERE r.role_name = 'ROLE_SECRETARY'
AND p.perm_slug IN ('member.view', 'transaction.view', 'loan.view', 
                    'accounting.view', 'accounting.edit', 'accounting.post', 
                    'accounting.close', 'report.operational', 'report.financial');

-- Seed data (President permissions)
INSERT INTO role_permissions (role_id, perm_id)
SELECT r.role_id, p.perm_id
FROM roles r, permissions p
WHERE r.role_name = 'ROLE_PRESIDENT'
AND p.perm_slug IN ('member.view', 'transaction.view', 'loan.view', 
                    'loan.approve', 'accounting.view', 'report.operational', 
                    'report.financial', 'audit.view', 'system.manage_users', 
                    'system.set_dividend');

-- Seed data (Member permissions)
INSERT INTO role_permissions (role_id, perm_id)
SELECT r.role_id, p.perm_id
FROM roles r, permissions p
WHERE r.role_name = 'ROLE_MEMBER'
AND p.perm_slug IN ('member.view');
```


#### Users Table (Extended)
```sql
-- Extend existing users table
ALTER TABLE users
ADD COLUMN role_id INT,
ADD COLUMN status ENUM('Active', 'Suspended') DEFAULT 'Active',
ADD COLUMN deleted_at TIMESTAMP NULL,
ADD FOREIGN KEY (role_id) REFERENCES roles(role_id);

-- Add index for performance
CREATE INDEX idx_users_role ON users(role_id);
CREATE INDEX idx_users_status ON users(status);
```

#### System_Audit_Log Table
```sql
CREATE TABLE system_audit_log (
    log_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id INT,
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(50),
    entity_id BIGINT,
    ip_address VARCHAR(45),
    old_values JSON,
    new_values JSON,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    INDEX idx_audit_user (user_id),
    INDEX idx_audit_timestamp (timestamp),
    INDEX idx_audit_action (action),
    INDEX idx_audit_entity (entity_type, entity_id)
);
```

#### Cash_Reconciliations Table
```sql
CREATE TABLE cash_reconciliations (
    reconciliation_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    date DATE NOT NULL,
    officer_id INT NOT NULL,
    physical_count DECIMAL(15, 2) NOT NULL,
    database_balance DECIMAL(15, 2) NOT NULL,
    variance DECIMAL(15, 2) NOT NULL,
    status ENUM('PENDING', 'APPROVED', 'REJECTED') DEFAULT 'PENDING',
    secretary_id INT,
    secretary_notes TEXT,
    approved_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (officer_id) REFERENCES users(user_id),
    FOREIGN KEY (secretary_id) REFERENCES users(user_id),
    INDEX idx_reconciliation_date (date),
    INDEX idx_reconciliation_status (status)
);
```

#### Login_Attempts Table (Extended)
```sql
-- Extend existing login_attempts table
ALTER TABLE login_attempts
ADD COLUMN locked_until TIMESTAMP NULL,
ADD INDEX idx_locked_until (locked_until);
```

### Java Entity Models

#### Role Entity
```java
@Entity
@Table(name = "roles")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer roleId;
    
    @Column(unique = true, nullable = false)
    private String roleName;
    
    private String description;
    
    @ManyToMany
    @JoinTable(
        name = "role_permissions",
        joinColumns = @JoinColumn(name = "role_id"),
        inverseJoinColumns = @JoinColumn(name = "perm_id")
    )
    private Set<Permission> permissions = new HashSet<>();
    
    @CreationTimestamp
    private LocalDateTime createdAt;
}
```


#### Permission Entity
```java
@Entity
@Table(name = "permissions")
public class Permission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer permId;
    
    @Column(unique = true, nullable = false)
    private String permSlug;
    
    @Column(nullable = false)
    private String module;
    
    private String description;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
}
```

#### AuditLog Entity
```java
@Entity
@Table(name = "system_audit_log")
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long logId;
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    
    @Column(nullable = false)
    private String action;
    
    private String entityType;
    private Long entityId;
    private String ipAddress;
    
    @Column(columnDefinition = "JSON")
    private String oldValues;
    
    @Column(columnDefinition = "JSON")
    private String newValues;
    
    @CreationTimestamp
    private LocalDateTime timestamp;
}
```

#### CashReconciliation Entity
```java
@Entity
@Table(name = "cash_reconciliations")
public class CashReconciliation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reconciliationId;
    
    @Column(nullable = false)
    private LocalDate date;
    
    @ManyToOne
    @JoinColumn(name = "officer_id", nullable = false)
    private User officer;
    
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal physicalCount;
    
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal databaseBalance;
    
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal variance;
    
    @Enumerated(EnumType.STRING)
    private ReconciliationStatus status;
    
    @ManyToOne
    @JoinColumn(name = "secretary_id")
    private User secretary;
    
    @Column(columnDefinition = "TEXT")
    private String secretaryNotes;
    
    private LocalDateTime approvedAt;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
}

enum ReconciliationStatus {
    PENDING, APPROVED, REJECTED
}
```

### TypeScript Interfaces

```typescript
// Role and Permission
interface Role {
  roleId: number;
  roleName: string;
  description: string;
  permissions: Permission[];
}

interface Permission {
  permId: number;
  permSlug: string;
  module: string;
  description: string;
}

// User with Role
interface User {
  userId: number;
  username: string;
  memberId: number;
  role: Role;
  status: 'Active' | 'Suspended';
  permissions: string[]; // Array of permission slugs for quick checks
}

// Audit Log
interface AuditLog {
  logId: number;
  user: {
    userId: number;
    username: string;
  };
  action: string;
  entityType: string;
  entityId: number;
  ipAddress: string;
  oldValues: any;
  newValues: any;
  timestamp: string;
}

// Cash Reconciliation
interface CashReconciliation {
  reconciliationId: number;
  date: string;
  officer: {
    userId: number;
    username: string;
  };
  physicalCount: number;
  databaseBalance: number;
  variance: number;
  status: 'PENDING' | 'APPROVED' | 'REJECTED';
  secretary?: {
    userId: number;
    username: string;
  };
  secretaryNotes?: string;
  approvedAt?: string;
}

// User Management
interface CreateUserRequest {
  username: string;
  password: string;
  memberId: number;
  roleName: string;
}

interface UpdateRoleRequest {
  userId: number;
  newRoleName: string;
}
```


## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system-essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

### Property 1: Single role assignment enforcement

*For any* user creation or role update operation, the system should accept the operation if and only if exactly one valid role is assigned, rejecting operations with zero roles or multiple roles.

**Validates: Requirements 1.1**

### Property 2: Permission loading on login

*For any* user login, the loaded permission set should exactly match the set of permissions associated with the user's assigned role in the role-permission matrix.

**Validates: Requirements 1.2**

### Property 3: Complete permission replacement on role change

*For any* role change operation, the user's permission set after the change should exactly match the new role's permissions with no permissions from the previous role remaining.

**Validates: Requirements 1.3**

### Property 4: Authorization based on current role only

*For any* access control decision, the system should grant or deny access based solely on the user's current role, with no influence from historical role assignments.

**Validates: Requirements 1.4**

### Property 5: Permission-based action authorization

*For any* user action attempt, the system should allow the action if and only if the user's role has the required permission slug for that action.

**Validates: Requirements 2.2**

### Property 6: Immediate permission propagation on addition

*For any* permission added to a role, all users with that role should immediately gain the capability to perform actions requiring that permission.

**Validates: Requirements 2.3**

### Property 7: Immediate permission revocation on removal

*For any* permission removed from a role, all users with that role should immediately lose the capability to perform actions requiring that permission.

**Validates: Requirements 2.4**

### Property 8: Permission slug format consistency

*For any* permission check in the system, the permission identifier should follow the module.action slug format (e.g., "loan.approve", "cash.entry").

**Validates: Requirements 2.5**


### Property 9: Transaction validation and persistence

*For any* valid deposit or withdrawal created by an Officer, the system should validate the transaction data and successfully persist it to the database.

**Validates: Requirements 3.2**

### Property 10: Time-based void authorization for recent transactions

*For any* transaction with a timestamp less than 24 hours from the current time, an Officer should be allowed to void the transaction.

**Validates: Requirements 3.3**

### Property 11: Time-based void denial for old transactions

*For any* transaction with a timestamp 24 hours or more from the current time, an Officer should be denied the ability to void the transaction.

**Validates: Requirements 3.4**

### Property 12: Loan approval audit trail

*For any* loan approval or rejection by a President, the system should update the loan status and create an audit log entry with old and new values.

**Validates: Requirements 5.2**

### Property 13: Member data isolation

*For any* member accessing their profile, the system should display only that member's personal financial data and deny access to other members' data.

**Validates: Requirements 6.1, 6.2**

### Property 14: Member transaction operation denial

*For any* transaction operation (create, update, delete) attempted by a Member, the system should deny the operation.

**Validates: Requirements 6.4**

### Property 15: Member administrative access denial

*For any* administrative function access attempted by a Member, the system should deny access.

**Validates: Requirements 6.5**

### Property 16: Guarantor relationship-based access

*For any* member who is an active guarantor for a loan, the system should grant read access to that specific loan's details.

**Validates: Requirements 7.1**

### Property 17: Guaranteed loans widget accuracy

*For any* guarantor viewing their dashboard, the "Guaranteed Loans" widget should display exactly the set of loans for which they are currently an active guarantor.

**Validates: Requirements 7.2**

### Property 18: Loan status visual indicator

*For any* guaranteed loan viewed by a guarantor, the system should display a green indicator if the loan is current and a red indicator if the loan is delinquent.

**Validates: Requirements 7.3**

### Property 19: Guarantor access revocation on loan completion

*For any* loan that transitions to fully repaid or written-off status, the system should remove guarantor read access to that loan.

**Validates: Requirements 7.4**

### Property 20: Non-guarantor access denial

*For any* member-loan pair where no active guarantor relationship exists and the member is not the borrower, the system should deny the member access to that loan's details.

**Validates: Requirements 7.5**


### Property 21: Cash reconciliation variance calculation

*For any* physical cash count entered by an Officer, the system should calculate the variance as the difference between the physical count and the database balance.

**Validates: Requirements 8.2**

### Property 22: Day close authorization with zero variance

*For any* cash reconciliation with zero variance, the system should allow the Officer to close the day.

**Validates: Requirements 8.3**

### Property 23: Day close denial with non-zero variance

*For any* cash reconciliation with non-zero variance, the system should prevent the Officer from closing the day until Secretary approval.

**Validates: Requirements 8.4**

### Property 24: Variance escalation to Secretary

*For any* cash reconciliation with non-zero variance, the system should create a notification for the Secretary to review and approve the discrepancy.

**Validates: Requirements 8.5, 9.1**

### Property 25: Discrepancy approval accounting entry

*For any* cash discrepancy approved by a Secretary, the system should create an accounting entry booking the variance as an expense or accounts payable.

**Validates: Requirements 9.3**

### Property 26: Day close after discrepancy approval

*For any* cash discrepancy approved by a Secretary, the system should allow the day to be closed.

**Validates: Requirements 9.4**

### Property 27: Recount requirement on discrepancy rejection

*For any* cash discrepancy rejected by a Secretary, the system should require the Officer to perform a recount or correct entries before attempting to close the day.

**Validates: Requirements 9.5**

### Property 28: Off-hours activity security alert

*For any* data entry activity with a timestamp outside normal meeting hours (e.g., before 6 AM or after 10 PM), the system should flag the activity as a security alert.

**Validates: Requirements 10.3**

### Property 29: Unauthorized access attempt logging

*For any* access attempt to a resource without proper authorization, the system should log the attempt as a role violation with user, timestamp, and attempted resource.

**Validates: Requirements 10.4**

### Property 30: Comprehensive CUD operation audit logging

*For any* CREATE, UPDATE, or DELETE operation, the system should create an audit log entry containing user ID, timestamp, IP address, entity type, entity ID, and affected data.

**Validates: Requirements 11.1**

### Property 31: Loan approval audit with state capture

*For any* loan approval or rejection, the system should create an audit log entry containing both old values (previous loan state) and new values (updated loan state).

**Validates: Requirements 11.2**

### Property 32: Role change audit with administrator tracking

*For any* user role change, the system should create an audit log entry containing the target user, old role, new role, and the administrator who performed the change.

**Validates: Requirements 11.3**

### Property 33: Access denial audit logging

*For any* unauthorized action attempt, the system should create an audit log entry containing the user, attempted action, and the denied permission.

**Validates: Requirements 11.4**

### Property 34: Audit log JSON format for state changes

*For any* audit log entry involving state changes, the old values and new values should be stored in valid JSON format to enable rollback capability.

**Validates: Requirements 11.5**


### Property 35: Role assignment persistence

*For any* role assigned to a user, the system should store the role_id foreign key in the users table and the relationship should be retrievable via database query.

**Validates: Requirements 12.2**

### Property 36: Permission query correctness

*For any* user, querying their permissions should return exactly the set of permissions obtained by joining users, roles, role_permissions, and permissions tables.

**Validates: Requirements 12.3**

### Property 37: Permission modification isolation

*For any* permission addition or removal operation, the system should modify only the role_permissions junction table without affecting roles, permissions, or users tables.

**Validates: Requirements 12.4**

### Property 38: Audit log foreign key persistence

*For any* audit log entry created, the system should store a valid foreign key reference to the users table.

**Validates: Requirements 12.5**

### Property 39: User creation required fields validation

*For any* user creation request, the system should accept the request if and only if it contains username, password, member linkage, and role assignment.

**Validates: Requirements 13.2**

### Property 40: Role update validation and persistence

*For any* role update request, the system should validate that the new role exists and is valid, then update the user record if validation passes.

**Validates: Requirements 13.3**

### Property 41: User suspension status and login denial

*For any* user suspended by a President, the system should set the user status to "Suspended" and deny all subsequent login attempts until the user is reactivated.

**Validates: Requirements 13.4**

### Property 42: Soft deletion with audit preservation

*For any* user deleted by a President, the system should mark the user record as deleted (soft delete) while preserving all associated audit log entries.

**Validates: Requirements 13.5**

### Property 43: Password complexity validation

*For any* password creation attempt, the system should accept the password if and only if it contains at least 8 characters with at least one uppercase letter, one lowercase letter, and one number.

**Validates: Requirements 14.1**

### Property 44: Password bcrypt hashing

*For any* password stored in the database, the password should be hashed using bcrypt with a cost factor of 12.

**Validates: Requirements 14.2**

### Property 45: Brute-force protection lockout

*For any* user with 5 failed login attempts within a 15-minute window, the system should lock the account for 30 minutes.

**Validates: Requirements 14.3**

### Property 46: Lockout message display

*For any* login attempt on a locked account, the system should return an error message indicating the account is locked and the remaining lockout duration.

**Validates: Requirements 14.4**

### Property 47: Failed login counter reset

*For any* successful login following failed attempts, the system should reset the failed login counter to zero.

**Validates: Requirements 14.5**

### Property 48: Transaction creator tracking

*For any* transaction created by an Officer, the system should record the Officer's user ID as the creator in the transaction record.

**Validates: Requirements 15.1**

### Property 49: Approval queue visibility

*For any* transaction requiring approval, the system should display the transaction in the approval queue for authorized approvers.

**Validates: Requirements 15.2**

### Property 50: Self-approval denial

*For any* approval attempt where the approver's user ID matches the creator's user ID, the system should deny the approval operation.

**Validates: Requirements 15.3**

### Property 51: Different-user approval authorization

*For any* approval attempt where the approver's user ID differs from the creator's user ID and the approver has the required permission, the system should allow the approval operation.

**Validates: Requirements 15.4**

### Property 52: Separate creator and approver tracking

*For any* approved transaction, the system should store both the creator's user ID and the approver's user ID in separate fields.

**Validates: Requirements 15.5**


## Error Handling

### Backend Error Handling

#### 1. Authorization Exceptions

```java
@ControllerAdvice
public class SecurityExceptionHandler {
    
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(
            AccessDeniedException ex, HttpServletRequest request) {
        
        // Log the access denial
        User user = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        auditService.logAccessDenied(user, request.getRequestURI(), ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(new ErrorResponse("ACCESS_DENIED", 
                "You do not have permission to access this resource"));
    }
    
    @ExceptionHandler(InsufficientAuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientAuth(
            InsufficientAuthenticationException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(new ErrorResponse("AUTHENTICATION_REQUIRED", 
                "Please log in to access this resource"));
    }
}
```

#### 2. Role and Permission Exceptions

```java
public class RoleNotFoundException extends RuntimeException {
    public RoleNotFoundException(String roleName) {
        super("Role not found: " + roleName);
    }
}

public class PermissionNotFoundException extends RuntimeException {
    public PermissionNotFoundException(String permSlug) {
        super("Permission not found: " + permSlug);
    }
}

public class InvalidRoleAssignmentException extends RuntimeException {
    public InvalidRoleAssignmentException(String message) {
        super(message);
    }
}
```

#### 3. Audit Logging Failures

```java
@Service
public class AuditService {
    
    public void logAction(User user, String action, Object oldValues, Object newValues) {
        try {
            AuditLog log = new AuditLog();
            log.setUser(user);
            log.setAction(action);
            log.setOldValues(objectMapper.writeValueAsString(oldValues));
            log.setNewValues(objectMapper.writeValueAsString(newValues));
            log.setIpAddress(getCurrentIpAddress());
            
            auditLogRepository.save(log);
        } catch (Exception e) {
            // Critical: Audit logging must not fail silently
            logger.error("CRITICAL: Failed to create audit log", e);
            // Send alert to administrators
            alertService.sendCriticalAlert("Audit logging failure", e.getMessage());
            // Optionally: Fail the operation if audit logging is mandatory
            throw new AuditLoggingException("Failed to create audit log", e);
        }
    }
}
```

#### 4. Cash Reconciliation Errors

```java
public class VarianceExistsException extends RuntimeException {
    private final BigDecimal variance;
    
    public VarianceExistsException(BigDecimal variance) {
        super("Cannot close day: variance of " + variance + " exists");
        this.variance = variance;
    }
}

public class ReconciliationNotFoundException extends RuntimeException {
    public ReconciliationNotFoundException(Long id) {
        super("Cash reconciliation not found: " + id);
    }
}
```


#### 5. Login Attempt Lockout

```java
@Service
public class LoginAttemptService {
    
    public void validateLoginAllowed(String username) throws AccountLockedException {
        if (isBlocked(username)) {
            LocalDateTime lockoutExpiry = getLockoutExpiry(username);
            long minutesRemaining = ChronoUnit.MINUTES.between(
                LocalDateTime.now(), lockoutExpiry);
            
            throw new AccountLockedException(
                "Account is locked due to too many failed login attempts. " +
                "Please try again in " + minutesRemaining + " minutes.");
        }
    }
}

public class AccountLockedException extends AuthenticationException {
    public AccountLockedException(String message) {
        super(message);
    }
}
```

### Frontend Error Handling

#### 1. Authorization Error Interceptor

```typescript
// In apiSlice.ts
const apiSlice = createApi({
  baseQuery: fetchBaseQuery({
    baseUrl: '/api',
    prepareHeaders: (headers, { getState }) => {
      const token = (getState() as RootState).auth.token;
      if (token) {
        headers.set('authorization', `Bearer ${token}`);
      }
      return headers;
    },
  }),
  endpoints: (builder) => ({}),
});

// Add response interceptor
apiSlice.middleware = (api) => (next) => (action) => {
  if (action.type.endsWith('/rejected')) {
    const error = action.payload;
    
    if (error?.status === 403) {
      // Access denied - show error message
      toast.error('You do not have permission to perform this action');
    } else if (error?.status === 401) {
      // Unauthorized - redirect to login
      api.dispatch(logout());
      window.location.href = '/login';
    }
  }
  
  return next(action);
};
```

#### 2. Role-Based Error Messages

```typescript
const RoleBasedRoute: React.FC<RoleBasedRouteProps> = ({ 
  allowedRoles, 
  children 
}) => {
  const user = useSelector((state: RootState) => state.auth.user);
  
  if (!user) {
    return <Navigate to="/login" state={{ from: location }} />;
  }
  
  if (!allowedRoles.includes(user.role)) {
    return (
      <Box p={4}>
        <Alert severity="error">
          <AlertTitle>Access Denied</AlertTitle>
          Your role ({user.role}) does not have permission to access this page.
          Please contact an administrator if you believe this is an error.
        </Alert>
      </Box>
    );
  }
  
  return <>{children}</>;
};
```

#### 3. Audit Dashboard Error Handling

```typescript
const AuditDashboard: React.FC = () => {
  const { data, error, isLoading } = useGetAuditLogsQuery();
  
  if (error) {
    return (
      <Alert severity="error">
        <AlertTitle>Failed to Load Audit Logs</AlertTitle>
        {error.status === 403 
          ? 'You do not have permission to view audit logs.'
          : 'An error occurred while loading audit logs. Please try again.'}
      </Alert>
    );
  }
  
  // ... render audit dashboard
};
```


## Testing Strategy

### Dual Testing Approach

The RBAC security system will employ both unit testing and property-based testing to ensure comprehensive correctness:

- **Unit tests** verify specific examples, edge cases, and integration points between security components
- **Property tests** verify universal security properties that should hold across all inputs and scenarios
- Together they provide comprehensive coverage: unit tests catch concrete security bugs, property tests verify general correctness of authorization logic

### Unit Testing

#### Backend Unit Tests (JUnit + Mockito)

**Test Coverage**:
- Role and permission CRUD operations
- Permission evaluation for specific role-action pairs
- Guarantor access evaluation logic
- Audit logging with various action types
- Cash reconciliation calculations
- Login attempt tracking and lockout
- Password validation and hashing
- User management operations

**Example Unit Tests**:
```java
@ExtendWith(MockitoExtension.class)
class RolePermissionServiceTest {
    
    @Mock
    private RoleRepository roleRepository;
    
    @Mock
    private PermissionRepository permissionRepository;
    
    @InjectMocks
    private RolePermissionService rolePermissionService;
    
    @Test
    void shouldReturnPermissionsForOfficerRole() {
        Role officerRole = new Role("ROLE_OFFICER");
        officerRole.setPermissions(Set.of(
            new Permission("transaction.create"),
            new Permission("transaction.view"),
            new Permission("member.view")
        ));
        
        when(roleRepository.findByRoleName("ROLE_OFFICER"))
            .thenReturn(Optional.of(officerRole));
        
        Set<String> permissions = rolePermissionService.getPermissionsForRole("ROLE_OFFICER");
        
        assertEquals(3, permissions.size());
        assertTrue(permissions.contains("transaction.create"));
        assertTrue(permissions.contains("transaction.view"));
        assertTrue(permissions.contains("member.view"));
    }
    
    @Test
    void shouldDenyAccessWhenPermissionMissing() {
        User user = new User();
        user.setRole(new Role("ROLE_MEMBER"));
        user.getRole().setPermissions(Set.of(new Permission("member.view")));
        
        boolean hasPermission = rolePermissionService.hasPermission(user, "loan.approve");
        
        assertFalse(hasPermission);
    }
}

@ExtendWith(MockitoExtension.class)
class GuarantorAccessEvaluatorTest {
    
    @Mock
    private GuarantorRepository guarantorRepository;
    
    @Mock
    private LoanRepository loanRepository;
    
    @InjectMocks
    private GuarantorAccessEvaluator guarantorAccessEvaluator;
    
    @Test
    void shouldAllowAccessWhenUserIsGuarantor() {
        User user = new User();
        user.setMember(new Member(1L));
        
        when(guarantorRepository.existsByLoanIdAndMemberId(100L, 1L))
            .thenReturn(true);
        
        boolean canView = guarantorAccessEvaluator.canViewLoan(user, 100L);
        
        assertTrue(canView);
    }
    
    @Test
    void shouldDenyAccessWhenUserIsNotGuarantor() {
        User user = new User();
        user.setMember(new Member(1L));
        
        Loan loan = new Loan();
        loan.setBorrower(new Member(2L)); // Different member
        
        when(loanRepository.findById(100L)).thenReturn(Optional.of(loan));
        when(guarantorRepository.existsByLoanIdAndMemberId(100L, 1L))
            .thenReturn(false);
        
        boolean canView = guarantorAccessEvaluator.canViewLoan(user, 100L);
        
        assertFalse(canView);
    }
}

@ExtendWith(MockitoExtension.class)
class LoginAttemptServiceTest {
    
    @Mock
    private LoginAttemptRepository loginAttemptRepository;
    
    @InjectMocks
    private LoginAttemptService loginAttemptService;
    
    @Test
    void shouldLockAccountAfterFiveFailedAttempts() {
        String username = "testuser";
        LoginAttempt attempt = new LoginAttempt(username);
        
        when(loginAttemptRepository.findByUsername(username))
            .thenReturn(Optional.of(attempt));
        
        // Simulate 5 failed attempts
        for (int i = 0; i < 5; i++) {
            loginAttemptService.loginFailed(username);
        }
        
        assertTrue(loginAttemptService.isBlocked(username));
    }
    
    @Test
    void shouldResetCounterOnSuccessfulLogin() {
        String username = "testuser";
        LoginAttempt attempt = new LoginAttempt(username);
        attempt.addFailedAttempt(LocalDateTime.now());
        
        when(loginAttemptRepository.findByUsername(username))
            .thenReturn(Optional.of(attempt));
        
        loginAttemptService.loginSucceeded(username);
        
        assertEquals(0, loginAttemptService.getRemainingAttempts(username));
    }
}
```


#### Frontend Unit Tests (Jest + React Testing Library)

**Test Coverage**:
- Role-based route protection
- Permission guard component rendering
- User management UI operations
- Audit dashboard display
- Cash reconciliation UI
- Error message display for authorization failures

**Example Unit Tests**:
```typescript
describe('RoleBasedRoute', () => {
  it('should render children when user has allowed role', () => {
    const { getByText } = render(
      <RoleBasedRoute allowedRoles={['ROLE_PRESIDENT']}>
        <div>Protected Content</div>
      </RoleBasedRoute>,
      {
        preloadedState: {
          auth: { user: { role: 'ROLE_PRESIDENT' } }
        }
      }
    );
    
    expect(getByText('Protected Content')).toBeInTheDocument();
  });
  
  it('should redirect when user does not have allowed role', () => {
    const { queryByText } = render(
      <RoleBasedRoute allowedRoles={['ROLE_PRESIDENT']}>
        <div>Protected Content</div>
      </RoleBasedRoute>,
      {
        preloadedState: {
          auth: { user: { role: 'ROLE_MEMBER' } }
        }
      }
    );
    
    expect(queryByText('Protected Content')).not.toBeInTheDocument();
  });
});

describe('PermissionGuard', () => {
  it('should render children when user has permission', () => {
    const { getByText } = render(
      <PermissionGuard permission="loan.approve">
        <button>Approve Loan</button>
      </PermissionGuard>,
      {
        preloadedState: {
          auth: { 
            user: { 
              role: 'ROLE_PRESIDENT',
              permissions: ['loan.approve', 'loan.view']
            }
          }
        }
      }
    );
    
    expect(getByText('Approve Loan')).toBeInTheDocument();
  });
  
  it('should not render children when user lacks permission', () => {
    const { queryByText } = render(
      <PermissionGuard permission="loan.approve">
        <button>Approve Loan</button>
      </PermissionGuard>,
      {
        preloadedState: {
          auth: { 
            user: { 
              role: 'ROLE_MEMBER',
              permissions: ['member.view']
            }
          }
        }
      }
    );
    
    expect(queryByText('Approve Loan')).not.toBeInTheDocument();
  });
});
```

### Property-Based Testing

#### Property Testing Library

**Backend**: Use **jqwik** for Java property-based testing (already in project dependencies)
**Frontend**: Use **fast-check** for TypeScript property-based testing

#### Configuration

Each property-based test should run a minimum of 100 iterations to ensure thorough coverage of the input space.

#### Property Test Implementation

Each property-based test MUST be tagged with a comment explicitly referencing the correctness property in the design document using this exact format:

```
**Feature: rbac-security-system, Property {number}: {property_text}**
```


**Backend Property Tests (jqwik)**:

```java
class RBACPropertyTest {
    
    @Provide
    Arbitrary<String> roles() {
        return Arbitraries.of("ROLE_OFFICER", "ROLE_SECRETARY", 
                             "ROLE_PRESIDENT", "ROLE_MEMBER");
    }
    
    @Provide
    Arbitrary<String> permissions() {
        return Arbitraries.of("transaction.create", "transaction.view", "transaction.void",
                             "loan.view", "loan.approve", "accounting.view", 
                             "accounting.edit", "member.view", "audit.view");
    }
    
    /**
     * Feature: rbac-security-system, Property 1: Single role assignment enforcement
     */
    @Property
    void userCreationShouldRequireExactlyOneRole(
            @ForAll String username,
            @ForAll String password,
            @ForAll Long memberId) {
        
        // Test with no role - should fail
        assertThrows(InvalidRoleAssignmentException.class, () -> {
            userService.createUser(username, password, memberId, null);
        });
        
        // Test with valid role - should succeed
        User user = userService.createUser(username + "_valid", password, memberId, "ROLE_OFFICER");
        assertNotNull(user.getRole());
        assertEquals("ROLE_OFFICER", user.getRole().getRoleName());
    }
    
    /**
     * Feature: rbac-security-system, Property 2: Permission loading on login
     */
    @Property
    void loginShouldLoadExactPermissionsForRole(@ForAll("roles") String roleName) {
        User user = createTestUser(roleName);
        Set<String> expectedPermissions = rolePermissionService.getPermissionsForRole(roleName);
        
        // Simulate login
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        Set<String> loadedPermissions = userDetails.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.toSet());
        
        assertEquals(expectedPermissions, loadedPermissions);
    }
    
    /**
     * Feature: rbac-security-system, Property 3: Complete permission replacement on role change
     */
    @Property
    void roleChangeShouldCompletelyReplacePermissions(
            @ForAll("roles") String oldRole,
            @ForAll("roles") String newRole) {
        
        Assume.that(!oldRole.equals(newRole));
        
        User user = createTestUser(oldRole);
        Set<String> oldPermissions = rolePermissionService.getPermissionsForRole(oldRole);
        
        userService.updateUserRole(user.getId(), newRole, adminUser);
        
        User updatedUser = userRepository.findById(user.getId()).get();
        Set<String> newPermissions = rolePermissionService.getPermissionsForRole(
            updatedUser.getRole().getRoleName());
        Set<String> expectedNewPermissions = rolePermissionService.getPermissionsForRole(newRole);
        
        // New permissions should match new role exactly
        assertEquals(expectedNewPermissions, newPermissions);
        
        // No permissions from old role should remain (unless they're also in new role)
        Set<String> oldOnlyPermissions = new HashSet<>(oldPermissions);
        oldOnlyPermissions.removeAll(expectedNewPermissions);
        
        for (String oldPerm : oldOnlyPermissions) {
            assertFalse(newPermissions.contains(oldPerm));
        }
    }
    
    /**
     * Feature: rbac-security-system, Property 5: Permission-based action authorization
     */
    @Property
    void actionShouldBeAuthorizedOnlyWhenPermissionExists(
            @ForAll("roles") String roleName,
            @ForAll("permissions") String permission) {
        
        User user = createTestUser(roleName);
        Set<String> rolePermissions = rolePermissionService.getPermissionsForRole(roleName);
        
        boolean hasPermission = rolePermissionService.hasPermission(user, permission);
        boolean shouldHavePermission = rolePermissions.contains(permission);
        
        assertEquals(shouldHavePermission, hasPermission);
    }
    
    /**
     * Feature: rbac-security-system, Property 10: Time-based void authorization for recent transactions
     */
    @Property
    void recentTransactionsShouldBeVoidable(
            @ForAll @IntRange(min = 0, max = 23) int hoursAgo) {
        
        Transaction transaction = createTestTransaction();
        transaction.setCreatedAt(LocalDateTime.now().minusHours(hoursAgo));
        
        User officer = createTestUser("ROLE_OFFICER");
        
        boolean canVoid = transactionService.canVoid(officer, transaction);
        
        assertTrue(canVoid, "Transaction " + hoursAgo + " hours old should be voidable");
    }
    
    /**
     * Feature: rbac-security-system, Property 11: Time-based void denial for old transactions
     */
    @Property
    void oldTransactionsShouldNotBeVoidable(
            @ForAll @IntRange(min = 24, max = 1000) int hoursAgo) {
        
        Transaction transaction = createTestTransaction();
        transaction.setCreatedAt(LocalDateTime.now().minusHours(hoursAgo));
        
        User officer = createTestUser("ROLE_OFFICER");
        
        boolean canVoid = transactionService.canVoid(officer, transaction);
        
        assertFalse(canVoid, "Transaction " + hoursAgo + " hours old should not be voidable");
    }
    
    /**
     * Feature: rbac-security-system, Property 16: Guarantor relationship-based access
     */
    @Property
    void guarantorShouldHaveAccessToGuaranteedLoan(
            @ForAll @LongRange(min = 1, max = 1000) Long memberId,
            @ForAll @LongRange(min = 1, max = 1000) Long loanId) {
        
        // Create guarantor relationship
        Guarantor guarantor = new Guarantor();
        guarantor.setMemberId(memberId);
        guarantor.setLoanId(loanId);
        guarantorRepository.save(guarantor);
        
        User user = new User();
        user.setMember(new Member(memberId));
        
        boolean canView = guarantorAccessEvaluator.canViewLoan(user, loanId);
        
        assertTrue(canView, "Guarantor should have access to guaranteed loan");
    }
    
    /**
     * Feature: rbac-security-system, Property 21: Cash reconciliation variance calculation
     */
    @Property
    void varianceShouldBeCalculatedCorrectly(
            @ForAll @BigDecimalRange(min = "0", max = "1000000") BigDecimal physicalCount,
            @ForAll @BigDecimalRange(min = "0", max = "1000000") BigDecimal databaseBalance) {
        
        User officer = createTestUser("ROLE_OFFICER");
        
        when(cashReconciliationService.calculateDatabaseBalance(any()))
            .thenReturn(databaseBalance);
        
        CashReconciliation reconciliation = cashReconciliationService.createReconciliation(
            officer, physicalCount);
        
        BigDecimal expectedVariance = physicalCount.subtract(databaseBalance);
        
        assertEquals(0, expectedVariance.compareTo(reconciliation.getVariance()));
    }
    
    /**
     * Feature: rbac-security-system, Property 43: Password complexity validation
     */
    @Property
    void passwordShouldBeValidatedForComplexity(@ForAll String password) {
        boolean hasMinLength = password.length() >= 8;
        boolean hasUppercase = password.chars().anyMatch(Character::isUpperCase);
        boolean hasLowercase = password.chars().anyMatch(Character::isLowerCase);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        
        boolean shouldBeValid = hasMinLength && hasUppercase && hasLowercase && hasDigit;
        boolean isValid = passwordService.validatePassword(password);
        
        assertEquals(shouldBeValid, isValid);
    }
    
    /**
     * Feature: rbac-security-system, Property 50: Self-approval denial
     */
    @Property
    void userShouldNotBeAbleToApproveOwnTransaction(
            @ForAll @LongRange(min = 1, max = 1000) Long userId) {
        
        Transaction transaction = new Transaction();
        transaction.setCreatorId(userId);
        
        User approver = new User();
        approver.setId(userId);
        
        boolean canApprove = permissionEvaluator.canApproveOwnTransaction(approver, transaction);
        
        assertFalse(canApprove, "User should not be able to approve their own transaction");
    }
}
```


**Frontend Property Tests (fast-check)**:

```typescript
import fc from 'fast-check';

describe('RBAC Property Tests', () => {
  
  /**
   * Feature: rbac-security-system, Property 4: Authorization based on current role only
   */
  it('should authorize based on current role only', () => {
    fc.assert(
      fc.property(
        fc.constantFrom('ROLE_OFFICER', 'ROLE_SECRETARY', 'ROLE_PRESIDENT', 'ROLE_MEMBER'),
        fc.constantFrom('transaction.create', 'loan.approve', 'accounting.edit', 'audit.view'),
        (role, permission) => {
          const user = { role, permissions: getPermissionsForRole(role) };
          const hasPermission = user.permissions.includes(permission);
          
          // Authorization should be based solely on current role
          const isAuthorized = checkAuthorization(user, permission);
          
          expect(isAuthorized).toBe(hasPermission);
        }
      ),
      { numRuns: 100 }
    );
  });
  
  /**
   * Feature: rbac-security-system, Property 13: Member data isolation
   */
  it('should isolate member data access', () => {
    fc.assert(
      fc.property(
        fc.integer({ min: 1, max: 1000 }),
        fc.integer({ min: 1, max: 1000 }),
        (memberId1, memberId2) => {
          fc.pre(memberId1 !== memberId2); // Ensure different members
          
          const user = { memberId: memberId1, role: 'ROLE_MEMBER' };
          
          // User should be able to access their own data
          expect(canAccessMemberData(user, memberId1)).toBe(true);
          
          // User should NOT be able to access other member's data
          expect(canAccessMemberData(user, memberId2)).toBe(false);
        }
      ),
      { numRuns: 100 }
    );
  });
  
  /**
   * Feature: rbac-security-system, Property 18: Loan status visual indicator
   */
  it('should display correct color indicator for loan status', () => {
    fc.assert(
      fc.property(
        fc.record({
          loanId: fc.integer({ min: 1, max: 1000 }),
          isDelinquent: fc.boolean(),
          daysOverdue: fc.integer({ min: 0, max: 90 })
        }),
        (loan) => {
          const indicator = getLoanStatusIndicator(loan);
          
          if (loan.isDelinquent || loan.daysOverdue > 0) {
            expect(indicator.color).toBe('red');
          } else {
            expect(indicator.color).toBe('green');
          }
        }
      ),
      { numRuns: 100 }
    );
  });
  
  /**
   * Feature: rbac-security-system, Property 22: Day close authorization with zero variance
   */
  it('should allow day close when variance is zero', () => {
    fc.assert(
      fc.property(
        fc.float({ min: 0, max: 1000000, noNaN: true }),
        (balance) => {
          const reconciliation = {
            physicalCount: balance,
            databaseBalance: balance,
            variance: 0
          };
          
          const canClose = canCloseDay(reconciliation);
          
          expect(canClose).toBe(true);
        }
      ),
      { numRuns: 100 }
    );
  });
  
  /**
   * Feature: rbac-security-system, Property 23: Day close denial with non-zero variance
   */
  it('should prevent day close when variance exists', () => {
    fc.assert(
      fc.property(
        fc.float({ min: 0, max: 1000000, noNaN: true }),
        fc.float({ min: -10000, max: 10000, noNaN: true }).filter(v => v !== 0),
        (balance, variance) => {
          const reconciliation = {
            physicalCount: balance + variance,
            databaseBalance: balance,
            variance: variance
          };
          
          const canClose = canCloseDay(reconciliation);
          
          expect(canClose).toBe(false);
        }
      ),
      { numRuns: 100 }
    );
  });
  
  /**
   * Feature: rbac-security-system, Property 30: Comprehensive CUD operation audit logging
   */
  it('should create audit log for all CUD operations', () => {
    fc.assert(
      fc.property(
        fc.constantFrom('CREATE', 'UPDATE', 'DELETE'),
        fc.record({
          userId: fc.integer({ min: 1, max: 100 }),
          entityType: fc.constantFrom('Member', 'Loan', 'Transaction'),
          entityId: fc.integer({ min: 1, max: 1000 })
        }),
        (operation, data) => {
          const auditLogs: AuditLog[] = [];
          const mockAuditService = {
            logAction: (log: AuditLog) => auditLogs.push(log)
          };
          
          performOperation(operation, data, mockAuditService);
          
          expect(auditLogs.length).toBeGreaterThan(0);
          const log = auditLogs[0];
          expect(log.userId).toBe(data.userId);
          expect(log.entityType).toBe(data.entityType);
          expect(log.entityId).toBe(data.entityId);
          expect(log.action).toBe(operation);
        }
      ),
      { numRuns: 100 }
    );
  });
});
```

### Integration Testing

Integration tests verify end-to-end security flows:

```java
@SpringBootTest
@AutoConfigureMockMvc
class RBACIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    @WithMockUser(roles = "OFFICER")
    void officerShouldBeAbleToCreateTransaction() throws Exception {
        mockMvc.perform(post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"memberId\":1,\"amount\":1000,\"type\":\"DEPOSIT\"}"))
            .andExpect(status().isOk());
    }
    
    @Test
    @WithMockUser(roles = "OFFICER")
    void officerShouldNotBeAbleToApproveLoan() throws Exception {
        mockMvc.perform(post("/api/loans/1/approve"))
            .andExpect(status().isForbidden());
    }
    
    @Test
    @WithMockUser(roles = "PRESIDENT")
    void presidentShouldBeAbleToApproveLoan() throws Exception {
        mockMvc.perform(post("/api/loans/1/approve")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"approved\":true,\"notes\":\"Approved\"}"))
            .andExpect(status().isOk());
    }
    
    @Test
    @WithMockUser(roles = "MEMBER")
    void memberShouldOnlyAccessOwnData() throws Exception {
        // Should succeed for own data
        mockMvc.perform(get("/api/members/1/profile"))
            .andExpect(status().isOk());
        
        // Should fail for other member's data
        mockMvc.perform(get("/api/members/2/profile"))
            .andExpect(status().isForbidden());
    }
    
    @Test
    void unauthorizedAccessShouldBeLogged() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                .with(user("testuser").roles("MEMBER")))
            .andExpect(status().isForbidden());
        
        // Verify audit log was created
        List<AuditLog> logs = auditLogRepository.findByAction("ACCESS_DENIED");
        assertTrue(logs.size() > 0);
    }
}
```

## Security Considerations

### 1. SQL Injection Prevention
- Use parameterized queries and JPA for all database operations
- Never concatenate user input into SQL strings

### 2. Session Management
- Implement JWT with short expiration times (15 minutes)
- Use refresh tokens stored securely
- Invalidate sessions on role change

### 3. Password Security
- Use bcrypt with cost factor 12
- Never log or transmit passwords in plain text
- Implement password history to prevent reuse

### 4. Audit Log Integrity
- Audit logs should be append-only
- Implement database triggers to prevent audit log modification
- Regular backup of audit logs to immutable storage

### 5. Principle of Least Privilege
- Grant minimum permissions necessary for each role
- Regularly review and audit permission assignments
- Implement time-limited elevated permissions for sensitive operations

