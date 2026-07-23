package com.sysluna.api.infrastructure.tenant;

import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Tells Hibernate which tenant schema to use for the current unit of work.
 *
 * Falls back to the default tenant's schema (computed from app.tenant.default-slug via
 * TenantSchemaNames.forSlug) when no TenantContext is set (e.g. code that touches
 * tenant-scoped repositories outside an authenticated HTTP request - startup jobs, tests
 * calling repositories directly). This is safe: every authenticated business request has
 * TenantContext set by JwtAuthenticationFilter before any tenant-scoped repository runs,
 * so the fallback never fires on real traffic. Global entities (Tenant, User) are
 * unaffected either way since they're pinned to the identity schema via
 * @Table(schema = TenantSchemaNames.IDENTITY_SCHEMA).
 */
@Component
public class TenantIdentifierResolver implements CurrentTenantIdentifierResolver<String> {

  private final String defaultSchema;

  public TenantIdentifierResolver(@Value("${app.tenant.default-slug}") String defaultSlug) {
    this.defaultSchema = TenantSchemaNames.forSlug(defaultSlug);
  }

  @Override
  public String resolveCurrentTenantIdentifier() {
    String schema = TenantContext.get();
    return schema != null ? schema : defaultSchema;
  }

  @Override
  public boolean validateExistingCurrentSessions() {
    return true;
  }
}
