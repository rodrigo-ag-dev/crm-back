package com.sysluna.api.application;

import java.math.BigInteger;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.sysluna.api.domain.dto.ContactDTO;
import com.sysluna.api.domain.exception.BusinessException;
import com.sysluna.api.domain.exception.NotFoundException;
import com.sysluna.api.domain.model.Contact;
import com.sysluna.api.ports.in.ContactPortIn;
import com.sysluna.api.ports.out.CompanyPortOut;
import com.sysluna.api.ports.out.ContactPortOut;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class ContactService implements ContactPortIn {

  private final ContactPortOut contactPortOut;
  private final CompanyPortOut companyPortOut;

  @Override
  public ContactDTO save(ContactDTO contactDTO) {
    if (!companyPortOut.existsById(contactDTO.getCompanyId())) {
      throw new BusinessException("The linked company does not exist.");
    }
    if (contactDTO.getId() == null
        && contactPortOut.existsByEmailAndCompanyId(contactDTO.getEmail(), contactDTO.getCompanyId())) {
      throw new BusinessException("A contact with this email already exists in this company.");
    }
    Contact contact = Contact.fromDTO(contactDTO);
    if (contactDTO.getId() != null) {
      contact.setId(contactDTO.getId());
    }
    Contact savedContact = contactPortOut.save(contact);
    return ContactDTO.fromContact(savedContact);
  }

  @Override
  public BigInteger getTotalContacts() {
    return contactPortOut.countActiveContacts();
  }

  @Override
  public boolean deleteContact(String id) {
    Contact contact = contactPortOut.findById(id)
        .orElseThrow(() -> new NotFoundException("Contact not found with ID: " + id));
    contact.setActive(false);
    contactPortOut.save(contact);
    return true;
  }

  @Override
  public List<ContactDTO> getContactByName(String name) {
    List<Contact> contacts = contactPortOut.findByName(name);
    return contacts != null && !contacts.isEmpty()
        ? contacts.stream().map(ContactDTO::fromContact).toList()
        : null;
  }

  @Override
  public Page<ContactDTO> searchContacts(String name, String companyId, Pageable pageable) {
    return contactPortOut.searchContacts(name, companyId, pageable).map(ContactDTO::fromContact);
  }

  @Override
  public ContactDTO getContactById(String id) {
    Contact contact = contactPortOut.findById(id)
        .orElseThrow(() -> new NotFoundException("Contact not found with ID: " + id));
    return ContactDTO.fromContact(contact);
  }
}
