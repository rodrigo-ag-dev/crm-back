package com.sysluna.api.ports.in;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.sysluna.api.domain.dto.TicketCommentDTO;
import com.sysluna.api.domain.dto.TicketStageDTO;
import com.sysluna.api.domain.dto.TicketStageHistoryDTO;
import com.sysluna.api.domain.dto.TicketDTO;

public interface TicketPortIn {
  TicketDTO getTicketById(String id);

  List<TicketStageDTO> listTicketStages();

  Page<TicketDTO> searchTickets(
      String title,
      String companyId,
      String contactId,
      String ticketStageId,
      Boolean canceled,
      Pageable pageable);

  TicketDTO save(TicketDTO ticketDTO);

  TicketDTO update(String id, TicketDTO ticketDTO);

  TicketDTO updateStage(String id, String ticketStageId);

  TicketDTO cancel(String id);

  List<TicketDTO> getPendingTickets(int limit);

  List<TicketStageHistoryDTO> getTicketHistory(String ticketId);

  List<TicketCommentDTO> getTicketComments(String ticketId);

  TicketCommentDTO addTicketComment(String ticketId, TicketCommentDTO commentDTO);
}
