# Sprint 5: Approval Workflow Service

## Overview

Sprint 5 implements **Approval Workflow Service** which manages the loan approval process, state transitions, and presidential authorization. This sprint provides the governance and approval mechanisms for loan applications.

## Sprint Objectives

### Primary Goals
- ✅ Implement loan approval workflow
- ✅ Create state transition management
- ✅ Set up presidential approval system
- ✅ Develop notification mechanisms
- ✅ Implement approval audit trail

### Success Criteria
- Loan applications move through proper approval states
- Presidential approval is required and enforced
- Notifications are sent for state changes
- Complete audit trail is maintained
- Rejection reasons are properly documented

## Technical Implementation

### 1. Entity Classes

#### ApprovalWorkflow Entity
```java
@Entity
@Table(name = "approval_workflow")
public class ApprovalWorkflow {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long workflowId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_id", nullable = false)
    private Loan loan;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ApprovalStatus currentStatus;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ApprovalAction lastAction;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processed_by")
    private UserEntity processedBy;
    
    @Column(length = 1000)
    private String comments;
    
    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;
    
    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    @LastModifiedDate
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "workflow", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ApprovalHistory> history;
    
    // Constructors, getters, setters
    
    public void approve(UserEntity approver, String comments) {
        this.currentStatus = ApprovalStatus.APPROVED;
        this.lastAction = ApprovalAction.APPROVED;
        this.processedBy = approver;
        this.comments = comments;
    }
    
    public void reject(UserEntity rejecter, String reason) {
        this.currentStatus = ApprovalStatus.REJECTED;
        this.lastAction = ApprovalAction.REJECTED;
        this.processedBy = rejecter;
        this.rejectionReason = reason;
    }
    
    public boolean canApprove() {
        return this.currentStatus == ApprovalStatus.PENDING ||
               this.currentStatus == ApprovalStatus.UNDER_REVIEW;
    }
    
    public boolean canReject() {
        return this.currentStatus == ApprovalStatus.PENDING ||
               this.currentStatus == ApprovalStatus.UNDER_REVIEW;
    }
}
```

#### ApprovalHistory Entity
```java
@Entity
@Table(name = "approval_history")
public class ApprovalHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long historyId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_id", nullable = false)
    private ApprovalWorkflow workflow;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ApprovalStatus fromStatus;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ApprovalStatus toStatus;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ApprovalAction action;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processed_by", nullable = false)
    private UserEntity processedBy;
    
    @Column(length = 1000)
    private String comments;
    
    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    // Constructors, getters, setters
}
```

### 2. Enums

#### ApprovalStatus
```java
public enum ApprovalStatus {
    PENDING("Pending Review"),
    UNDER_REVIEW("Under Review"),
    PENDING_PRESIDENTIAL("Pending Presidential Approval"),
    APPROVED("Approved"),
    REJECTED("Rejected"),
    CANCELLED("Cancelled");
    
    private final String description;
    
    ApprovalStatus(String description) {
        this.description = description;
    }
    
    // Getters
}
```

#### ApprovalAction
```java
public enum ApprovalAction {
    SUBMITTED("Application Submitted"),
    REVIEW_STARTED("Review Started"),
    REQUESTED_INFO("Additional Information Requested"),
    INFO_PROVIDED("Additional Information Provided"),
    SENT_TO_PRESIDENT("Sent to President"),
    APPROVED("Approved"),
    REJECTED("Rejected"),
    CANCELLED("Cancelled");
    
    private final String description;
    
    ApprovalAction(String description) {
        this.description = description;
    }
    
    // Getters
}
```

### 3. Data Transfer Objects (DTOs)

#### LoanApprovalDTO
```java
public class LoanApprovalDTO {
    
    @NotBlank(message = "Approved by is required")
    private String approvedBy;
    
    @Size(max = 1000, message = "Approval notes must not exceed 1000 characters")
    private String approvalNotes;
    
    private Boolean waiveRequirements = false;
    
    @Size(max = 500, message = "Waiver reason must not exceed 500 characters")
    private String waiverReason;
    
    private List<String> conditions;
    
    // Constructors, getters, setters
}
```

