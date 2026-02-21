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
import com.wulghash.gamereleasetracker.infrastructure.web.dto.GameRequest;
import com.wulghash.gamereleasetracker.infrastructure.web.dto.GameResponse;
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
    public GameResponse create(GameRequest request) {
        LocalDateTime now = LocalDateTime.now();
        Game game = Game.builder()
                .id(UUID.randomUUID())
                .title(request.title())
                .description(request.description())
                .releaseDate(request.releaseDate())
                .platforms(request.platforms())
                .status(GameStatus.UPCOMING)
                .shopUrl(request.shopUrl())
                .imageUrl(request.imageUrl())
                .developer(request.developer())
                .publisher(request.publisher())
                .tba(request.tba())
                .createdAt(now)
                .updatedAt(now)
                .build();

        return GameResponse.from(gameRepository.save(game));
    }

    @Override
    public GameResponse getById(UUID id) {
        return gameRepository.findById(id)
                .map(GameResponse::from)
                .orElseThrow(() -> new GameNotFoundException(id));
    }

    @Override
    public Page<GameResponse> list(Platform platform, GameStatus status, LocalDate from, LocalDate to, Pageable pageable) {
        return gameRepository.findAll(platform, status, from, to, pageable)
                .map(GameResponse::from);
    }

    @Override
    @Transactional
    public GameResponse update(UUID id, GameRequest request) {
        Game existing = gameRepository.findById(id)
                .orElseThrow(() -> new GameNotFoundException(id));

        Game updated = existing.toBuilder()
                .title(request.title())
                .description(request.description())
                .releaseDate(request.releaseDate())
                .platforms(request.platforms())
                .shopUrl(request.shopUrl())
                .imageUrl(request.imageUrl())
                .developer(request.developer())
                .publisher(request.publisher())
                .tba(request.tba())
                .updatedAt(LocalDateTime.now())
                .build();

        return GameResponse.from(gameRepository.save(updated));
    }

    @Override
    @Transactional
    public GameResponse updateStatus(UUID id, GameStatus status) {
        Game existing = gameRepository.findById(id)
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

        return GameResponse.from(saved);
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
    public void delete(UUID id) {
        if (!gameRepository.existsById(id)) {
            throw new GameNotFoundException(id);
        }
        gameRepository.deleteById(id);
    }
}
