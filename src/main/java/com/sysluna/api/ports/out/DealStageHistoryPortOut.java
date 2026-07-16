package com.sysluna.api.ports.out;

import com.sysluna.api.domain.model.DealStageHistory;

public interface DealStageHistoryPortOut {
  DealStageHistory save(DealStageHistory history);
}
