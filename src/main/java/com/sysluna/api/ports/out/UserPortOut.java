package com.sysluna.api.ports.out;

import java.util.List;
import java.util.Optional;

import com.sysluna.api.domain.model.User;

public interface UserPortOut {
  User findByEmail(String email);
  User findByUsername(String username);
  Optional<User> findById(String id);
  List<User> findAll();
  User save(User user);
}
