package com.sysluna.api.domain.dto;

import com.sysluna.api.domain.model.Deal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
public class DealDTO extends BaseDTO {
  private String id;

  @NotBlank(message = "Company ID is required")
  private String companyId;

  @NotBlank(message = "Contact ID is required")
  private String contactId;

  private String ownerId;
  private String stageId;

  @NotBlank(message = "Title is required")
  @Size(max = 255, message = "Title must be at most 255 characters")
  private String title;

  private String description;
  private Boolean lost;
  private Boolean won;

  @Min(value = 0, message = "Probability must be between 0 and 100")
  @Max(value = 100, message = "Probability must be between 0 and 100")
  private Integer probability;

  @DecimalMin(value = "0.0", message = "Amount cannot be negative")
  private java.math.BigDecimal amount;

  private java.time.LocalDateTime closeDateExpected;

  public static DealDTO fromDeal(Deal deal) {
    return DealDTO.builder()
        .id(deal.getId())
        .companyId(deal.getCompanyId())
        .contactId(deal.getContactId())
        .ownerId(deal.getOwnerId())
        .stageId(deal.getStageId())
        .title(deal.getTitle())
        .description(deal.getDescription())
        .lost(deal.getLost())
        .won(deal.getWon())
        .probability(deal.getProbability())
        .amount(deal.getAmount())
        .closeDateExpected(deal.getCloseDateExpected())
        .createdAt(deal.getCreatedAt())
        .updatedAt(deal.getUpdatedAt())
        .build();
  }
}