#### LoanRejectionDTO
```java
public class LoanRejectionDTO {
    
    @NotBlank(message = "Rejected by is required")
    private String rejectedBy;
    
    @NotBlank(message = "Rejection reason is required")
    @Size(max = 500, message = "Rejection reason must not exceed 500 characters")
    private String rejectionReason;
    
    private RejectionCategory category;
    
    private Boolean allowReapplication = false;
    
    @Size(max = 500, message = "Reapplication instructions must not exceed 500 characters")
    private String reapplicationInstructions;
    
    // Constructors, getters, setters
}
```

#### ApprovalWorkflowDTO
```java
public class ApprovalWorkflowDTO {
    private Long workflowId;
    private Long loanId;
    private String memberName;
    private BigDecimal loanAmount;
    private String loanType;
    private ApprovalStatus currentStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String lastProcessedBy;
    private ApprovalAction lastAction;
    private List<ApprovalHistoryDTO> history;
    
    // Constructors, getters, setters
}
```

### 4. Repository Layer

#### ApprovalWorkflowRepository
```java
@Repository
public interface ApprovalWorkflowRepository extends JpaRepository<ApprovalWorkflow, Long> {
    
    Optional<ApprovalWorkflow> findByLoanId(Long loanId);
    
    List<ApprovalWorkflow> findByCurrentStatus(ApprovalStatus status);
    
    @Query("SELECT aw FROM ApprovalWorkflow aw WHERE aw.currentStatus IN :statuses ORDER BY aw.createdAt DESC")
    List<ApprovalWorkflow> findByCurrentStatusInOrderByCreatedAtDesc(@Param("statuses") List<ApprovalStatus> statuses);
    
    @Query("SELECT aw FROM ApprovalWorkflow aw WHERE aw.processedBy.username = :username ORDER BY aw.updatedAt DESC")
    List<ApprovalWorkflow> findByProcessedByUsername(@Param("username") String username);
    
    @Query("SELECT COUNT(aw) FROM ApprovalWorkflow aw WHERE aw.currentStatus = :status AND aw.createdAt BETWEEN :startDate AND :endDate")
    long countByStatusAndDateRange(@Param("status") ApprovalStatus status,
                                     @Param("startDate") LocalDateTime startDate,
                                     @Param("endDate") LocalDateTime endDate);
}
```

#### ApprovalHistoryRepository
```java
@Repository
public interface ApprovalHistoryRepository extends JpaRepository<ApprovalHistory, Long> {
    
    List<ApprovalHistory> findByWorkflowIdOrderByCreatedAtDesc(Long workflowId);
    
    @Query("SELECT ah FROM ApprovalHistory ah WHERE ah.workflow.loan.id = :loanId ORDER BY ah.createdAt DESC")
    List<ApprovalHistory> findByLoanIdOrderByCreatedAtDesc(@Param("loanId") Long loanId);
    
    @Query("SELECT COUNT(ah) FROM ApprovalHistory ah WHERE ah.processedBy.username = :username AND ah.createdAt BETWEEN :startDate AND :endDate")
    long countActionsByUsername(@Param("username") String username,
                              @Param("startDate") LocalDateTime startDate,
                              @Param("endDate") LocalDateTime endDate);
}
```

### 5. Service Layer

