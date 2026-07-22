package com.sysluna.api.domain.dto;

import com.sysluna.api.domain.model.Role;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDTO {
  private String id;
  private String username;
  private String fullName;
  private String email;
  private Role role;
  private boolean active;
  private boolean mustChangePassword;
  private String tenantId;
  private String tenantName;
  private boolean platformAdmin;

  public UserDTO(String id, String username, String fullName, String email, Role role, boolean active,
      boolean mustChangePassword, String tenantId, String tenantName, boolean platformAdmin) {
    this.id = id;
    this.username = username;
    this.fullName = fullName;
    this.email = email;
    this.role = role;
    this.active = active;
    this.mustChangePassword = mustChangePassword;
    this.tenantId = tenantId;
    this.tenantName = tenantName;
    this.platformAdmin = platformAdmin;
  }
}
