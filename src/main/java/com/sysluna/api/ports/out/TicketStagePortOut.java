package com.sysluna.api.ports.out;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Sort;

import com.sysluna.api.domain.model.TicketStage;

public interface TicketStagePortOut {
  List<TicketStage> findAll(Sort sort);
  Optional<TicketStage> findByNameIgnoreCase(String name);
  Optional<TicketStage> findById(String id);
}
