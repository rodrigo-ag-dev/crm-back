package com.sysluna.api.application;

import java.math.BigInteger;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.sysluna.api.domain.dto.CompanyDTO;
import com.sysluna.api.domain.exception.BusinessException;
import com.sysluna.api.domain.exception.NotFoundException;
import com.sysluna.api.domain.model.Company;
import com.sysluna.api.ports.in.CompanyPortIn;
import com.sysluna.api.ports.out.CompanyPortOut;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class CompanyService implements CompanyPortIn {

  private final CompanyPortOut companyPortOut;

  @Override
  public CompanyDTO save(CompanyDTO companyDTO) {
    if (companyDTO.getId() == null && companyDTO.getEmail() != null
        && companyPortOut.existsByEmail(companyDTO.getEmail())) {
      throw new BusinessException("A company with this email already exists.");
    }
    Company company = Company.fromDTO(companyDTO);
    if (companyDTO.getId() != null) {
      company.setId(companyDTO.getId());
    }
    Company savedCompany = companyPortOut.save(company);
    return CompanyDTO.fromCompany(savedCompany);
  }

  @Override
  public BigInteger getTotalCompanies() {
    return companyPortOut.countActiveCompanies();
  }

  @Override
  public boolean deleteCompany(String id) {
    Company company = companyPortOut.findById(id)
        .orElseThrow(() -> new NotFoundException("Company not found with ID: " + id));
    company.setActive(false);
    companyPortOut.save(company);
    return true;
  }

  @Override
  public List<CompanyDTO> getCompanyByName(String name) {
    List<Company> companies = companyPortOut.findByName(name);
    return companies != null && !companies.isEmpty()
        ? companies.stream().map(CompanyDTO::fromCompany).toList()
        : null;
  }

  @Override
  public Page<CompanyDTO> searchCompanies(String name, Pageable pageable) {
    return companyPortOut.searchActiveByName(name, pageable)
        .map(CompanyDTO::fromCompany);
  }

  @Override
  public CompanyDTO getCompanyById(String id) {
    Company company = companyPortOut.findById(id)
        .orElseThrow(() -> new NotFoundException("Company not found with ID: " + id));
    return CompanyDTO.fromCompany(company);
  }
}
