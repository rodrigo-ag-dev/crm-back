package com.sysluna.api.infrastructure.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sysluna.api.domain.model.Tenant;

public interface TenantRepository extends JpaRepository<Tenant, String> {
  Optional<Tenant> findBySlug(String slug);

  boolean existsBySlug(String slug);
}
