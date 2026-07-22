package com.sysluna.api.application;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import com.sysluna.api.domain.dto.StageDTO;
import com.sysluna.api.infrastructure.cache.CachedPage;
import com.sysluna.api.ports.out.StagePortOut;

import lombok.AllArgsConstructor;

/**
 * Split out of StageService so the @Cacheable call goes through the Spring proxy: a method
 * calling another @Cacheable method on the same bean (self-invocation) bypasses the proxy and
 * silently skips caching entirely.
 */
@Component
@AllArgsConstructor
class StageSearchCache {

  private final StagePortOut stagePortOut;

  @Cacheable(cacheNames = "stages", cacheResolver = "tenantCacheResolver")
  public CachedPage<StageDTO> search(String name, Pageable pageable) {
    Page<StageDTO> page = stagePortOut.searchStages(name, pageable).map(StageDTO::fromStage);
    return new CachedPage<>(page.getContent(), page.getTotalElements());
  }
}
