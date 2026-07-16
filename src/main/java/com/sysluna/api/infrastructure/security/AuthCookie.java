package com.sysluna.api.infrastructure.security;

import jakarta.servlet.http.HttpServletRequest;

public final class AuthCookie {
  public static final String NAME = "crm_token";

  private AuthCookie() {
  }

  public static String extractToken(HttpServletRequest request) {
    if (request.getCookies() == null) {
      return null;
    }
    for (jakarta.servlet.http.Cookie cookie : request.getCookies()) {
      if (NAME.equals(cookie.getName())) {
        return cookie.getValue();
      }
    }
    return null;
  }
}
