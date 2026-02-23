package com.wulghash.gamereleasetracker.domain.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder(toBuilder = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class BacklogEntry {

    @EqualsAndHashCode.Include
    private final UUID id;

    private final UUID userId;
    private final Long igdbId;
    private final String name;
    private final String coverUrl;
    private final LocalDate releaseDate;
    private final BacklogStatus backlogStatus;
    private final Integer igdbScore;
    private final Integer rating;
    private final String notes;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
}
