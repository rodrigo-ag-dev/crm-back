package com.sysluna.api.infrastructure.tenant;

import java.util.Set;
import java.util.regex.Pattern;

/**
 * Validation shared by everything that turns a tenant-supplied slug into a literal
 * Postgres schema name. JDBC has no way to bind an identifier as a parameter (`SET
 * search_path TO ?` isn't valid syntax), so every schema name that reaches raw SQL -
 * here or in TenantProvisioningService - MUST be validated against this allowlist
 * first. Never interpolate an unvalidated string into schema-qualified SQL.
 */
public final class TenantSchemaNames {

  /**
   * Every tenant's Postgres schema is namespaced under this prefix (e.g. slug "acme" ->
   * schema "crm_acme"), so tenant schemas are easy to pick out from other schemas/apps
   * sharing the same Postgres instance. This prefix is applied ONLY here, in code - the
   * "crm_"-prefixed name is never persisted anywhere (Tenant only stores the bare slug);
   * every caller that needs the physical schema name (connection routing, Flyway
   * targeting) computes it fresh via {@link #forSlug} instead of reading it off a row.
   */
  public static final String TENANT_SCHEMA_PREFIX = "crm_";

  private static final Pattern SAFE_SLUG = Pattern.compile("^[a-z][a-z0-9_]{1,45}$");
  private static final Pattern SAFE_SCHEMA_NAME = Pattern.compile("^[a-z][a-z0-9_]{1,50}$");

  private static final Set<String> RESERVED = Set.of(
      "public", "pg_catalog", "information_schema", "pg_toast");

  private TenantSchemaNames() {
  }

  /** Computes a tenant's physical Postgres schema name from its (persisted) slug. */
  public static String forSlug(String slug) {
    if (slug == null || !SAFE_SLUG.matcher(slug).matches()) {
      throw new IllegalArgumentException("Invalid tenant slug: " + slug);
    }
    return TENANT_SCHEMA_PREFIX + slug;
  }

  public static boolean isValid(String schemaName) {
    return schemaName != null
        && SAFE_SCHEMA_NAME.matcher(schemaName).matches()
        && !RESERVED.contains(schemaName)
        && !schemaName.startsWith("pg_");
  }
}
