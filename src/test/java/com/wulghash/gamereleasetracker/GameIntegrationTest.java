package com.wulghash.gamereleasetracker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wulghash.gamereleasetracker.domain.model.AppUser;
import com.wulghash.gamereleasetracker.domain.model.GameStatus;
import com.wulghash.gamereleasetracker.domain.model.Platform;
import com.wulghash.gamereleasetracker.domain.port.out.UserRepository;
import com.wulghash.gamereleasetracker.infrastructure.web.dto.GameRequest;
import com.wulghash.gamereleasetracker.infrastructure.web.dto.GameStatusRequest;
import com.wulghash.gamereleasetracker.infrastructure.web.security.AppUserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.security.oauth2.client.registration.google.client-id=test-id",
        "spring.security.oauth2.client.registration.google.client-secret=test-secret"
})
@AutoConfigureMockMvc
@Testcontainers
class GameIntegrationTest {

    static final UUID TEST_USER_ID = UUID.randomUUID();

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("gamereleasetracker_it")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void insertTestUser() {
        userRepository.findByGoogleId("google-integration-test")
                .orElseGet(() -> userRepository.save(AppUser.builder()
                        .id(TEST_USER_ID)
                        .googleId("google-integration-test")
                        .email("integration@example.com")
                        .name("Integration Test User")
                        .createdAt(LocalDateTime.now())
                        .build()));
    }

    private AppUserPrincipal testPrincipal() {
        AppUser user = AppUser.builder()
                .id(TEST_USER_ID)
                .googleId("google-integration-test")
                .email("integration@example.com")
                .name("Integration Test User")
                .createdAt(LocalDateTime.now())
                .build();
        return new AppUserPrincipal(user);
    }

    @Test
    void fullCrudLifecycle() throws Exception {
        // CREATE
        GameRequest createRequest = GameRequest.builder()
                .title("Elden Ring 2")
                .description("Epic souls-like")
                .releaseDate(LocalDate.of(2026, 6, 15))
                .platforms(Set.of(Platform.PC, Platform.PS5))
                .developer("FromSoftware")
                .publisher("Bandai Namco")
                .build();

        MvcResult createResult = mockMvc.perform(post("/api/v1/games")
                        .with(oauth2Login().oauth2User(testPrincipal()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Elden Ring 2"))
                .andExpect(jsonPath("$.status").value("UPCOMING"))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andReturn();

        String responseBody = createResult.getResponse().getContentAsString();
        UUID id = UUID.fromString(objectMapper.readTree(responseBody).get("id").asText());

        // READ by id
        mockMvc.perform(get("/api/v1/games/{id}", id)
                        .with(oauth2Login().oauth2User(testPrincipal())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Elden Ring 2"))
                .andExpect(jsonPath("$.developer").value("FromSoftware"));

        // UPDATE
        GameRequest updateRequest = GameRequest.builder()
                .title("Elden Ring 2 — Updated")
                .releaseDate(LocalDate.of(2026, 9, 1))
                .platforms(Set.of(Platform.PC))
                .build();

        mockMvc.perform(put("/api/v1/games/{id}", id)
                        .with(oauth2Login().oauth2User(testPrincipal()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Elden Ring 2 — Updated"));

        // PATCH STATUS
        mockMvc.perform(patch("/api/v1/games/{id}/status", id)
                        .with(oauth2Login().oauth2User(testPrincipal()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new GameStatusRequest(GameStatus.RELEASED))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RELEASED"));

        // DELETE
        mockMvc.perform(delete("/api/v1/games/{id}", id)
                        .with(oauth2Login().oauth2User(testPrincipal())))
                .andExpect(status().isNoContent());

        // VERIFY deleted
        mockMvc.perform(get("/api/v1/games/{id}", id)
                        .with(oauth2Login().oauth2User(testPrincipal())))
                .andExpect(status().isNotFound());
    }

    @Test
    void listShouldFilterByPlatform() throws Exception {
        mockMvc.perform(post("/api/v1/games")
                        .with(oauth2Login().oauth2User(testPrincipal()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(GameRequest.builder()
                                .title("PC Only Game")
                                .releaseDate(LocalDate.of(2026, 3, 1))
                                .platforms(Set.of(Platform.PC))
                                .build())))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/games")
                        .with(oauth2Login().oauth2User(testPrincipal()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(GameRequest.builder()
                                .title("PS5 Only Game")
                                .releaseDate(LocalDate.of(2026, 5, 1))
                                .platforms(Set.of(Platform.PS5))
                                .build())))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/games").param("platform", "PC")
                        .with(oauth2Login().oauth2User(testPrincipal())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].title").value(org.hamcrest.Matchers.hasItem("PC Only Game")));
    }

    @Test
    void shouldReturn404ForUnknownId() throws Exception {
        mockMvc.perform(get("/api/v1/games/{id}", UUID.randomUUID())
                        .with(oauth2Login().oauth2User(testPrincipal())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void shouldReturn400ForInvalidRequest() throws Exception {
        mockMvc.perform(post("/api/v1/games")
                        .with(oauth2Login().oauth2User(testPrincipal()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray());
    }
}
