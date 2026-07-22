package com.sysluna.api.application;

import org.springframework.stereotype.Service;

import com.sysluna.api.domain.dto.CreateTenantRequest;
import com.sysluna.api.domain.dto.TenantDTO;
import com.sysluna.api.domain.exception.BusinessException;
import com.sysluna.api.domain.model.Tenant;
import com.sysluna.api.infrastructure.repository.TenantRepository;
import com.sysluna.api.infrastructure.tenant.TenantFlywayMigrator;
import com.sysluna.api.infrastructure.tenant.TenantSchemaNames;

/**
 * Creates new tenants: registers them (by slug) in the shared public.tenant table, then
 * provisions their dedicated Postgres schema (created automatically by Flyway, named
 * "crm_<slug>" - see TenantSchemaNames.forSlug) and applies the tenant migration
 * template to it.
 *
 * Idempotent by slug: if a tenant row already exists for the given slug (e.g. a
 * previous call's schema/migration step failed after the row was committed), this
 * reuses that row and simply (re)runs the tenant migration, which is itself idempotent.
 */
@Service
public class TenantProvisioningService {

  private final TenantRepository tenantRepository;
  private final TenantFlywayMigrator tenantFlywayMigrator;

  public TenantProvisioningService(TenantRepository tenantRepository, TenantFlywayMigrator tenantFlywayMigrator) {
    this.tenantRepository = tenantRepository;
    this.tenantFlywayMigrator = tenantFlywayMigrator;
  }

  public TenantDTO createTenant(CreateTenantRequest request) {
    String slug = request.getSlug();
    String schemaName;
    try {
      schemaName = TenantSchemaNames.forSlug(slug);
    } catch (IllegalArgumentException e) {
      throw new BusinessException(e.getMessage());
    }

    Tenant tenant = tenantRepository.findBySlug(slug)
        .orElseGet(() -> tenantRepository.save(Tenant.builder()
            .slug(slug)
            .name(request.getName())
            .active(true)
            .build()));

    tenantFlywayMigrator.migrate(schemaName);

    return toDTO(tenant);
  }

  private static TenantDTO toDTO(Tenant tenant) {
    return new TenantDTO(tenant.getId(), tenant.getName(), tenant.getSlug(),
        TenantSchemaNames.forSlug(tenant.getSlug()), tenant.isActive());
  }
}
