package com.sysluna.api.domain.dto;

import com.sysluna.api.domain.model.TicketStage;

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
public class TicketStageDTO extends BaseDTO {
  private String id;
  private String name;
  private String description;
  private String color;
  private int order;

  public static TicketStageDTO fromTicketStage(TicketStage ticketStage) {
    return TicketStageDTO.builder()
        .id(ticketStage.getId())
        .name(ticketStage.getName())
        .description(ticketStage.getDescription())
        .color(ticketStage.getColor())
        .order(ticketStage.getOrder())
        .createdAt(ticketStage.getCreatedAt())
        .updatedAt(ticketStage.getUpdatedAt())
        .build();
  }
}
