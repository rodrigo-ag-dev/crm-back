package com.sysluna.api.domain.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserRequest {

  /** Ignored unless the caller is a platform admin. */
  private String tenantId;

  @NotBlank(message = "Username is required")
  private String username;

  @NotBlank(message = "Full name is required")
  private String fullName;

  @NotBlank(message = "Email is required")
  @Email(message = "Email must be valid")
  private String email;
}
