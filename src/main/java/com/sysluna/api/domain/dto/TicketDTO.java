package com.sysluna.api.domain.dto;

import java.time.LocalDateTime;

import com.sysluna.api.domain.model.Ticket;

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
public class TicketDTO extends BaseDTO {
  private String id;

  @NotBlank(message = "Company ID is required")
  private String companyId;

  private String companyName;

  @NotBlank(message = "Contact ID is required")
  private String contactId;

  private String contactName;
  private String ownerId;
  private String ticketStageId;
  private String canceledStageId;

  @NotBlank(message = "Title is required")
  @Size(max = 255, message = "Title must be at most 255 characters")
  private String title;

  private String description;
  private LocalDateTime dueDate;
  private LocalDateTime closedAt;
  private LocalDateTime canceledAt;
  private Boolean canceled;

  public static TicketDTO fromTicket(Ticket ticket) {
    return TicketDTO.builder()
        .id(ticket.getId())
        .companyId(ticket.getCompanyId())
        .companyName(ticket.getCompany() != null ? ticket.getCompany().getName() : null)
        .contactId(ticket.getContactId())
        .contactName(ticket.getContact() != null ? ticket.getContact().getName() : null)
        .ownerId(ticket.getOwnerId())
        .ticketStageId(ticket.getTicketStageId())
        .canceledStageId(ticket.getCanceledStageId())
        .title(ticket.getTitle())
        .description(ticket.getDescription())
        .dueDate(ticket.getDueDate())
        .closedAt(ticket.getClosedAt())
        .canceledAt(ticket.getCanceledAt())
        .canceled(ticket.getCanceled())
        .createdAt(ticket.getCreatedAt())
        .updatedAt(ticket.getUpdatedAt())
        .build();
  }
}
