package com.sysluna.api.infrastructure.repository;

import java.math.BigInteger;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sysluna.api.domain.model.Contact;

public interface ContactRepository extends JpaRepository<Contact, String> {
  List<Contact> findByName(String name);

  boolean existsByEmailAndCompanyId(String email, String companyId);

  @Query("""
      SELECT c FROM Contact c
      WHERE c.active = true
        AND (:name IS NULL OR LOWER(c.name) LIKE CONCAT('%', LOWER(CAST(:name AS string)), '%'))
        AND (:companyId IS NULL OR c.companyId = :companyId)
      """)
  Page<Contact> searchContacts(@Param("name") String name, @Param("companyId") String companyId, Pageable pageable);

  @Query("SELECT COUNT(id) FROM Contact WHERE active = true")
  BigInteger countActiveContacts();
}
