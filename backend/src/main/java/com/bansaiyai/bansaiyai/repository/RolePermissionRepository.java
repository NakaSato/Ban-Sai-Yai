package com.bansaiyai.bansaiyai.repository;

import com.bansaiyai.bansaiyai.entity.Permission;
import com.bansaiyai.bansaiyai.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

/**
 * Repository for managing the role_permissions junction table.
 * Provides methods for querying and manipulating role-permission relationships.
 */
@Repository
public interface RolePermissionRepository extends JpaRepository<Role, Integer> {

  /**
   * Get all permissions for a specific role.
   *
   * @param roleId the ID of the role
   * @return a set of permissions associated with the role
   */
  @Query("SELECT r.permissions FROM Role r WHERE r.roleId = :roleId")
  Set<Permission> findPermissionsByRoleId(@Param("roleId") Integer roleId);

  /**
   * Get all permissions for a specific role by role name.
   *
   * @param roleName the name of the role
   * @return a set of permissions associated with the role
   */
  @Query("SELECT r.permissions FROM Role r WHERE r.roleName = :roleName")
  Set<Permission> findPermissionsByRoleName(@Param("roleName") String roleName);

  /**
   * Get all roles that have a specific permission.
   *
   * @param permissionId the ID of the permission
   * @return a list of roles that have the permission
   */
  @Query("SELECT r FROM Role r JOIN r.permissions p WHERE p.permId = :permissionId")
  List<Role> findRolesByPermissionId(@Param("permissionId") Integer permissionId);

  /**
   * Check if a role has a specific permission.
   *
   * @param roleId the ID of the role
   * @param permissionId the ID of the permission
   * @return true if the role has the permission, false otherwise
   */
  @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END " +
         "FROM Role r JOIN r.permissions p " +
         "WHERE r.roleId = :roleId AND p.permId = :permissionId")
  boolean roleHasPermission(@Param("roleId") Integer roleId, @Param("permissionId") Integer permissionId);

  /**
   * Add a permission to a role.
   * Note: This is typically handled through the Role entity's permissions set,
   * but this method provides a direct database operation if needed.
   *
   * @param roleId the ID of the role
   * @param permissionId the ID of the permission
   */
  @Modifying
  @Query(value = "INSERT INTO role_permissions (role_id, perm_id) VALUES (:roleId, :permissionId) " +
                 "ON DUPLICATE KEY UPDATE role_id = role_id", nativeQuery = true)
  void addPermissionToRole(@Param("roleId") Integer roleId, @Param("permissionId") Integer permissionId);

  /**
   * Remove a permission from a role.
   *
   * @param roleId the ID of the role
   * @param permissionId the ID of the permission
   */
  @Modifying
  @Query(value = "DELETE FROM role_permissions WHERE role_id = :roleId AND perm_id = :permissionId", 
         nativeQuery = true)
  void removePermissionFromRole(@Param("roleId") Integer roleId, @Param("permissionId") Integer permissionId);

  /**
   * Remove all permissions from a role.
   *
   * @param roleId the ID of the role
   */
  @Modifying
  @Query(value = "DELETE FROM role_permissions WHERE role_id = :roleId", nativeQuery = true)
  void removeAllPermissionsFromRole(@Param("roleId") Integer roleId);

  /**
   * Get the count of permissions for a role.
   *
   * @param roleId the ID of the role
   * @return the number of permissions associated with the role
   */
  @Query("SELECT COUNT(p) FROM Role r JOIN r.permissions p WHERE r.roleId = :roleId")
  long countPermissionsByRoleId(@Param("roleId") Integer roleId);
}
