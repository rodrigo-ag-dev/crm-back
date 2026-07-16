package com.sysluna.api.ports.out;

import java.util.List;

import com.sysluna.api.domain.dto.TicketStageHistoryDTO;
import com.sysluna.api.domain.model.TicketStageHistory;

public interface TicketStageHistoryPortOut {
  TicketStageHistory save(TicketStageHistory history);

  List<TicketStageHistoryDTO> findByTicketId(String ticketId);
}
