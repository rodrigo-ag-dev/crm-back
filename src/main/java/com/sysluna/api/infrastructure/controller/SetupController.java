package com.sysluna.api.infrastructure.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sysluna.api.application.UserService;
import com.sysluna.api.domain.dto.SetupRequest;
import com.sysluna.api.domain.dto.UserDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/setup")
@Tag(name = "Setup", description = "Initial setup for a tenant's first admin user")
public class SetupController {

  private final UserService userService;

  public SetupController(UserService userService) {
    this.userService = userService;
  }

  @PostMapping
  @Operation(summary = "Create first admin user", description = "Creates the initial admin account for a tenant when it has no users yet")
  public ResponseEntity<UserDTO> createFirstAdmin(@Valid @RequestBody SetupRequest request) {
    UserDTO created = userService.createFirstAdminUser(
        request.getTenantSlug(), request.getUsername(), request.getFullName(), request.getEmail(),
        request.getPassword());
    return ResponseEntity.status(HttpStatus.CREATED).body(created);
  }
}
