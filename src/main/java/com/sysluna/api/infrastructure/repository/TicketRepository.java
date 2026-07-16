package com.sysluna.api.infrastructure.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sysluna.api.domain.model.Ticket;

public interface TicketRepository extends JpaRepository<Ticket, String> {
  Optional<Ticket> findByIdAndOwnerId(String id, String ownerId);

  List<Ticket> findByTitleAndOwnerId(String title, String ownerId);

  @Query("""
      SELECT t
      FROM Ticket t
      JOIN FETCH t.company c
      JOIN FETCH t.contact co
      WHERE t.ownerId = :ownerId
        AND (:title IS NULL OR t.title LIKE :title)
        AND (:companyId IS NULL OR t.companyId = :companyId)
        AND (:contactId IS NULL OR t.contactId = :contactId)
        AND (:ticketStageId IS NULL OR t.ticketStageId = :ticketStageId)
        AND (:canceled IS NULL OR t.canceled = :canceled)
      """)
  Page<Ticket> searchTickets(
      @Param("title") String title,
      @Param("companyId") String companyId,
      @Param("contactId") String contactId,
      @Param("ticketStageId") String ticketStageId,
      @Param("canceled") Boolean canceled,
      @Param("ownerId") String ownerId,
      Pageable pageable);

  @Query("""
      SELECT t
      FROM Ticket t
      JOIN FETCH t.company c
      JOIN FETCH t.contact co
      WHERE t.ownerId = :ownerId
        AND t.canceled = false
        AND t.closedAt IS NULL
        AND t.dueDate IS NOT NULL
      ORDER BY t.dueDate ASC
      """)
  List<Ticket> findPendingTickets(@Param("ownerId") String ownerId, Pageable pageable);
}
