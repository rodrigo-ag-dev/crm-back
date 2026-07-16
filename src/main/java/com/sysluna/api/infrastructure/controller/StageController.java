package com.sysluna.api.infrastructure.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sysluna.api.domain.dto.StageDTO;
import com.sysluna.api.ports.in.StagePortIn;

import jakarta.validation.Valid;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("/api/stages")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "stage", description = "Stage management API")
public class StageController {
  private final StagePortIn stagePortIn;

  @GetMapping("/search")
  public Page<StageDTO> searchStages(
      @RequestParam(required = false) String name,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {
    return stagePortIn.searchStages(name, PageRequest.of(page, size, Sort.by("name").ascending()));
  }

  @GetMapping("/{id}")
  public StageDTO getStageById(@PathVariable String id) {
    return stagePortIn.getStageById(id);
  }

  @GetMapping("name")
  public List<StageDTO> getStageByName(@RequestParam String name) {
    List<StageDTO> stageDTO = stagePortIn.getStageByName(name);
    return stageDTO;
  }

  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  public StageDTO saveStage(@Valid @RequestBody StageDTO stageDTO) {
    return stagePortIn.save(stageDTO);
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Void> deleteStage(@PathVariable String id) {
    boolean deleted = stagePortIn.deleteStage(id);
    if (!deleted) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.noContent().build();
  }
}
