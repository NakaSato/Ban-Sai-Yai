package com.bansaiyai.bansaiyai.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldNameConstants;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.UUID;

import com.bansaiyai.bansaiyai.entity.enums.LoanStatus;

/**
 * Member entity representing savings group members.
 * Contains member personal information, registration details, and business
 * logic methods.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldNameConstants
@Entity
@Table(name = "member", indexes = {
    @Index(name = "idx_member_id", columnList = "memberId"),
    @Index(name = "idx_id_card", columnList = "idCard"),
    @Index(name = "idx_registration_date", columnList = "registrationDate"),
    @Index(name = "idx_member_uuid", columnList = "uuid")
})
@EntityListeners(AuditingEntityListener.class)
public class Member extends BaseEntity {

  /**
   * UUID for external API use - prevents ID enumeration attacks
   * This is the primary identifier exposed in public APIs
   */
  @Column(name = "uuid", nullable = false, unique = true)
  private UUID uuid;

  @Column(name = "member_id", unique = true, nullable = false, length = 20)
  @NotBlank(message = "Member ID is required")
  @Size(min = 3, max = 20, message = "Member ID must be between 3 and 20 characters")
  private String memberId;

  @Column(name = "name", nullable = false, length = 100)
  @NotBlank(message = "Name is required")
  @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
  private String name;

  @Column(name = "id_card", unique = true, nullable = false, length = 13)
  @NotBlank(message = "ID card is required")
  @Pattern(regexp = "^\\d{13}$", message = "ID card must be exactly 13 digits")
  private String idCard;

  @Column(name = "date_of_birth", nullable = false)
  @NotNull(message = "Date of birth is required")
  @Past(message = "Date of birth must be in the past")
  private LocalDate dateOfBirth;

  @Column(name = "address", nullable = false, length = 200)
  @NotBlank(message = "Address is required")
  @Size(min = 10, max = 200, message = "Address must be between 10 and 200 characters")
  private String address;

  @Column(name = "phone", nullable = false, length = 20)
  @NotBlank(message = "Phone number is required")
  @Pattern(regexp = "^[0-9]{9,10}$", message = "Phone number must be 9-10 digits")
  private String phone;

  @Column(name = "email", length = 100)
  @Email(message = "Invalid email format")
  private String email;

  @Column(name = "photo_path", length = 500)
  private String photoPath;

  @Column(name = "registration_date", nullable = false)
  @NotNull(message = "Registration date is required")
  private LocalDate registrationDate;

  @Column(name = "is_active", nullable = false)
  @Builder.Default
  private Boolean isActive = true;

  @Column(name = "occupation", length = 100)
  private String occupation;

  @Column(name = "monthly_income", precision = 12, scale = 2)
  private BigDecimal monthlyIncome;

  @Column(name = "marital_status", length = 20)
  private String maritalStatus;

  @Column(name = "spouse_name", length = 100)
  private String spouseName;

  @Column(name = "number_of_children")
  private Integer numberOfChildren;

  @Column(name = "share_capital", precision = 15, scale = 2)
  private BigDecimal shareCapital;

  // Relationships
  @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private List<Loan> loans;

  @OneToOne(mappedBy = "member", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private SavingAccount savingAccount;

  // Additional getters and setters for Lombok compatibility
  public SavingAccount getSavingAccount() {
    return savingAccount;
  }

  public void setSavingAccount(SavingAccount savingAccount) {
    this.savingAccount = savingAccount;
  }

  public Boolean getIsActive() {
    return isActive;
  }

  public void setIsActive(Boolean isActive) {
    this.isActive = isActive;
  }

  public void setUser(User user) {
    this.user = user;
  }

  @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private List<Payment> payments;

  @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", unique = true)
  private User user;

  // Business logic methods
  /**
   * Calculate member's current age based on date of birth
   */
  public int getAge() {
    return Period.between(dateOfBirth, LocalDate.now()).getYears();
  }

  /**
   * Check if member is eligible for loan based on business rules
   * - Must be at least 18 years old
   * - Must be active member
   * - Must have been registered for at least 6 months
   */
  public boolean isEligibleForLoan() {
    return isActive &&
        getAge() >= 18 &&
        registrationDate.isBefore(LocalDate.now().minusMonths(6));
  }

  /**
   * Check if member can apply for additional loan
   * - Maximum 2 active loans allowed
   */
  public boolean canApplyForLoan() {
    return isEligibleForLoan() &&
        (loans == null ||
            loans.stream()
                .filter(loan -> loan.getStatus() != null &&
                    (loan.getStatus() == LoanStatus.ACTIVE ||
                        loan.getStatus() == LoanStatus.PENDING))
                .count() < 2);
  }

  /**
   * Get masked ID card for display purposes
   */
  public String getMaskedIdCard() {
    if (idCard == null || idCard.length() < 13) {
      return idCard;
    }
    return idCard.substring(0, 3) + "XXXXX" + idCard.substring(8);
  }

  /**
   * Get full name with title based on marital status
   */
  public String getDisplayName() {
    String title = "คุณ"; // Thai title for "Mr./Ms."
    if (maritalStatus != null && maritalStatus.toLowerCase().contains("married") && genderFromIdCard() == 'F') {
      title = "คุณนาง"; // Thai title for "Mrs."
    } else if (maritalStatus != null && maritalStatus.toLowerCase().contains("single") && genderFromIdCard() == 'M') {
      title = "นาย"; // Thai title for "Mr."
    }
    return title + " " + name;
  }

  /**
   * Extract gender from Thai ID card (1st digit: 1=Male, 2=Female)
   */
  private char genderFromIdCard() {
    if (idCard == null || idCard.length() < 1) {
      return 'U'; // Unknown
    }
    return idCard.charAt(0) == '1' ? 'M' : 'F';
  }

  @PrePersist
  protected void onCreate() {
    // Auto-generate UUID for security
    if (uuid == null) {
      uuid = UUID.randomUUID();
    }
    if (memberId == null || memberId.trim().isEmpty()) {
      memberId = generateMemberId();
    }
    if (registrationDate == null) {
      registrationDate = LocalDate.now();
    }
  }

  /**
   * Generate unique member ID with format: BSY-YYYY-MM-DD-XXXX
   */
  private String generateMemberId() {
    String date = LocalDate.now().toString().replace("-", "");
    String random = String.format("%04d", (int) (Math.random() * 10000));
    return "BSY-" + date + "-" + random;
  }

  // Additional methods for Lombok compatibility
  public void setId(Long id) {
    super.setId(id);
  }

  public BigDecimal getShareCapital() {
    return shareCapital;
  }

  public void setShareCapital(BigDecimal shareCapital) {
    this.shareCapital = shareCapital;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getMemberId() {
    return memberId;
  }

  public void setMemberId(String memberId) {
    this.memberId = memberId;
  }

  // Manual builder for Lombok compatibility
  public static MemberBuilder builder() {
    return new MemberBuilder();
  }

  public static class MemberBuilder {
    private UUID uuid;
    private String memberId;
    private String name;
    private String idCard;
    private java.time.LocalDate dateOfBirth;
    private String address;
    private String phone;
    private String email;
    private String photoPath;
    private java.time.LocalDate registrationDate;
    private Boolean isActive = true;
    private String occupation;
    private java.math.BigDecimal monthlyIncome;
    private String maritalStatus;
    private String spouseName;
    private Integer numberOfChildren;
    private java.math.BigDecimal shareCapital;
    private User user;
    private java.util.List<Loan> loans;
    private SavingAccount savingAccount;
    private java.util.List<Payment> payments;

    public MemberBuilder uuid(UUID uuid) {
      this.uuid = uuid;
      return this;
    }

    public MemberBuilder memberId(String memberId) {
      this.memberId = memberId;
      return this;
    }

    public MemberBuilder name(String name) {
      this.name = name;
      return this;
    }

    public MemberBuilder idCard(String idCard) {
      this.idCard = idCard;
      return this;
    }

    public MemberBuilder dateOfBirth(java.time.LocalDate dateOfBirth) {
      this.dateOfBirth = dateOfBirth;
      return this;
    }

    public MemberBuilder address(String address) {
      this.address = address;
      return this;
    }

    public MemberBuilder phone(String phone) {
      this.phone = phone;
      return this;
    }

    public MemberBuilder email(String email) {
      this.email = email;
      return this;
    }

    public MemberBuilder photoPath(String photoPath) {
      this.photoPath = photoPath;
      return this;
    }

    public MemberBuilder registrationDate(java.time.LocalDate registrationDate) {
      this.registrationDate = registrationDate;
      return this;
    }

    public MemberBuilder isActive(Boolean isActive) {
      this.isActive = isActive;
      return this;
    }

    public MemberBuilder occupation(String occupation) {
      this.occupation = occupation;
      return this;
    }

    public MemberBuilder monthlyIncome(java.math.BigDecimal monthlyIncome) {
      this.monthlyIncome = monthlyIncome;
      return this;
    }

    public MemberBuilder maritalStatus(String maritalStatus) {
      this.maritalStatus = maritalStatus;
      return this;
    }

    public MemberBuilder spouseName(String spouseName) {
      this.spouseName = spouseName;
      return this;
    }

    public MemberBuilder numberOfChildren(Integer numberOfChildren) {
      this.numberOfChildren = numberOfChildren;
      return this;
    }

    public MemberBuilder shareCapital(java.math.BigDecimal shareCapital) {
      this.shareCapital = shareCapital;
      return this;
    }

    public MemberBuilder user(User user) {
      this.user = user;
      return this;
    }

    public MemberBuilder loans(java.util.List<Loan> loans) {
      this.loans = loans;
      return this;
    }

    public MemberBuilder savingAccount(SavingAccount savingAccount) {
      this.savingAccount = savingAccount;
      return this;
    }

    public MemberBuilder payments(java.util.List<Payment> payments) {
      this.payments = payments;
      return this;
    }

    public MemberBuilder createdAt(java.time.LocalDateTime createdAt) {
      // Note: This is needed for testing
      return this;
    }

    public MemberBuilder id(Long id) {
      // Note: ID is typically set by JPA, but added for testing
      return this;
    }

    public Member build() {
      Member member = new Member();
      member.uuid = this.uuid;
      member.memberId = this.memberId;
      member.name = this.name;
      member.idCard = this.idCard;
      member.dateOfBirth = this.dateOfBirth;
      member.address = this.address;
      member.phone = this.phone;
      member.email = this.email;
      member.photoPath = this.photoPath;
      member.registrationDate = this.registrationDate;
      member.isActive = this.isActive;
      member.occupation = this.occupation;
      member.monthlyIncome = this.monthlyIncome;
      member.maritalStatus = this.maritalStatus;
      member.spouseName = this.spouseName;
      member.numberOfChildren = this.numberOfChildren;
      member.shareCapital = this.shareCapital;
      member.user = this.user;
      member.loans = this.loans;
      member.savingAccount = this.savingAccount;
      member.payments = this.payments;
      return member;
    }
  }
}
