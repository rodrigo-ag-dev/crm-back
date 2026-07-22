package com.sysluna.api.infrastructure.tenant;

/**
 * Holds the current request's tenant schema name for the duration of the thread that
 * handles it. Set by JwtAuthenticationFilter right after authentication succeeds, and
 * MUST be cleared in a finally block once the request finishes - the servlet container
 * reuses threads across requests, so a stale value here would leak one tenant's schema
 * into another tenant's request.
 */
public final class TenantContext {

  private static final ThreadLocal<String> CURRENT_SCHEMA = new ThreadLocal<>();

  private TenantContext() {
  }

  public static void set(String schemaName) {
    CURRENT_SCHEMA.set(schemaName);
  }

  public static String get() {
    return CURRENT_SCHEMA.get();
  }

  public static void clear() {
    CURRENT_SCHEMA.remove();
  }
}
