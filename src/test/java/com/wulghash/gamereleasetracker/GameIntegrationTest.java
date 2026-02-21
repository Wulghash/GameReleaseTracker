package com.wulghash.gamereleasetracker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wulghash.gamereleasetracker.domain.model.GameStatus;
import com.wulghash.gamereleasetracker.domain.model.Platform;
import com.wulghash.gamereleasetracker.infrastructure.web.dto.GameRequest;
import com.wulghash.gamereleasetracker.infrastructure.web.dto.GameStatusRequest;
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
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class GameIntegrationTest {

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
        mockMvc.perform(get("/api/v1/games/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Elden Ring 2"))
                .andExpect(jsonPath("$.developer").value("FromSoftware"));

        // LIST
        mockMvc.perform(get("/api/v1/games"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(1));

        // UPDATE
        GameRequest updateRequest = GameRequest.builder()
                .title("Elden Ring 2 — Updated")
                .releaseDate(LocalDate.of(2026, 9, 1))
                .platforms(Set.of(Platform.PC))
                .build();

        mockMvc.perform(put("/api/v1/games/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Elden Ring 2 — Updated"));

        // PATCH STATUS
        mockMvc.perform(patch("/api/v1/games/{id}/status", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new GameStatusRequest(GameStatus.RELEASED))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RELEASED"));

        // DELETE
        mockMvc.perform(delete("/api/v1/games/{id}", id))
                .andExpect(status().isNoContent());

        // VERIFY deleted
        mockMvc.perform(get("/api/v1/games/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void listShouldFilterByPlatform() throws Exception {
        mockMvc.perform(post("/api/v1/games")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(GameRequest.builder()
                                .title("PC Only Game")
                                .releaseDate(LocalDate.of(2026, 3, 1))
                                .platforms(Set.of(Platform.PC))
                                .build())))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/games")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(GameRequest.builder()
                                .title("PS5 Only Game")
                                .releaseDate(LocalDate.of(2026, 5, 1))
                                .platforms(Set.of(Platform.PS5))
                                .build())))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/games").param("platform", "PC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].title").value(org.hamcrest.Matchers.hasItem("PC Only Game")));
    }

    @Test
    void shouldReturn404ForUnknownId() throws Exception {
        mockMvc.perform(get("/api/v1/games/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void shouldReturn400ForInvalidRequest() throws Exception {
        String invalidBody = "{}";

        mockMvc.perform(post("/api/v1/games")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray());
    }
}
