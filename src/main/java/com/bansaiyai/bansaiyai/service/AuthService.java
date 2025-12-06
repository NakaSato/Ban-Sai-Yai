package com.bansaiyai.bansaiyai.service;

import com.bansaiyai.bansaiyai.dto.SignUpRequest;
import com.bansaiyai.bansaiyai.entity.Member;
import com.bansaiyai.bansaiyai.entity.User;
import com.bansaiyai.bansaiyai.exception.BusinessException;
import com.bansaiyai.bansaiyai.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  @Transactional
  public User registerUser(SignUpRequest signUpRequest) {
    log.info("Registering new user: {}", signUpRequest.getUsername());

    // Check if username already exists
    if (userRepository.existsByUsername(signUpRequest.getUsername())) {
      throw new BusinessException("Username is already taken", "USERNAME_EXISTS");
    }

    // Check if email already exists
    if (userRepository.existsByEmail(signUpRequest.getEmail())) {
      throw new BusinessException("Email is already registered", "EMAIL_EXISTS");
    }

    // Create new user
    User user = User.builder()
        .username(signUpRequest.getUsername())
        .email(signUpRequest.getEmail())
        .password(passwordEncoder.encode(signUpRequest.getPassword()))
        .firstName(signUpRequest.getFirstName())
        .lastName(signUpRequest.getLastName())
        .phoneNumber(signUpRequest.getPhoneNumber())
        .role(signUpRequest.getRole())
        .emailVerified(false)
        .enabled(true)
        .accountNonExpired(true)
        .accountNonLocked(true)
        .credentialsNonExpired(true)
        .build();

    // Create associated member with required fields
    String memberId = "MBR" + System.currentTimeMillis(); // Generate unique member ID
    Member member = Member.builder()
        .memberId(memberId)
        .name(signUpRequest.getFirstName() + " " + signUpRequest.getLastName())
        .email(signUpRequest.getEmail())
        .phone(signUpRequest.getPhoneNumber())
        .idCard(signUpRequest.getIdCard())
        .dateOfBirth(signUpRequest.getDateOfBirth())
        .address(signUpRequest.getAddress())
        .registrationDate(LocalDate.now())
        .isActive(true)
        .build();

    user.setMember(member);
    member.setUser(user);

    return userRepository.save(user);
  }

  public Boolean isUsernameAvailable(String username) {
    return !userRepository.existsByUsername(username);
  }

  public Boolean isEmailAvailable(String email) {
    return !userRepository.existsByEmail(email);
  }

  @Transactional
  public void initiatePasswordReset(String email) {
    log.info("Initiating password reset for email: {}", email);

    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new BusinessException("User not found with email: " + email, "USER_NOT_FOUND"));

    // Generate reset token
    String resetToken = UUID.randomUUID().toString();
    user.setPasswordResetToken(resetToken);
    user.setPasswordResetExpires(LocalDateTime.now().plusHours(24)); // Token valid for 24 hours

    userRepository.save(user);

    // TODO: Send email with reset token
    // emailService.sendPasswordResetEmail(user.getEmail(), resetToken);
  }

  @Transactional
  public void resetPassword(String token, String newPassword, String confirmPassword) {
    log.info("Resetting password with token");

    if (!newPassword.equals(confirmPassword)) {
      throw new BusinessException("Passwords do not match", "PASSWORD_MISMATCH");
    }

    User user = userRepository.findByPasswordResetToken(token)
        .orElseThrow(() -> new BusinessException("Invalid or expired reset token", "INVALID_TOKEN"));

    if (user.getPasswordResetExpires().isBefore(LocalDateTime.now())) {
      throw new BusinessException("Reset token has expired", "TOKEN_EXPIRED");
    }

    user.setPassword(passwordEncoder.encode(newPassword));
    user.setPasswordResetToken(null);
    user.setPasswordResetExpires(null);
    user.setLastLogin(LocalDateTime.now());

    userRepository.save(user);
  }
}
