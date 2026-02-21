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

    GameResponse create(GameRequest request);

    GameResponse getById(UUID id);

    Page<GameResponse> list(Platform platform, GameStatus status, LocalDate from, LocalDate to, Pageable pageable);

    GameResponse update(UUID id, GameRequest request);

    GameResponse updateStatus(UUID id, GameStatus status);

    void delete(UUID id);
}
