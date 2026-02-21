package com.wulghash.gamereleasetracker.infrastructure.persistence;

import com.wulghash.gamereleasetracker.domain.model.Subscription;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "subscriptions")
@Getter
@Setter
@NoArgsConstructor
public class SubscriptionJpaEntity {

    @Id
    private UUID id;

    @Column(name = "game_id", nullable = false)
    private UUID gameId;

    @Column(nullable = false)
    private String email;

    @Column(name = "unsubscribe_token", nullable = false, unique = true)
    private UUID unsubscribeToken;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public static SubscriptionJpaEntity from(Subscription subscription) {
        SubscriptionJpaEntity entity = new SubscriptionJpaEntity();
        entity.id = subscription.getId();
        entity.gameId = subscription.getGameId();
        entity.email = subscription.getEmail();
        entity.unsubscribeToken = subscription.getUnsubscribeToken();
        entity.createdAt = subscription.getCreatedAt();
        return entity;
    }

    public Subscription toDomain() {
        return Subscription.builder()
                .id(id)
                .gameId(gameId)
                .email(email)
                .unsubscribeToken(unsubscribeToken)
                .createdAt(createdAt)
                .build();
    }
}
