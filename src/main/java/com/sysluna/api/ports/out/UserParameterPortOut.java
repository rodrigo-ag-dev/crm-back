package com.sysluna.api.ports.out;

import java.util.List;
import java.util.Optional;

import com.sysluna.api.domain.model.UserParameter;

public interface UserParameterPortOut {
  Optional<UserParameter> findByUserIdAndParameterId(String userId, String parameterId);
  List<UserParameter> findByUserId(String userId);
  UserParameter save(UserParameter userParameter);
  void deleteByUserIdAndParameterId(String userId, String parameterId);
}
