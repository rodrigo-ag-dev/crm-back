package com.sysluna.api.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import com.sysluna.api.domain.model.Stage;
import com.sysluna.api.infrastructure.repository.StageRepository;
import com.sysluna.api.ports.out.StagePortOut;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class StagePersistenceAdapter implements StagePortOut {

  private final StageRepository stageRepository;

  @Override
  public Stage save(Stage stage) {
    return stageRepository.save(stage);
  }

  @Override
  public Optional<Stage> findById(String id) {
    return stageRepository.findById(id);
  }

  @Override
  public void delete(Stage stage) {
    stageRepository.delete(stage);
  }

  @Override
  public List<Stage> findByName(String name) {
    return stageRepository.findByName(name);
  }

  @Override
  public Page<Stage> searchStages(String name, Pageable pageable) {
    return stageRepository.searchStages(name, pageable);
  }
}
