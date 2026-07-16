package com.sysluna.api.domain.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@Table(name = "ticket_stage_history")
public class TicketStageHistory {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @JsonIgnore
  @Schema(hidden = true)
  private String id;

  @Column(name = "ticket_id", nullable = false)
  private String ticketId;

  @Column(name = "changed_by_id", nullable = false)
  private String changedById;

  @Column(name = "from_stage_id")
  private String fromStageId;

  @Column(name = "to_stage_id")
  private String toStageId;

  @Column(name = "changed_at")
  @CreationTimestamp
  private LocalDateTime changedAt;
}
