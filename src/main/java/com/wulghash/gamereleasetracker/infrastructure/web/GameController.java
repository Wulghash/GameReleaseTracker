package com.wulghash.gamereleasetracker.infrastructure.web;

import com.wulghash.gamereleasetracker.domain.model.GameStatus;
import com.wulghash.gamereleasetracker.domain.model.Platform;
import com.wulghash.gamereleasetracker.domain.port.in.GameUseCase;
import com.wulghash.gamereleasetracker.infrastructure.web.dto.GameRequest;
import com.wulghash.gamereleasetracker.infrastructure.web.dto.GameResponse;
import com.wulghash.gamereleasetracker.infrastructure.web.dto.GameStatusRequest;
import com.wulghash.gamereleasetracker.infrastructure.web.security.AppUserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/games")
@RequiredArgsConstructor
public class GameController {

    private final GameUseCase gameUseCase;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GameResponse create(@Valid @RequestBody GameRequest request,
                               @AuthenticationPrincipal AppUserPrincipal principal) {
        return gameUseCase.create(principal.getUserId(), request);
    }

    @GetMapping("/{id}")
    public GameResponse getById(@PathVariable UUID id,
                                @AuthenticationPrincipal AppUserPrincipal principal) {
        return gameUseCase.getById(id, principal.getUserId());
    }

    @GetMapping
    public Page<GameResponse> list(
            @RequestParam(required = false) Platform platform,
            @RequestParam(required = false) GameStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @PageableDefault(size = 20, sort = "releaseDate", direction = Sort.Direction.ASC) Pageable pageable,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        return gameUseCase.list(principal.getUserId(), platform, status, from, to, pageable);
    }

    @PutMapping("/{id}")
    public GameResponse update(@PathVariable UUID id,
                               @Valid @RequestBody GameRequest request,
                               @AuthenticationPrincipal AppUserPrincipal principal) {
        return gameUseCase.update(id, principal.getUserId(), request);
    }

    @PatchMapping("/{id}/status")
    public GameResponse updateStatus(@PathVariable UUID id,
                                     @Valid @RequestBody GameStatusRequest request,
                                     @AuthenticationPrincipal AppUserPrincipal principal) {
        return gameUseCase.updateStatus(id, principal.getUserId(), request.status());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id,
                       @AuthenticationPrincipal AppUserPrincipal principal) {
        gameUseCase.delete(id, principal.getUserId());
    }
}
