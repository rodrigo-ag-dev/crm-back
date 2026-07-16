package com.sysluna.api.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sysluna.api.domain.model.DealStageHistory;

public interface DealStafeHistoryRepository extends JpaRepository<DealStageHistory, String> {
}
