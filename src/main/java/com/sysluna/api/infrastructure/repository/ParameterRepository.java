package com.sysluna.api.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sysluna.api.domain.model.Parameter;

public interface ParameterRepository extends JpaRepository<Parameter, String> {
  Parameter findByName(String name);
}

