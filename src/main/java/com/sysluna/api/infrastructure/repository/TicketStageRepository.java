package com.sysluna.api.infrastructure.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sysluna.api.domain.model.TicketStage;

public interface TicketStageRepository extends JpaRepository<TicketStage, String> {
  Optional<TicketStage> findByNameIgnoreCase(String name);
}
