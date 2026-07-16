package com.sysluna.api.infrastructure.controller;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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

import com.sysluna.api.domain.model.Company;
import com.sysluna.api.domain.model.Contact;
import com.sysluna.api.infrastructure.repository.CompanyRepository;
import com.sysluna.api.infrastructure.repository.ContactRepository;
import com.sysluna.api.infrastructure.repository.StageRepository;
import com.sysluna.api.infrastructure.security.RateLimiter;

import jakarta.servlet.http.Cookie;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class DealControllerIT {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private RateLimiter rateLimiter;

  @Autowired
  private CompanyRepository companyRepository;

  @Autowired
  private ContactRepository contactRepository;

  @Autowired
  private StageRepository stageRepository;

  private String companyId;
  private String contactId;
  private String stageId;

  @BeforeEach
  void setUp() {
    rateLimiter.reset();

    Company company = companyRepository.save(Company.builder()
        .name("Acme " + UUID.randomUUID())
        .alias("Acme")
        .email("contato@acme.example.com")
        .phone("11999990000")
        .description("Test company")
        .idRegional("SP")
        .build());
    companyId = company.getId();

    Contact contact = contactRepository.save(Contact.builder()
        .companyId(companyId)
        .name("Contato Teste")
        .alias("Contato")
        .email("contato.pessoa@acme.example.com")
        .phone("11988880000")
        .build());
    contactId = contact.getId();

    stageId = stageRepository.findAll().stream().findFirst()
        .orElseThrow(() -> new IllegalStateException("No seeded stage found for tests"))
        .getId();
  }

  private Cookie registerAndLogin(String email, String password) throws Exception {
    mockMvc.perform(post("/api/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"username":"user_%s","fullName":"Deal IT User","email":"%s","password":"%s"}
                """.formatted(UUID.randomUUID(), email, password)))
        .andExpect(status().isCreated());

    MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"email\":\"%s\",\"password\":\"%s\"}".formatted(email, password)))
        .andExpect(status().isOk())
        .andReturn();

    return loginResult.getResponse().getCookie("crm_token");
  }

  private String uniqueEmail(String prefix) {
    return prefix + "-" + UUID.randomUUID() + "@example.com";
  }

  @Test
  void creatingDealWithoutSessionIsUnauthorized() throws Exception {
    mockMvc.perform(post("/api/deals")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"companyId":"%s","contactId":"%s","title":"Sem sessão"}
                """.formatted(companyId, contactId)))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void creatingDealWithBlankTitleReturnsBadRequest() throws Exception {
    Cookie session = registerAndLogin(uniqueEmail("owner"), "SenhaForte123");

    mockMvc.perform(post("/api/deals")
            .cookie(session)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"companyId":"%s","contactId":"%s","title":""}
                """.formatted(companyId, contactId)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void creatingDealAssignsCurrentUserAsOwnerAndIsSearchable() throws Exception {
    Cookie session = registerAndLogin(uniqueEmail("owner"), "SenhaForte123");
    String title = "Negócio " + UUID.randomUUID();

    mockMvc.perform(post("/api/deals")
            .cookie(session)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"companyId":"%s","contactId":"%s","title":"%s"}
                """.formatted(companyId, contactId, title)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.ownerId").isNotEmpty())
        .andExpect(jsonPath("$.title").value(title));

    mockMvc.perform(get("/api/deals/search")
            .cookie(session)
            .param("title", title))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].title").value(title));
  }

  @Test
  void dealsAreIsolatedBetweenOwners() throws Exception {
    Cookie ownerASession = registerAndLogin(uniqueEmail("ownerA"), "SenhaForte123");
    Cookie ownerBSession = registerAndLogin(uniqueEmail("ownerB"), "SenhaForte123");
    String title = "Negócio Privado " + UUID.randomUUID();

    MvcResult createResult = mockMvc.perform(post("/api/deals")
            .cookie(ownerASession)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"companyId":"%s","contactId":"%s","title":"%s"}
                """.formatted(companyId, contactId, title)))
        .andExpect(status().isOk())
        .andReturn();

    String dealId = com.jayway.jsonpath.JsonPath.read(createResult.getResponse().getContentAsString(), "$.id");

    // Owner B's search must not surface owner A's deal.
    MvcResult searchAsB = mockMvc.perform(get("/api/deals/search")
            .cookie(ownerBSession)
            .param("title", title))
        .andExpect(status().isOk())
        .andReturn();
    java.util.List<?> contentAsB = com.jayway.jsonpath.JsonPath.read(searchAsB.getResponse().getContentAsString(), "$.content");
    assertTrue(contentAsB.isEmpty());

    // Owner B cannot delete owner A's deal.
    mockMvc.perform(delete("/api/deals/" + dealId).cookie(ownerBSession))
        .andExpect(status().isNotFound());

    // Owner A can still manage their own deal.
    mockMvc.perform(delete("/api/deals/" + dealId).cookie(ownerASession))
        .andExpect(status().isNoContent());
  }

  @Test
  void dealLifecycleStageChangeAndWon() throws Exception {
    Cookie session = registerAndLogin(uniqueEmail("owner"), "SenhaForte123");
    String title = "Negócio Ciclo " + UUID.randomUUID();

    MvcResult createResult = mockMvc.perform(post("/api/deals")
            .cookie(session)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"companyId":"%s","contactId":"%s","title":"%s"}
                """.formatted(companyId, contactId, title)))
        .andExpect(status().isOk())
        .andReturn();
    String dealId = com.jayway.jsonpath.JsonPath.read(createResult.getResponse().getContentAsString(), "$.id");

    mockMvc.perform(post("/api/deals/stage")
            .cookie(session)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"id\":\"%s\",\"stageId\":\"%s\"}".formatted(dealId, stageId)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.stageId").value(stageId));

    mockMvc.perform(post("/api/deals/won")
            .cookie(session)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"id\":\"%s\"}".formatted(dealId)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.won").value(true))
        .andExpect(jsonPath("$.lost").value(false));

    mockMvc.perform(delete("/api/deals/" + dealId).cookie(session))
        .andExpect(status().isNoContent());

    mockMvc.perform(delete("/api/deals/" + dealId).cookie(session))
        .andExpect(status().isNotFound());
  }
}
