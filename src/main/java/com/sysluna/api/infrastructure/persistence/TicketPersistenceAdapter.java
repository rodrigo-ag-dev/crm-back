package com.sysluna.api.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import com.sysluna.api.domain.model.Ticket;
import com.sysluna.api.infrastructure.repository.TicketRepository;
import com.sysluna.api.ports.out.TicketPortOut;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class TicketPersistenceAdapter implements TicketPortOut {

  private final TicketRepository ticketRepository;

  @Override
  public Ticket save(Ticket ticket) {
    return ticketRepository.save(ticket);
  }

  @Override
  public Optional<Ticket> findByIdAndOwnerId(String id, String ownerId) {
    return ticketRepository.findByIdAndOwnerId(id, ownerId);
  }

  @Override
  public Page<Ticket> searchTickets(
      String title,
      String companyId,
      String contactId,
      String ticketStageId,
      Boolean canceled,
      String ownerId,
      Pageable pageable) {
    return ticketRepository.searchTickets(title, companyId, contactId, ticketStageId, canceled, ownerId, pageable);
  }

  @Override
  public List<Ticket> findPendingTickets(String ownerId, Pageable pageable) {
    return ticketRepository.findPendingTickets(ownerId, pageable);
  }
}
