package com.sysluna.api.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AuthDTO {
  @NotBlank(message = "Username is required")
  @Schema(description = "Username", example = "joao_silva")
  private String username;

  @NotBlank(message = "Full name is required")
  @Schema(description = "User full name", example = "John Silva")
  private String fullName;

  @NotBlank(message = "Email is required")
  @Schema(description = "User email", example = "john@example.com")
  private String email;

  @NotBlank(message = "Password is required")
  @Schema(description = "User password", example = "password123")
  private String password;

}
