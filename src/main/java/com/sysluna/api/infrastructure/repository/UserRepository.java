package com.sysluna.api.infrastructure.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sysluna.api.domain.model.User;

public interface UserRepository extends JpaRepository<User, String> {
  User findByEmail(String email);

  User findByUsername(String username);

  Optional<User> findByIdAndTenantId(String id, String tenantId);

  List<User> findAllByTenantId(String tenantId);
}
