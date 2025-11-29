package com.bansaiyai.bansaiyai.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldNameConstants;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldNameConstants
@EntityListeners(AuditingEntityListener.class)
public class User extends BaseEntity {

  @Column(unique = true, nullable = false)
  private String username;

  @Column(unique = true, nullable = false)
  private String email;

  @Column(nullable = false)
  private String password;

  @Column(name = "first_name")
  private String firstName;

  @Column(name = "last_name")
  private String lastName;

  @Column(name = "phone_number")
  private String phoneNumber;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Role role;

  @Column(nullable = false)
  private Boolean enabled = true;

  @Column(name = "account_non_expired")
  private Boolean accountNonExpired = true;

  @Column(name = "account_non_locked")
  private Boolean accountNonLocked = true;

  @Column(name = "credentials_non_expired")
  private Boolean credentialsNonExpired = true;

  @Column(name = "email_verified")
  private Boolean emailVerified = false;

  @Column(name = "email_verification_token")
  private String emailVerificationToken;

  @Column(name = "password_reset_token")
  private String passwordResetToken;

  @Column(name = "password_reset_expires")
  private LocalDateTime passwordResetExpires;

  @Column(name = "last_login")
  private LocalDateTime lastLogin;

  @Column(name = "login_attempts")
  private Integer loginAttempts = 0;

