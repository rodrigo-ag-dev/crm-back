package com.sysluna.api.domain.dto;

import com.sysluna.api.domain.model.Contact;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ContactDTO extends BaseDTO {
  private String id;

  @NotBlank(message = "Company ID is required")
  private String companyId;

  @NotBlank(message = "Name is required")
  @Size(max = 255, message = "Name must be at most 255 characters")
  private String name;

  @Size(max = 255, message = "Alias must be at most 255 characters")
  private String alias;

  @Email(message = "Email must be valid")
  @Size(max = 255, message = "Email must be at most 255 characters")
  private String email;

  @Size(max = 255, message = "Phone must be at most 255 characters")
  private String phone;

  public static ContactDTO fromContact(Contact contact) {
    return ContactDTO.builder()
        .id(contact.getId())
        .companyId(contact.getCompanyId())
        .name(contact.getName())
        .alias(contact.getAlias())
        .email(contact.getEmail())
        .phone(contact.getPhone())
        .createdAt(contact.getCreatedAt())
        .updatedAt(contact.getUpdatedAt())
        .build();
  }
}
