package com.wulghash.gamereleasetracker.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GameTest {

    @Test
    void shouldCreateGameWithRequiredFields() {
        LocalDate releaseDate = LocalDate.of(2026, 6, 15);
        Set<Platform> platforms = Set.of(Platform.PC, Platform.PS5);

        Game game = Game.builder()
                .id(UUID.randomUUID())
                .title("Elden Ring 2")
                .releaseDate(releaseDate)
                .platforms(platforms)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        assertThat(game.getTitle()).isEqualTo("Elden Ring 2");
        assertThat(game.getReleaseDate()).isEqualTo(releaseDate);
        assertThat(game.getPlatforms()).containsExactlyInAnyOrder(Platform.PC, Platform.PS5);
    }

    @Test
    void shouldCreateGameWithAllOptionalFields() {
        Game game = Game.builder()
                .id(UUID.randomUUID())
                .title("Hollow Knight 2")
                .description("Sequel to the beloved metroidvania")
                .releaseDate(LocalDate.of(2026, 9, 1))
                .platforms(Set.of(Platform.PC, Platform.SWITCH))
                .shopUrl("https://store.steampowered.com/app/hollow-knight-2")
                .imageUrl("https://example.com/image.jpg")
                .developer("Team Cherry")
                .publisher("Team Cherry")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        assertThat(game.getDescription()).isEqualTo("Sequel to the beloved metroidvania");
        assertThat(game.getShopUrl()).isEqualTo("https://store.steampowered.com/app/hollow-knight-2");
        assertThat(game.getImageUrl()).isEqualTo("https://example.com/image.jpg");
        assertThat(game.getDeveloper()).isEqualTo("Team Cherry");
        assertThat(game.getPublisher()).isEqualTo("Team Cherry");
    }

    @Test
    void shouldSupportAllPlatformValues() {
        assertThat(Platform.values()).containsExactlyInAnyOrder(
                Platform.PC,
                Platform.PS5,
                Platform.XBOX,
                Platform.SWITCH
        );
    }

    @Test
    void twoGamesWithSameIdShouldBeEqual() {
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        Game game1 = Game.builder().id(id).title("Game A")
                .releaseDate(LocalDate.now()).platforms(Set.of(Platform.PC))
                .createdAt(now).updatedAt(now).build();

        Game game2 = Game.builder().id(id).title("Game A")
                .releaseDate(LocalDate.now()).platforms(Set.of(Platform.PC))
                .createdAt(now).updatedAt(now).build();

        assertThat(game1).isEqualTo(game2);
        assertThat(game1.hashCode()).isEqualTo(game2.hashCode());
    }
}