#### ApprovalWorkflowService
```java
@Service
@Transactional
public class ApprovalWorkflowService {
    
    @Autowired
    private ApprovalWorkflowRepository workflowRepository;
    
    @Autowired
    private ApprovalHistoryRepository historyRepository;
    
    @Autowired
    private LoanRepository loanRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private NotificationService notificationService;
    
    public ApprovalWorkflowDTO initiateApprovalWorkflow(Long loanId) {
        Loan loan = loanRepository.findById(loanId)
            .orElseThrow(() -> new ResourceNotFoundException("Loan not found"));
        
        // Check if workflow already exists
        Optional<ApprovalWorkflow> existing = workflowRepository.findByLoanId(loanId);
        if (existing.isPresent()) {
            throw new BusinessRuleException("Approval workflow already exists for this loan");
        }
        
        // Create workflow
        ApprovalWorkflow workflow = new ApprovalWorkflow();
        workflow.setLoan(loan);
        workflow.setCurrentStatus(ApprovalStatus.PENDING);
        workflow.setLastAction(ApprovalAction.SUBMITTED);
        workflow.setComments("Loan application submitted for approval");
        
        workflow = workflowRepository.save(workflow);
        
        // Create initial history record
        createHistoryRecord(workflow, ApprovalStatus.PENDING, ApprovalAction.SUBMITTED, 
                          null, "Application submitted");
        
        // Send notification to officers
        notificationService.notifyLoanSubmitted(loan);
        
        return ApprovalWorkflowDTO.fromEntity(workflow);
    }
    
    @PreAuthorize("hasRole('PRESIDENT')")
    public ApprovalWorkflowDTO approveLoan(Long loanId, LoanApprovalDTO approvalDTO) {
        ApprovalWorkflow workflow = getWorkflowByLoanId(loanId);
        
        if (!workflow.canApprove()) {
            throw new BusinessRuleException("Loan cannot be approved in current status: " + workflow.getCurrentStatus());
        }
        
        // Get approver
        UserEntity approver = userRepository.findByUsername(approvalDTO.getApprovedBy())
            .orElseThrow(() -> new ResourceNotFoundException("Approver not found"));
        
        ApprovalStatus previousStatus = workflow.getCurrentStatus();
        
        // Approve the workflow
        workflow.approve(approver, approvalDTO.getApprovalNotes());
        workflow = workflowRepository.save(workflow);
        
        // Create history record
        createHistoryRecord(workflow, previousStatus, ApprovalStatus.APPROVED, 
                          approver, approvalDTO.getApprovalNotes());
        
        // Update loan status
        Loan loan = workflow.getLoan();
        loan.approve(approvalDTO.getApprovedBy(), approvalDTO.getApprovalNotes());
        loanRepository.save(loan);
        
        // Send notifications
        notificationService.notifyLoanApproved(loan);
        
        return ApprovalWorkflowDTO.fromEntity(workflow);
    }
    
    @PreAuthorize("hasRole('PRESIDENT')")
    public ApprovalWorkflowDTO rejectLoan(Long loanId, LoanRejectionDTO rejectionDTO) {
        ApprovalWorkflow workflow = getWorkflowByLoanId(loanId);
        
        if (!workflow.canReject()) {
            throw new BusinessRuleException("Loan cannot be rejected in current status: " + workflow.getCurrentStatus());
        }
        
        // Get rejecter
        UserEntity rejecter = userRepository.findByUsername(rejectionDTO.getRejectedBy())
            .orElseThrow(() -> new ResourceNotFoundException("Rejecter not found"));
        
        ApprovalStatus previousStatus = workflow.getCurrentStatus();
        
        // Reject the workflow
        workflow.reject(rejecter, rejectionDTO.getRejectionReason());
        workflow = workflowRepository.save(workflow);
        
        // Create history record
        createHistoryRecord(workflow, previousStatus, ApprovalStatus.REJECTED, 
                          rejecter, rejectionDTO.getRejectionReason());
        
        // Update loan status
        Loan loan = workflow.getLoan();
        loan.reject(rejectionDTO.getRejectionReason());
        loanRepository.save(loan);
        
        // Send notifications
        notificationService.notifyLoanRejected(loan, rejectionDTO);
        
        return ApprovalWorkflowDTO.fromEntity(workflow);
    }
    
    @PreAuthorize("hasAnyRole('OFFICER', 'SECRETARY')")
    public ApprovalWorkflowDTO requestPresidentialApproval(Long loanId, String reviewerNotes) {
        ApprovalWorkflow workflow = getWorkflowByLoanId(loanId);
        
        if (workflow.getCurrentStatus() != ApprovalStatus.UNDER_REVIEW) {
            throw new BusinessRuleException("Loan must be under review to request presidential approval");
        }
        
        ApprovalStatus previousStatus = workflow.getCurrentStatus();
        
        // Update workflow status
        workflow.setCurrentStatus(ApprovalStatus.PENDING_PRESIDENTIAL);
        workflow.setLastAction(ApprovalAction.SENT_TO_PRESIDENT);
        workflow.setComments(reviewerNotes);
        workflow = workflowRepository.save(workflow);
        
        // Create history record
        createHistoryRecord(workflow, previousStatus, ApprovalStatus.PENDING_PRESIDENTIAL, 
                          null, reviewerNotes);
        
        // Send notification to president
        notificationService.notifyPresidentialApprovalRequired(workflow.getLoan());
        
        return ApprovalWorkflowDTO.fromEntity(workflow);
    }
    
    @Transactional(readOnly = true)
    public List<ApprovalWorkflowDTO> getPendingApprovals() {
        List<ApprovalWorkflow> workflows = workflowRepository
            .findByCurrentStatusInOrderByCreatedAtDesc(Arrays.asList(
                ApprovalStatus.PENDING, 
                ApprovalStatus.UNDER_REVIEW,
                ApprovalStatus.PENDING_PRESIDENTIAL
            ));
        
        return workflows.stream()
            .map(ApprovalWorkflowDTO::fromEntity)
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public ApprovalWorkflowDTO getLoanApprovalWorkflow(Long loanId) {
        ApprovalWorkflow workflow = getWorkflowByLoanId(loanId);
        return ApprovalWorkflowDTO.fromEntity(workflow);
    }
    
    @Transactional(readOnly = true)
    public List<ApprovalHistoryDTO> getApprovalHistory(Long loanId) {
        List<ApprovalHistory> history = historyRepository.findByLoanIdOrderByCreatedAtDesc(loanId);
        
        return history.stream()
            .map(ApprovalHistoryDTO::fromEntity)
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public ApprovalSummaryDTO getApprovalSummary() {
        Map<ApprovalStatus, Long> statusCounts = Arrays.stream(ApprovalStatus.values())
            .collect(Collectors.toMap(
                status -> status,
                status -> workflowRepository.countByStatusAndDateRange(status, 
                    LocalDateTime.now().minusMonths(1), LocalDateTime.now())
            ));
        
        return ApprovalSummaryDTO.builder()
            .totalPending(statusCounts.getOrDefault(ApprovalStatus.PENDING, 0L))
            .underReview(statusCounts.getOrDefault(ApprovalStatus.UNDER_REVIEW, 0L))
            .pendingPresidential(statusCounts.getOrDefault(ApprovalStatus.PENDING_PRESIDENTIAL, 0L))
            .approved(statusCounts.getOrDefault(ApprovalStatus.APPROVED, 0L))
            .rejected(statusCounts.getOrDefault(ApprovalStatus.REJECTED, 0L))
            .summaryDate(LocalDate.now())
            .build();
    }
    
    private ApprovalWorkflow getWorkflowByLoanId(Long loanId) {
        return workflowRepository.findByLoanId(loanId)
            .orElseThrow(() -> new ResourceNotFoundException("Approval workflow not found for loan"));
    }
    
    private void createHistoryRecord(ApprovalWorkflow workflow, ApprovalStatus toStatus, 
                                  ApprovalAction action, UserEntity processedBy, String comments) {
        ApprovalHistory history = new ApprovalHistory();
        history.setWorkflow(workflow);
        history.setFromStatus(workflow.getCurrentStatus());
        history.setToStatus(toStatus);
        history.setAction(action);
        history.setProcessedBy(processedBy);
        history.setComments(comments);
        
        historyRepository.save(history);
    }
}
```

