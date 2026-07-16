package com.sysluna.api.ports.in;

import java.util.List;

import com.sysluna.api.domain.dto.CreateUserRequest;
import com.sysluna.api.domain.dto.ResetPasswordResponseDTO;
import com.sysluna.api.domain.dto.UpdateUserRequest;
import com.sysluna.api.domain.dto.UserDTO;
import com.sysluna.api.domain.model.Role;

public interface UserPortIn {
  List<UserDTO> listAllUsers();

  UserDTO getUserByEmail(String email);

  UserDTO updateUserRole(String id, Role role);

  UserDTO createUser(CreateUserRequest createUserRequest);

  UserDTO updateUser(String id, UpdateUserRequest updateUserRequest);

  UserDTO setUserActive(String id, boolean active);

  ResetPasswordResponseDTO resetPassword(String id);

  UserDTO changePassword(String id, String currentPassword, String newPassword);
}
