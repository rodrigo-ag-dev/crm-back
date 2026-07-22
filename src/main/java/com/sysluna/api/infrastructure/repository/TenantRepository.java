package com.sysluna.api.infrastructure.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sysluna.api.domain.model.Tenant;

public interface TenantRepository extends JpaRepository<Tenant, String> {
  Optional<Tenant> findBySlug(String slug);

  boolean existsBySlug(String slug);

  @Query("SELECT t FROM Tenant t WHERE t.active = true AND (:name IS NULL OR LOWER(t.name) LIKE CONCAT('%', LOWER(CAST(:name AS string)), '%'))")
  Page<Tenant> searchActiveByName(@Param("name") String name, Pageable pageable);
}
