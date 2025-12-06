package com.bansaiyai.bansaiyai.integration;

import com.bansaiyai.bansaiyai.controller.UserController;
import com.bansaiyai.bansaiyai.dto.*;
import com.bansaiyai.bansaiyai.entity.Role;
import com.bansaiyai.bansaiyai.entity.User;
import com.bansaiyai.bansaiyai.repository.RoleRepository;
import com.bansaiyai.bansaiyai.repository.UserRepository;
import com.bansaiyai.bansaiyai.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for User Management endpoints
 * Tests role-based access control, validation, and audit logging
 * Requirements: 13.1, 13.2, 13.3
 */
@WebMvcTest(controllers = UserController.class)
@ActiveProfiles("test")
class UserManagementIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private UserService userService;

  @MockitoBean
  private UserRepository userRepository;

  @MockitoBean
  private RoleRepository roleRepository;

  private User testPresident;
  private User testSecretary;
  private User testOfficer;
  private User testMember;
  private Role presidentRole;
  private Role secretaryRole;

  @BeforeEach
  void setUp() {
    // Setup test roles
    presidentRole = new Role();
    presidentRole.setRoleId(1);
    presidentRole.setRoleName("PRESIDENT");

    secretaryRole = new Role();
    secretaryRole.setRoleId(2);
    secretaryRole.setRoleName("SECRETARY");

    // Setup test users
    testPresident = new User();
    testPresident.setId(1L);
    testPresident.setUsername("president@test.com");
    testPresident.setEmail("president@test.com");
    testPresident.setRole(User.Role.PRESIDENT);
    testPresident.setRbacRole(presidentRole);

    testSecretary = new User();
    testSecretary.setId(2L);
    testSecretary.setUsername("secretary@test.com");
    testSecretary.setEmail("secretary@test.com");
    testSecretary.setRole(User.Role.SECRETARY);
    testSecretary.setRbacRole(secretaryRole);

    testOfficer = new User();
    testOfficer.setId(3L);
    testOfficer.setUsername("officer@test.com");
    testOfficer.setEmail("officer@test.com");
    testOfficer.setRole(User.Role.OFFICER);

    testMember = new User();
    testMember.setId(4L);
    testMember.setUsername("member@test.com");
    testMember.setEmail("member@test.com");
    testMember.setRole(User.Role.MEMBER);

    when(roleRepository.findByRoleName("PRESIDENT")).thenReturn(Optional.of(presidentRole));
    when(roleRepository.findByRoleName("SECRETARY")).thenReturn(Optional.of(secretaryRole));
  }

  @Test
  @WithMockUser(roles = { "PRESIDENT" })
  void testPresidentCanAccessUserManagement() throws Exception {
    // Arrange
    Page<User> userPage = new PageImpl<>(Arrays.asList(testPresident, testSecretary, testOfficer, testMember));
    when(userService.getAllUsers(any(Pageable.class))).thenReturn(userPage);

    // Act & Assert
    mockMvc.perform(get("/api/admin/users")
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON));

    verify(userService).getAllUsers(any(Pageable.class));
  }

  @Test
  @WithMockUser(roles = { "SECRETARY" })
  void testSecretaryCannotAccessUserManagement() throws Exception {
    // Act & Assert
    mockMvc.perform(get("/api/admin/users")
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(roles = { "OFFICER" })
  void testOfficerCannotAccessUserManagement() throws Exception {
    // Act & Assert
    mockMvc.perform(get("/api/admin/users")
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(roles = { "MEMBER" })
  void testMemberCannotAccessUserManagement() throws Exception {
    // Act & Assert
    mockMvc.perform(get("/api/admin/users")
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());
  }

  @Test
  void testUnauthenticatedUserCannotAccessUserManagement() throws Exception {
    // Act & Assert
    mockMvc.perform(get("/api/admin/users")
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @WithMockUser(roles = { "PRESIDENT" })
  void testCreateUserWithValidData() throws Exception {
    // Arrange
    CreateUserRequest request = new CreateUserRequest();
    request.setUsername("newuser@test.com");
    request.setEmail("newuser@test.com");
    request.setPassword("SecurePass123!");
    request.setFirstName("New");
    request.setLastName("User");
    request.setRole("SECRETARY");

    User createdUser = new User();
    createdUser.setId(5L);
    createdUser.setUsername("newuser@test.com");
    createdUser.setEmail("newuser@test.com");
    createdUser.setFirstName("New");
    createdUser.setLastName("User");
    createdUser.setRole(User.Role.SECRETARY);
    createdUser.setRbacRole(secretaryRole);

    when(userService.createUser(anyString(), anyString(), anyString(), anyString(), anyString(), anyString(),
        anyString(), any(User.class)))
        .thenReturn(createdUser);

    // Act & Assert
    mockMvc.perform(post("/api/admin/users")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(5))
        .andExpect(jsonPath("$.username").value("newuser@test.com"))
        .andExpect(jsonPath("$.email").value("newuser@test.com"))
        .andExpect(jsonPath("$.firstName").value("New"))
        .andExpect(jsonPath("$.lastName").value("User"));

    verify(userService).createUser(anyString(), anyString(), anyString(), anyString(), anyString(), anyString(),
        anyString(), any(User.class));
  }

  @Test
  @WithMockUser(roles = { "PRESIDENT" })
  void testCreateUserWithInvalidData_ReturnsBadRequest() throws Exception {
    // Arrange
    CreateUserRequest request = new CreateUserRequest();
    request.setUsername(""); // Invalid: empty username
    request.setEmail("invalid-email"); // Invalid: bad email format
    request.setPassword("123"); // Invalid: too simple
    request.setRole("INVALID_ROLE"); // Invalid: non-existent role

    // Act & Assert
    mockMvc.perform(post("/api/admin/users")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());

    verify(userService, never()).createUser(anyString(), anyString(), anyString(), anyString(), anyString(),
        anyString(), anyString(), any(User.class));
  }

  @Test
  @WithMockUser(roles = { "PRESIDENT" })
  void testCreateUserWithNonExistentRole_ReturnsBadRequest() throws Exception {
    // Arrange
    CreateUserRequest request = new CreateUserRequest();
    request.setUsername("test@test.com");
    request.setEmail("test@test.com");
    request.setPassword("SecurePass123!");
    request.setRole("NONEXISTENT_ROLE");

    // Act & Assert
    mockMvc.perform(post("/api/admin/users")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());

    verify(userService, never()).createUser(anyString(), anyString(), anyString(), anyString(), anyString(),
        anyString(), anyString(), any(User.class));
  }

  @Test
  @WithMockUser(roles = { "SECRETARY" })
  void testSecretaryCannotCreateUser() throws Exception {
    // Arrange
    CreateUserRequest request = new CreateUserRequest();
    request.setUsername("test@test.com");
    request.setEmail("test@test.com");
    request.setPassword("SecurePass123!");
    request.setRole("MEMBER");

    // Act & Assert
    mockMvc.perform(post("/api/admin/users")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isForbidden());

    verify(userService, never()).createUser(anyString(), anyString(), anyString(), anyString(), anyString(),
        anyString(), anyString(), any(User.class));
  }

  @Test
  @WithMockUser(roles = { "PRESIDENT" })
  void testUpdateUserRole() throws Exception {
    // Arrange
    UpdateRoleRequest request = new UpdateRoleRequest();
    request.setRole("OFFICER");

    User updatedUser = new User();
    updatedUser.setId(2L);
    updatedUser.setUsername("secretary@test.com");
    updatedUser.setEmail("secretary@test.com");

    Role officerRole = new Role();
    officerRole.setRoleName("OFFICER");
    updatedUser.setRbacRole(officerRole);

    when(userService.updateUserRole(eq(2L), anyString(), any(User.class))).thenReturn(updatedUser);

    // Act & Assert
    mockMvc.perform(put("/api/admin/users/2/role")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(2));

    verify(userService).updateUserRole(eq(2L), anyString(), any(User.class));
  }

  @Test
  @WithMockUser(roles = { "SECRETARY" })
  void testSecretaryCannotUpdateUserRole() throws Exception {
    // Arrange
    UpdateRoleRequest request = new UpdateRoleRequest();
    request.setRole("MEMBER");

    // Act & Assert
    mockMvc.perform(put("/api/admin/users/2/role")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isForbidden());

    verify(userService, never()).updateUserRole(anyLong(), anyString(), any(User.class));
  }

  @Test
  @WithMockUser(roles = { "PRESIDENT" })
  void testSuspendUser() throws Exception {
    // Arrange
    when(userService.suspendUser(eq(3L), any(User.class))).thenReturn(testOfficer);

    // Act & Assert
    mockMvc.perform(put("/api/admin/users/3/suspend")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    verify(userService).suspendUser(eq(3L), any(User.class));
  }

  @Test
  @WithMockUser(roles = { "PRESIDENT" })
  void testSuspendNonExistentUser_ReturnsNotFound() throws Exception {
    // Arrange
    when(userService.suspendUser(eq(999L), any(User.class)))
        .thenThrow(new UserService.UserNotFoundException("User not found: 999"));

    // Act & Assert
    mockMvc.perform(put("/api/admin/users/999/suspend")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());
  }

  @Test
  @WithMockUser(roles = { "OFFICER" })
  void testOfficerCannotSuspendUser() throws Exception {
    // Act & Assert
    mockMvc.perform(put("/api/admin/users/3/suspend")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());

    verify(userService, never()).suspendUser(anyLong(), any(User.class));
  }

  @Test
  @WithMockUser(roles = { "PRESIDENT" })
  void testSoftDeleteUser() throws Exception {
    // Act & Assert
    mockMvc.perform(delete("/api/admin/users/4")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    verify(userService).deleteUser(eq(4L), any(User.class));
  }

  @Test
  @WithMockUser(roles = { "PRESIDENT" })
  void testSoftDeleteNonExistentUser_ReturnsNotFound() throws Exception {
    // Arrange
    doThrow(new UserService.UserNotFoundException("User not found: 999"))
        .when(userService).deleteUser(eq(999L), any(User.class));

    // Act & Assert
    mockMvc.perform(delete("/api/admin/users/999")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());
  }

  @Test
  @WithMockUser(roles = { "MEMBER" })
  void testMemberCannotDeleteUser() throws Exception {
    // Act & Assert
    mockMvc.perform(delete("/api/admin/users/4")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());

    verify(userService, never()).deleteUser(anyLong(), any(User.class));
  }

  @Test
  @WithMockUser(roles = { "PRESIDENT" })
  void testRoleUpdateWithAuditLogging() throws Exception {
    // Arrange
    UpdateRoleRequest request = new UpdateRoleRequest();
    request.setRole("SECRETARY");

    User updatedUser = new User();
    updatedUser.setId(3L);
    updatedUser.setUsername("officer@test.com");

    Role secretaryRole = new Role();
    secretaryRole.setRoleName("SECRETARY");
    updatedUser.setRbacRole(secretaryRole);

    when(userService.updateUserRole(eq(3L), anyString(), any(User.class))).thenReturn(updatedUser);

    // Act & Assert
    mockMvc.perform(put("/api/admin/users/3/role")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk());

    // Verify that service was called, which should trigger audit logging
    verify(userService).updateUserRole(eq(3L), anyString(), any(User.class));
  }

  @Test
  @WithMockUser(roles = { "PRESIDENT" })
  void testUserCreationValidation() throws Exception {
    // Test multiple validation scenarios
    String[] invalidEmails = { "", "invalid", "test@", "@test.com", "test..test@test.com" };
    String[] invalidUsernames = { "", "ab", "user@name", "user name" };

    for (String email : invalidEmails) {
      CreateUserRequest request = new CreateUserRequest();
      request.setUsername("validuser");
      request.setEmail(email);
      request.setPassword("SecurePass123!");
      request.setRole("MEMBER");

      mockMvc.perform(post("/api/admin/users")
          .with(csrf())
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest());
    }

    for (String username : invalidUsernames) {
      CreateUserRequest request = new CreateUserRequest();
      request.setUsername(username);
      request.setEmail("valid@test.com");
      request.setPassword("SecurePass123!");
      request.setRole("MEMBER");

      mockMvc.perform(post("/api/admin/users")
          .with(csrf())
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest());
    }
  }

  @Test
  @WithMockUser(roles = { "PRESIDENT" })
  void testPasswordValidationOnUserCreation() throws Exception {
    // Test password complexity requirements
    String[] weakPasswords = {
        "123456", // Only numbers, too simple
        "password", // Common word
        "Pass123", // Too short
        "NOlowercase1!", // No lowercase
        "nouppercase1!", // No uppercase
        "NoNumbers!", // No numbers
        "NoSymbols123" // No special characters
    };

    for (String password : weakPasswords) {
      CreateUserRequest request = new CreateUserRequest();
      request.setUsername("testuser");
      request.setEmail("test@test.com");
      request.setPassword(password);
      request.setRole("MEMBER");

      mockMvc.perform(post("/api/admin/users")
          .with(csrf())
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest());
    }
  }
}
