package com.sysluna.api.infrastructure.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sysluna.api.application.UserService;
import com.sysluna.api.domain.dto.HealthCheckResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/healthcheck")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "HealthCheck", description = "System health check API")
public class HealthCheckController {

  private final UserService userService;

  public HealthCheckController(UserService userService) {
    this.userService = userService;
  }

  @GetMapping
  @Operation(summary = "Verify system integrity", description = "Returns the integrity stage of the system and whether the initial setup still needs to run")
  @ApiResponse(responseCode = "200", description = "Online and healthy")
  public ResponseEntity<HealthCheckResponse> healthcheck() {
    return ResponseEntity.ok(new HealthCheckResponse("UP", userService.isDefaultTenantSetupRequired()));
  }
}
