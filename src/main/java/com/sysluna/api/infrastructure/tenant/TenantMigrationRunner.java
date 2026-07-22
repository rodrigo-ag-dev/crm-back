package com.sysluna.api.infrastructure.tenant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.sysluna.api.domain.model.Tenant;
import com.sysluna.api.infrastructure.repository.TenantRepository;

/**
 * Runs at application startup, after the global Flyway migration (db/migration/global)
 * has created public.tenant: ensures a default tenant exists (so a fresh install works
 * out of the box, matching the previous single-tenant setup flow) and applies the
 * tenant migration template to every registered tenant's physical schema (computed from
 * its slug via TenantSchemaNames.forSlug - never read off a stored column). Re-running
 * the tenant template on every boot is intentional and cheap - it's how newly added
 * tenant migrations reach already-provisioned tenants (the same pattern Flyway itself
 * uses for a single schema).
 */
@Component
@Order
public class TenantMigrationRunner implements ApplicationRunner {

  private final TenantRepository tenantRepository;
  private final TenantFlywayMigrator tenantFlywayMigrator;
  private final String defaultSlug;

  public TenantMigrationRunner(
      TenantRepository tenantRepository,
      TenantFlywayMigrator tenantFlywayMigrator,
      @Value("${app.tenant.default-slug}") String defaultSlug) {
    this.tenantRepository = tenantRepository;
    this.tenantFlywayMigrator = tenantFlywayMigrator;
    this.defaultSlug = defaultSlug;
  }

  @Override
  public void run(ApplicationArguments args) {
    if (tenantRepository.count() == 0) {
      tenantRepository.save(Tenant.builder()
          .slug(defaultSlug)
          .name("Default")
          .active(true)
          .build());
    }

    for (Tenant tenant : tenantRepository.findAll()) {
      tenantFlywayMigrator.migrate(TenantSchemaNames.forSlug(tenant.getSlug()));
    }
  }
}
