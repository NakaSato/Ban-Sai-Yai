package com.bansaiyai.bansaiyai.repository;

import com.bansaiyai.bansaiyai.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Permission entity operations.
 * Provides methods for querying permissions in the RBAC system.
 */
@Repository
public interface PermissionRepository extends JpaRepository<Permission, Integer> {

  /**
   * Find a permission by its permission slug.
   *
   * @param permSlug the permission slug (e.g., "loan.approve", "cash.entry")
   * @return an Optional containing the permission if found
   */
  Optional<Permission> findByPermSlug(String permSlug);

  /**
   * Check if a permission exists by its permission slug.
   *
   * @param permSlug the permission slug
   * @return true if the permission exists, false otherwise
   */
  boolean existsByPermSlug(String permSlug);

  /**
   * Find all permissions for a specific module.
   *
   * @param module the module name (e.g., "Loans", "Transactions")
   * @return a list of permissions for the module
   */
  List<Permission> findByModule(String module);

  /**
   * Find permissions by multiple permission slugs.
   *
   * @param permSlugs the list of permission slugs
   * @return a list of permissions matching the slugs
   */
  @Query("SELECT p FROM Permission p WHERE p.permSlug IN :permSlugs")
  List<Permission> findByPermSlugIn(@Param("permSlugs") List<String> permSlugs);
}
