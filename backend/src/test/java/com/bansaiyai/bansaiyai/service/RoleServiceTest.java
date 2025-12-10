package com.bansaiyai.bansaiyai.service;

import com.bansaiyai.bansaiyai.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class RoleServiceTest {

  private RoleService roleService;

  @BeforeEach
  void setUp() {
    roleService = new RoleService();
  }

  @Test
  void testGetAllRoles() {
    List<User.Role> roles = roleService.getAllRoles();

    assertNotNull(roles);
    assertEquals(4, roles.size());
    assertTrue(roles.contains(User.Role.PRESIDENT));
    assertTrue(roles.contains(User.Role.SECRETARY));
    assertTrue(roles.contains(User.Role.OFFICER));
    assertTrue(roles.contains(User.Role.MEMBER));
  }

  @Test
  void testGetRoleHierarchy() {
    List<User.Role> hierarchy = roleService.getRoleHierarchy();

    assertNotNull(hierarchy);
    assertEquals(4, hierarchy.size());
    assertEquals(User.Role.PRESIDENT, hierarchy.get(0));
    assertEquals(User.Role.SECRETARY, hierarchy.get(1));
    assertEquals(User.Role.OFFICER, hierarchy.get(2));
    assertEquals(User.Role.MEMBER, hierarchy.get(3));
  }

  @Test
  void testGetRolePermissions() {

    // Test PRESIDENT permissions
    Set<String> presidentPermissions = roleService.getRolePermissions(User.Role.PRESIDENT);
    assertNotNull(presidentPermissions);
    assertTrue(presidentPermissions.contains("MEMBER_DELETE"));
    assertTrue(presidentPermissions.contains("LOAN_APPROVE"));
    assertFalse(presidentPermissions.contains("SYSTEM_CONFIG"));

    // Test MEMBER permissions
    Set<String> memberPermissions = roleService.getRolePermissions(User.Role.MEMBER);
    assertNotNull(memberPermissions);
    assertTrue(memberPermissions.contains("MEMBER_READ_SELF"));
    assertFalse(memberPermissions.contains("MEMBER_READ"));
    assertFalse(memberPermissions.contains("MEMBER_DELETE"));
  }

  @Test
  void testGetRoleDescription() {

    assertEquals("President - Organization leadership with full operational access",
        roleService.getRoleDescription(User.Role.PRESIDENT));
    assertEquals("Secretary - Record keeping and reporting responsibilities",
        roleService.getRoleDescription(User.Role.SECRETARY));
    assertEquals("Officer - Daily operations and member services",
        roleService.getRoleDescription(User.Role.OFFICER));
    assertEquals("Member - Basic access to own information",
        roleService.getRoleDescription(User.Role.MEMBER));
  }

  @Test
  void testCanManageRole() {

    // PRESIDENT can manage lower roles
    assertTrue(roleService.canManageRole(User.Role.PRESIDENT, User.Role.SECRETARY));
    assertTrue(roleService.canManageRole(User.Role.PRESIDENT, User.Role.OFFICER));
    assertTrue(roleService.canManageRole(User.Role.PRESIDENT, User.Role.MEMBER));
    assertFalse(roleService.canManageRole(User.Role.PRESIDENT, User.Role.PRESIDENT));

    // MEMBER cannot manage any roles
    assertFalse(roleService.canManageRole(User.Role.MEMBER, User.Role.OFFICER));
    assertFalse(roleService.canManageRole(User.Role.MEMBER, User.Role.MEMBER));
  }

  @Test
  void testGetAllPermissions() {
    Set<String> allPermissions = roleService.getAllPermissions();

    assertNotNull(allPermissions);
    assertTrue(allPermissions.size() > 0);
    assertTrue(allPermissions.contains("MEMBER_READ"));
    assertTrue(allPermissions.contains("LOAN_APPROVE"));
    assertTrue(allPermissions.contains("SYSTEM_CONFIG"));
    assertTrue(allPermissions.contains("USER_MANAGEMENT"));
  }

  @Test
  void testGetRolesWithPermission() {
    // Test permission that multiple roles have
    List<User.Role> memberReadRoles = roleService.getRolesWithPermission("MEMBER_READ");
    assertTrue(memberReadRoles.contains(User.Role.PRESIDENT));
    assertTrue(memberReadRoles.contains(User.Role.SECRETARY));
    assertTrue(memberReadRoles.contains(User.Role.OFFICER));
    assertFalse(memberReadRoles.contains(User.Role.MEMBER));
  }

  @Test
  void testIsValidRoleAssignment() {
    // PRESIDENT can assign any role (except themselves potentially, but validation
    // logic might differ)
    assertTrue(roleService.isValidRoleAssignment(User.Role.PRESIDENT, User.Role.SECRETARY));

    // SECRETARY can assign OFFICER and MEMBER
    assertTrue(roleService.isValidRoleAssignment(User.Role.SECRETARY, User.Role.OFFICER));
    assertTrue(roleService.isValidRoleAssignment(User.Role.SECRETARY, User.Role.MEMBER));
    assertFalse(roleService.isValidRoleAssignment(User.Role.SECRETARY, User.Role.SECRETARY));
    assertFalse(roleService.isValidRoleAssignment(User.Role.SECRETARY, User.Role.PRESIDENT));

    // OFFICER can only assign MEMBER
    assertTrue(roleService.isValidRoleAssignment(User.Role.OFFICER, User.Role.MEMBER));
    assertFalse(roleService.isValidRoleAssignment(User.Role.OFFICER, User.Role.OFFICER));
    assertFalse(roleService.isValidRoleAssignment(User.Role.OFFICER, User.Role.SECRETARY));

    // MEMBER cannot assign any roles
    assertFalse(roleService.isValidRoleAssignment(User.Role.MEMBER, User.Role.MEMBER));
    assertFalse(roleService.isValidRoleAssignment(User.Role.MEMBER, User.Role.OFFICER));
  }

  @Test
  void testRegisterAllRoles() {
    // This test just ensures the method doesn't throw exceptions
    assertDoesNotThrow(() -> roleService.registerAllRoles());
  }
}
