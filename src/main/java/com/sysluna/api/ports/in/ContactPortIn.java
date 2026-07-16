package com.sysluna.api.ports.in;

import java.math.BigInteger;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.sysluna.api.domain.dto.ContactDTO;

public interface ContactPortIn {
  ContactDTO getContactById(String id);

  BigInteger getTotalContacts();

  List<ContactDTO> getContactByName(String name);

  Page<ContactDTO> searchContacts(String name, String companyId, Pageable pageable);

  ContactDTO save(ContactDTO companyDTO);

  boolean deleteContact(String id);
}
