package com.sysluna.api.application;

import java.security.SecureRandom;
import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.sysluna.api.domain.dto.CreateUserRequest;
import com.sysluna.api.domain.dto.ResetPasswordResponseDTO;
import com.sysluna.api.domain.dto.UpdateUserRequest;
import com.sysluna.api.domain.dto.UserDTO;
import com.sysluna.api.domain.exception.BusinessException;
import com.sysluna.api.domain.exception.NotFoundException;
import com.sysluna.api.domain.model.Role;
import com.sysluna.api.domain.model.User;
import com.sysluna.api.ports.in.UserPortIn;
import com.sysluna.api.ports.out.UserPortOut;

@Service
public class UserService implements UserPortIn {

  private static final String TEMP_PASSWORD_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789";
  private static final int TEMP_PASSWORD_LENGTH = 10;
  private static final SecureRandom RANDOM = new SecureRandom();

  private final UserPortOut userPortOut;
  private final PasswordEncoder passwordEncoder;

  public UserService(UserPortOut userPortOut, PasswordEncoder passwordEncoder) {
    this.userPortOut = userPortOut;
    this.passwordEncoder = passwordEncoder;
  }

  public List<UserDTO> listAllUsers() {
    return userPortOut.findAll()
        .stream()
        .map(UserService::toDTO)
        .toList();
  }

  public UserDTO getUserByEmail(String email) {
    User user = userPortOut.findByEmail(email);
    if (user == null) {
      throw new NotFoundException("User not found with email: " + email);
    }
    return toDTO(user);
  }

  public User getUserByEmailAllData(String email) {
    return userPortOut.findByEmail(email);
  }

  public boolean hasAnyUsers() {
    return !userPortOut.findAll().isEmpty();
  }

  public UserDTO createFirstAdminUser(String username, String fullName, String email, String password) {
    if (hasAnyUsers()) {
      throw new BusinessException("Setup already completed");
    }

    if (userPortOut.findByEmail(email) != null) {
      throw new BusinessException("Email is already registered");
    }

    User user = new User();
    user.setUsername(username);
    user.setFullName(fullName);
    user.setEmail(email);
    user.setPasswordHash(passwordEncoder.encode(password));
    user.setRole(Role.ADMIN);
    user.setMustChangePassword(true);

    return toDTO(userPortOut.save(user));
  }

  public User saveUser(User user) {
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

    User user = new User();
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

  private User findByIdOrThrow(String id) {
    return userPortOut.findById(id)
        .orElseThrow(() -> new NotFoundException("User not found with ID: " + id));
  }

  private static String generateTemporaryPassword() {
    StringBuilder sb = new StringBuilder(TEMP_PASSWORD_LENGTH);
    for (int i = 0; i < TEMP_PASSWORD_LENGTH; i++) {
      sb.append(TEMP_PASSWORD_CHARS.charAt(RANDOM.nextInt(TEMP_PASSWORD_CHARS.length())));
    }
    return sb.toString();
  }

  private static UserDTO toDTO(User user) {
    return new UserDTO(user.getId(), user.getUsername(), user.getFullName(), user.getEmail(), user.getRole(),
        user.isActive(), user.isMustChangePassword());
  }
}
