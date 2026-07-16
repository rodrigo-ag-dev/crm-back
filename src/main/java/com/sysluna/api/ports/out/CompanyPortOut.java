package com.sysluna.api.ports.out;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.sysluna.api.domain.model.Company;

public interface CompanyPortOut {
  Company save(Company company);
  Optional<Company> findById(String id);
  boolean existsById(String id);
  boolean existsByEmail(String email);
  List<Company> findByName(String name);
  Page<Company> searchActiveByName(String name, Pageable pageable);
  BigInteger countActiveCompanies();
}
