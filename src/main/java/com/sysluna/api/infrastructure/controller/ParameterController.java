package com.sysluna.api.infrastructure.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sysluna.api.domain.dto.ParameterDTO;
import com.sysluna.api.domain.dto.UserParameterDTO;
import com.sysluna.api.ports.in.ParameterPortIn;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("/api/parameter")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Parameters", description = "System parameter management API")
public class ParameterController {
  private final ParameterPortIn parameterService;

  @GetMapping
  @Operation(summary = "List all parameters")
  public List<ParameterDTO> listParameters() {
    return parameterService.listAllParameters();
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get a parameter by id")
  public ParameterDTO getParameterById(@PathVariable String id) {
    return parameterService.getParameterById(id);
  }

  @GetMapping("/name")
  @Operation(summary = "Get a parameter by name")
  public ParameterDTO getParameterByName(@RequestParam String name) {
    return parameterService.getParameterByName(name);
  }

  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Create or update a parameter (admin only)")
  public ParameterDTO saveParameter(@Valid @RequestBody ParameterDTO parameterDTO) {
    return parameterService.save(parameterDTO);
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Delete a parameter by id (admin only)")
  public ResponseEntity<Void> deleteParameter(@PathVariable String id) {
    boolean deleted = parameterService.deleteParameterById(id);
    if (!deleted) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/user/{userId}/value")
  @Operation(summary = "Get the effective value for a user-specific parameter")
  public String getParameterUser(
      @PathVariable String userId,
      @RequestParam String parameterId) {
    return parameterService.getParameterValue(parameterId, userId);
  }

  @GetMapping("/user/{userId}")
  @Operation(summary = "List all user-specific parameter overrides")
  public List<UserParameterDTO> listUserParameters(@PathVariable String userId) {
    return parameterService.listUserParameters(userId);
  }

  @PostMapping("/user")
  @Operation(summary = "Create or update a user-specific parameter override")
  public UserParameterDTO saveUserParameter(@Valid @RequestBody UserParameterDTO userParameterDTO) {
    return parameterService.saveUserParameter(userParameterDTO);
  }

  @DeleteMapping("/user/{userId}/{parameterId}")
  @Operation(summary = "Delete a user-specific parameter override")
  public ResponseEntity<Void> deleteUserParameter(
      @PathVariable String userId,
      @PathVariable String parameterId) {
    boolean deleted = parameterService.deleteUserParameter(userId, parameterId);
    if (!deleted) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.noContent().build();
  }
}
