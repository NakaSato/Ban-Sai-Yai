package com.bansaiyai.bansaiyai.repository;

import com.bansaiyai.bansaiyai.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

  Optional<User> findByUsername(String username);

  Optional<User> findByEmail(String email);

  Boolean existsByUsername(String username);

  Boolean existsByEmail(String email);

  @Query("SELECT u FROM User u WHERE u.username = :username AND u.enabled = true")
  Optional<User> findActiveUserByUsername(@Param("username") String username);

  @Query("SELECT u FROM User u WHERE u.email = :email AND u.enabled = true")
  Optional<User> findActiveUserByEmail(@Param("email") String email);

  Optional<User> findByPasswordResetToken(String token);

  /**
   * Find all users that have not been soft-deleted.
   *
   * @param pageable pagination parameters
   * @return a page of active (non-deleted) users
   */
  Page<User> findByDeletedAtIsNull(Pageable pageable);

  /**
   * Find all users by status.
   *
   * @param status   the user status to filter by
   * @param pageable pagination parameters
   * @return a page of users with the specified status
   */
  Page<User> findByStatus(User.UserStatus status, Pageable pageable);

  /**
   * Find all users by role.
   *
   * @param rbacRole the RBAC role to filter by
   * @param pageable pagination parameters
   * @return a page of users with the specified role
   */
  Page<User> findByRbacRole(com.bansaiyai.bansaiyai.entity.Role rbacRole, Pageable pageable);
}
