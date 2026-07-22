package com.sysluna.api.infrastructure.config;

import org.springframework.boot.cache.autoconfigure.RedisCacheManagerBuilderCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Redis-backed Spring Cache abstraction. Values are cached indefinitely (no TTL) - entries are
 * expected to be cleared explicitly via @CacheEvict on writes (see StageService), not to expire
 * on their own, since the data cached here (e.g. Stage) changes rarely. Cache reads/writes go
 * through {@link com.sysluna.api.infrastructure.cache.TenantCacheResolver} so every cache is
 * implicitly namespaced per tenant.
 *
 * Never cache Spring Data's Page/Pageable/Sort types directly - they serialize fine but have no
 * Jackson-usable constructor to deserialize back from, so a cached page blows up on the very
 * next read (see CachedPage, used by StageService instead of caching Page&lt;T&gt; as-is).
 */
@Configuration
@EnableCaching
public class CacheConfig {

  @Bean
  public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer() {
    ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    // EVERYTHING, not NON_FINAL: cached values are cached as their root type (e.g. CachedPage,
    // a record - implicitly final), and NON_FINAL deliberately skips embedding type info for
    // final root classes since there's normally no ambiguity to resolve. But deserialize(byte[])
    // always reads back through a generic Object target, which unconditionally expects a type
    // wrapper - so a final root type written without one fails to read back. EVERYTHING always
    // wraps, keeping write and read symmetric regardless of the cached type's finality.
    objectMapper.activateDefaultTyping(
        objectMapper.getPolymorphicTypeValidator(),
        ObjectMapper.DefaultTyping.EVERYTHING);

    // Deprecated (for removal) in favor of the Jackson-3-based GenericJacksonJsonRedisSerializer,
    // but that one requires tools.jackson.databind.ObjectMapper, not the com.fasterxml Jackson 2
    // this app (and its @RestController serialization) otherwise uses. Fine to keep until Spring
    // Data Redis actually drops it.
    GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer(objectMapper);

    RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
        .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
        .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer));

    return builder -> builder.cacheDefaults(config);
  }
}
