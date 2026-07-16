package com.sysluna.api.infrastructure.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sysluna.api.domain.dto.TicketCommentDTO;
import com.sysluna.api.domain.model.TicketComment;

public interface TicketCommentRepository extends JpaRepository<TicketComment, String> {

  @Query("""
      SELECT new com.sysluna.api.domain.dto.TicketCommentDTO(
          c.id, c.ticketId, c.authorId, u.fullName, c.type, c.body, c.createdAt, c.updatedAt
      )
      FROM TicketComment c
      LEFT JOIN User u ON c.authorId = u.id
      WHERE c.ticketId = :ticketId
      ORDER BY c.createdAt ASC
      """)
  List<TicketCommentDTO> findByTicketId(@Param("ticketId") String ticketId);

  @Query("""
      SELECT new com.sysluna.api.domain.dto.TicketCommentDTO(
          c.id, c.ticketId, c.authorId, u.fullName, c.type, c.body, c.createdAt, c.updatedAt
      )
      FROM TicketComment c
      LEFT JOIN User u ON c.authorId = u.id
      WHERE c.id = :id
      """)
  Optional<TicketCommentDTO> findDTOById(@Param("id") String id);
}
