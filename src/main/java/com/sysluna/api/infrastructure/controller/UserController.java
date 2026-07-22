package com.sysluna.api.infrastructure.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.sysluna.api.domain.dto.CreateUserRequest;
import com.sysluna.api.domain.dto.ResetPasswordResponseDTO;
import com.sysluna.api.domain.dto.UpdateUserActiveDTO;
import com.sysluna.api.domain.dto.UpdateUserRequest;
import com.sysluna.api.domain.dto.UpdateUserRoleDTO;
import com.sysluna.api.domain.dto.UserDTO;
import com.sysluna.api.domain.exception.NotFoundException;
import com.sysluna.api.infrastructure.security.CurrentUserProvider;
import com.sysluna.api.ports.in.UserPortIn;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("/api/user")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Users", description = "User management API")
public class UserController {
  private final UserPortIn userPortIn;
  private final CurrentUserProvider currentUserProvider;

  @GetMapping
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "List all users", description = "Returns a list of all registered users (admin only)")
  @ApiResponse(responseCode = "200", description = "User list returned successfully")
  public List<UserDTO> listUsers() {
    return userPortIn.listAllUsers();
  }

  @GetMapping("/{email}")
  @Operation(summary = "Get user by email", description = "Returns user data by email")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "User found", content = @Content(schema = @Schema(implementation = UserDTO.class))),
      @ApiResponse(responseCode = "404", description = "User not found")
  })
  public UserDTO getUserByEmail(@PathVariable String email) {
    UserDTO user = userPortIn.getUserByEmail(email);
    com.sysluna.api.domain.model.User currentUser = currentUserProvider.getCurrentUser();
    if (!currentUser.isPlatformAdmin() && !currentUser.getTenantId().equals(user.getTenantId())) {
      // Same response as "not found" - avoids confirming another tenant's user exists.
      throw new NotFoundException("User not found with email: " + email);
    }
    return user;
  }

  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Create user", description = "Creates a new user account (admin only)")
  public UserDTO createUser(@Valid @RequestBody CreateUserRequest createUserRequest) {
    return userPortIn.createUser(createUserRequest);
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Update user", description = "Updates a user's username, full name and email (admin only)")
  public UserDTO updateUser(@PathVariable String id, @Valid @RequestBody UpdateUserRequest updateUserRequest) {
    return userPortIn.updateUser(id, updateUserRequest);
  }

  @PatchMapping("/{id}/role")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Update user role", description = "Updates a user's role (admin only)")
  public UserDTO updateUserRole(@PathVariable String id, @Valid @RequestBody UpdateUserRoleDTO updateUserRoleDTO) {
    return userPortIn.updateUserRole(id, updateUserRoleDTO.getRole());
  }

  @PatchMapping("/{id}/active")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Activate or deactivate user", description = "Enables or disables a user's ability to log in (admin only)")
  public UserDTO updateUserActive(@PathVariable String id, @Valid @RequestBody UpdateUserActiveDTO updateUserActiveDTO) {
    return userPortIn.setUserActive(id, updateUserActiveDTO.getActive());
  }

  @PostMapping("/{id}/reset-password")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Reset user password", description = "Generates a new temporary password and forces the user to change it on next login (admin only)")
  public ResetPasswordResponseDTO resetPassword(@PathVariable String id) {
    return userPortIn.resetPassword(id);
  }
}
