package com.sysluna.api.ports.in;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.sysluna.api.domain.dto.StageDTO;

public interface StagePortIn {
  Page<StageDTO> searchStages(String name, Pageable pageable);

  StageDTO getStageById(String id);

  List<StageDTO> getStageByName(String name);

  StageDTO save(StageDTO stageDTO);

  boolean deleteStage(String id);
}
