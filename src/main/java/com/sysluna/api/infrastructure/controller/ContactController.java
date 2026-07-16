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

import com.sysluna.api.domain.dto.ContactDTO;
import com.sysluna.api.ports.in.ContactPortIn;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("/api/contacts")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Contacts", description = "Contact management API")
public class ContactController {
  private final ContactPortIn contactPortIn;

  @GetMapping("/total")
  public BigInteger getTotalContacts() {
    return contactPortIn.getTotalContacts();
  }
  @GetMapping("/{id}")
  public ContactDTO getContactById(@PathVariable String id) {
    return contactPortIn.getContactById(id);
  }

  @GetMapping("name")
  public List<ContactDTO> getContactByName(@RequestParam String name) {
    List<ContactDTO> contactDTO = contactPortIn.getContactByName(name);
    return contactDTO;
  }

  @GetMapping("/search")
  public Page<ContactDTO> searchContacts(
      @RequestParam(required = false) String name,
      @RequestParam(required = false) String companyId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {
    return contactPortIn.searchContacts(name, companyId, PageRequest.of(page, size, Sort.by("name").ascending()));
  }

  @PostMapping
  public ContactDTO saveContact(@Valid @RequestBody ContactDTO contactDTO) {
    return contactPortIn.save(contactDTO);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteContact(@PathVariable String id) {
    boolean deleted = contactPortIn.deleteContact(id);
    if (!deleted) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.noContent().build();
  }
}
