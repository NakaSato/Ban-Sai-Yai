package com.bansaiyai.bansaiyai.service;

import com.bansaiyai.bansaiyai.entity.Permission;
import com.bansaiyai.bansaiyai.entity.Role;
import com.bansaiyai.bansaiyai.entity.User;
import com.bansaiyai.bansaiyai.repository.PermissionRepository;
import com.bansaiyai.bansaiyai.repository.RolePermissionRepository;
import com.bansaiyai.bansaiyai.repository.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for RolePermissionService.
 * Tests specific examples and edge cases for role-permission management.
 * 
 * Requirements: 2.1, 2.2, 2.3, 2.4
 */
@ExtendWith(MockitoExtension.class)
class RolePermissionServiceTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PermissionRepository permissionRepository;

    @Mock
    private RolePermissionRepository rolePermissionRepository;

    @InjectMocks
    private RolePermissionService rolePermissionService;

    private Role officerRole;
    private Role secretaryRole;
    private Permission transactionCreatePerm;
    private Permission transactionViewPerm;
    private Permission loanApprovePerm;

    @BeforeEach
    void setUp() {
        // Setup Officer role with permissions
        officerRole = new Role();
        officerRole.setRoleId(1);
        officerRole.setRoleName("ROLE_OFFICER");
        officerRole.setDescription("Officer role");
        
        transactionCreatePerm = new Permission();
        transactionCreatePerm.setPermId(1);
        transactionCreatePerm.setPermSlug("transaction.create");
        transactionCreatePerm.setModule("Transactions");
        
        transactionViewPerm = new Permission();
        transactionViewPerm.setPermId(2);
        transactionViewPerm.setPermSlug("transaction.view");
        transactionViewPerm.setModule("Transactions");
        
        Set<Permission> officerPermissions = new HashSet<>();
        officerPermissions.add(transactionCreatePerm);
        officerPermissions.add(transactionViewPerm);
        officerRole.setPermissions(officerPermissions);
        
        // Setup Secretary role
        secretaryRole = new Role();
        secretaryRole.setRoleId(2);
        secretaryRole.setRoleName("ROLE_SECRETARY");
        secretaryRole.setDescription("Secretary role");
        
        loanApprovePerm = new Permission();
        loanApprovePerm.setPermId(3);
        loanApprovePerm.setPermSlug("loan.approve");
        loanApprovePerm.setModule("Loans");
        
        Set<Permission> secretaryPermissions = new HashSet<>();
        secretaryPermissions.add(transactionViewPerm);
        secretaryPermissions.add(loanApprovePerm);
        secretaryRole.setPermissions(secretaryPermissions);
    }

    /**
     * Test: getPermissionsForRole returns correct set of permissions
     * Requirement: 2.1
     */
    @Test
    void shouldReturnPermissionsForOfficerRole() {
        // Given
        when(roleRepository.findByRoleName("ROLE_OFFICER"))
                .thenReturn(Optional.of(officerRole));
        
        // When
        Set<String> permissions = rolePermissionService.getPermissionsForRole("ROLE_OFFICER");
        
        // Then
        assertNotNull(permissions);
        assertEquals(2, permissions.size());
        assertTrue(permissions.contains("transaction.create"));
        assertTrue(permissions.contains("transaction.view"));
        
        verify(roleRepository, times(1)).findByRoleName("ROLE_OFFICER");
    }

    /**
     * Test: getPermissionsForRole throws exception for non-existent role
     * Requirement: 2.1
     */
    @Test
    void shouldThrowExceptionForNonExistentRole() {
        // Given
        when(roleRepository.findByRoleName("ROLE_INVALID"))
                .thenReturn(Optional.empty());
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> rolePermissionService.getPermissionsForRole("ROLE_INVALID")
        );
        
        assertTrue(exception.getMessage().contains("Role not found"));
        verify(roleRepository, times(1)).findByRoleName("ROLE_INVALID");
    }

    /**
     * Test: hasPermission returns true when user has the permission
     * Requirement: 2.2
     */
    @Test
    void shouldReturnTrueWhenUserHasPermission() {
        // Given
        User user = new User();
        user.setUsername("officer1");
        user.setRbacRole(officerRole);
        
        when(roleRepository.findByRoleName("ROLE_OFFICER"))
                .thenReturn(Optional.of(officerRole));
        
        // When
        boolean hasPermission = rolePermissionService.hasPermission(user, "transaction.create");
        
        // Then
        assertTrue(hasPermission);
    }

    /**
     * Test: hasPermission returns false when user lacks the permission
     * Requirement: 2.2
     */
    @Test
    void shouldReturnFalseWhenUserLacksPermission() {
        // Given
        User user = new User();
        user.setUsername("officer1");
        user.setRbacRole(officerRole);
        
        when(roleRepository.findByRoleName("ROLE_OFFICER"))
                .thenReturn(Optional.of(officerRole));
        
        // When
        boolean hasPermission = rolePermissionService.hasPermission(user, "loan.approve");
        
        // Then
        assertFalse(hasPermission);
    }

    /**
     * Test: hasPermission returns false when user is null
     * Requirement: 2.2
     */
    @Test
    void shouldReturnFalseWhenUserIsNull() {
        // When
        boolean hasPermission = rolePermissionService.hasPermission(null, "transaction.create");
        
        // Then
        assertFalse(hasPermission);
    }

    /**
     * Test: hasPermission returns false when user role is null
     * Requirement: 2.2
     */
    @Test
    void shouldReturnFalseWhenUserRoleIsNull() {
        // Given
        User user = new User();
        user.setUsername("user1");
        user.setRbacRole(null);
        
        // When
        boolean hasPermission = rolePermissionService.hasPermission(user, "transaction.create");
        
        // Then
        assertFalse(hasPermission);
    }

    /**
     * Test: addPermissionToRole successfully adds permission
     * Requirement: 2.3
     */
    @Test
    void shouldAddPermissionToRole() {
        // Given
        Permission newPermission = new Permission();
        newPermission.setPermId(4);
        newPermission.setPermSlug("member.create");
        newPermission.setModule("Members");
        
        when(roleRepository.findByRoleName("ROLE_OFFICER"))
                .thenReturn(Optional.of(officerRole));
        when(permissionRepository.findByPermSlug("member.create"))
                .thenReturn(Optional.of(newPermission));
        when(roleRepository.save(any(Role.class)))
                .thenReturn(officerRole);
        
        // When
        rolePermissionService.addPermissionToRole("ROLE_OFFICER", "member.create");
        
        // Then
        verify(roleRepository, times(1)).findByRoleName("ROLE_OFFICER");
        verify(permissionRepository, times(1)).findByPermSlug("member.create");
        verify(roleRepository, times(1)).save(officerRole);
        assertTrue(officerRole.getPermissions().contains(newPermission));
    }

    /**
     * Test: addPermissionToRole throws exception for non-existent role
     * Requirement: 2.3
     */
    @Test
    void shouldThrowExceptionWhenAddingPermissionToNonExistentRole() {
        // Given
        when(roleRepository.findByRoleName("ROLE_INVALID"))
                .thenReturn(Optional.empty());
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> rolePermissionService.addPermissionToRole("ROLE_INVALID", "member.create")
        );
        
        assertTrue(exception.getMessage().contains("Role not found"));
        verify(roleRepository, times(1)).findByRoleName("ROLE_INVALID");
        verify(permissionRepository, never()).findByPermSlug(any());
        verify(roleRepository, never()).save(any());
    }

    /**
     * Test: addPermissionToRole throws exception for non-existent permission
     * Requirement: 2.3
     */
    @Test
    void shouldThrowExceptionWhenAddingNonExistentPermission() {
        // Given
        when(roleRepository.findByRoleName("ROLE_OFFICER"))
                .thenReturn(Optional.of(officerRole));
        when(permissionRepository.findByPermSlug("invalid.permission"))
                .thenReturn(Optional.empty());
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> rolePermissionService.addPermissionToRole("ROLE_OFFICER", "invalid.permission")
        );
        
        assertTrue(exception.getMessage().contains("Permission not found"));
        verify(roleRepository, times(1)).findByRoleName("ROLE_OFFICER");
        verify(permissionRepository, times(1)).findByPermSlug("invalid.permission");
        verify(roleRepository, never()).save(any());
    }

    /**
     * Test: removePermissionFromRole successfully removes permission
     * Requirement: 2.4
     */
    @Test
    void shouldRemovePermissionFromRole() {
        // Given
        when(roleRepository.findByRoleName("ROLE_OFFICER"))
                .thenReturn(Optional.of(officerRole));
        when(permissionRepository.findByPermSlug("transaction.create"))
                .thenReturn(Optional.of(transactionCreatePerm));
        when(roleRepository.save(any(Role.class)))
                .thenReturn(officerRole);
        
        // When
        rolePermissionService.removePermissionFromRole("ROLE_OFFICER", "transaction.create");
        
        // Then
        verify(roleRepository, times(1)).findByRoleName("ROLE_OFFICER");
        verify(permissionRepository, times(1)).findByPermSlug("transaction.create");
        verify(roleRepository, times(1)).save(officerRole);
        assertFalse(officerRole.getPermissions().contains(transactionCreatePerm));
    }

    /**
     * Test: removePermissionFromRole throws exception for non-existent role
     * Requirement: 2.4
     */
    @Test
    void shouldThrowExceptionWhenRemovingPermissionFromNonExistentRole() {
        // Given
        when(roleRepository.findByRoleName("ROLE_INVALID"))
                .thenReturn(Optional.empty());
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> rolePermissionService.removePermissionFromRole("ROLE_INVALID", "transaction.create")
        );
        
        assertTrue(exception.getMessage().contains("Role not found"));
        verify(roleRepository, times(1)).findByRoleName("ROLE_INVALID");
        verify(permissionRepository, never()).findByPermSlug(any());
        verify(roleRepository, never()).save(any());
    }

    /**
     * Test: getRolePermissionMatrix returns complete matrix
     * Requirement: 2.1
     */
    @Test
    void shouldReturnCompleteRolePermissionMatrix() {
        // Given
        List<Role> allRoles = Arrays.asList(officerRole, secretaryRole);
        when(roleRepository.findAll()).thenReturn(allRoles);
        
        // When
        Map<String, Set<String>> matrix = rolePermissionService.getRolePermissionMatrix();
        
        // Then
        assertNotNull(matrix);
        assertEquals(2, matrix.size());
        
        assertTrue(matrix.containsKey("ROLE_OFFICER"));
        assertTrue(matrix.containsKey("ROLE_SECRETARY"));
        
        Set<String> officerPerms = matrix.get("ROLE_OFFICER");
        assertEquals(2, officerPerms.size());
        assertTrue(officerPerms.contains("transaction.create"));
        assertTrue(officerPerms.contains("transaction.view"));
        
        Set<String> secretaryPerms = matrix.get("ROLE_SECRETARY");
        assertEquals(2, secretaryPerms.size());
        assertTrue(secretaryPerms.contains("transaction.view"));
        assertTrue(secretaryPerms.contains("loan.approve"));
        
        verify(roleRepository, times(1)).findAll();
    }

    /**
     * Test: getRolePermissionMatrix returns empty map when no roles exist
     * Requirement: 2.1
     */
    @Test
    void shouldReturnEmptyMatrixWhenNoRolesExist() {
        // Given
        when(roleRepository.findAll()).thenReturn(Collections.emptyList());
        
        // When
        Map<String, Set<String>> matrix = rolePermissionService.getRolePermissionMatrix();
        
        // Then
        assertNotNull(matrix);
        assertTrue(matrix.isEmpty());
        verify(roleRepository, times(1)).findAll();
    }

    /**
     * Test: Cache invalidation on permission addition
     * Requirement: 2.3
     */
    @Test
    void shouldInvalidateCacheOnPermissionAddition() {
        // Given
        Permission newPermission = new Permission();
        newPermission.setPermId(4);
        newPermission.setPermSlug("member.create");
        
        when(roleRepository.findByRoleName("ROLE_OFFICER"))
                .thenReturn(Optional.of(officerRole));
        when(permissionRepository.findByPermSlug("member.create"))
                .thenReturn(Optional.of(newPermission));
        when(roleRepository.save(any(Role.class)))
                .thenReturn(officerRole);
        
        // When
        rolePermissionService.addPermissionToRole("ROLE_OFFICER", "member.create");
        
        // Then - verify that the method was called (cache should be invalidated)
        verify(roleRepository, times(1)).save(officerRole);
        
        // Subsequent call should fetch fresh data
        when(roleRepository.findByRoleName("ROLE_OFFICER"))
                .thenReturn(Optional.of(officerRole));
        
        Set<String> permissions = rolePermissionService.getPermissionsForRole("ROLE_OFFICER");
        assertTrue(permissions.contains("member.create"));
    }

    /**
     * Test: Cache invalidation on permission removal
     * Requirement: 2.4
     */
    @Test
    void shouldInvalidateCacheOnPermissionRemoval() {
        // Given
        when(roleRepository.findByRoleName("ROLE_OFFICER"))
                .thenReturn(Optional.of(officerRole));
        when(permissionRepository.findByPermSlug("transaction.create"))
                .thenReturn(Optional.of(transactionCreatePerm));
        when(roleRepository.save(any(Role.class)))
                .thenReturn(officerRole);
        
        // When
        rolePermissionService.removePermissionFromRole("ROLE_OFFICER", "transaction.create");
        
        // Then - verify that the method was called (cache should be invalidated)
        verify(roleRepository, times(1)).save(officerRole);
        
        // Subsequent call should fetch fresh data
        when(roleRepository.findByRoleName("ROLE_OFFICER"))
                .thenReturn(Optional.of(officerRole));
        
        Set<String> permissions = rolePermissionService.getPermissionsForRole("ROLE_OFFICER");
        assertFalse(permissions.contains("transaction.create"));
    }
}
