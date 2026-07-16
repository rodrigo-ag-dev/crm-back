package com.sysluna.api.ports.out;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.sysluna.api.domain.model.Ticket;

public interface TicketPortOut {
  Ticket save(Ticket ticket);
  Optional<Ticket> findByIdAndOwnerId(String id, String ownerId);
  Page<Ticket> searchTickets(
      String title,
      String companyId,
      String contactId,
      String ticketStageId,
      Boolean canceled,
      String ownerId,
      Pageable pageable);
  List<Ticket> findPendingTickets(String ownerId, Pageable pageable);
}
