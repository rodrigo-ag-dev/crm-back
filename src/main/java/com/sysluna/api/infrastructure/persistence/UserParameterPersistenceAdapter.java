package com.sysluna.api.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.sysluna.api.domain.model.UserParameter;
import com.sysluna.api.infrastructure.repository.ParameterUserRepository;
import com.sysluna.api.ports.out.UserParameterPortOut;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class UserParameterPersistenceAdapter implements UserParameterPortOut {

  private final ParameterUserRepository parameterUserRepository;

  @Override
  public Optional<UserParameter> findByUserIdAndParameterId(String userId, String parameterId) {
    return parameterUserRepository.findByUserIdAndParameterId(userId, parameterId);
  }

  @Override
  public List<UserParameter> findByUserId(String userId) {
    return parameterUserRepository.findByUserId(userId);
  }

  @Override
  public UserParameter save(UserParameter userParameter) {
    return parameterUserRepository.save(userParameter);
  }

  @Override
  public void deleteByUserIdAndParameterId(String userId, String parameterId) {
    parameterUserRepository.deleteByUserIdAndParameterId(userId, parameterId);
  }
}
