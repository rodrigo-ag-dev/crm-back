package com.sysluna.api.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "\"user\"", schema = "public")
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseModel {
  @Column(name = "tenant_id", nullable = false)
  private String tenantId;

  // Unique per tenant, not globally (see UNIQUE (tenant_id, username) in the global migration).
  @Column(nullable = false)
  private String username;

  @Column(name = "full_name", nullable = false)
  private String fullName;

  @Column(nullable = false, unique = true)
  private String email;

  @Column(name = "password_hash", nullable = false)
  private String passwordHash;

  @Transient
  private String password;

  @Column(nullable = false)
  @JsonIgnore
  private boolean active = true;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Role role = Role.USER;

  @Column(name = "must_change_password", nullable = false)
  private boolean mustChangePassword = false;

  // Cross-tenant reach, independent of role (still ADMIN/USER within their own tenant).
  // No API grants this - see the V3 migration that adds this column.
  @Column(name = "platform_admin", nullable = false)
  private boolean platformAdmin = false;
}
