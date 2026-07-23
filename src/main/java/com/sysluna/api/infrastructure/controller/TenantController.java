package com.sysluna.api.infrastructure.controller;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sysluna.api.application.TenantProvisioningService;
import com.sysluna.api.domain.dto.CreateTenantRequest;
import com.sysluna.api.domain.dto.TenantDTO;
import com.sysluna.api.domain.exception.UnauthorizedException;
import com.sysluna.api.infrastructure.security.CurrentUserProvider;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * POST (provisioning a new tenant/schema) accepts either the standing shared secret
 * (APP_PROVISIONING_SECRET header, the same pattern as an ops/webhook endpoint - for
 * scripts/CI with no logged-in user) OR a regular JWT session belonging to a platform
 * admin (User.isPlatformAdmin() - for the in-app "create tenant" screen). Both checks are
 * done here rather than via @PreAuthorize, since there's no "platform admin" *role* wired
 * into Spring Security - a plain role check would let any tenant's ADMIN provision
 * schemas for other companies.
 *
 * GET (listing tenants, e.g. to populate a tenant picker) is JWT + platform-admin only;
 * unlike POST it has no ops/secret fallback, since there's no legitimate scripted use case
 * for it and it would be one more way to leak tenant data if the secret ever leaked.
 */
@RestController
@RequestMapping("/api/tenants")
@Tag(name = "Tenants", description = "Tenant provisioning and listing")
public class TenantController {

  private static final String SECRET_HEADER = "X-Provisioning-Key";

  private final TenantProvisioningService tenantProvisioningService;
  private final CurrentUserProvider currentUserProvider;
  private final String provisioningSecret;

  public TenantController(
      TenantProvisioningService tenantProvisioningService,
      CurrentUserProvider currentUserProvider,
      @Value("${app.provisioning.secret}") String provisioningSecret) {
    this.tenantProvisioningService = tenantProvisioningService;
    this.currentUserProvider = currentUserProvider;
    this.provisioningSecret = provisioningSecret;
  }

  @GetMapping
  @Operation(summary = "List tenants", description = "Returns every tenant (platform admin only)")
  public List<TenantDTO> listTenants() {
    requirePlatformAdmin();
    return tenantProvisioningService.listTenants();
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get tenant by id", description = "Returns a single tenant (platform admin only)")
  public TenantDTO getTenantById(@PathVariable String id) {
    requirePlatformAdmin();
    return tenantProvisioningService.getTenantById(id);
  }

  @GetMapping("/search")
  @Operation(summary = "Search tenants", description = "Paginated tenant search by name, for pickers (platform admin only)")
  public Page<TenantDTO> searchTenants(
      @RequestParam(required = false) String name,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {
    requirePlatformAdmin();
    return tenantProvisioningService.searchTenants(name, PageRequest.of(page, size, Sort.by("name").ascending()));
  }

  private void requirePlatformAdmin() {
    if (!currentUserProvider.isPlatformAdmin()) {
      throw new UnauthorizedException("Only platform admins can access tenants.");
    }
  }

  @PostMapping
  @Operation(summary = "Create tenant", description = "Provisions a new tenant schema (platform admin, or ops via X-Provisioning-Key)")
  public ResponseEntity<TenantDTO> createTenant(
      @RequestHeader(value = SECRET_HEADER, required = false) String providedSecret,
      @Valid @RequestBody CreateTenantRequest request) {
    boolean validSecret = constantTimeEquals(providedSecret, provisioningSecret);
    if (!validSecret && !currentUserProvider.isAuthenticatedPlatformAdmin()) {
      throw new UnauthorizedException("Only platform admins (or a valid provisioning key) can create tenants.");
    }
    TenantDTO created = tenantProvisioningService.createTenant(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(created);
  }

  // Constant-time compare to avoid a timing side-channel on the shared secret.
  private static boolean constantTimeEquals(String provided, String expected) {
    if (provided == null || expected == null) {
      return false;
    }
    return MessageDigest.isEqual(
        provided.getBytes(StandardCharsets.UTF_8),
        expected.getBytes(StandardCharsets.UTF_8));
  }
}
