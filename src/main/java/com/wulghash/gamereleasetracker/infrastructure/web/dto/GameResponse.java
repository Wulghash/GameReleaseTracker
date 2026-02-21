package com.wulghash.gamereleasetracker.infrastructure.web.dto;

import com.wulghash.gamereleasetracker.domain.model.Game;
import com.wulghash.gamereleasetracker.domain.model.GameStatus;
import com.wulghash.gamereleasetracker.domain.model.Platform;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

public record GameResponse(
        UUID id,
        String title,
        String description,
        LocalDate releaseDate,
        Set<Platform> platforms,
        GameStatus status,
        String shopUrl,
        String imageUrl,
        String developer,
        String publisher,
        boolean tba,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static GameResponse from(Game game) {
        return new GameResponse(
                game.getId(),
                game.getTitle(),
                game.getDescription(),
                game.getReleaseDate(),
                game.getPlatforms(),
                game.getStatus(),
                game.getShopUrl(),
                game.getImageUrl(),
                game.getDeveloper(),
                game.getPublisher(),
                game.isTba(),
                game.getCreatedAt(),
                game.getUpdatedAt()
        );
    }
}
