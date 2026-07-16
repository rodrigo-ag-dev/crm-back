package com.sysluna.api.domain.model;

import com.sysluna.api.domain.dto.DealDTO;
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
@Table(name = "deal")
public class Deal extends BaseModel {

  @Column(name = "company_id", nullable = false)
  private String companyId;

  @Column(name = "contact_id", nullable = false)
  private String contactId;

  @Column(name = "owner_id")
  private String ownerId;

  @Column(name = "stage_id")
  private String stageId;

  @Column(nullable = false)
  private String title;

  @Column(columnDefinition = "TEXT")
  private String description;

  @Column
  private Integer probability;

  @Column
  private java.math.BigDecimal amount;

  @Column(name = "close_date_expected")
  private java.time.LocalDateTime closeDateExpected;

  @Column(name = "lost")
  @Builder.Default
  private Boolean lost = false;

  @Column(name = "won")
  @Builder.Default
  private Boolean won = false;

  public static Deal fromDTO(DealDTO dto) {
    Deal deal = Deal.builder()
        .companyId(dto.getCompanyId())
        .contactId(dto.getContactId())
        .ownerId(dto.getOwnerId())
        .stageId(dto.getStageId())
        .title(dto.getTitle())
        .description(dto.getDescription())
        .probability(dto.getProbability())
        .amount(dto.getAmount())
        .closeDateExpected(dto.getCloseDateExpected())
        .build();
    deal.validate();
    return deal;
  }

  public void validate() {
    if (this.title == null || this.title.trim().isEmpty()) {
      throw new BusinessException("Deal title is required.");
    }
    if (this.companyId == null || this.companyId.trim().isEmpty()) {
      throw new BusinessException("Company ID is required.");
    }
    if (this.contactId == null || this.contactId.trim().isEmpty()) {
      throw new BusinessException("Contact ID is required.");
    }
  }

  public void changeStage(String newStageId) {
    if (newStageId == null || newStageId.trim().isEmpty()) {
      throw new BusinessException("New stage cannot be null.");
    }
    if (this.stageId != null && this.stageId.equals(newStageId)) {
      throw new BusinessException("The deal is already in this stage.");
    }
    this.stageId = newStageId;
  }

  public void markAsLost() {
    this.setLost(true);
    this.setWon(false);
  }

  public void markAsWon() {
    this.setLost(false);
    this.setWon(true);
  }
}
