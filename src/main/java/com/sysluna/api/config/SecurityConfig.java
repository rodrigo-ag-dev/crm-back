package com.sysluna.api.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.sysluna.api.infrastructure.security.AuthRateLimitFilter;
import com.sysluna.api.infrastructure.security.CustomUserDetailsService;
import com.sysluna.api.infrastructure.security.JwtAuthenticationFilter;

@org.springframework.context.annotation.Configuration
@EnableMethodSecurity
public class SecurityConfig {

  private final JwtAuthenticationFilter jwtAuthenticationFilter;
  private final AuthRateLimitFilter authRateLimitFilter;
  private final CustomUserDetailsService userDetailsService;
  private final com.fasterxml.jackson.databind.ObjectMapper objectMapper;

  @Value("${app.cors.allowed-origins:http://localhost:5173,http://127.0.0.1:5173,http://192.168.10.20:5173,http://192.168.10.20}")
  private List<String> allowedOrigins;

  public SecurityConfig(
      JwtAuthenticationFilter jwtAuthenticationFilter,
      AuthRateLimitFilter authRateLimitFilter,
      CustomUserDetailsService userDetailsService,
      com.fasterxml.jackson.databind.ObjectMapper objectMapper) {
    this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    this.authRateLimitFilter = authRateLimitFilter;
    this.userDetailsService = userDetailsService;
    this.objectMapper = objectMapper;
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
    AuthenticationManagerBuilder authenticationManagerBuilder = http
        .getSharedObject(AuthenticationManagerBuilder.class);
    authenticationManagerBuilder
        .userDetailsService(userDetailsService)
        .passwordEncoder(passwordEncoder());
    return authenticationManagerBuilder.build();
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .csrf(csrf -> csrf.disable())
        .exceptionHandling(ex -> ex
            .authenticationEntryPoint((request, response, authException) -> writeProblemDetail(
                response,
                HttpStatus.UNAUTHORIZED,
                "Unauthorized",
                authException.getMessage(),
                request.getRequestURI()))
            .accessDeniedHandler((request, response, accessDeniedException) -> writeProblemDetail(
                response,
                HttpStatus.FORBIDDEN,
                "Forbidden",
                accessDeniedException.getMessage(),
                request.getRequestURI())))
        .authorizeHttpRequests(auth -> auth
            // Public routes
            .requestMatchers("/api/healthcheck").permitAll()
            .requestMatchers("/error").permitAll()
            .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
            .requestMatchers(HttpMethod.POST, "/api/setup").permitAll()
            .requestMatchers(HttpMethod.POST, "/api/tenants").permitAll()
            .requestMatchers("/api/auth/login", "/api/auth/register", "/api/auth/logout").permitAll()
            .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs", "/v3/api-docs/**", "/webjars/**")
            .permitAll()
            // Every other route requires authentication
            .anyRequest().authenticated())
        // Adds the JWT and rate-limiting filters before the default authentication filter
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
        .addFilterBefore(authRateLimitFilter, JwtAuthenticationFilter.class);

    return http.build();
  }

  private void writeProblemDetail(
      jakarta.servlet.http.HttpServletResponse response,
      HttpStatus status,
      String title,
      String detail,
      String path) throws java.io.IOException {
    ProblemDetail problemDetail = ProblemDetail.forStatus(status);
    problemDetail.setTitle(title);
    problemDetail.setDetail(detail);
    problemDetail.setProperty("path", path);

    response.setStatus(status.value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    objectMapper.writeValue(response.getOutputStream(), problemDetail);
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(allowedOrigins);
    configuration.setAllowedOriginPatterns(List.of("http://localhost:*", "http://127.0.0.1:*", "http://192.168.10.20:*"));
    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
    configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With", "Accept", "Origin", "X-Auth-Token"));
    configuration.setAllowCredentials(true);
    configuration.setExposedHeaders(List.of("Authorization", "Set-Cookie"));
    configuration.setMaxAge(3600L);
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }
}
