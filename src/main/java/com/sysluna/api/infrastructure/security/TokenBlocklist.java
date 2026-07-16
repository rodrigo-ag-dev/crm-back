package com.sysluna.api.infrastructure.security;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.stereotype.Component;

/**
 * Tracks JWTs that were explicitly logged out so they stop being accepted even though the token
 * itself remains cryptographically valid until it expires (JWTs are stateless by design).
 * In-memory and single-instance only, same tradeoff as {@link RateLimiter}.
 */
@Component
public class TokenBlocklist {

  private final Map<String, Long> blockedUntilEpochMs = new ConcurrentHashMap<>();

  public void block(String token, long expiresAtEpochMs) {
    blockedUntilEpochMs.put(token, expiresAtEpochMs);
    if (ThreadLocalRandom.current().nextInt(20) == 0) {
      cleanup();
    }
  }

  public boolean isBlocked(String token) {
    Long expiresAt = blockedUntilEpochMs.get(token);
    if (expiresAt == null) {
      return false;
    }
    if (expiresAt < System.currentTimeMillis()) {
      blockedUntilEpochMs.remove(token);
      return false;
    }
    return true;
  }

  private void cleanup() {
    long now = System.currentTimeMillis();
    blockedUntilEpochMs.entrySet().removeIf(entry -> entry.getValue() < now);
  }
}
