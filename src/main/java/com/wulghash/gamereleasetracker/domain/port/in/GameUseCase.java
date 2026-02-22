package com.wulghash.gamereleasetracker.domain.port.in;

import com.wulghash.gamereleasetracker.domain.model.GameStatus;
import com.wulghash.gamereleasetracker.domain.model.Platform;
import com.wulghash.gamereleasetracker.infrastructure.web.dto.GameRequest;
import com.wulghash.gamereleasetracker.infrastructure.web.dto.GameResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.UUID;

public interface GameUseCase {

    GameResponse create(UUID userId, GameRequest request);

    GameResponse getById(UUID id, UUID userId);

    Page<GameResponse> list(UUID userId, Platform platform, GameStatus status, LocalDate from, LocalDate to, Pageable pageable);

    GameResponse update(UUID id, UUID userId, GameRequest request);

    GameResponse updateStatus(UUID id, UUID userId, GameStatus status);

    void delete(UUID id, UUID userId);
}
