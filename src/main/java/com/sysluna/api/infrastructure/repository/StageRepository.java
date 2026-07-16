package com.sysluna.api.infrastructure.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sysluna.api.domain.model.Stage;

public interface StageRepository extends JpaRepository<Stage, String> {
  List<Stage> findByName(String name);

  @Query("SELECT s FROM Stage s WHERE (:name IS NULL OR LOWER(s.name) LIKE CONCAT('%', LOWER(CAST(:name AS string)), '%')) ORDER by s.order ASC")
  Page<Stage> searchStages(@Param("name") String name, Pageable pageable);
}
