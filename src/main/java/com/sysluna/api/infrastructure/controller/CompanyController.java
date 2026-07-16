package com.sysluna.api.infrastructure.controller;

import java.math.BigInteger;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sysluna.api.domain.dto.CompanyDTO;
import com.sysluna.api.ports.in.CompanyPortIn;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("/api/companies")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Companies", description = "Company management API")
public class CompanyController {
  private final CompanyPortIn companyPortIn;

  @GetMapping("/total")
  public BigInteger getTotalCompanies() {
    return companyPortIn.getTotalCompanies();
  }

  @GetMapping("/{id}")
  public CompanyDTO getCompanyById(@PathVariable String id) {
    return companyPortIn.getCompanyById(id);
  }

  @GetMapping("name")
  public List<CompanyDTO> getCompanyByName(@RequestParam String name) {
    List<CompanyDTO> companyDTO = companyPortIn.getCompanyByName(name);
    return companyDTO;
  }

  @GetMapping("/search")
  public Page<CompanyDTO> searchCompanies(
      @RequestParam(required = false) String name,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {
    return companyPortIn.searchCompanies(name, PageRequest.of(page, size, Sort.by("name").ascending()));
  }

  @PostMapping
  public CompanyDTO saveCompany(@Valid @RequestBody CompanyDTO companyDTO) {
    return companyPortIn.save(companyDTO);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteCompany(@PathVariable String id) {
    boolean deleted = companyPortIn.deleteCompany(id);
    if (!deleted) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.noContent().build();
  }
}
