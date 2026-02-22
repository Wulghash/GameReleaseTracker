package com.wulghash.gamereleasetracker.infrastructure.persistence;

import com.wulghash.gamereleasetracker.domain.model.GameStatus;
import com.wulghash.gamereleasetracker.domain.model.Platform;
import com.wulghash.gamereleasetracker.domain.model.Subscription;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({SubscriptionRepositoryAdapter.class, GameRepositoryAdapter.class})
class SubscriptionRepositoryAdapterTest {

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
    private SubscriptionRepositoryAdapter subscriptionRepository;

    @Autowired
    private GameRepositoryAdapter gameRepository;

    @Autowired
    private TestEntityManager em;

    private UUID gameId;

    @BeforeEach
    void setUp() {
        AppUserJpaEntity user = new AppUserJpaEntity();
        user.setId(TEST_USER_ID);
        user.setGoogleId("google_" + TEST_USER_ID);
        user.setEmail("test@example.com");
        user.setName("Test User");
        user.setCreatedAt(LocalDateTime.now());
        em.persistAndFlush(user);

        var game = gameRepository.save(com.wulghash.gamereleasetracker.domain.model.Game.builder()
                .id(UUID.randomUUID())
                .userId(TEST_USER_ID)
                .title("Test Game")
                .releaseDate(LocalDate.of(2026, 6, 15))
                .platforms(Set.of(Platform.PC))
                .status(GameStatus.UPCOMING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());
        gameId = game.getId();
    }

    @Test
    void saveShouldPersistSubscription() {
        Subscription subscription = buildSubscription(gameId, "player@example.com");

        Subscription saved = subscriptionRepository.save(subscription);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getEmail()).isEqualTo("player@example.com");
        assertThat(saved.getUnsubscribeToken()).isNotNull();
    }

    @Test
    void existsByGameIdAndEmailShouldReturnTrueWhenExists() {
        subscriptionRepository.save(buildSubscription(gameId, "player@example.com"));

        assertThat(subscriptionRepository.existsByGameIdAndEmail(gameId, "player@example.com")).isTrue();
    }

    @Test
    void existsByGameIdAndEmailShouldReturnFalseWhenNotExists() {
        assertThat(subscriptionRepository.existsByGameIdAndEmail(gameId, "nobody@example.com")).isFalse();
    }

    @Test
    void findByUnsubscribeTokenShouldReturnSubscription() {
        Subscription saved = subscriptionRepository.save(buildSubscription(gameId, "player@example.com"));

        Optional<Subscription> found = subscriptionRepository.findByUnsubscribeToken(saved.getUnsubscribeToken());

        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("player@example.com");
    }

    @Test
    void findByUnsubscribeTokenShouldReturnEmptyForUnknownToken() {
        Optional<Subscription> found = subscriptionRepository.findByUnsubscribeToken(UUID.randomUUID());
        assertThat(found).isEmpty();
    }

    @Test
    void deleteByIdShouldRemoveSubscription() {
        Subscription saved = subscriptionRepository.save(buildSubscription(gameId, "player@example.com"));

        subscriptionRepository.deleteById(saved.getId());

        assertThat(subscriptionRepository.findByUnsubscribeToken(saved.getUnsubscribeToken())).isEmpty();
    }

    @Test
    void findAllByGameIdShouldReturnAllSubscribersForGame() {
        subscriptionRepository.save(buildSubscription(gameId, "alice@example.com"));
        subscriptionRepository.save(buildSubscription(gameId, "bob@example.com"));

        List<Subscription> result = subscriptionRepository.findAllByGameId(gameId);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(Subscription::getEmail)
                .containsExactlyInAnyOrder("alice@example.com", "bob@example.com");
    }

    private Subscription buildSubscription(UUID gameId, String email) {
        return Subscription.builder()
                .id(UUID.randomUUID())
                .gameId(gameId)
                .email(email)
                .unsubscribeToken(UUID.randomUUID())
                .createdAt(LocalDateTime.now())
                .build();
    }
}
