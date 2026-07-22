package com.sysluna.api.application;

import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.sysluna.api.domain.dto.StageDTO;
import com.sysluna.api.domain.model.Stage;
import com.sysluna.api.infrastructure.cache.CachedPage;
import com.sysluna.api.ports.in.StagePortIn;
import com.sysluna.api.ports.out.StagePortOut;

import lombok.AllArgsConstructor;

/**
 * Reads are cached in Redis (see CacheConfig/TenantCacheResolver) since stages change rarely.
 * The cache is populated lazily, only on the first request for a given query - never eagerly.
 * Both writes evict the whole "stages" cache for the current tenant rather than just the
 * affected key, since a save/delete can shift every page's ordering and any name-search result.
 */
@Service
@AllArgsConstructor
public class StageService implements StagePortIn {

  private final StagePortOut stagePortOut;
  private final StageSearchCache stageSearchCache;

  @Override
  public Page<StageDTO> searchStages(String name, Pageable pageable) {
    CachedPage<StageDTO> cached = stageSearchCache.search(name, pageable);
    return new PageImpl<>(cached.content(), pageable, cached.totalElements());
  }

  @Override
  @CacheEvict(cacheNames = "stages", cacheResolver = "tenantCacheResolver", allEntries = true)
  public StageDTO save(StageDTO stageDTO) {
    Stage stage = Stage.fromDTO(stageDTO);
    if (stageDTO.getId() != null) {
      stage.setId(stageDTO.getId());
    }
    return StageDTO.fromStage(stagePortOut.save(stage));
  }

  @Override
  @CacheEvict(cacheNames = "stages", cacheResolver = "tenantCacheResolver", allEntries = true)
  public boolean deleteStage(String id) {
    Stage stage = stagePortOut.findById(id).orElse(null);
    if (stage == null) {
      return false;
    }
    stagePortOut.delete(stage);
    return true;
  }

  @Override
  @Cacheable(cacheNames = "stages", cacheResolver = "tenantCacheResolver")
  public List<StageDTO> getStageByName(String name) {
    List<Stage> stages = stagePortOut.findByName(name);
    return stages != null && !stages.isEmpty()
        ? stages.stream().map(StageDTO::fromStage).toList()
        : null;
  }

  @Override
  @Cacheable(cacheNames = "stages", cacheResolver = "tenantCacheResolver")
  public StageDTO getStageById(String id) {
    return stagePortOut.findById(id).map(StageDTO::fromStage).orElse(null);
  }
}
