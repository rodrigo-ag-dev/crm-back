package com.sysluna.api.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.sysluna.api.domain.model.User;
import com.sysluna.api.infrastructure.repository.UserRepository;
import com.sysluna.api.ports.out.UserPortOut;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class UserPersistenceAdapter implements UserPortOut {

  private final UserRepository userRepository;

  @Override
  public User findByEmail(String email) {
    return userRepository.findByEmail(email);
  }

  @Override
  public User findByUsername(String username) {
    return userRepository.findByUsername(username);
  }

  @Override
  public Optional<User> findById(String id) {
    return userRepository.findById(id);
  }

  @Override
  public Optional<User> findByIdAndTenantId(String id, String tenantId) {
    return userRepository.findByIdAndTenantId(id, tenantId);
  }

  @Override
  public List<User> findAllByTenantId(String tenantId) {
    return userRepository.findAllByTenantId(tenantId);
  }

  @Override
  public User save(User user) {
    return userRepository.save(user);
  }
}
