package com.sysluna.api.infrastructure.security;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import lombok.Getter;

@Getter
public class AppUserPrincipal extends User {

  private final boolean mustChangePassword;
  private final String tenantId;
  private final String tenantSchema;

  public AppUserPrincipal(
      String email,
      String passwordHash,
      boolean enabled,
      boolean mustChangePassword,
      String tenantId,
      String tenantSchema,
      Collection<? extends GrantedAuthority> authorities) {
    super(email, passwordHash, enabled, true, true, true, authorities);
    this.mustChangePassword = mustChangePassword;
    this.tenantId = tenantId;
    this.tenantSchema = tenantSchema;
  }
}
