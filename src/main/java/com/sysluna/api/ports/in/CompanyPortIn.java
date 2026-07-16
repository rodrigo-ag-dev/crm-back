package com.sysluna.api.ports.in;

import java.math.BigInteger;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.sysluna.api.domain.dto.CompanyDTO;

public interface CompanyPortIn {
  CompanyDTO getCompanyById(String id);

  BigInteger getTotalCompanies();

  List<CompanyDTO> getCompanyByName(String name);

  Page<CompanyDTO> searchCompanies(String name, Pageable pageable);

  CompanyDTO save(CompanyDTO companyDTO);

  boolean deleteCompany(String id);
}
