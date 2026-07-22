package com.sysluna.api.application;

import java.security.SecureRandom;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.sysluna.api.domain.dto.CreateUserRequest;
import com.sysluna.api.domain.dto.ResetPasswordResponseDTO;
import com.sysluna.api.domain.dto.UpdateUserRequest;
import com.sysluna.api.domain.dto.UserDTO;
import com.sysluna.api.domain.exception.BusinessException;
import com.sysluna.api.domain.exception.NotFoundException;
import com.sysluna.api.domain.model.Role;
import com.sysluna.api.domain.model.Tenant;
import com.sysluna.api.domain.model.User;
import com.sysluna.api.infrastructure.repository.TenantRepository;
import com.sysluna.api.infrastructure.security.CurrentUserProvider;
import com.sysluna.api.ports.in.UserPortIn;
import com.sysluna.api.ports.out.UserPortOut;

@Service
public class UserService implements UserPortIn {

  private static final String TEMP_PASSWORD_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789";
  private static final int TEMP_PASSWORD_LENGTH = 10;
  private static final SecureRandom RANDOM = new SecureRandom();

  private final UserPortOut userPortOut;
  private final TenantRepository tenantRepository;
  private final CurrentUserProvider currentUserProvider;
  private final PasswordEncoder passwordEncoder;
  private final String defaultTenantSlug;

  public UserService(
      UserPortOut userPortOut,
      TenantRepository tenantRepository,
      CurrentUserProvider currentUserProvider,
      PasswordEncoder passwordEncoder,
      @Value("${app.tenant.default-slug}") String defaultTenantSlug) {
    this.userPortOut = userPortOut;
    this.tenantRepository = tenantRepository;
    this.currentUserProvider = currentUserProvider;
    this.passwordEncoder = passwordEncoder;
    this.defaultTenantSlug = defaultTenantSlug;
  }

  /**
   * Platform admins (User.isPlatformAdmin(), granted only via direct DB update - see the
   * V3 migration) see every user across every tenant; everyone else sees only their own
   * tenant's users, same as before.
   */
  @Override
  public List<UserDTO> listAllUsers() {
    User current = currentUserProvider.getCurrentUser();
    List<User> targetUsers = current.isPlatformAdmin()
        ? userPortOut.findAll()
        : userPortOut.findAllByTenantId(current.getTenantId());

    Map<String, String> tenantNamesById = tenantRepository
        .findAllById(targetUsers.stream().map(User::getTenantId).distinct().toList())
        .stream()
        .collect(Collectors.toMap(Tenant::getId, Tenant::getName));

    return targetUsers.stream().map(u -> toDTO(u, tenantNamesById.get(u.getTenantId()))).toList();
  }

  @Override
  public UserDTO getUserByEmail(String email) {
    return toDTO(getUserByEmailOrThrow(email));
  }

  public User getUserByEmailAllData(String email) {
    return userPortOut.findByEmail(email);
  }

  /**
   * Whether the platform's default tenant still needs its first admin created. Used by
   * the public healthcheck endpoint to decide whether the frontend should show the
   * initial-setup screen; mirrors the guard in createFirstAdminUser for that same tenant.
   */
  public boolean isDefaultTenantSetupRequired() {
    return tenantRepository.findBySlug(defaultTenantSlug)
        .map(tenant -> userPortOut.findAllByTenantId(tenant.getId()).isEmpty())
        .orElse(true);
  }

  /**
   * Creates the first admin user of a tenant. Guarded per-tenant (not globally, as
   * before multi-tenancy) so this stays safely public/unauthenticated: it only ever
   * succeeds against a tenant that has zero users yet, so it can't be used to hijack an
   * already-set-up tenant. Defaults to the platform's default tenant when tenantSlug is
   * omitted, preserving the single-tenant/local-dev "just call /api/setup" flow.
   */
  public UserDTO createFirstAdminUser(String tenantSlug, String username, String fullName, String email,
      String password) {
    Tenant tenant = resolveTenantOrThrow(tenantSlug);

    if (!userPortOut.findAllByTenantId(tenant.getId()).isEmpty()) {
      throw new BusinessException("Setup already completed for this tenant");
    }

    if (userPortOut.findByEmail(email) != null) {
      throw new BusinessException("Email is already registered");
    }

    User user = new User();
    user.setTenantId(tenant.getId());
    user.setUsername(username);
    user.setFullName(fullName);
    user.setEmail(email);
    user.setPasswordHash(passwordEncoder.encode(password));
    user.setRole(Role.ADMIN);
    user.setMustChangePassword(true);

    return toDTO(userPortOut.save(user), tenant.getName());
  }

  /**
   * Self-service signup of a regular USER into an existing, active tenant. Defaults to
   * the platform's default tenant when tenantSlug is omitted.
   */
  public User registerUser(String tenantSlug, String username, String fullName, String email, String rawPassword,
      PasswordEncoder encoder) {
    Tenant tenant = resolveTenantOrThrow(tenantSlug);

    User user = new User();
    user.setTenantId(tenant.getId());
    user.setUsername(username);
    user.setFullName(fullName);
    user.setEmail(email);
    user.setPasswordHash(encoder.encode(rawPassword != null ? rawPassword : ""));

    return userPortOut.save(user);
  }

  @Override
  public UserDTO updateUserRole(String id, Role role) {
    User user = findByIdOrThrow(id);
    user.setRole(role);
    return toDTO(userPortOut.save(user));
  }

