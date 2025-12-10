package com.bansaiyai.bansaiyai.config;

import com.bansaiyai.bansaiyai.entity.Member;
import com.bansaiyai.bansaiyai.entity.User;
import com.bansaiyai.bansaiyai.repository.MemberRepository;
import com.bansaiyai.bansaiyai.repository.UserRepository;
import com.bansaiyai.bansaiyai.service.RoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.UUID;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

  private final UserRepository userRepository;
  private final MemberRepository memberRepository;
  private final PasswordEncoder passwordEncoder;
  private final RoleService roleService;
  private final PlatformTransactionManager transactionManager;

  @Override
  public void run(String... args) throws Exception {
    // Only run initialization if no users exist (prevent repeated runs)
    // Register all roles in system
    roleService.registerAllRoles();

    // Create or update default users (Ensures passwords are set correctly even if
    // seeded by migration)
    createDefaultUsers();

    if (userRepository.count() == 0) {
      log.info("Starting data initialization - no users found in database (Legacy check)");
      // createDefaultUsers() already ran
      log.info("Data initialization completed");
    } else {
      log.info("Users exist in database. Default users checked/updated.");

      // Ensure member profile exists
      userRepository.findByUsername("member").ifPresent(user -> createMemberProfile(user.getId()));
    }
  }

  private void createDefaultUsers() {

    // Create or update PRESIDENT user
    userRepository.findByUsername("president").ifPresentOrElse(
        existingUser -> {
          existingUser.setPassword(passwordEncoder.encode("president123"));
          existingUser.setAccountNonLocked(true);
          existingUser.setEnabled(true);
          existingUser.setLoginAttempts(0);
          existingUser.setLockedUntil(null);
          userRepository.save(existingUser);
          log.info("President user password reset to: president123");
        },
        () -> {
          User president = User.builder()
              .username("president")
              .email("president@bansaiyai.com")
              .password(passwordEncoder.encode("president123"))
              .firstName("President")
              .lastName("User")
              .phoneNumber("0000000002")
              .role(User.Role.PRESIDENT)
              .enabled(true)
              .accountNonExpired(true)
              .accountNonLocked(true)
              .credentialsNonExpired(true)
              .emailVerified(true)
              .loginAttempts(0)
              .build();
          userRepository.save(president);
          log.info("Default president user created with username: president and password: president123");
        });

    // Create or update SECRETARY user
    userRepository.findByUsername("secretary").ifPresentOrElse(
        existingUser -> {
          existingUser.setPassword(passwordEncoder.encode("secretary123"));
          existingUser.setAccountNonLocked(true);
          existingUser.setEnabled(true);
          existingUser.setLoginAttempts(0);
          existingUser.setLockedUntil(null);
          userRepository.save(existingUser);
          log.info("Secretary user password reset to: secretary123");
        },
        () -> {
          User secretary = User.builder()
              .username("secretary")
              .email("secretary@bansaiyai.com")
              .password(passwordEncoder.encode("secretary123"))
              .firstName("Secretary")
              .lastName("User")
              .phoneNumber("0000000003")
              .role(User.Role.SECRETARY)
              .enabled(true)
              .accountNonExpired(true)
              .accountNonLocked(true)
              .credentialsNonExpired(true)
              .emailVerified(true)
              .loginAttempts(0)
              .build();
          userRepository.save(secretary);
          log.info("Default secretary user created with username: secretary and password: secretary123");
        });

    // Create or update OFFICER user
    userRepository.findByUsername("officer").ifPresentOrElse(
        existingUser -> {
          existingUser.setPassword(passwordEncoder.encode("officer123"));
          existingUser.setAccountNonLocked(true);
          existingUser.setEnabled(true);
          existingUser.setLoginAttempts(0);
          existingUser.setLockedUntil(null);
          userRepository.save(existingUser);
          log.info("Officer user password reset to: officer123");
        },
        () -> {
          User officer = User.builder()
              .username("officer")
              .email("officer@bansaiyai.com")
              .password(passwordEncoder.encode("officer123"))
              .firstName("Officer")
              .lastName("User")
              .phoneNumber("0000000004")
              .role(User.Role.OFFICER)
              .enabled(true)
              .accountNonExpired(true)
              .accountNonLocked(true)
              .credentialsNonExpired(true)
              .emailVerified(true)
              .loginAttempts(0)
              .build();
          userRepository.save(officer);
          log.info("Default officer user created with username: officer and password: officer123");
        });

    // Create or update MEMBER user
    userRepository.findByUsername("member").ifPresentOrElse(
        existingUser -> {
          existingUser.setPassword(passwordEncoder.encode("member123"));
          existingUser.setAccountNonLocked(true);
          existingUser.setEnabled(true);
          existingUser.setLoginAttempts(0);
          existingUser.setLockedUntil(null);
          userRepository.save(existingUser);
          log.info("Member user password reset to: member123");
          createMemberProfile(existingUser.getId());
        },
        () -> {
          User member = User.builder()
              .username("member")
              .email("member@bansaiyai.com")
              .password(passwordEncoder.encode("member123"))
              .firstName("Member")
              .lastName("User")
              .phoneNumber("0000000005")
              .role(User.Role.MEMBER)
              .enabled(true)
              .accountNonExpired(true)
              .accountNonLocked(true)
              .credentialsNonExpired(true)
              .emailVerified(true)
              .loginAttempts(0)
              .build();
          userRepository.save(member);
          log.info("Default member user created with username: member and password: member123");
          createMemberProfile(member.getId());
        });

    log.info("All default user accounts initialization completed");
  }

  public void createMemberProfile(Long userId) {
    new TransactionTemplate(transactionManager).execute(status -> {
      userRepository.findById(userId).ifPresent(managedUser -> {
        if (memberRepository.findByUserId(userId).isEmpty()) {
          Member member = Member.builder()
              .user(managedUser)
              // Safe fallbacks for missing user details
              .name((managedUser.getFirstName() != null ? managedUser.getFirstName() : "Member") + " " +
                  (managedUser.getLastName() != null ? managedUser.getLastName() : "User"))
              .idCard("1234567890123") // Dummy valid ID card
              .dateOfBirth(LocalDate.of(1990, 1, 1))
              .address("123 Default Ban Sai Yai Address")
              .phone(managedUser.getPhoneNumber() != null ? managedUser.getPhoneNumber() : "0812345678")
              .email(managedUser.getEmail())
              .occupation("Farmer")
              .monthlyIncome(new java.math.BigDecimal("15000.00"))
              .maritalStatus("Married")
              .spouseName("Mrs. Member User")
              .numberOfChildren(2)
              .registrationDate(LocalDate.now())
              .isActive(true)
              .uuid(UUID.randomUUID())
              .build();

          memberRepository.save(member);
          log.info("Detailed Member profile created for user: {}", managedUser.getUsername());
        }
      });
      return null;
    });
  }
}
