package com.wulghash.gamereleasetracker.infrastructure.persistence;

import com.wulghash.gamereleasetracker.domain.model.BacklogEntry;
import com.wulghash.gamereleasetracker.domain.model.BacklogStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "backlog_entries")
@Getter
@Setter
@NoArgsConstructor
public class BacklogEntryJpaEntity {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "igdb_id", nullable = false)
    private Long igdbId;

    @Column(nullable = false)
    private String name;

    @Column(name = "cover_url", length = 500)
    private String coverUrl;

    @Column(name = "release_date")
    private LocalDate releaseDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "backlog_status", nullable = false)
    private BacklogStatus backlogStatus;

    @Column(name = "igdb_score")
    private Short igdbScore;

    private Short rating;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public static BacklogEntryJpaEntity from(BacklogEntry entry) {
        BacklogEntryJpaEntity entity = new BacklogEntryJpaEntity();
        entity.id = entry.getId();
        entity.userId = entry.getUserId();
        entity.igdbId = entry.getIgdbId();
        entity.name = entry.getName();
        entity.coverUrl = entry.getCoverUrl();
        entity.releaseDate = entry.getReleaseDate();
        entity.backlogStatus = entry.getBacklogStatus();
        entity.igdbScore = entry.getIgdbScore() != null ? entry.getIgdbScore().shortValue() : null;
        entity.rating = entry.getRating() != null ? entry.getRating().shortValue() : null;
        entity.notes = entry.getNotes();
        entity.createdAt = entry.getCreatedAt();
        entity.updatedAt = entry.getUpdatedAt();
        return entity;
    }

    public BacklogEntry toDomain() {
        return BacklogEntry.builder()
                .id(id)
                .userId(userId)
                .igdbId(igdbId)
                .name(name)
                .coverUrl(coverUrl)
                .releaseDate(releaseDate)
                .backlogStatus(backlogStatus)
                .igdbScore(igdbScore != null ? igdbScore.intValue() : null)
                .rating(rating != null ? rating.intValue() : null)
                .notes(notes)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }
}
