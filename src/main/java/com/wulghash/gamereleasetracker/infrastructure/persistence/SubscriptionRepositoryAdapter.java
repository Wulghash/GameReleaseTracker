package com.wulghash.gamereleasetracker.infrastructure.persistence;

import com.wulghash.gamereleasetracker.domain.model.Subscription;
import com.wulghash.gamereleasetracker.domain.port.out.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SubscriptionRepositoryAdapter implements SubscriptionRepository {

    private final SpringDataSubscriptionRepository jpaRepository;

    @Override
    public Subscription save(Subscription subscription) {
        return jpaRepository.save(SubscriptionJpaEntity.from(subscription)).toDomain();
    }

    @Override
    public boolean existsByGameIdAndEmail(UUID gameId, String email) {
        return jpaRepository.existsByGameIdAndEmail(gameId, email);
    }

    @Override
    public Optional<Subscription> findByUnsubscribeToken(UUID token) {
        return jpaRepository.findByUnsubscribeToken(token).map(SubscriptionJpaEntity::toDomain);
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public List<Subscription> findAllByGameId(UUID gameId) {
        return jpaRepository.findAllByGameId(gameId).stream()
                .map(SubscriptionJpaEntity::toDomain)
                .toList();
    }
}
