package com.sysluna.api.ports.out;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.sysluna.api.domain.model.Contact;

public interface ContactPortOut {
  Contact save(Contact contact);
  Optional<Contact> findById(String id);
  boolean existsByEmailAndCompanyId(String email, String companyId);
  List<Contact> findByName(String name);
  Page<Contact> searchContacts(String name, String companyId, Pageable pageable);
  BigInteger countActiveContacts();
}
