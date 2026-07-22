package com.sysluna.api.infrastructure.cache;

import java.util.List;

/**
 * Cache-safe stand-in for a Spring Data {@code Page<T>}: content plus the total count needed to
 * rebuild pagination metadata, nothing else. Page/PageRequest/Sort serialize to Redis fine but
 * have no Jackson-usable constructor to deserialize back from - caching them directly blows up
 * on the next read. Callers reconstruct a {@code PageImpl} from this using the same
 * {@code Pageable} they queried with.
 */
public record CachedPage<T>(List<T> content, long totalElements) {
}
