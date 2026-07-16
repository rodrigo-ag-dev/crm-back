package com.sysluna.api.infrastructure.security;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Component;

/**
 * In-memory fixed-window rate limiter, keyed by an arbitrary string (e.g. "login:203.0.113.4").
 * Single-instance only: attempts are not shared across app instances. Good enough for a single
 * backend deployment; a multi-instance deployment would need a shared store (e.g. Redis).
 */
@Component
public class RateLimiter {

  private static final long CLEANUP_MAX_AGE_MS = Duration.ofHours(2).toMillis();

  private final Map<String, Window> windows = new ConcurrentHashMap<>();

  public boolean tryAcquire(String key, int maxAttempts, Duration window) {
    long now = System.currentTimeMillis();
    long windowMs = window.toMillis();

    Window current = windows.compute(key, (k, existing) -> {
      if (existing == null || now - existing.windowStart >= windowMs) {
        return new Window(now, new AtomicInteger(1));
      }
      existing.count.incrementAndGet();
      return existing;
    });

    if (ThreadLocalRandom.current().nextInt(100) == 0) {
      cleanup(now);
    }

    return current.count.get() <= maxAttempts;
  }

  public void reset() {
    windows.clear();
  }

  private void cleanup(long now) {
    windows.entrySet().removeIf(entry -> now - entry.getValue().windowStart > CLEANUP_MAX_AGE_MS);
  }

  private static final class Window {
    private final long windowStart;
    private final AtomicInteger count;

    private Window(long windowStart, AtomicInteger count) {
      this.windowStart = windowStart;
      this.count = count;
    }
  }
}
