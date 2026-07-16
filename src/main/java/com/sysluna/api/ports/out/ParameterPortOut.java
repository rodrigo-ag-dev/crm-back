package com.sysluna.api.ports.out;

import java.util.List;
import java.util.Optional;

import com.sysluna.api.domain.model.Parameter;

public interface ParameterPortOut {
  List<Parameter> findAll();
  Parameter findByName(String name);
  Optional<Parameter> findById(String id);
  Parameter save(Parameter parameter);
  boolean existsById(String id);
  void deleteById(String id);
}
