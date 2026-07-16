package com.sysluna.api.infrastructure.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sysluna.api.domain.dto.TicketStageHistoryDTO;
import com.sysluna.api.domain.model.TicketStageHistory;

public interface TicketStageHistoryRepository extends JpaRepository<TicketStageHistory, String> {

  @Query("""
      SELECT new com.sysluna.api.domain.dto.TicketStageHistoryDTO(
          h.id, h.ticketId, h.fromStageId, fromStage.name, h.toStageId, toStage.name,
          h.changedById, u.fullName, h.changedAt
      )
      FROM TicketStageHistory h
      LEFT JOIN TicketStage fromStage ON h.fromStageId = fromStage.id
      LEFT JOIN TicketStage toStage ON h.toStageId = toStage.id
      LEFT JOIN User u ON h.changedById = u.id
      WHERE h.ticketId = :ticketId
      ORDER BY h.changedAt ASC
      """)
  List<TicketStageHistoryDTO> findByTicketId(@Param("ticketId") String ticketId);
}
