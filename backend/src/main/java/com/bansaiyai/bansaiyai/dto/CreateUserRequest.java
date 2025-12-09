package com.bansaiyai.bansaiyai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO for user creation requests.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserRequest {

  @NotBlank(message = "Username is required")
  @Size(min = 4, max = 50, message = "Username must be between 4 and 50 characters")
  @Pattern(regexp = "^[a-zA-Z0-9_@.-]+$", message = "Username can only contain letters, numbers, underscore, @, dot, and hyphen")
  private String username;

  @NotBlank(message = "Email is required")
  @Email(message = "Invalid email format")
  @Size(max = 100, message = "Email must not exceed 100 characters")
  private String email;

  @NotBlank(message = "Password is required")
  @Size(min = 8, max = 128, message = "Password must be between 8 and 128 characters")
  @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$", message = "Password must contain at least one lowercase letter, one uppercase letter, one digit, and one special character")
  private String password;

  @NotBlank(message = "First name is required")
  @Size(max = 50, message = "First name must not exceed 50 characters")
  private String firstName;

  @NotBlank(message = "Last name is required")
  @Size(max = 50, message = "Last name must not exceed 50 characters")
  private String lastName;

  @NotBlank(message = "Phone number is required")
  @Size(max = 20, message = "Phone number must not exceed 20 characters")
  private String phone;

  @NotBlank(message = "Role is required")
  @Pattern(regexp = "^(PRESIDENT|SECRETARY|OFFICER|MEMBER)$", message = "Role must be one of: PRESIDENT, SECRETARY, OFFICER, MEMBER")
  private String role;
}
