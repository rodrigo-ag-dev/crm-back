package com.sysluna.api.domain.model;

import java.util.regex.Pattern;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sysluna.api.domain.dto.CompanyDTO;
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
@Table(name = "company")
public class Company extends BaseModel {

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private String alias;

  @Column(nullable = false)
  private String email;

  @Column(nullable = false)
  private String phone;

  @Column(nullable = false)
  private String description;

  @Column(name = "id_regional", nullable = false)
  private String idRegional;

  @Column(nullable = false)
  @JsonIgnore
  @Builder.Default
  private boolean active = true;

  public static Company fromDTO(CompanyDTO dto) {
    Company company = Company.builder()
        .name(dto.getName())
        .alias(dto.getAlias())
        .email(dto.getEmail())
        .phone(dto.getPhone())
        .description(dto.getDescription())
        .idRegional(dto.getIdRegional())
        .build();
    company.validate();
    return company;
  }

  public void validate() {
    if (this.name == null || this.name.trim().isEmpty()) {
      throw new BusinessException("Company name is required.");
    }
    if (this.email != null && !this.email.trim().isEmpty()) {
      if (!Pattern.matches("^[A-Za-z0-9+_.-]+@(.+)$", this.email)) {
        throw new BusinessException("Invalid company email format.");
      }
    }
  }
}
