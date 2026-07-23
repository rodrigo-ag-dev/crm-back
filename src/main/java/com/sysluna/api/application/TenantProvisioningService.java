package com.sysluna.api.application;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.sysluna.api.domain.dto.CreateTenantRequest;
import com.sysluna.api.domain.dto.TenantDTO;
import com.sysluna.api.domain.exception.BusinessException;
import com.sysluna.api.domain.exception.NotFoundException;
import com.sysluna.api.domain.model.Tenant;
import com.sysluna.api.infrastructure.repository.TenantRepository;
import com.sysluna.api.infrastructure.tenant.TenantFlywayMigrator;
import com.sysluna.api.infrastructure.tenant.TenantSchemaNames;

/**
 * Creates new tenants: registers them (by slug) in the shared crm_setup.tenant table, then
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

  /** Caller is responsible for checking User.isPlatformAdmin() before calling this. */
  public List<TenantDTO> listTenants() {
    return tenantRepository.findAll().stream().map(TenantProvisioningService::toDTO).toList();
  }

  /** Caller is responsible for checking User.isPlatformAdmin() before calling this. */
  public Page<TenantDTO> searchTenants(String name, Pageable pageable) {
    return tenantRepository.searchActiveByName(name, pageable).map(TenantProvisioningService::toDTO);
  }

  /** Caller is responsible for checking User.isPlatformAdmin() before calling this. */
  public TenantDTO getTenantById(String id) {
    return tenantRepository.findById(id)
        .map(TenantProvisioningService::toDTO)
        .orElseThrow(() -> new NotFoundException("Tenant not found: " + id));
  }

  private static TenantDTO toDTO(Tenant tenant) {
    return new TenantDTO(tenant.getId(), tenant.getName(), tenant.getSlug(),
        TenantSchemaNames.forSlug(tenant.getSlug()), tenant.isActive());
  }
}
