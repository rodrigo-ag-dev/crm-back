package com.sysluna.api.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TicketStageChangeDTO {
  private String id;

  @NotBlank(message = "Ticket stage ID is required")
  private String ticketStageId;
}
