package com.bansaiyai.bansaiyai.config;

import com.bansaiyai.bansaiyai.entity.User;
import com.bansaiyai.bansaiyai.repository.UserRepository;
import com.bansaiyai.bansaiyai.service.RoleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DataInitializer implements CommandLineRunner {

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @Autowired
  private RoleService roleService;

  @Override
  public void run(String... args) throws Exception {
    // Only run initialization if no users exist (prevent repeated runs)
    if (userRepository.count() == 0) {
      log.info("Starting data initialization - no users found in database");

      // Register all roles in system
      roleService.registerAllRoles();

      // Create default users for each role
      createDefaultUsers();

      log.info("Data initialization completed");
    } else {
      log.info("Skipping data initialization - {} users already exist", userRepository.count());
    }
  }

  private void createDefaultUsers() {
    // Create or update ADMIN user
    userRepository.findByUsername("admin").ifPresentOrElse(
        existingUser -> {
          existingUser.setPassword(passwordEncoder.encode("admin123"));
          existingUser.setAccountNonLocked(true);
          existingUser.setEnabled(true);
          existingUser.setLoginAttempts(0);
          existingUser.setLockedUntil(null);
          userRepository.save(existingUser);
          log.info("Admin user password reset to: admin123");
        },
        () -> {
          User admin = User.builder()
              .username("admin")
              .email("admin@bansaiyai.com")
              .password(passwordEncoder.encode("admin123"))
              .firstName("System")
              .lastName("Administrator")
              .phoneNumber("0000000001")
              .role(User.Role.ADMIN)
              .enabled(true)
              .accountNonExpired(true)
              .accountNonLocked(true)
              .credentialsNonExpired(true)
              .emailVerified(true)
              .loginAttempts(0)
              .build();
          userRepository.save(admin);
          log.info("Default admin user created with username: admin and password: admin123");
        });

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
        });

    log.info("All default user accounts initialization completed");
  }
}
