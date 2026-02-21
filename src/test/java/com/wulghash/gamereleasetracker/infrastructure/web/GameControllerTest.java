package com.wulghash.gamereleasetracker.infrastructure.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wulghash.gamereleasetracker.domain.model.GameNotFoundException;
import com.wulghash.gamereleasetracker.domain.model.GameStatus;
import com.wulghash.gamereleasetracker.domain.model.InvalidStatusTransitionException;
import com.wulghash.gamereleasetracker.domain.model.Platform;
import com.wulghash.gamereleasetracker.domain.port.in.GameUseCase;
import com.wulghash.gamereleasetracker.infrastructure.web.dto.GameRequest;
import com.wulghash.gamereleasetracker.infrastructure.web.dto.GameResponse;
import com.wulghash.gamereleasetracker.infrastructure.web.dto.GameStatusRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GameController.class)
class GameControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private GameUseCase gameUseCase;

    @Test
    void postShouldReturn201WithUpcomingStatus() throws Exception {
        GameRequest request = GameRequest.builder()
                .title("Elden Ring 2")
                .releaseDate(LocalDate.of(2026, 6, 15))
                .platforms(Set.of(Platform.PC))
                .build();

        GameResponse response = buildResponse(UUID.randomUUID(), "Elden Ring 2", GameStatus.UPCOMING);
        when(gameUseCase.create(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/games")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Elden Ring 2"))
                .andExpect(jsonPath("$.status").value("UPCOMING"))
                .andExpect(jsonPath("$.id").isNotEmpty());
    }

    @Test
    void postWithMissingTitleShouldReturn400() throws Exception {
        GameRequest request = GameRequest.builder()
                .releaseDate(LocalDate.of(2026, 6, 15))
                .platforms(Set.of(Platform.PC))
                .build();

        mockMvc.perform(post("/api/v1/games")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void postWithMissingPlatformsShouldReturn400() throws Exception {
        GameRequest request = GameRequest.builder()
                .title("Some Game")
                .releaseDate(LocalDate.of(2026, 6, 15))
                .build();

        mockMvc.perform(post("/api/v1/games")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getByIdShouldReturn200WhenFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(gameUseCase.getById(id)).thenReturn(buildResponse(id, "Hollow Knight 2", GameStatus.UPCOMING));

        mockMvc.perform(get("/api/v1/games/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.title").value("Hollow Knight 2"));
    }

    @Test
    void getByIdShouldReturn404WhenNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(gameUseCase.getById(id)).thenThrow(new GameNotFoundException(id));

        mockMvc.perform(get("/api/v1/games/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void getShouldReturnPagedList() throws Exception {
        List<GameResponse> games = List.of(
                buildResponse(UUID.randomUUID(), "Game A", GameStatus.UPCOMING),
                buildResponse(UUID.randomUUID(), "Game B", GameStatus.UPCOMING)
        );
        when(gameUseCase.list(any(), any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(games, PageRequest.of(0, 10), 2));

        mockMvc.perform(get("/api/v1/games"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void putShouldReturn200WithUpdatedGame() throws Exception {
        UUID id = UUID.randomUUID();
        GameRequest request = GameRequest.builder()
                .title("Updated Title")
                .releaseDate(LocalDate.of(2026, 9, 1))
                .platforms(Set.of(Platform.PS5))
                .build();

        when(gameUseCase.update(eq(id), any())).thenReturn(buildResponse(id, "Updated Title", GameStatus.UPCOMING));

        mockMvc.perform(put("/api/v1/games/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"));
    }

    @Test
    void putShouldReturn404WhenGameNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        GameRequest request = GameRequest.builder()
                .title("Updated")
                .releaseDate(LocalDate.of(2026, 9, 1))
                .platforms(Set.of(Platform.PC))
                .build();

        when(gameUseCase.update(eq(id), any())).thenThrow(new GameNotFoundException(id));

        mockMvc.perform(put("/api/v1/games/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void patchStatusShouldReturn200WithUpdatedStatus() throws Exception {
        UUID id = UUID.randomUUID();
        GameStatusRequest request = new GameStatusRequest(GameStatus.RELEASED);
        when(gameUseCase.updateStatus(eq(id), eq(GameStatus.RELEASED)))
                .thenReturn(buildResponse(id, "Elden Ring 2", GameStatus.RELEASED));

        mockMvc.perform(patch("/api/v1/games/{id}/status", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RELEASED"));
    }

    @Test
    void patchStatusShouldReturn422OnInvalidTransition() throws Exception {
        UUID id = UUID.randomUUID();
        GameStatusRequest request = new GameStatusRequest(GameStatus.UPCOMING);
        when(gameUseCase.updateStatus(eq(id), any()))
                .thenThrow(new InvalidStatusTransitionException(GameStatus.RELEASED, GameStatus.UPCOMING));

        mockMvc.perform(patch("/api/v1/games/{id}/status", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void patchStatusShouldReturn404WhenGameNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        GameStatusRequest request = new GameStatusRequest(GameStatus.RELEASED);
        when(gameUseCase.updateStatus(eq(id), any())).thenThrow(new GameNotFoundException(id));

        mockMvc.perform(patch("/api/v1/games/{id}/status", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteShouldReturn204() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/games/{id}", id))
                .andExpect(status().isNoContent());

        verify(gameUseCase).delete(id);
    }

    @Test
    void deleteShouldReturn404WhenNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(new GameNotFoundException(id)).when(gameUseCase).delete(id);

        mockMvc.perform(delete("/api/v1/games/{id}", id))
                .andExpect(status().isNotFound());
    }

    private GameResponse buildResponse(UUID id, String title, GameStatus status) {
        return new GameResponse(
                id, title, null,
                LocalDate.of(2026, 6, 15),
                Set.of(Platform.PC),
                status,
                null, null, null, null,
                false,
                LocalDateTime.now(), LocalDateTime.now()
        );
    }
}
