package com.bansaiyai.bansaiyai.repository;

import com.bansaiyai.bansaiyai.entity.User;
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
}
