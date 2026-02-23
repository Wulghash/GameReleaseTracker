package com.wulghash.gamereleasetracker.infrastructure.web.dto;

import com.wulghash.gamereleasetracker.domain.model.BacklogEntry;
import com.wulghash.gamereleasetracker.domain.model.BacklogStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record BacklogEntryResponse(
        UUID id,
        UUID userId,
        Long igdbId,
        String name,
        String coverUrl,
        LocalDate releaseDate,
        BacklogStatus backlogStatus,
        Integer igdbScore,
        Integer rating,
        String notes,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static BacklogEntryResponse from(BacklogEntry entry) {
        return new BacklogEntryResponse(
                entry.getId(),
                entry.getUserId(),
                entry.getIgdbId(),
                entry.getName(),
                entry.getCoverUrl(),
                entry.getReleaseDate(),
                entry.getBacklogStatus(),
                entry.getIgdbScore(),
                entry.getRating(),
                entry.getNotes(),
                entry.getCreatedAt(),
                entry.getUpdatedAt()
        );
    }
}
