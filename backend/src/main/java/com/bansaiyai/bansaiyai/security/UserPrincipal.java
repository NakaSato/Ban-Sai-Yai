package com.bansaiyai.bansaiyai.security;

import com.bansaiyai.bansaiyai.entity.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class UserPrincipal implements UserDetails {

  private Long id;
  private String username;
  private String email;

  @JsonIgnore
  private String password;

  private Collection<? extends GrantedAuthority> authorities;
  private User.Role role;
  
  @JsonIgnore
  private User user;

  public UserPrincipal(Long id, String username, String email, String password,
      Collection<? extends GrantedAuthority> authorities, User.Role role) {
    this.id = id;
    this.username = username;
    this.email = email;
    this.password = password;
    this.authorities = authorities;
    this.role = role;
  }
  
  public UserPrincipal(Long id, String username, String email, String password,
      Collection<? extends GrantedAuthority> authorities, User.Role role, User user) {
    this.id = id;
    this.username = username;
    this.email = email;
    this.password = password;
    this.authorities = authorities;
    this.role = role;
    this.user = user;
  }

  public static UserPrincipal create(User user) {
    List<GrantedAuthority> authorities = user.getPermissions().stream()
        .map(permission -> new SimpleGrantedAuthority(permission))
        .collect(Collectors.toList());

    // Add role as authority
    authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));

    return new UserPrincipal(
        user.getId(),
        user.getUsername(),
        user.getEmail(),
        user.getPassword(),
        authorities,
        user.getRole(),
        user);
  }

  public Long getId() {
    return id;
  }

  public String getEmail() {
    return email;
  }

  public User.Role getRole() {
    return role;
  }
  
  public User getUser() {
    return user;
  }

  @Override
  public String getUsername() {
    return username;
  }

  @Override
  public String getPassword() {
    return password;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return authorities;
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }
}
