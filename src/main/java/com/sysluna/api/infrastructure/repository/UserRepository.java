package com.sysluna.api.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sysluna.api.domain.model.User;

public interface UserRepository extends JpaRepository<User, String> {
  User findByEmail(String email);

  User findByUsername(String username);
}