#### NotificationService
```java
@Service
public class NotificationService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private SMSService smsService;
    
    public void notifyLoanSubmitted(Loan loan) {
        // Notify officers and secretary
        List<UserEntity> staffUsers = userRepository.findByRoleIn(Arrays.asList(
            UserRole.ROLE_OFFICER, UserRole.ROLE_SECRETARY));
        
        for (UserEntity user : staffUsers) {
            emailService.sendLoanSubmissionNotification(user, loan);
        }
        
        // Notify member
        emailService.sendLoanSubmissionConfirmationToMember(loan.getMember(), loan);
    }
    
    public void notifyLoanApproved(Loan loan) {
        // Notify member
        emailService.sendLoanApprovalNotification(loan.getMember(), loan);
        smsService.sendLoanApprovalSMS(loan.getMember(), loan);
        
        // Notify loan officer
        if (loan.getApprovedBy() != null) {
            userRepository.findByUsername(loan.getApprovedBy())
                .ifPresent(officer -> emailService.sendLoanProcessedConfirmation(officer, loan));
        }
    }
    
    public void notifyLoanRejected(Loan loan, LoanRejectionDTO rejectionDTO) {
        // Notify member
        emailService.sendLoanRejectionNotification(loan.getMember(), loan, rejectionDTO);
        smsService.sendLoanRejectionSMS(loan.getMember(), loan);
    }
    
    public void notifyPresidentialApprovalRequired(Loan loan) {
        // Notify president
        userRepository.findByRole(UserRole.ROLE_PRESIDENT)
            .ifPresent(president -> {
                emailService.sendPresidentialApprovalRequest(president, loan);
                smsService.sendUrgentNotification(president, "Presidential approval required");
            });
    }
}
```

