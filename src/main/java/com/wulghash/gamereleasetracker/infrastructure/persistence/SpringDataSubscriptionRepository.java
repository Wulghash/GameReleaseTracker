package com.wulghash.gamereleasetracker.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface SpringDataSubscriptionRepository extends JpaRepository<SubscriptionJpaEntity, UUID> {

    boolean existsByGameIdAndEmail(UUID gameId, String email);

    Optional<SubscriptionJpaEntity> findByUnsubscribeToken(UUID unsubscribeToken);

    List<SubscriptionJpaEntity> findAllByGameId(UUID gameId);
}
