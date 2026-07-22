package com.sysluna.api.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Request to provision a new tenant (company) with its own isolated schema")
public class CreateTenantRequest {

  @NotBlank(message = "Name is required")
  @Schema(description = "Tenant display name", example = "Acme Ltda")
  private String name;

  @NotBlank(message = "Slug is required")
  @Pattern(
      regexp = "^[a-z][a-z0-9_]{1,45}$",
      message = "Slug must start with a lowercase letter and contain only lowercase letters, digits and underscores")
  @Schema(description = "Short, unique tenant identifier. This exact value is what's used as tenantSlug "
      + "elsewhere (POST /api/setup, POST /api/auth/register). The tenant's Postgres schema name "
      + "(\"crm_<slug>\") is derived from it automatically and never needs to be typed anywhere.",
      example = "acme")
  private String slug;
}
