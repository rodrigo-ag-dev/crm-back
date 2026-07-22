package com.sysluna.api.infrastructure.controller;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
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
 * Two very different access models on the same resource:
 *  - POST (provisioning a new tenant/schema) is gated by a standing shared secret
 *    (APP_PROVISIONING_SECRET), the same pattern as an ops/webhook endpoint - there is no
 *    "platform admin" *role* wired into Spring Security, so this can't be a normal
 *    @PreAuthorize check without letting any tenant's ADMIN provision schemas for other
 *    companies.
 *  - GET (listing tenants, e.g. to populate a tenant picker) is gated by the regular JWT
 *    session plus User.isPlatformAdmin() - safe to expose to a logged-in platform admin,
 *    unlike POST which does real, potentially expensive provisioning work.
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
    if (!currentUserProvider.isPlatformAdmin()) {
      throw new UnauthorizedException("Only platform admins can list tenants.");
    }
    return tenantProvisioningService.listTenants();
  }

  @PostMapping
  @Operation(summary = "Create tenant", description = "Provisions a new tenant schema (ops-only, requires X-Provisioning-Key)")
  public ResponseEntity<TenantDTO> createTenant(
      @RequestHeader(SECRET_HEADER) String providedSecret,
      @Valid @RequestBody CreateTenantRequest request) {
    if (!constantTimeEquals(providedSecret, provisioningSecret)) {
      throw new UnauthorizedException("Invalid provisioning key.");
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
