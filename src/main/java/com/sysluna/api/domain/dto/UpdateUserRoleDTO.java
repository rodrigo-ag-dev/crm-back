package com.sysluna.api.domain.dto;

import com.sysluna.api.domain.model.Role;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserRoleDTO {
  @NotNull(message = "Role is required")
  private Role role;
}
