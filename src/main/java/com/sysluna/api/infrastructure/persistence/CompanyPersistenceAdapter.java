package com.sysluna.api.infrastructure.persistence;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import com.sysluna.api.domain.model.Company;
import com.sysluna.api.infrastructure.repository.CompanyRepository;
import com.sysluna.api.ports.out.CompanyPortOut;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class CompanyPersistenceAdapter implements CompanyPortOut {

  private final CompanyRepository companyRepository;

  @Override
  public Company save(Company company) {
    return companyRepository.save(company);
  }

  @Override
  public Optional<Company> findById(String id) {
    return companyRepository.findById(id);
  }

  @Override
  public boolean existsById(String id) {
    return companyRepository.existsById(id);
  }

  @Override
  public boolean existsByEmail(String email) {
    return companyRepository.existsByEmail(email);
  }

  @Override
  public List<Company> findByName(String name) {
    return companyRepository.findByName(name);
  }

  @Override
  public Page<Company> searchActiveByName(String name, Pageable pageable) {
    return companyRepository.searchActiveByName(name, pageable);
  }

  @Override
  public BigInteger countActiveCompanies() {
    return companyRepository.countActiveCompanies();
  }
}
