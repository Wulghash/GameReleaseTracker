package com.wulghash.gamereleasetracker.application.service;

import com.wulghash.gamereleasetracker.domain.model.GameAlreadySubscribedException;
import com.wulghash.gamereleasetracker.domain.model.GameNotFoundException;
import com.wulghash.gamereleasetracker.domain.model.Subscription;
import com.wulghash.gamereleasetracker.domain.model.SubscriptionNotFoundException;
import com.wulghash.gamereleasetracker.domain.port.in.SubscriptionUseCase;
import com.wulghash.gamereleasetracker.domain.port.out.GameRepository;
import com.wulghash.gamereleasetracker.domain.port.out.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubscriptionService implements SubscriptionUseCase {

    private final SubscriptionRepository subscriptionRepository;
    private final GameRepository gameRepository;

    @Override
    @Transactional
    public void subscribe(UUID gameId, String email) {
        if (!gameRepository.existsByIdForAnyUser(gameId)) {
            throw new GameNotFoundException(gameId);
        }
        if (subscriptionRepository.existsByGameIdAndEmail(gameId, email)) {
            throw new GameAlreadySubscribedException(gameId, email);
        }

        Subscription subscription = Subscription.builder()
                .id(UUID.randomUUID())
                .gameId(gameId)
                .email(email)
                .unsubscribeToken(UUID.randomUUID())
                .createdAt(LocalDateTime.now())
                .build();

        subscriptionRepository.save(subscription);
    }

    @Override
    @Transactional
    public void unsubscribe(UUID unsubscribeToken) {
        Subscription subscription = subscriptionRepository.findByUnsubscribeToken(unsubscribeToken)
                .orElseThrow(() -> new SubscriptionNotFoundException(unsubscribeToken));

        subscriptionRepository.deleteById(subscription.getId());
    }
}
