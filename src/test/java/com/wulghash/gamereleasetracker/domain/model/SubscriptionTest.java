package com.wulghash.gamereleasetracker.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SubscriptionTest {

    @Test
    void shouldCreateSubscriptionWithRequiredFields() {
        UUID id = UUID.randomUUID();
        UUID gameId = UUID.randomUUID();
        UUID token = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        Subscription subscription = Subscription.builder()
                .id(id)
                .gameId(gameId)
                .email("player@example.com")
                .unsubscribeToken(token)
                .createdAt(now)
                .build();

        assertThat(subscription.getId()).isEqualTo(id);
        assertThat(subscription.getGameId()).isEqualTo(gameId);
        assertThat(subscription.getEmail()).isEqualTo("player@example.com");
        assertThat(subscription.getUnsubscribeToken()).isEqualTo(token);
    }

    @Test
    void twoSubscriptionsWithSameIdShouldBeEqual() {
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        Subscription s1 = Subscription.builder()
                .id(id).gameId(UUID.randomUUID())
                .email("a@b.com").unsubscribeToken(UUID.randomUUID())
                .createdAt(now).build();

        Subscription s2 = Subscription.builder()
                .id(id).gameId(UUID.randomUUID())
                .email("a@b.com").unsubscribeToken(UUID.randomUUID())
                .createdAt(now).build();

        assertThat(s1).isEqualTo(s2);
        assertThat(s1.hashCode()).isEqualTo(s2.hashCode());
    }
}
