package com.sysluna.api.infrastructure.persistence;

import org.springframework.stereotype.Component;

import com.sysluna.api.domain.model.DealStageHistory;
import com.sysluna.api.infrastructure.repository.DealStafeHistoryRepository;
import com.sysluna.api.ports.out.DealStageHistoryPortOut;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class DealStageHistoryPersistenceAdapter implements DealStageHistoryPortOut {

  private final DealStafeHistoryRepository dealStageHistoryRepository;

  @Override
  public DealStageHistory save(DealStageHistory history) {
    return dealStageHistoryRepository.save(history);
  }
}
