package com.sysluna.api.infrastructure.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import com.sysluna.api.infrastructure.security.RateLimiter;

import jakarta.servlet.http.Cookie;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthControllerIT {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private RateLimiter rateLimiter;

  @BeforeEach
  void resetRateLimiter() {
    rateLimiter.reset();
  }

  private String uniqueEmail() {
    return "auth-it-" + UUID.randomUUID() + "@example.com";
  }

  private void register(String email, String password) throws Exception {
    mockMvc.perform(post("/api/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"username":"user_%s","fullName":"IT User","email":"%s","password":"%s"}
                """.formatted(UUID.randomUUID(), email, password)))
        .andExpect(status().isCreated());
  }

  @Test
  void registerCreatesUserWithDefaultRoleAndNoPasswordLeak() throws Exception {
    String email = uniqueEmail();
    mockMvc.perform(post("/api/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"username":"user_%s","fullName":"IT User","email":"%s","password":"SenhaForte123"}
                """.formatted(UUID.randomUUID(), email)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.email").value(email))
        .andExpect(jsonPath("$.role").value("USER"))
        .andExpect(jsonPath("$.passwordHash").doesNotExist())
        .andExpect(jsonPath("$.password").doesNotExist());
  }

  @Test
  void setupCreatesFirstAdminUserWhenNoUsersExist() throws Exception {
    mockMvc.perform(post("/api/setup")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"username":"admin","fullName":"Administrator","email":"admin-setup@example.com","password":"SenhaForte123"}
                """))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.email").value("admin-setup@example.com"))
        .andExpect(jsonPath("$.role").value("ADMIN"));
  }

  @Test
  void setupIgnoresStaleAuthCookie() throws Exception {
    mockMvc.perform(post("/api/setup")
            .cookie(new Cookie("crm_token", "stale-token"))
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"username":"admin2","fullName":"Administrator Two","email":"admin-setup-2@example.com","password":"SenhaForte123"}
                """))
        .andExpect(status().isCreated());
  }

  @Test
  void registerWithDuplicateEmailReturnsConflict() throws Exception {
    String email = uniqueEmail();
    register(email, "SenhaForte123");

    mockMvc.perform(post("/api/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"username":"user_%s","fullName":"IT User 2","email":"%s","password":"OutraSenha123"}
                """.formatted(UUID.randomUUID(), email)))
        .andExpect(status().isConflict());
  }

  @Test
  void registerWithBlankFieldsReturnsBadRequest() throws Exception {
    mockMvc.perform(post("/api/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"username\":\"\",\"fullName\":\"\",\"email\":\"\",\"password\":\"\"}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void loginWithWrongPasswordReturnsUnauthorized() throws Exception {
    String email = uniqueEmail();
    register(email, "CorrectPass123");

    mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"email\":\"%s\",\"password\":\"WrongPass\"}".formatted(email)))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void meWithoutSessionReturnsUnauthorized() throws Exception {
    mockMvc.perform(get("/api/auth/me"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void loginSetsHttpOnlySessionCookieAndMeReturnsUser() throws Exception {
    String email = uniqueEmail();
    register(email, "CorrectPass123");

    MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"email\":\"%s\",\"password\":\"CorrectPass123\"}".formatted(email)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.user.email").value(email))
        .andExpect(jsonPath("$.token").exists())
        .andExpect(jsonPath("$.token").isString())
        .andReturn();

    Cookie sessionCookie = loginResult.getResponse().getCookie("crm_token");
    assertNotNull(sessionCookie, "login should set a crm_token cookie");
    assertTrue(sessionCookie.isHttpOnly(), "session cookie must be httpOnly");
    assertNotNull(sessionCookie.getValue());
    assertTrue(!sessionCookie.getValue().isBlank());

    mockMvc.perform(get("/api/auth/me").cookie(sessionCookie))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.email").value(email));
  }

  @Test
  void logoutClearsSessionCookie() throws Exception {
    String email = uniqueEmail();
    register(email, "CorrectPass123");

    MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"email\":\"%s\",\"password\":\"CorrectPass123\"}".formatted(email)))
        .andExpect(status().isOk())
        .andReturn();
    Cookie sessionCookie = loginResult.getResponse().getCookie("crm_token");

    MvcResult logoutResult = mockMvc.perform(post("/api/auth/logout").cookie(sessionCookie))
        .andExpect(status().isNoContent())
        .andReturn();
    Cookie clearedCookie = logoutResult.getResponse().getCookie("crm_token");
    assertNotNull(clearedCookie);
    assertEquals(0, clearedCookie.getMaxAge());

    mockMvc.perform(get("/api/auth/me").cookie(sessionCookie))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void loginIsRateLimitedAfterTooManyAttempts() throws Exception {
    String email = uniqueEmail();
    register(email, "CorrectPass123");

    String wrongLoginBody = "{\"email\":\"%s\",\"password\":\"WrongPass\"}".formatted(email);

    for (int i = 0; i < 10; i++) {
      mockMvc.perform(post("/api/auth/login")
              .contentType(MediaType.APPLICATION_JSON)
              .content(wrongLoginBody))
          .andExpect(status().isUnauthorized());
    }

    mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(wrongLoginBody))
        .andExpect(status().isTooManyRequests());
  }
}
