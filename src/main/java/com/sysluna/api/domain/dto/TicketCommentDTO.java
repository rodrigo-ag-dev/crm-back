package com.sysluna.api.domain.dto;

import java.time.LocalDateTime;

import com.sysluna.api.domain.model.TicketCommentType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TicketCommentDTO {
  private String id;
  private String ticketId;
  private String authorId;
  private String authorName;

  @NotNull(message = "Comment type is required")
  private TicketCommentType type;

  @NotBlank(message = "Comment body is required")
  private String body;

  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
