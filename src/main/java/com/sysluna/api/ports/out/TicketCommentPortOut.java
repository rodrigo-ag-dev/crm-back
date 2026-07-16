package com.sysluna.api.ports.out;

import java.util.List;
import java.util.Optional;

import com.sysluna.api.domain.dto.TicketCommentDTO;
import com.sysluna.api.domain.model.TicketComment;

public interface TicketCommentPortOut {
  TicketComment save(TicketComment comment);

  List<TicketCommentDTO> findByTicketId(String ticketId);

  Optional<TicketCommentDTO> findDTOById(String id);
}
