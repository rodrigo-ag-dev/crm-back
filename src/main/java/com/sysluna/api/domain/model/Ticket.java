package com.sysluna.api.domain.model;

import java.time.LocalDateTime;

import com.sysluna.api.domain.dto.TicketDTO;
import com.sysluna.api.domain.exception.BusinessException;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@Table(name = "ticket")
public class Ticket extends BaseModel {

  @Column(name = "company_id", nullable = false)
  private String companyId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "company_id", insertable = false, updatable = false, foreignKey = @ForeignKey(name = "fk_ticket_company"))
  private Company company;

  @Column(name = "contact_id", nullable = false)
  private String contactId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "contact_id", insertable = false, updatable = false, foreignKey = @ForeignKey(name = "fk_ticket_contact"))
  private Contact contact;

  @Column(name = "owner_id")
  private String ownerId;

  @Column(name = "ticket_stage_id")
  private String ticketStageId;

  @Column(name = "canceled_stage_id")
  private String canceledStageId;

  @Column(nullable = false)
  private String title;

  @Column(columnDefinition = "TEXT")
  private String description;

  @Column(name = "due_date")
  private LocalDateTime dueDate;

  @Column(name = "closed_at")
  private LocalDateTime closedAt;

  @Column(name = "canceled_at")
  private LocalDateTime canceledAt;

  @Column(name = "is_canceled", nullable = false)
  @Builder.Default
  private Boolean canceled = false;

  public static Ticket fromDTO(TicketDTO dto) {
    Ticket ticket = Ticket.builder()
        .companyId(dto.getCompanyId())
        .contactId(dto.getContactId())
        .ownerId(dto.getOwnerId())
        .ticketStageId(dto.getTicketStageId())
        .canceledStageId(dto.getCanceledStageId())
        .title(dto.getTitle())
        .description(dto.getDescription())
        .dueDate(dto.getDueDate())
        .closedAt(dto.getClosedAt())
        .canceledAt(dto.getCanceledAt())
        .canceled(dto.getCanceled())
        .build();
    ticket.validate();
    return ticket;
  }

  public void validate() {
    if (this.title == null || this.title.trim().isEmpty()) {
      throw new BusinessException("Ticket title is required.");
    }
    if (this.companyId == null || this.companyId.trim().isEmpty()) {
      throw new BusinessException("Company ID is required.");
    }
    if (this.contactId == null || this.contactId.trim().isEmpty()) {
      throw new BusinessException("Contact ID is required.");
    }
    if (this.ticketStageId == null || this.ticketStageId.trim().isEmpty()) {
      throw new BusinessException("Ticket stage ID is required.");
    }
  }

  public void changeStage(String newStageId) {
    if (newStageId == null || newStageId.trim().isEmpty()) {
      throw new BusinessException("New ticket stage cannot be null.");
    }
    if (this.ticketStageId != null && this.ticketStageId.equals(newStageId)) {
      throw new BusinessException("The ticket is already in this stage.");
    }
    this.ticketStageId = newStageId;
  }

  public void markAsCanceled(String canceledStageId, LocalDateTime canceledAt) {
    this.canceled = true;
    this.canceledStageId = canceledStageId;
    this.canceledAt = canceledAt;
  }

  public void markAsClosed(LocalDateTime closedAt) {
    this.closedAt = closedAt;
  }

  public void reopen() {
    this.closedAt = null;
  }
}
