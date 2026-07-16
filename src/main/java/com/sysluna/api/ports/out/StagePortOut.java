package com.sysluna.api.ports.out;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.sysluna.api.domain.model.Stage;

public interface StagePortOut {
  Stage save(Stage stage);
  Optional<Stage> findById(String id);
  void delete(Stage stage);
  List<Stage> findByName(String name);
  Page<Stage> searchStages(String name, Pageable pageable);
}
