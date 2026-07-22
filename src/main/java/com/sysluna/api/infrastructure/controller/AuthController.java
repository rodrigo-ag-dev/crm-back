package com.sysluna.api.infrastructure.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sysluna.api.application.UserService;
import com.sysluna.api.domain.dto.AuthDTO;
import com.sysluna.api.domain.dto.AuthenticationRequest;
import com.sysluna.api.domain.dto.AuthenticationResponse;
import com.sysluna.api.domain.dto.ChangePasswordRequest;
import com.sysluna.api.domain.dto.UserDTO;
import com.sysluna.api.domain.exception.NotFoundException;
import com.sysluna.api.domain.model.User;
import com.sysluna.api.infrastructure.security.AuthCookie;
import com.sysluna.api.infrastructure.security.CurrentUserProvider;
import com.sysluna.api.infrastructure.security.JwtTokenProvider;
import com.sysluna.api.infrastructure.security.TokenBlocklist;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "User authentication")
public class AuthController {

  private final AuthenticationManager authenticationManager;
  private final JwtTokenProvider tokenProvider;
  private final PasswordEncoder passwordEncoder;
  private final UserService userService;
  private final CurrentUserProvider currentUserProvider;
  private final TokenBlocklist tokenBlocklist;

  @Value("${app.cookie.secure:false}")
  private boolean cookieSecure;

  public AuthController(
      AuthenticationManager authenticationManager,
      JwtTokenProvider tokenProvider,
      PasswordEncoder passwordEncoder,
      UserService userService,
      CurrentUserProvider currentUserProvider,
      TokenBlocklist tokenBlocklist) {
    this.authenticationManager = authenticationManager;
    this.tokenProvider = tokenProvider;
    this.passwordEncoder = passwordEncoder;
    this.userService = userService;
    this.currentUserProvider = currentUserProvider;
    this.tokenBlocklist = tokenBlocklist;
  }

  @PostMapping("/login")
  @Operation(summary = "Log in", description = "Authenticates a user and sets an httpOnly session cookie")
  public ResponseEntity<?> authenticateUser(@Valid @RequestBody AuthenticationRequest loginRequest) {
    try {
      authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(
              loginRequest.getEmail(),
              loginRequest.getPassword()));

      String token = tokenProvider.generateToken(loginRequest.getEmail());
      UserDTO user = userService.getUserByEmail(loginRequest.getEmail());

      return ResponseEntity.ok()
          .header(HttpHeaders.SET_COOKIE, buildTokenCookie(token, tokenProvider.getExpirationTime() / 1000).toString())
          .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
          .body(new AuthenticationResponse(tokenProvider.getExpirationTime(), user, token));
    } catch (AuthenticationException e) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body("Invalid email or password");
    }
  }

  @PostMapping("/logout")
  @Operation(summary = "Log out", description = "Clears the session cookie and invalidates the token")
  public ResponseEntity<Void> logout(HttpServletRequest request) {
    String token = AuthCookie.extractToken(request);
    if (token != null && !token.isBlank()) {
      tokenBlocklist.block(token, System.currentTimeMillis() + tokenProvider.getExpirationTime());
    }

    return ResponseEntity.noContent()
        .header(HttpHeaders.SET_COOKIE, buildTokenCookie("", 0).toString())
        .build();
  }

  @GetMapping("/me")
  @Operation(summary = "Current user", description = "Returns the currently authenticated user")
  public ResponseEntity<UserDTO> currentUser() {
    User user = currentUserProvider.getCurrentUser();
    return ResponseEntity.ok(new UserDTO(user.getId(), user.getUsername(), user.getFullName(), user.getEmail(),
        user.getRole(), user.isActive(), user.isMustChangePassword(), user.getTenantId()));
  }

  @PostMapping("/change-password")
  @Operation(summary = "Change password", description = "Changes the current user's password, clearing any forced-change flag")
  public ResponseEntity<UserDTO> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
    User user = currentUserProvider.getCurrentUser();
    UserDTO updated = userService.changePassword(user.getId(), request.getCurrentPassword(), request.getNewPassword());
    return ResponseEntity.ok(updated);
  }

  @PostMapping("/register")
  @Operation(summary = "Register a new user", description = "Creates a new user account in a tenant (defaults to the platform's default tenant)")
  public ResponseEntity<?> registerUser(@Valid @RequestBody AuthDTO auth) {
    try {
      userService.getUserByEmail(auth.getEmail());
      return ResponseEntity.status(HttpStatus.CONFLICT)
          .body("Email is already registered");
    } catch (NotFoundException e) {
      // User does not exist yet, continue with registration.
    }

    User savedUser = userService.registerUser(
        auth.getTenantSlug(), auth.getUsername(), auth.getFullName(), auth.getEmail(), auth.getPassword(),
        passwordEncoder);

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(new UserDTO(savedUser.getId(), savedUser.getUsername(), savedUser.getFullName(), savedUser.getEmail(),
            savedUser.getRole(), savedUser.isActive(), savedUser.isMustChangePassword(), savedUser.getTenantId()));
  }

  private ResponseCookie buildTokenCookie(String value, long maxAgeSeconds) {
    return ResponseCookie.from(AuthCookie.NAME, value)
        .httpOnly(true)
        .secure(cookieSecure)
        .sameSite(cookieSecure ? "None" : "Lax")
        .path("/")
        .maxAge(maxAgeSeconds)
        .build();
  }
}
