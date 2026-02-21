package com.wulghash.gamereleasetracker.infrastructure.web;

import com.wulghash.gamereleasetracker.domain.model.GameStatus;
import com.wulghash.gamereleasetracker.domain.model.Platform;
import com.wulghash.gamereleasetracker.domain.port.in.GameUseCase;
import com.wulghash.gamereleasetracker.infrastructure.web.dto.GameRequest;
import com.wulghash.gamereleasetracker.infrastructure.web.dto.GameResponse;
import com.wulghash.gamereleasetracker.infrastructure.web.dto.GameStatusRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
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
    public GameResponse create(@Valid @RequestBody GameRequest request) {
        return gameUseCase.create(request);
    }

    @GetMapping("/{id}")
    public GameResponse getById(@PathVariable UUID id) {
        return gameUseCase.getById(id);
    }

    @GetMapping
    public Page<GameResponse> list(
            @RequestParam(required = false) Platform platform,
            @RequestParam(required = false) GameStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @PageableDefault(size = 20, sort = "releaseDate", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return gameUseCase.list(platform, status, from, to, pageable);
    }

    @PutMapping("/{id}")
    public GameResponse update(@PathVariable UUID id, @Valid @RequestBody GameRequest request) {
        return gameUseCase.update(id, request);
    }

    @PatchMapping("/{id}/status")
    public GameResponse updateStatus(@PathVariable UUID id, @Valid @RequestBody GameStatusRequest request) {
        return gameUseCase.updateStatus(id, request.status());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        gameUseCase.delete(id);
    }
}
