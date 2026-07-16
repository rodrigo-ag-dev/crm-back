package com.sysluna.api.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Request to register a new user")
public class RegisterRequest {

  @NotBlank(message = "Username is required")
  @Schema(description = "Username", example = "joao_silva")
  private String username;

  @NotBlank(message = "Full name is required")
  @Schema(description = "User full name", example = "John Silva")
  private String fullName;

  @NotBlank(message = "Email is required")
  @Email(message = "Email must be valid")
  @Schema(description = "User email", example = "john@example.com")
  private String email;

  @NotBlank(message = "Password is required")
  @Schema(description = "Password (minimum 6 characters)", example = "password123", minLength = 6)
  private String password;
}

