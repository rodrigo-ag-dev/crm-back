package com.sysluna.api.ports.out;

import java.util.List;
import java.util.Optional;

import com.sysluna.api.domain.model.User;

public interface UserPortOut {
  User findByEmail(String email);
  User findByUsername(String username);
  Optional<User> findById(String id);
  Optional<User> findByIdAndTenantId(String id, String tenantId);
  List<User> findAllByTenantId(String tenantId);
  User save(User user);
}
