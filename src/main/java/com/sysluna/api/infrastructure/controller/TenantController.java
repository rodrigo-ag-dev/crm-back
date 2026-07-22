package com.sysluna.api.infrastructure.controller;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sysluna.api.application.TenantProvisioningService;
import com.sysluna.api.domain.dto.CreateTenantRequest;
import com.sysluna.api.domain.dto.TenantDTO;
import com.sysluna.api.domain.exception.UnauthorizedException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * Provisions new tenants (companies). There is no "platform admin" role yet - only
 * per-tenant ADMIN/USER (see Role) - so this is deliberately not gated by the regular
 * JWT/role security, which would let any tenant's admin provision schemas for other
 * companies. Instead it's gated by a standing shared secret (APP_PROVISIONING_SECRET),
 * the same pattern as an ops/webhook endpoint. Revisit this if/when a real platform
 * operator role is introduced.
 */
@RestController
@RequestMapping("/api/tenants")
@Tag(name = "Tenants", description = "Tenant provisioning (ops-only)")
public class TenantController {

  private static final String SECRET_HEADER = "X-Provisioning-Key";

  private final TenantProvisioningService tenantProvisioningService;
  private final String provisioningSecret;

  public TenantController(
      TenantProvisioningService tenantProvisioningService,
      @Value("${app.provisioning.secret}") String provisioningSecret) {
    this.tenantProvisioningService = tenantProvisioningService;
    this.provisioningSecret = provisioningSecret;
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
