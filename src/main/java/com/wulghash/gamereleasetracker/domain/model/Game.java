package com.wulghash.gamereleasetracker.domain.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Getter
@Builder(toBuilder = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Game {

    @EqualsAndHashCode.Include
    private final UUID id;

    private final String title;
    private final String description;
    private final LocalDate releaseDate;
    private final Set<Platform> platforms;
    private final GameStatus status;
    private final String shopUrl;
    private final String imageUrl;
    private final String developer;
    private final String publisher;
    private final boolean tba;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
}
