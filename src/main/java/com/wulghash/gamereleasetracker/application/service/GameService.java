package com.wulghash.gamereleasetracker.application.service;

import com.wulghash.gamereleasetracker.domain.model.Game;
import com.wulghash.gamereleasetracker.domain.model.GameNotFoundException;
import com.wulghash.gamereleasetracker.domain.model.GameStatus;
import com.wulghash.gamereleasetracker.domain.model.InvalidStatusTransitionException;
import com.wulghash.gamereleasetracker.domain.model.Platform;
import com.wulghash.gamereleasetracker.domain.model.Subscription;
import com.wulghash.gamereleasetracker.domain.port.in.GameUseCase;
import com.wulghash.gamereleasetracker.domain.port.out.GameRepository;
import com.wulghash.gamereleasetracker.domain.port.out.SubscriptionRepository;
import com.wulghash.gamereleasetracker.infrastructure.mail.EmailNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GameService implements GameUseCase {

    private final GameRepository gameRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final EmailNotificationService emailNotificationService;

    @Override
    @Transactional
    public Game create(UUID userId, GameCommand cmd) {
        LocalDateTime now = LocalDateTime.now();
        Game game = Game.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .title(cmd.title())
                .description(cmd.description())
                .releaseDate(cmd.releaseDate())
                .platforms(cmd.platforms())
                .status(GameStatus.UPCOMING)
                .shopUrl(cmd.shopUrl())
                .imageUrl(cmd.imageUrl())
                .developer(cmd.developer())
                .publisher(cmd.publisher())
                .igdbId(cmd.igdbId())
                .tba(cmd.tba())
                .createdAt(now)
                .updatedAt(now)
                .build();

        return gameRepository.save(game);
    }

    @Override
    public Game getById(UUID id, UUID userId) {
        return gameRepository.findById(id, userId)
                .orElseThrow(() -> new GameNotFoundException(id));
    }

    @Override
    public Page<Game> list(UUID userId, Platform platform, GameStatus status, LocalDate from, LocalDate to, Pageable pageable) {
        return gameRepository.findAll(userId, platform, status, from, to, pageable);
    }

    @Override
    @Transactional
    public Game update(UUID id, UUID userId, GameCommand cmd) {
        Game existing = gameRepository.findById(id, userId)
                .orElseThrow(() -> new GameNotFoundException(id));

        Game updated = existing.toBuilder()
                .title(cmd.title())
                .description(cmd.description())
                .releaseDate(cmd.releaseDate())
                .platforms(cmd.platforms())
                .shopUrl(cmd.shopUrl())
                .imageUrl(cmd.imageUrl())
                .developer(cmd.developer())
                .publisher(cmd.publisher())
                .igdbId(cmd.igdbId())
                .tba(cmd.tba())
                .updatedAt(LocalDateTime.now())
                .build();

        return gameRepository.save(updated);
    }

    @Override
    @Transactional
    public Game updateStatus(UUID id, UUID userId, GameStatus status) {
        Game existing = gameRepository.findById(id, userId)
                .orElseThrow(() -> new GameNotFoundException(id));

        if (!existing.getStatus().canTransitionTo(status)) {
            throw new InvalidStatusTransitionException(existing.getStatus(), status);
        }

        Game updated = existing.toBuilder()
                .status(status)
                .updatedAt(LocalDateTime.now())
                .build();

        Game saved = gameRepository.save(updated);

        if (status == GameStatus.CANCELLED) {
            notifyAndRemoveSubscribers(saved);
        }

        return saved;
    }

    private void notifyAndRemoveSubscribers(Game game) {
        List<Subscription> subscriptions = subscriptionRepository.findAllByGameId(game.getId());
        for (Subscription subscription : subscriptions) {
            emailNotificationService.sendCancellation(subscription.getEmail(), game);
            subscriptionRepository.deleteById(subscription.getId());
        }
    }

    @Override
    @Transactional
    public void delete(UUID id, UUID userId) {
        if (!gameRepository.existsById(id, userId)) {
            throw new GameNotFoundException(id);
        }
        gameRepository.deleteById(id, userId);
    }
}
