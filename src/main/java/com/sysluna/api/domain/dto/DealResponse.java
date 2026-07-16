package com.sysluna.api.domain.dto;

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
public class DealResponse extends BaseDTO {
  private String id;
  private String companyId;
  private String contactId;
  private String ownerId;
  private String stageId;
  private String title;
  private String description;
  private Boolean lost;
  private Boolean won;
  private Integer probability;
  private java.math.BigDecimal amount;
  private java.time.LocalDateTime closeDateExpected;
  private java.time.LocalDateTime createdAt;
  private String stageName;
}
