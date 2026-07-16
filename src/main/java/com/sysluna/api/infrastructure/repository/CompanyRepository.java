package com.sysluna.api.infrastructure.repository;

import java.math.BigInteger;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sysluna.api.domain.model.Company;

public interface CompanyRepository extends JpaRepository<Company, String> {
  List<Company> findByName(String name);

  boolean existsByEmail(String email);

  @Query("SELECT c FROM Company c WHERE c.active = true AND (:name IS NULL OR LOWER(c.name) LIKE CONCAT('%', LOWER(CAST(:name AS string)), '%'))")
  Page<Company> searchActiveByName(@Param("name") String name, Pageable pageable);

  @Query("SELECT COUNT(id) FROM Company WHERE active = true")
  BigInteger countActiveCompanies();
}
