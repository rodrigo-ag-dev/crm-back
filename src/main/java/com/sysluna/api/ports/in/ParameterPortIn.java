package com.sysluna.api.ports.in;

import java.util.List;

import com.sysluna.api.domain.dto.ParameterDTO;
import com.sysluna.api.domain.dto.UserParameterDTO;

public interface ParameterPortIn {
  List<ParameterDTO> listAllParameters();

  ParameterDTO getParameterById(String id);

  ParameterDTO getParameterByName(String name);

  ParameterDTO save(ParameterDTO parameterDTO);

  boolean deleteParameterById(String id);

  String getParameterValue(String parameterId, String userId);

  List<UserParameterDTO> listUserParameters(String userId);

  UserParameterDTO saveUserParameter(UserParameterDTO userParameterDTO);

  boolean deleteUserParameter(String userId, String parameterId);
}