  @Override
  public UserDTO createUser(CreateUserRequest createUserRequest) {
    if (userPortOut.findByEmail(createUserRequest.getEmail()) != null) {
      throw new BusinessException("Email is already registered");
    }

    User current = currentUserProvider.getCurrentUser();

    User user = new User();
    // Tenant is the current admin's own tenant by default. A platform admin may instead
    // target any tenant by slug - everyone else can't, or an admin from one tenant could
    // create users in another tenant just by sending a different tenantSlug.
    user.setTenantId(resolveTargetTenantId(current, createUserRequest.getTenantSlug()));
    user.setUsername(createUserRequest.getUsername());
    user.setFullName(createUserRequest.getFullName());
    user.setEmail(createUserRequest.getEmail());
    user.setPasswordHash(passwordEncoder.encode(createUserRequest.getPassword()));
    user.setRole(Role.USER);
    user.setMustChangePassword(true);

    return toDTO(userPortOut.save(user));
  }

  @Override
  public UserDTO updateUser(String id, UpdateUserRequest updateUserRequest) {
    User user = findByIdOrThrow(id);

    User existingWithEmail = userPortOut.findByEmail(updateUserRequest.getEmail());
    if (existingWithEmail != null && !existingWithEmail.getId().equals(id)) {
      throw new BusinessException("Email is already registered");
    }

    User current = currentUserProvider.getCurrentUser();
    if (current.isPlatformAdmin() && isPresent(updateUserRequest.getTenantSlug())) {
      user.setTenantId(resolveTenantBySlugOrThrow(updateUserRequest.getTenantSlug()).getId());
    }

    user.setUsername(updateUserRequest.getUsername());
    user.setFullName(updateUserRequest.getFullName());
    user.setEmail(updateUserRequest.getEmail());
    return toDTO(userPortOut.save(user));
  }

  @Override
  public UserDTO setUserActive(String id, boolean active) {
    User user = findByIdOrThrow(id);
    user.setActive(active);
    return toDTO(userPortOut.save(user));
  }

  @Override
  public ResetPasswordResponseDTO resetPassword(String id) {
    User user = findByIdOrThrow(id);
    String temporaryPassword = generateTemporaryPassword();
    user.setPasswordHash(passwordEncoder.encode(temporaryPassword));
    user.setMustChangePassword(true);
    userPortOut.save(user);

    ResetPasswordResponseDTO response = new ResetPasswordResponseDTO();
    response.setTemporaryPassword(temporaryPassword);
    return response;
  }

  @Override
  public UserDTO changePassword(String id, String currentPassword, String newPassword) {
    User user = findByIdOrThrow(id);
    if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
      throw new BusinessException("Current password is incorrect");
    }
    user.setPasswordHash(passwordEncoder.encode(newPassword));
    user.setMustChangePassword(false);
    return toDTO(userPortOut.save(user));
  }

  private User getUserByEmailOrThrow(String email) {
    User user = userPortOut.findByEmail(email);
    if (user == null) {
      throw new NotFoundException("User not found with email: " + email);
    }
    return user;
  }

  /**
   * Platform admins can act on a user in any tenant; everyone else only within their own.
   */
  private User findByIdOrThrow(String id) {
    User current = currentUserProvider.getCurrentUser();
    if (current.isPlatformAdmin()) {
      return userPortOut.findById(id).orElseThrow(() -> new NotFoundException("User not found with ID: " + id));
    }
    return userPortOut.findByIdAndTenantId(id, current.getTenantId())
        .orElseThrow(() -> new NotFoundException("User not found with ID: " + id));
  }

  private Tenant resolveTenantOrThrow(String tenantSlug) {
    String slug = isPresent(tenantSlug) ? tenantSlug : defaultTenantSlug;
    return resolveTenantBySlugOrThrow(slug);
  }

  private Tenant resolveTenantBySlugOrThrow(String slug) {
    Tenant tenant = tenantRepository.findBySlug(slug)
        .orElseThrow(() -> new NotFoundException("Tenant not found: " + slug));

    if (!tenant.isActive()) {
      throw new BusinessException("Tenant is inactive: " + tenant.getSlug());
    }
    return tenant;
  }

  /** Platform admins may target any tenant by slug; everyone else stays in their own. */
  private String resolveTargetTenantId(User currentUser, String requestedTenantSlug) {
    if (currentUser.isPlatformAdmin() && isPresent(requestedTenantSlug)) {
      return resolveTenantBySlugOrThrow(requestedTenantSlug).getId();
    }
    return currentUser.getTenantId();
  }

  private static boolean isPresent(String value) {
    return value != null && !value.isBlank();
  }

  private static String generateTemporaryPassword() {
    StringBuilder sb = new StringBuilder(TEMP_PASSWORD_LENGTH);
    for (int i = 0; i < TEMP_PASSWORD_LENGTH; i++) {
      sb.append(TEMP_PASSWORD_CHARS.charAt(RANDOM.nextInt(TEMP_PASSWORD_CHARS.length())));
    }
    return sb.toString();
  }

  private UserDTO toDTO(User user) {
    String tenantName = tenantRepository.findById(user.getTenantId()).map(Tenant::getName).orElse(null);
    return toDTO(user, tenantName);
  }

  private static UserDTO toDTO(User user, String tenantName) {
    return new UserDTO(user.getId(), user.getUsername(), user.getFullName(), user.getEmail(), user.getRole(),
        user.isActive(), user.isMustChangePassword(), user.getTenantId(), tenantName, user.isPlatformAdmin());
  }
}
