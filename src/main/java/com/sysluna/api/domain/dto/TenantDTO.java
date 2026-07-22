package com.sysluna.api.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class TenantDTO {
  private String id;
  private String name;
  private String slug;

  @Schema(description = "The tenant's actual Postgres schema name, computed from slug for display only - "
      + "not persisted anywhere as such. Use slug, not this value, in tenantSlug fields elsewhere.")
  private String schemaName;

  private boolean active;
}
