package com.sysluna.api.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.sysluna.api.domain.dto.TicketCommentDTO;
import com.sysluna.api.domain.model.TicketComment;
import com.sysluna.api.infrastructure.repository.TicketCommentRepository;
import com.sysluna.api.ports.out.TicketCommentPortOut;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class TicketCommentPersistenceAdapter implements TicketCommentPortOut {

  private final TicketCommentRepository ticketCommentRepository;

  @Override
  public TicketComment save(TicketComment comment) {
    return ticketCommentRepository.save(comment);
  }

  @Override
  public List<TicketCommentDTO> findByTicketId(String ticketId) {
    return ticketCommentRepository.findByTicketId(ticketId);
  }

  @Override
  public Optional<TicketCommentDTO> findDTOById(String id) {
    return ticketCommentRepository.findDTOById(id);
  }
}
