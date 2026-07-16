package com.sysluna.api.application;

import java.util.List;

import org.springframework.stereotype.Service;

import com.sysluna.api.domain.dto.ParameterDTO;
import com.sysluna.api.domain.dto.UserParameterDTO;
import com.sysluna.api.domain.exception.BusinessException;
import com.sysluna.api.domain.model.Parameter;
import com.sysluna.api.domain.model.UserParameter;
import com.sysluna.api.infrastructure.security.CurrentUserProvider;
import com.sysluna.api.ports.in.ParameterPortIn;
import com.sysluna.api.ports.out.ParameterPortOut;
import com.sysluna.api.ports.out.UserParameterPortOut;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class ParameterService implements ParameterPortIn {

  private final ParameterPortOut parameterPortOut;
  private final UserParameterPortOut userParameterPortOut;
  private final CurrentUserProvider currentUserProvider;

  @Override
  public List<ParameterDTO> listAllParameters() {
    return parameterPortOut.findAll().stream().map(this::toParameterDTO).toList();
  }

  @Override
  public ParameterDTO save(ParameterDTO parameterDTO) {
    Parameter parameter = parameterPortOut.findByName(parameterDTO.getName());
    if (parameter == null) {
      parameter = Parameter.builder()
          .name(parameterDTO.getName())
          .value(parameterDTO.isUserSpecific() ? null : parameterDTO.getValue())
          .userSpecific(parameterDTO.isUserSpecific())
          .build();
    } else {
      parameter.setName(parameterDTO.getName());
      parameter.setUserSpecific(parameterDTO.isUserSpecific());
      parameter.setValue(parameterDTO.isUserSpecific() ? null : parameterDTO.getValue());
    }
    parameterPortOut.save(parameter);
    return toParameterDTO(parameter);
  }

  @Override
  public ParameterDTO getParameterById(String id) {
    return parameterPortOut.findById(id).map(this::toParameterDTO).orElse(null);
  }

  @Override
  public ParameterDTO getParameterByName(String name) {
    Parameter parameter = parameterPortOut.findByName(name);
    return parameter != null ? toParameterDTO(parameter) : null;
  }

  @Override
  public boolean deleteParameterById(String id) {
    if (!parameterPortOut.existsById(id)) {
      return false;
    }
    parameterPortOut.deleteById(id);
    return true;
  }

  @Override
  public String getParameterValue(String parameterId, String userId) {
    currentUserProvider.requireSelfOrAdmin(userId);
    Parameter parameter = parameterPortOut.findById(parameterId).orElse(null);
    if (parameter == null) {
      return null;
    }
    if (!parameter.isUserSpecific()) {
      return parameter.getValue();
    }
    return userParameterPortOut.findByUserIdAndParameterId(userId, parameterId)
        .map(UserParameter::getValue)
        .orElse(null);
  }

  @Override
  public List<UserParameterDTO> listUserParameters(String userId) {
    currentUserProvider.requireSelfOrAdmin(userId);
    return userParameterPortOut.findByUserId(userId)
        .stream()
        .map(up -> new UserParameterDTO(up.getUserId(), up.getParameterId(), up.getValue()))
        .toList();
  }

  @Override
  public UserParameterDTO saveUserParameter(UserParameterDTO userParameterDTO) {
    currentUserProvider.requireSelfOrAdmin(userParameterDTO.getUserId());
    Parameter parameter = parameterPortOut.findById(userParameterDTO.getParameterId())
        .orElseThrow(() -> new BusinessException("Parameter not found with ID: " + userParameterDTO.getParameterId()));
    if (!parameter.isUserSpecific()) {
      throw new BusinessException("This parameter is not marked as user-specific.");
    }
    UserParameter userParameter = userParameterPortOut
        .findByUserIdAndParameterId(userParameterDTO.getUserId(), userParameterDTO.getParameterId())
        .orElseGet(() -> UserParameter.builder()
            .userId(userParameterDTO.getUserId())
            .parameterId(userParameterDTO.getParameterId())
            .build());
    userParameter.setValue(userParameterDTO.getValue());
    userParameterPortOut.save(userParameter);
    return userParameterDTO;
  }

  @Override
  public boolean deleteUserParameter(String userId, String parameterId) {
    currentUserProvider.requireSelfOrAdmin(userId);
    if (userParameterPortOut.findByUserIdAndParameterId(userId, parameterId).isEmpty()) {
      return false;
    }
    userParameterPortOut.deleteByUserIdAndParameterId(userId, parameterId);
    return true;
  }

  private ParameterDTO toParameterDTO(Parameter parameter) {
    return new ParameterDTO(
        parameter.getId(),
        parameter.getName(),
        parameter.getValue(),
        parameter.isUserSpecific());
  }
}
