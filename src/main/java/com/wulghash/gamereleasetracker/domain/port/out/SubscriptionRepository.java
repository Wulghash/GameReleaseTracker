package com.wulghash.gamereleasetracker.domain.port.out;

import com.wulghash.gamereleasetracker.domain.model.Subscription;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SubscriptionRepository {

    Subscription save(Subscription subscription);

    boolean existsByGameIdAndEmail(UUID gameId, String email);

    Optional<Subscription> findByUnsubscribeToken(UUID token);

    void deleteById(UUID id);

    List<Subscription> findAllByGameId(UUID gameId);
}
