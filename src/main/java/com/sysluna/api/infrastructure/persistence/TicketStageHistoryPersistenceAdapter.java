package com.sysluna.api.infrastructure.persistence;

import java.util.List;

import org.springframework.stereotype.Component;

import com.sysluna.api.domain.dto.TicketStageHistoryDTO;
import com.sysluna.api.domain.model.TicketStageHistory;
import com.sysluna.api.infrastructure.repository.TicketStageHistoryRepository;
import com.sysluna.api.ports.out.TicketStageHistoryPortOut;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class TicketStageHistoryPersistenceAdapter implements TicketStageHistoryPortOut {

  private final TicketStageHistoryRepository ticketStageHistoryRepository;

  @Override
  public TicketStageHistory save(TicketStageHistory history) {
    return ticketStageHistoryRepository.save(history);
  }

  @Override
  public List<TicketStageHistoryDTO> findByTicketId(String ticketId) {
    return ticketStageHistoryRepository.findByTicketId(ticketId);
  }
}
