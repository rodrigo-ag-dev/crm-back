package com.sysluna.api.infrastructure.security;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import lombok.Getter;

@Getter
public class AppUserPrincipal extends User {

  private final boolean mustChangePassword;

  public AppUserPrincipal(
      String email,
      String passwordHash,
      boolean enabled,
      boolean mustChangePassword,
      Collection<? extends GrantedAuthority> authorities) {
    super(email, passwordHash, enabled, true, true, true, authorities);
    this.mustChangePassword = mustChangePassword;
  }
}
