package com.bansaiyai.bansaiyai.repository;

import com.bansaiyai.bansaiyai.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for Role entity operations.
 * Provides methods for querying roles in the RBAC system.
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {

  /**
   * Find a role by its role name.
   *
   * @param roleName the name of the role (e.g., "ROLE_OFFICER", "ROLE_SECRETARY")
   * @return an Optional containing the role if found
   */
  Optional<Role> findByRoleName(String roleName);

  /**
   * Check if a role exists by its role name.
   *
   * @param roleName the name of the role
   * @return true if the role exists, false otherwise
   */
  boolean existsByRoleName(String roleName);
}
