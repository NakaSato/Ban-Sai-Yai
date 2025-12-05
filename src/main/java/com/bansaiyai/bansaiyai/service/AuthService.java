package com.bansaiyai.bansaiyai.service;

import com.bansaiyai.bansaiyai.dto.SignUpRequest;
import com.bansaiyai.bansaiyai.entity.Member;
import com.bansaiyai.bansaiyai.entity.User;
import com.bansaiyai.bansaiyai.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AuthService {

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @Transactional
  public User registerUser(SignUpRequest signUpRequest) throws Exception {
    // Check if username already exists
    if (userRepository.existsByUsername(signUpRequest.getUsername())) {
      throw new RuntimeException("Username is already taken");
    }

    // Check if email already exists
    if (userRepository.existsByEmail(signUpRequest.getEmail())) {
      throw new RuntimeException("Email is already registered");
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
  public void initiatePasswordReset(String email) throws Exception {
    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

    // Generate reset token
    String resetToken = UUID.randomUUID().toString();
    user.setPasswordResetToken(resetToken);
    user.setPasswordResetExpires(LocalDateTime.now().plusHours(24)); // Token valid for 24 hours

    userRepository.save(user);

    // TODO: Send email with reset token
    // emailService.sendPasswordResetEmail(user.getEmail(), resetToken);
  }

  @Transactional
  public void resetPassword(String token, String newPassword, String confirmPassword) throws Exception {
    if (!newPassword.equals(confirmPassword)) {
      throw new RuntimeException("Passwords do not match");
    }

    User user = userRepository.findByPasswordResetToken(token)
        .orElseThrow(() -> new RuntimeException("Invalid or expired reset token"));

    if (user.getPasswordResetExpires().isBefore(LocalDateTime.now())) {
      throw new RuntimeException("Reset token has expired");
    }

    user.setPassword(passwordEncoder.encode(newPassword));
    user.setPasswordResetToken(null);
    user.setPasswordResetExpires(null);
    user.setLastLogin(LocalDateTime.now());

    userRepository.save(user);
  }
}
