package com.sysluna.api.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import com.sysluna.api.domain.model.TicketStage;
import com.sysluna.api.infrastructure.repository.TicketStageRepository;
import com.sysluna.api.ports.out.TicketStagePortOut;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class TicketStagePersistenceAdapter implements TicketStagePortOut {

  private final TicketStageRepository ticketStageRepository;

  @Override
  public List<TicketStage> findAll(Sort sort) {
    return ticketStageRepository.findAll(sort);
  }

  @Override
  public Optional<TicketStage> findByNameIgnoreCase(String name) {
    return ticketStageRepository.findByNameIgnoreCase(name);
  }

  @Override
  public Optional<TicketStage> findById(String id) {
    return ticketStageRepository.findById(id);
  }
}
