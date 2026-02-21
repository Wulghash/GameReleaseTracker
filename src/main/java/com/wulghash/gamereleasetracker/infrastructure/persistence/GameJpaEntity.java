package com.wulghash.gamereleasetracker.infrastructure.persistence;

import com.wulghash.gamereleasetracker.domain.model.Game;
import com.wulghash.gamereleasetracker.domain.model.GameStatus;
import com.wulghash.gamereleasetracker.domain.model.Platform;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Entity
@Table(name = "games")
@Getter
@Setter
@NoArgsConstructor
public class GameJpaEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "release_date", nullable = false)
    private LocalDate releaseDate;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "game_platforms", joinColumns = @JoinColumn(name = "game_id"))
    @Column(name = "platform")
    @Enumerated(EnumType.STRING)
    private Set<Platform> platforms = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GameStatus status;

    @Column(name = "shop_url")
    private String shopUrl;

    @Column(name = "image_url")
    private String imageUrl;

    private String developer;
    private String publisher;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public static GameJpaEntity from(Game game) {
        GameJpaEntity entity = new GameJpaEntity();
        entity.id = game.getId();
        entity.title = game.getTitle();
        entity.description = game.getDescription();
        entity.releaseDate = game.getReleaseDate();
        entity.platforms = game.getPlatforms() != null
                ? new HashSet<>(game.getPlatforms())
                : new HashSet<>();
        entity.status = game.getStatus();
        entity.shopUrl = game.getShopUrl();
        entity.imageUrl = game.getImageUrl();
        entity.developer = game.getDeveloper();
        entity.publisher = game.getPublisher();
        entity.createdAt = game.getCreatedAt();
        entity.updatedAt = game.getUpdatedAt();
        return entity;
    }

    public Game toDomain() {
        return Game.builder()
                .id(id)
                .title(title)
                .description(description)
                .releaseDate(releaseDate)
                .platforms(new HashSet<>(platforms))
                .status(status)
                .shopUrl(shopUrl)
                .imageUrl(imageUrl)
                .developer(developer)
                .publisher(publisher)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }
}
