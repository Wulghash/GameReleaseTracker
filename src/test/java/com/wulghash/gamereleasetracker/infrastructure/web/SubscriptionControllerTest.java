package com.wulghash.gamereleasetracker.infrastructure.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wulghash.gamereleasetracker.domain.model.GameAlreadySubscribedException;
import com.wulghash.gamereleasetracker.domain.model.GameNotFoundException;
import com.wulghash.gamereleasetracker.domain.model.SubscriptionNotFoundException;
import com.wulghash.gamereleasetracker.domain.port.in.SubscriptionUseCase;
import com.wulghash.gamereleasetracker.infrastructure.web.dto.SubscribeRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SubscriptionController.class)
class SubscriptionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SubscriptionUseCase subscriptionUseCase;

    @Test
    void subscribeShouldReturn201() throws Exception {
        UUID gameId = UUID.randomUUID();
        SubscribeRequest request = new SubscribeRequest("player@example.com");

        mockMvc.perform(post("/api/v1/games/{id}/subscribe", gameId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        verify(subscriptionUseCase).subscribe(gameId, "player@example.com");
    }

    @Test
    void subscribeShouldReturn404WhenGameNotFound() throws Exception {
        UUID gameId = UUID.randomUUID();
        doThrow(new GameNotFoundException(gameId))
                .when(subscriptionUseCase).subscribe(gameId, "player@example.com");

        mockMvc.perform(post("/api/v1/games/{id}/subscribe", gameId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new SubscribeRequest("player@example.com"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void subscribeShouldReturn409WhenAlreadySubscribed() throws Exception {
        UUID gameId = UUID.randomUUID();
        doThrow(new GameAlreadySubscribedException(gameId, "player@example.com"))
                .when(subscriptionUseCase).subscribe(gameId, "player@example.com");

        mockMvc.perform(post("/api/v1/games/{id}/subscribe", gameId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new SubscribeRequest("player@example.com"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void subscribeShouldReturn400ForInvalidEmail() throws Exception {
        UUID gameId = UUID.randomUUID();

        mockMvc.perform(post("/api/v1/games/{id}/subscribe", gameId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new SubscribeRequest("not-an-email"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void unsubscribeShouldReturn200() throws Exception {
        UUID token = UUID.randomUUID();

        mockMvc.perform(get("/api/v1/unsubscribe/{token}", token))
                .andExpect(status().isOk());

        verify(subscriptionUseCase).unsubscribe(token);
    }

    @Test
    void unsubscribeShouldReturn404WhenTokenNotFound() throws Exception {
        UUID token = UUID.randomUUID();
        doThrow(new SubscriptionNotFoundException(token))
                .when(subscriptionUseCase).unsubscribe(token);

        mockMvc.perform(get("/api/v1/unsubscribe/{token}", token))
                .andExpect(status().isNotFound());
    }
}
