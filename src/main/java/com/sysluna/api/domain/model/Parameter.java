package com.sysluna.api.domain.model;

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
@Table(name = "parameter")
public class Parameter extends BaseModel {
  @Column(nullable = false, unique = true)
  private String name;

  @Column
  private String value;

  @Column(name = "is_user_specific", nullable = false)
  @Builder.Default
  private boolean userSpecific = false;
}
