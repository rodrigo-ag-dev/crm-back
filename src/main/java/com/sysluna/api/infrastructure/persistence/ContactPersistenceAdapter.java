package com.sysluna.api.infrastructure.persistence;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import com.sysluna.api.domain.model.Contact;
import com.sysluna.api.infrastructure.repository.ContactRepository;
import com.sysluna.api.ports.out.ContactPortOut;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class ContactPersistenceAdapter implements ContactPortOut {

  private final ContactRepository contactRepository;

  @Override
  public Contact save(Contact contact) {
    return contactRepository.save(contact);
  }

  @Override
  public Optional<Contact> findById(String id) {
    return contactRepository.findById(id);
  }

  @Override
  public boolean existsByEmailAndCompanyId(String email, String companyId) {
    return contactRepository.existsByEmailAndCompanyId(email, companyId);
  }

  @Override
  public List<Contact> findByName(String name) {
    return contactRepository.findByName(name);
  }

  @Override
  public Page<Contact> searchContacts(String name, String companyId, Pageable pageable) {
    return contactRepository.searchContacts(name, companyId, pageable);
  }

  @Override
  public BigInteger countActiveContacts() {
    return contactRepository.countActiveContacts();
  }
}
