package com.sysluna.api.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.sysluna.api.domain.model.Parameter;
import com.sysluna.api.infrastructure.repository.ParameterRepository;
import com.sysluna.api.ports.out.ParameterPortOut;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class ParameterPersistenceAdapter implements ParameterPortOut {

  private final ParameterRepository parameterRepository;

  @Override
  public List<Parameter> findAll() {
    return parameterRepository.findAll();
  }

  @Override
  public Parameter findByName(String name) {
    return parameterRepository.findByName(name);
  }

  @Override
  public Optional<Parameter> findById(String id) {
    return parameterRepository.findById(id);
  }

  @Override
  public Parameter save(Parameter parameter) {
    return parameterRepository.save(parameter);
  }

  @Override
  public boolean existsById(String id) {
    return parameterRepository.existsById(id);
  }

  @Override
  public void deleteById(String id) {
    parameterRepository.deleteById(id);
  }
}
