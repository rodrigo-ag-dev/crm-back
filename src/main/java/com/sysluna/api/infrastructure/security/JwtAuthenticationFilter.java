package com.sysluna.api.infrastructure.security;

import java.io.IOException;
import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.sysluna.api.infrastructure.tenant.TenantContext;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private static final Set<String> PASSWORD_CHANGE_ALLOWLIST = Set.of(
      "/api/auth/me",
      "/api/auth/logout",
      "/api/auth/change-password");

  // POST /api/tenants is provisioning-secret-gated, not JWT-gated (see TenantController) -
  // it's deliberately NOT listed here though, since GET /api/tenants (list, for platform
  // admins) needs the normal JWT flow to resolve who's calling. POST works fine without
  // being public: no JWT cookie is ever sent to it, so the filter just no-ops and
  // SecurityConfig's explicit permitAll for that method+path takes over.
  private static final Set<String> PUBLIC_PATHS = Set.of(
      "/api/healthcheck",
      "/api/setup",
      "/api/auth/login",
      "/api/auth/register",
      "/api/auth/logout",
      "/swagger-ui.html",
      "/swagger-ui",
      "/v3/api-docs",
      "/webjars");

  private final JwtTokenProvider tokenProvider;
  private final CustomUserDetailsService userDetailsService;
  private final TokenBlocklist tokenBlocklist;
  private final ObjectMapper objectMapper;

  public JwtAuthenticationFilter(
      JwtTokenProvider tokenProvider,
      CustomUserDetailsService userDetailsService,
      TokenBlocklist tokenBlocklist,
      ObjectMapper objectMapper) {
    this.tokenProvider = tokenProvider;
    this.userDetailsService = userDetailsService;
    this.tokenBlocklist = tokenBlocklist;
    this.objectMapper = objectMapper;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    String path = request.getRequestURI();
    if (isPublicPath(path)) {
      SecurityContextHolder.clearContext();
      filterChain.doFilter(request, response);
      return;
    }

    try {
      String jwt = getJwtFromRequest(request);

      if (jwt != null) {
        if (!tokenProvider.validateToken(jwt)) {
          writeUnauthorized(response, request.getRequestURI(), "Invalid or expired token.");
          return;
        }

        if (tokenBlocklist.isBlocked(jwt)) {
          writeUnauthorized(response, request.getRequestURI(), "Session has been logged out.");
          return;
        }

        String email = tokenProvider.getEmailFromToken(jwt);
        var userDetails = userDetailsService.loadUserByUsername(email);

        if (!userDetails.isEnabled()) {
          writeUnauthorized(response, request.getRequestURI(), "This account has been deactivated.");
          return;
        }

        if (userDetails instanceof AppUserPrincipal principal
            && principal.isMustChangePassword()
            && !PASSWORD_CHANGE_ALLOWLIST.contains(request.getRequestURI())) {
          writeForbidden(response, request.getRequestURI(), "Password change required.");
          return;
        }

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
            userDetails, null, userDetails.getAuthorities());
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        if (userDetails instanceof AppUserPrincipal principal) {
          TenantContext.set(principal.getTenantSchema());
        }
      }
    } catch (Exception e) {
      logger.error("Could not set user authentication in security context", e);
      writeUnauthorized(response, request.getRequestURI(), "Invalid authentication token.");
      return;
    }

    try {
      filterChain.doFilter(request, response);
    } finally {
      // Threads are pooled and reused across requests - never let one request's
      // tenant schema leak into the next request handled by the same thread.
      TenantContext.clear();
    }
  }

  private boolean isPublicPath(String path) {
    if (path == null || path.isBlank()) {
      return false;
    }

    if (PUBLIC_PATHS.contains(path)) {
      return true;
    }

    return path.startsWith("/swagger-ui/") || path.startsWith("/v3/api-docs/") || path.startsWith("/webjars/");
  }

  private String getJwtFromRequest(HttpServletRequest request) {
    String bearerToken = request.getHeader("Authorization");
    if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
      return bearerToken.substring(7);
    }

    String xAuthToken = request.getHeader("X-Auth-Token");
    if (xAuthToken != null && !xAuthToken.isBlank()) {
      return xAuthToken;
    }

    return AuthCookie.extractToken(request);
  }

  private void writeUnauthorized(HttpServletResponse response, String path, String detail) throws IOException {
    SecurityContextHolder.clearContext();

    ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);
    problemDetail.setTitle("Unauthorized");
    problemDetail.setDetail(detail);
    problemDetail.setProperty("path", path);

    response.setStatus(HttpStatus.UNAUTHORIZED.value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    objectMapper.writeValue(response.getOutputStream(), problemDetail);
  }

  private void writeForbidden(HttpServletResponse response, String path, String detail) throws IOException {
    ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.FORBIDDEN);
    problemDetail.setTitle("Forbidden");
    problemDetail.setDetail(detail);
    problemDetail.setProperty("path", path);

    response.setStatus(HttpStatus.FORBIDDEN.value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    objectMapper.writeValue(response.getOutputStream(), problemDetail);
  }
}

