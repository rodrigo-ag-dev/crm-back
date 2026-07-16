package com.sysluna.api.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "ticket_comment")
public class TicketComment extends BaseModel {

  @Column(name = "ticket_id", nullable = false)
  private String ticketId;

  @Column(name = "author_id")
  private String authorId;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private TicketCommentType type;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String body;
}
