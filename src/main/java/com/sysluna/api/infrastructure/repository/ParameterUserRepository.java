package com.sysluna.api.infrastructure.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sysluna.api.domain.model.UserParameter;

public interface ParameterUserRepository extends JpaRepository<UserParameter, String> {

  Optional<UserParameter> findByUserIdAndParameterId(String userId, String parameterId);

  List<UserParameter> findByUserId(String userId);

  void deleteByUserIdAndParameterId(String userId, String parameterId);
}