### 6. Controller Layer

#### ApprovalController
```java
@RestController
@RequestMapping("/api/approvals")
@Validated
public class ApprovalController {
    
    @Autowired
    private ApprovalWorkflowService approvalWorkflowService;
    
    @PostMapping("/loan/{loanId}/initiate")
    @PreAuthorize("hasAnyRole('OFFICER', 'SECRETARY')")
    public ResponseEntity<ApiResponse<ApprovalWorkflowDTO>> initiateApproval(@PathVariable Long loanId) {
        
        ApprovalWorkflowDTO workflow = approvalWorkflowService.initiateApprovalWorkflow(loanId);
        
        return ResponseEntity.ok(ApiResponse.success(workflow, "Approval workflow initiated"));
    }
    
    @PutMapping("/loan/{loanId}/approve")
    @PreAuthorize("hasRole('PRESIDENT')")
    public ResponseEntity<ApiResponse<ApprovalWorkflowDTO>> approveLoan(
            @PathVariable Long loanId,
            @Valid @RequestBody LoanApprovalDTO approvalDTO) {
        
        ApprovalWorkflowDTO workflow = approvalWorkflowService.approveLoan(loanId, approvalDTO);
        
        return ResponseEntity.ok(ApiResponse.success(workflow, "Loan approved successfully"));
    }
    
    @PutMapping("/loan/{loanId}/reject")
    @PreAuthorize("hasRole('PRESIDENT')")
    public ResponseEntity<ApiResponse<ApprovalWorkflowDTO>> rejectLoan(
            @PathVariable Long loanId,
            @Valid @RequestBody LoanRejectionDTO rejectionDTO) {
        
        ApprovalWorkflowDTO workflow = approvalWorkflowService.rejectLoan(loanId, rejectionDTO);
        
        return ResponseEntity.ok(ApiResponse.success(workflow, "Loan rejected"));
    }
    
    @PutMapping("/loan/{loanId}/request-presidential")
    @PreAuthorize("hasAnyRole('OFFICER', 'SECRETARY')")
    public ResponseEntity<ApiResponse<ApprovalWorkflowDTO>> requestPresidentialApproval(
            @PathVariable Long loanId,
            @RequestBody Map<String, String> request) {
        
        String reviewerNotes = request.get("notes");
        ApprovalWorkflowDTO workflow = approvalWorkflowService.requestPresidentialApproval(loanId, reviewerNotes);
        
        return ResponseEntity.ok(ApiResponse.success(workflow, "Presidential approval requested"));
    }
    
    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('OFFICER', 'SECRETARY', 'PRESIDENT')")
    public ResponseEntity<ApiResponse<List<ApprovalWorkflowDTO>>> getPendingApprovals() {
        
        List<ApprovalWorkflowDTO> workflows = approvalWorkflowService.getPendingApprovals();
        
        return ResponseEntity.ok(ApiResponse.success(workflows));
    }
    
    @GetMapping("/loan/{loanId}/workflow")
    @PreAuthorize("hasRole('OFFICER') or hasRole('SECRETARY') or hasRole('PRESIDENT') or " +
            "@loanSecurity.canViewLoan(#loanId, authentication)")
    public ResponseEntity<ApiResponse<ApprovalWorkflowDTO>> getLoanWorkflow(@PathVariable Long loanId) {
        
        ApprovalWorkflowDTO workflow = approvalWorkflowService.getLoanApprovalWorkflow(loanId);
        
        return ResponseEntity.ok(ApiResponse.success(workflow));
    }
    
    @GetMapping("/loan/{loanId}/history")
    @PreAuthorize("hasRole('OFFICER') or hasRole('SECRETARY') or hasRole('PRESIDENT') or " +
            "@loanSecurity.canViewLoan(#loanId, authentication)")
    public ResponseEntity<ApiResponse<List<ApprovalHistoryDTO>>> getApprovalHistory(@PathVariable Long loanId) {
        
        List<ApprovalHistoryDTO> history = approvalWorkflowService.getApprovalHistory(loanId);
        
        return ResponseEntity.ok(ApiResponse.success(history));
    }
    
    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('SECRETARY', 'PRESIDENT')")
    public ResponseEntity<ApiResponse<ApprovalSummaryDTO>> getApprovalSummary() {
        
        ApprovalSummaryDTO summary = approvalWorkflowService.getApprovalSummary();
        
        return ResponseEntity.ok(ApiResponse.success(summary));
    }
}
```

