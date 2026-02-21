package com.wulghash.gamereleasetracker.infrastructure.web;

import com.wulghash.gamereleasetracker.domain.port.in.SubscriptionUseCase;
import com.wulghash.gamereleasetracker.infrastructure.web.dto.SubscribeRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionUseCase subscriptionUseCase;

    @PostMapping("/api/v1/games/{id}/subscribe")
    @ResponseStatus(HttpStatus.CREATED)
    public void subscribe(@PathVariable UUID id, @Valid @RequestBody SubscribeRequest request) {
        subscriptionUseCase.subscribe(id, request.email());
    }

    @GetMapping("/api/v1/unsubscribe/{token}")
    public void unsubscribe(@PathVariable UUID token) {
        subscriptionUseCase.unsubscribe(token);
    }
}
