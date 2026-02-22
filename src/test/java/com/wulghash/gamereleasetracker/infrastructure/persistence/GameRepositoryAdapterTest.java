package com.wulghash.gamereleasetracker.infrastructure.persistence;

import com.wulghash.gamereleasetracker.domain.model.Game;
import com.wulghash.gamereleasetracker.domain.model.GameStatus;
import com.wulghash.gamereleasetracker.domain.model.Platform;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(GameRepositoryAdapter.class)
class GameRepositoryAdapterTest {

    static final UUID TEST_USER_ID = UUID.randomUUID();

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("gamereleasetracker_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.flyway.enabled", () -> "true");
    }

    @Autowired
    private GameRepositoryAdapter repository;

    @Autowired
    private TestEntityManager em;

    @BeforeEach
    void insertUser() {
        AppUserJpaEntity user = new AppUserJpaEntity();
        user.setId(TEST_USER_ID);
        user.setGoogleId("google_" + TEST_USER_ID);
        user.setEmail("test@example.com");
        user.setName("Test User");
        user.setCreatedAt(LocalDateTime.now());
        em.persistAndFlush(user);
    }

    @Test
    void saveShouldPersistGame() {
        Game game = buildGame("Elden Ring 2", Set.of(Platform.PC, Platform.PS5),
                LocalDate.of(2026, 6, 15));

        Game saved = repository.save(game);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getTitle()).isEqualTo("Elden Ring 2");
        assertThat(saved.getPlatforms()).containsExactlyInAnyOrder(Platform.PC, Platform.PS5);
    }

    @Test
    void findByIdShouldReturnGameWhenExists() {
        Game saved = repository.save(buildGame("Hollow Knight 2", Set.of(Platform.PC),
                LocalDate.of(2026, 9, 1)));

        Optional<Game> found = repository.findById(saved.getId(), TEST_USER_ID);

        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("Hollow Knight 2");
    }

    @Test
    void findByIdShouldReturnEmptyWhenNotExists() {
        Optional<Game> found = repository.findById(UUID.randomUUID(), TEST_USER_ID);
        assertThat(found).isEmpty();
    }

    @Test
    void findAllShouldReturnPagedResults() {
        repository.save(buildGame("Game A", Set.of(Platform.PC), LocalDate.of(2026, 3, 1)));
        repository.save(buildGame("Game B", Set.of(Platform.PS5), LocalDate.of(2026, 5, 1)));
        repository.save(buildGame("Game C", Set.of(Platform.PC), LocalDate.of(2026, 8, 1)));

        Page<Game> result = repository.findAll(TEST_USER_ID, null, null, null, null, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSizeGreaterThanOrEqualTo(3);
    }

    @Test
    void findAllShouldFilterByPlatform() {
        repository.save(buildGame("PC Game", Set.of(Platform.PC), LocalDate.of(2026, 3, 1)));
        repository.save(buildGame("PS5 Game", Set.of(Platform.PS5), LocalDate.of(2026, 5, 1)));

        Page<Game> result = repository.findAll(TEST_USER_ID, Platform.PC, null, null, null, PageRequest.of(0, 10));

        assertThat(result.getContent())
                .allMatch(g -> g.getPlatforms().contains(Platform.PC));
    }

    @Test
    void findAllShouldFilterByDateRange() {
        repository.save(buildGame("Early Game", Set.of(Platform.PC), LocalDate.of(2026, 1, 1)));
        repository.save(buildGame("Mid Game", Set.of(Platform.PC), LocalDate.of(2026, 6, 1)));
        repository.save(buildGame("Late Game", Set.of(Platform.PC), LocalDate.of(2026, 12, 1)));

        Page<Game> result = repository.findAll(TEST_USER_ID, null, null,
                LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 7, 1),
                PageRequest.of(0, 10));

        assertThat(result.getContent()).allMatch(g ->
                !g.getReleaseDate().isBefore(LocalDate.of(2026, 5, 1)) &&
                !g.getReleaseDate().isAfter(LocalDate.of(2026, 7, 1)));
    }

    @Test
    void deleteByIdShouldRemoveGame() {
        Game saved = repository.save(buildGame("To Delete", Set.of(Platform.PC), LocalDate.now()));

        repository.deleteById(saved.getId(), TEST_USER_ID);

        assertThat(repository.findById(saved.getId(), TEST_USER_ID)).isEmpty();
    }

    @Test
    void existsByIdShouldReturnTrueWhenExists() {
        Game saved = repository.save(buildGame("Exists", Set.of(Platform.PC), LocalDate.now()));
        assertThat(repository.existsById(saved.getId(), TEST_USER_ID)).isTrue();
    }

    @Test
    void existsByIdShouldReturnFalseWhenNotExists() {
        assertThat(repository.existsById(UUID.randomUUID(), TEST_USER_ID)).isFalse();
    }

    @Test
    void findAllShouldFilterByStatus() {
        repository.save(buildGame("Upcoming Game", Set.of(Platform.PC), LocalDate.of(2026, 6, 1), GameStatus.UPCOMING));
        repository.save(buildGame("Released Game", Set.of(Platform.PC), LocalDate.of(2025, 1, 1), GameStatus.RELEASED));

        Page<Game> result = repository.findAll(TEST_USER_ID, null, GameStatus.RELEASED, null, null, PageRequest.of(0, 10));

        assertThat(result.getContent()).allMatch(g -> g.getStatus() == GameStatus.RELEASED);
    }

    private Game buildGame(String title, Set<Platform> platforms, LocalDate releaseDate) {
        return buildGame(title, platforms, releaseDate, GameStatus.UPCOMING);
    }

    private Game buildGame(String title, Set<Platform> platforms, LocalDate releaseDate, GameStatus status) {
        LocalDateTime now = LocalDateTime.now();
        return Game.builder()
                .id(UUID.randomUUID())
                .userId(TEST_USER_ID)
                .title(title)
                .releaseDate(releaseDate)
                .platforms(platforms)
                .status(status)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }
}
