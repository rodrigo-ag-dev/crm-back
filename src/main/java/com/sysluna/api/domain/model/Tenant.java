package com.sysluna.api.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Registry of tenants (companies using the CRM). Always lives in the shared "public"
 * schema so it can be resolved before any tenant schema is known (e.g. at login).
 *
 * Only the short, human-chosen {@link #slug} is persisted - the tenant's actual Postgres
 * schema name is never stored, always computed on the fly from the slug (see
 * TenantSchemaNames.forSlug), so the "crm_" naming convention lives entirely in code.
 */
@Getter
@Setter
@Entity
@Table(name = "tenant", schema = "public")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tenant extends BaseModel {

  @Column(nullable = false, unique = true)
  private String slug;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  @Builder.Default
  private boolean active = true;
}
