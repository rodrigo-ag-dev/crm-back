package com.sysluna.api.infrastructure.security;

import java.io.IOException;
import java.time.Duration;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Throttles brute-force/mass-account-creation attempts against the public auth endpoints.
 * Keyed by client IP; login and register are tracked as separate buckets.
 */
@Component
public class AuthRateLimitFilter extends OncePerRequestFilter {

  private static final int LOGIN_MAX_ATTEMPTS = 10;
  private static final Duration LOGIN_WINDOW = Duration.ofMinutes(5);

  private static final int REGISTER_MAX_ATTEMPTS = 5;
  private static final Duration REGISTER_WINDOW = Duration.ofHours(1);

  private final RateLimiter rateLimiter;
  private final ObjectMapper objectMapper;

  public AuthRateLimitFilter(RateLimiter rateLimiter, ObjectMapper objectMapper) {
    this.rateLimiter = rateLimiter;
    this.objectMapper = objectMapper;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    String uri = request.getRequestURI();
    boolean isLogin = uri.endsWith("/api/auth/login");
    boolean isRegister = uri.endsWith("/api/auth/register");

    if (!isLogin && !isRegister) {
      filterChain.doFilter(request, response);
      return;
    }

    int maxAttempts = isLogin ? LOGIN_MAX_ATTEMPTS : REGISTER_MAX_ATTEMPTS;
    Duration window = isLogin ? LOGIN_WINDOW : REGISTER_WINDOW;
    String key = (isLogin ? "login:" : "register:") + clientIp(request);

    if (!rateLimiter.tryAcquire(key, maxAttempts, window)) {
      writeTooManyRequests(response, uri, window);
      return;
    }

    filterChain.doFilter(request, response);
  }

  private String clientIp(HttpServletRequest request) {
    String forwardedFor = request.getHeader("X-Forwarded-For");
    if (forwardedFor != null && !forwardedFor.isBlank()) {
      return forwardedFor.split(",")[0].trim();
    }
    return request.getRemoteAddr();
  }

  private void writeTooManyRequests(HttpServletResponse response, String path, Duration window) throws IOException {
    ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.TOO_MANY_REQUESTS);
    problemDetail.setTitle("Too Many Requests");
    problemDetail.setDetail("Too many attempts. Please try again later.");
    problemDetail.setProperty("path", path);

    response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
    response.setHeader("Retry-After", String.valueOf(window.toSeconds()));
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    objectMapper.writeValue(response.getOutputStream(), problemDetail);
  }
}
