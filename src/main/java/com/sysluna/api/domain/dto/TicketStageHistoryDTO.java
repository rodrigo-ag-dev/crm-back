package com.sysluna.api.domain.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TicketStageHistoryDTO {
  private String id;
  private String ticketId;
  private String fromStageId;
  private String fromStageName;
  private String toStageId;
  private String toStageName;
  private String changedById;
  private String changedByName;
  private LocalDateTime changedAt;
}
