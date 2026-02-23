package com.wulghash.gamereleasetracker.domain.port.in;

import com.wulghash.gamereleasetracker.domain.model.Game;
import com.wulghash.gamereleasetracker.domain.model.GameStatus;
import com.wulghash.gamereleasetracker.domain.model.Platform;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

public interface GameUseCase {

    Game create(UUID userId, GameCommand cmd);

    Game getById(UUID id, UUID userId);

    Page<Game> list(UUID userId, Platform platform, GameStatus status, LocalDate from, LocalDate to, Pageable pageable);

    Game update(UUID id, UUID userId, GameCommand cmd);

    Game updateStatus(UUID id, UUID userId, GameStatus status);

    void delete(UUID id, UUID userId);

    record GameCommand(
            String title,
            String description,
            LocalDate releaseDate,
            Set<Platform> platforms,
            String shopUrl,
            String imageUrl,
            String developer,
            String publisher,
            Long igdbId,
            boolean tba
    ) {}
}