## Business Rules & Validation

### Approval Rules
1. **Sequential Process**: Applications must follow defined approval sequence
2. **Presidential Authority**: Only President can grant final approval
3. **Documentation Required**: All documents must be complete before approval
4. **Quota Limits**: Respect individual and group lending limits
5. **Conflict of Interest**: Approvers cannot be related to applicant

### State Transition Rules
1. **Pending → Under Review**: Officer review process
2. **Under Review → Presidential**: Presidential approval required
3. **Presidential → Approved/Rejected**: Final decision
4. **Any State → Cancelled**: Application can be cancelled

### Rejection Rules
1. **Clear Reasons**: Rejection must specify clear reasons
2. **Reapplication Policy**: Specify if and when reapplication allowed
3. **Documentation**: Maintain rejection documentation
4. **Appeal Process**: Defined appeal mechanism if applicable

## Testing Strategy

### Unit Tests
```java
@ExtendWith(MockitoExtension.class)
class ApprovalWorkflowServiceTest {
    
    @Mock
    private ApprovalWorkflowRepository workflowRepository;
    
    @Mock
    private LoanRepository loanRepository;
    
    @Mock
    private NotificationService notificationService;
    
    @InjectMocks
    private ApprovalWorkflowService approvalWorkflowService;
    
    @Test
    void testApproveLoan_Success() {
        // Given
        Long loanId = 1L;
        LoanApprovalDTO approvalDTO = createValidApprovalDTO();
        ApprovalWorkflow workflow = createTestWorkflow(ApprovalStatus.PENDING);
        UserEntity approver = createTestPresident();
        
        when(workflowRepository.findByLoanId(loanId)).thenReturn(Optional.of(workflow));
        when(userRepository.findByUsername(approvalDTO.getApprovedBy())).thenReturn(Optional.of(approver));
        
        // When
        ApprovalWorkflowDTO result = approvalWorkflowService.approveLoan(loanId, approvalDTO);
        
        // Then
        assertThat(result.getCurrentStatus()).isEqualTo(ApprovalStatus.APPROVED);
        assertThat(result.getLastAction()).isEqualTo(ApprovalAction.APPROVED);
        verify(workflowRepository).save(workflow);
        verify(notificationService).notifyLoanApproved(any(Loan.class));
    }
    
    @Test
    void testApproveLoan_InvalidStatus_ThrowsException() {
        // Given
        Long loanId = 1L;
        LoanApprovalDTO approvalDTO = createValidApprovalDTO();
        ApprovalWorkflow workflow = createTestWorkflow(ApprovalStatus.APPROVED); // Already approved
        
        when(workflowRepository.findByLoanId(loanId)).thenReturn(Optional.of(workflow));
        
        // When & Then
        assertThrows(BusinessRuleException.class, 
            () -> approvalWorkflowService.approveLoan(loanId, approvalDTO));
    }
}
```

---

**Related Documentation**:
- [Database Schema](../architecture/database-schema.md) - Entity definitions
- [API Documentation](../api/rest-endpoints.md) - Endpoint details
- [Security Implementation](../security/authentication-authorization.md) - Authorization details
