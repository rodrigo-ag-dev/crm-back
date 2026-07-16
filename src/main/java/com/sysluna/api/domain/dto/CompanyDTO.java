package com.sysluna.api.domain.dto;

import com.sysluna.api.domain.model.Company;

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
public class CompanyDTO extends BaseDTO {
  private String id;

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

  private String description;

  @Size(max = 255, message = "Regional ID must be at most 255 characters")
  private String idRegional;

  public static CompanyDTO fromCompany(Company company) {
    return CompanyDTO.builder()
        .id(company.getId())
        .name(company.getName())
        .alias(company.getAlias())
        .email(company.getEmail())
        .phone(company.getPhone())
        .description(company.getDescription())
        .idRegional(company.getIdRegional())
        .createdAt(company.getCreatedAt())
        .updatedAt(company.getUpdatedAt())
        .build();
  }
}
