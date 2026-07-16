package com.sysluna.api.application;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.sysluna.api.domain.dto.StageDTO;
import com.sysluna.api.domain.model.Stage;
import com.sysluna.api.ports.in.StagePortIn;
import com.sysluna.api.ports.out.StagePortOut;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class StageService implements StagePortIn {

  private final StagePortOut stagePortOut;

  @Override
  public Page<StageDTO> searchStages(String name, Pageable pageable) {
    return stagePortOut.searchStages(name, pageable).map(StageDTO::fromStage);
  }

  @Override
  public StageDTO save(StageDTO stageDTO) {
    Stage stage = Stage.fromDTO(stageDTO);
    if (stageDTO.getId() != null) {
      stage.setId(stageDTO.getId());
    }
    return StageDTO.fromStage(stagePortOut.save(stage));
  }

  @Override
  public boolean deleteStage(String id) {
    Stage stage = stagePortOut.findById(id).orElse(null);
    if (stage == null) {
      return false;
    }
    stagePortOut.delete(stage);
    return true;
  }

  @Override
  public List<StageDTO> getStageByName(String name) {
    List<Stage> stages = stagePortOut.findByName(name);
    return stages != null && !stages.isEmpty()
        ? stages.stream().map(StageDTO::fromStage).toList()
        : null;
  }

  @Override
  public StageDTO getStageById(String id) {
    return stagePortOut.findById(id).map(StageDTO::fromStage).orElse(null);
  }
}
