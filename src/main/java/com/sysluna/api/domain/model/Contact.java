package com.sysluna.api.domain.model;

import java.util.regex.Pattern;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sysluna.api.domain.dto.ContactDTO;
import com.sysluna.api.domain.exception.BusinessException;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "contact")
public class Contact extends BaseModel {

  @Column(name = "company_id", nullable = false)
  private String companyId;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private String alias;

  @Column(nullable = false)
  private String email;

  @Column(nullable = false)
  private String phone;

  @Column(nullable = false)
  @JsonIgnore
  @Builder.Default
  private boolean active = true;

  public static Contact fromDTO(ContactDTO dto) {
    Contact contact = Contact.builder()
        .companyId(dto.getCompanyId())
        .name(dto.getName())
        .alias(dto.getAlias())
        .email(dto.getEmail())
        .phone(dto.getPhone())
        .build();
    contact.validate();
    return contact;
  }

  public void validate() {
    if (this.companyId == null || this.companyId.trim().isEmpty()) {
      throw new BusinessException("Company ID is required for this contact.");
    }
    if (this.name == null || this.name.trim().isEmpty()) {
      throw new BusinessException("Contact name is required.");
    }
    if (this.email != null && !this.email.trim().isEmpty()) {
      if (!Pattern.matches("^[A-Za-z0-9+_.-]+@(.+)$", this.email)) {
        throw new BusinessException("Invalid contact email format.");
      }
    }
  }
}