  @Column(name = "locked_until")
  private LocalDateTime lockedUntil;

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "user_permissions", joinColumns = @JoinColumn(name = "user_id"))
  @Column(name = "permission")
  private Set<String> permissions = new HashSet<>();

  @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private Member member;

  @PrePersist
  @PreUpdate
  private void ensurePermissions() {
    if (permissions == null) {
      permissions = new HashSet<>();
    }
    // Add role-based permissions
    addRolePermissions();
  }

  private void addRolePermissions() {
    switch (role) {
      case PRESIDENT:
        permissions.addAll(Set.of(
            "MEMBER_READ", "MEMBER_WRITE", "MEMBER_DELETE",
            "LOAN_READ", "LOAN_WRITE", "LOAN_DELETE", "LOAN_APPROVE",
            "SAVINGS_READ", "SAVINGS_WRITE", "SAVINGS_DELETE",
            "PAYMENT_READ", "PAYMENT_WRITE", "PAYMENT_DELETE",
            "REPORT_READ", "REPORT_WRITE", "ADMIN_READ", "ADMIN_WRITE"));
        break;
      case SECRETARY:
        permissions.addAll(Set.of(
            "MEMBER_READ", "MEMBER_WRITE",
            "LOAN_READ", "LOAN_WRITE",
            "SAVINGS_READ", "SAVINGS_WRITE",
            "PAYMENT_READ", "PAYMENT_WRITE",
            "REPORT_READ", "REPORT_WRITE"));
        break;
      case OFFICER:
        permissions.addAll(Set.of(
            "MEMBER_READ", "MEMBER_WRITE",
            "LOAN_READ", "LOAN_WRITE",
            "SAVINGS_READ", "SAVINGS_WRITE",
            "PAYMENT_READ", "PAYMENT_WRITE"));
        break;
      case MEMBER:
        permissions.addAll(Set.of(
            "MEMBER_READ_SELF",
            "LOAN_READ_SELF",
            "SAVINGS_READ_SELF",
            "PAYMENT_READ_SELF"));
        break;
    }
  }

  // Getters and Setters
  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public String getPhoneNumber() {
    return phoneNumber;
  }

  public void setPhoneNumber(String phoneNumber) {
    this.phoneNumber = phoneNumber;
  }

  public Role getRole() {
    return role;
  }

  public void setRole(Role role) {
    this.role = role;
  }

  public Boolean getEnabled() {
    return enabled;
  }

  public void setEnabled(Boolean enabled) {
    this.enabled = enabled;
  }

  public Boolean getAccountNonExpired() {
    return accountNonExpired;
  }

  public void setAccountNonExpired(Boolean accountNonExpired) {
    this.accountNonExpired = accountNonExpired;
  }

  public Boolean getAccountNonLocked() {
    return accountNonLocked;
  }

  public void setAccountNonLocked(Boolean accountNonLocked) {
    this.accountNonLocked = accountNonLocked;
  }

  public Boolean getCredentialsNonExpired() {
    return credentialsNonExpired;
  }

  public void setCredentialsNonExpired(Boolean credentialsNonExpired) {
    this.credentialsNonExpired = credentialsNonExpired;
  }

  public Boolean getEmailVerified() {
    return emailVerified;
  }

  public void setEmailVerified(Boolean emailVerified) {
    this.emailVerified = emailVerified;
  }

  public String getEmailVerificationToken() {
    return emailVerificationToken;
  }

  public void setEmailVerificationToken(String emailVerificationToken) {
    this.emailVerificationToken = emailVerificationToken;
  }

  public String getPasswordResetToken() {
    return passwordResetToken;
  }

  public void setPasswordResetToken(String passwordResetToken) {
    this.passwordResetToken = passwordResetToken;
  }

  public LocalDateTime getPasswordResetExpires() {
    return passwordResetExpires;
  }

  public void setPasswordResetExpires(LocalDateTime passwordResetExpires) {
    this.passwordResetExpires = passwordResetExpires;
  }

  public LocalDateTime getLastLogin() {
    return lastLogin;
  }

  public void setLastLogin(LocalDateTime lastLogin) {
    this.lastLogin = lastLogin;
  }

  public Integer getLoginAttempts() {
    return loginAttempts;
  }

  public void setLoginAttempts(Integer loginAttempts) {
    this.loginAttempts = loginAttempts;
  }

  public LocalDateTime getLockedUntil() {
    return lockedUntil;
  }

  public void setLockedUntil(LocalDateTime lockedUntil) {
    this.lockedUntil = lockedUntil;
  }

  public Set<String> getPermissions() {
    return permissions;
  }

  public void setPermissions(Set<String> permissions) {
    this.permissions = permissions;
  }

  public Member getMember() {
    return member;
  }

  public void setMember(Member member) {
    this.member = member;
  }

  public void setId(Long id) {
    super.setId(id);
  }

  // Manual builder for Lombok compatibility
  public static UserBuilder builder() {
    return new UserBuilder();
  }

  public static class UserBuilder {
    private String username;
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private Role role;
    private Boolean enabled = true;
    private Boolean accountNonExpired = true;
    private Boolean accountNonLocked = true;
    private Boolean credentialsNonExpired = true;
    private Boolean emailVerified = false;
    private String emailVerificationToken;
    private String passwordResetToken;
    private LocalDateTime passwordResetExpires;
    private LocalDateTime lastLogin;
    private Integer loginAttempts = 0;
    private LocalDateTime lockedUntil;
    private Set<String> permissions = new HashSet<>();
    private Member member;

    public UserBuilder username(String username) {
      this.username = username;
      return this;
    }

    public UserBuilder email(String email) {
      this.email = email;
      return this;
    }

    public UserBuilder password(String password) {
      this.password = password;
      return this;
    }

    public UserBuilder firstName(String firstName) {
      this.firstName = firstName;
      return this;
    }

    public UserBuilder lastName(String lastName) {
      this.lastName = lastName;
      return this;
    }

    public UserBuilder phoneNumber(String phoneNumber) {
      this.phoneNumber = phoneNumber;
      return this;
    }

    public UserBuilder role(Role role) {
      this.role = role;
      return this;
    }

    public UserBuilder enabled(Boolean enabled) {
      this.enabled = enabled;
      return this;
    }

    public UserBuilder accountNonExpired(Boolean accountNonExpired) {
      this.accountNonExpired = accountNonExpired;
      return this;
    }

    public UserBuilder accountNonLocked(Boolean accountNonLocked) {
      this.accountNonLocked = accountNonLocked;
      return this;
    }

    public UserBuilder credentialsNonExpired(Boolean credentialsNonExpired) {
      this.credentialsNonExpired = credentialsNonExpired;
      return this;
    }

    public UserBuilder emailVerified(Boolean emailVerified) {
      this.emailVerified = emailVerified;
      return this;
    }

    public UserBuilder emailVerificationToken(String emailVerificationToken) {
      this.emailVerificationToken = emailVerificationToken;
      return this;
    }

    public UserBuilder passwordResetToken(String passwordResetToken) {
      this.passwordResetToken = passwordResetToken;
      return this;
    }

    public UserBuilder passwordResetExpires(LocalDateTime passwordResetExpires) {
      this.passwordResetExpires = passwordResetExpires;
      return this;
    }

    public UserBuilder lastLogin(LocalDateTime lastLogin) {
      this.lastLogin = lastLogin;
      return this;
    }

    public UserBuilder loginAttempts(Integer loginAttempts) {
      this.loginAttempts = loginAttempts;
      return this;
    }

    public UserBuilder lockedUntil(LocalDateTime lockedUntil) {
      this.lockedUntil = lockedUntil;
      return this;
    }

    public UserBuilder permissions(Set<String> permissions) {
      this.permissions = permissions;
      return this;
    }

    public UserBuilder member(Member member) {
      this.member = member;
      return this;
    }

    public User build() {
      User user = new User();
      user.username = this.username;
      user.email = this.email;
      user.password = this.password;
      user.firstName = this.firstName;
      user.lastName = this.lastName;
      user.phoneNumber = this.phoneNumber;
      user.role = this.role;
      user.enabled = this.enabled;
      user.accountNonExpired = this.accountNonExpired;
      user.accountNonLocked = this.accountNonLocked;
      user.credentialsNonExpired = this.credentialsNonExpired;
      user.emailVerified = this.emailVerified;
      user.emailVerificationToken = this.emailVerificationToken;
      user.passwordResetToken = this.passwordResetToken;
      user.passwordResetExpires = this.passwordResetExpires;
      user.lastLogin = this.lastLogin;
      user.loginAttempts = this.loginAttempts;
      user.lockedUntil = this.lockedUntil;
      user.permissions = this.permissions;
      user.member = this.member;
      return user;
    }
  }

  public enum Role {
    PRESIDENT,
    SECRETARY,
    OFFICER,
    MEMBER
  }
}
