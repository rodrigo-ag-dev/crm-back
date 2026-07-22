package com.sysluna.api.infrastructure.cache;

import java.util.Collection;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.CacheOperationInvocationContext;
import org.springframework.cache.interceptor.CacheResolver;
import org.springframework.stereotype.Component;

import com.sysluna.api.infrastructure.tenant.TenantContext;

import lombok.AllArgsConstructor;

/**
 * Namespaces every declared cache name by the current tenant's schema (e.g. "stages" becomes
 * cache "stages::crm_acme"), so cache reads/writes/evictions for one tenant never touch
 * another's data - Redis has no notion of the per-schema isolation Postgres gives us, so this
 * has to be enforced here. Falls back to "public" outside a request (jobs/tests), mirroring
 * TenantIdentifierResolver's own fallback.
 */
@Component("tenantCacheResolver")
@AllArgsConstructor
public class TenantCacheResolver implements CacheResolver {

  private final CacheManager cacheManager;

  @Override
  public Collection<? extends Cache> resolveCaches(CacheOperationInvocationContext<?> context) {
    String tenant = TenantContext.get();
    String suffix = tenant != null ? tenant : "public";

    return context.getOperation().getCacheNames().stream()
        .map(name -> cacheManager.getCache(name + "::" + suffix))
        .toList();
  }
}
