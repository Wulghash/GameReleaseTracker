package com.wulghash.gamereleasetracker.application.service;

import com.wulghash.gamereleasetracker.domain.model.*;
import com.wulghash.gamereleasetracker.domain.port.out.GameRepository;
import com.wulghash.gamereleasetracker.domain.port.out.SubscriptionRepository;
import com.wulghash.gamereleasetracker.infrastructure.mail.EmailNotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationSchedulerTest {

    @Mock
    private GameRepository gameRepository;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private EmailNotificationService emailNotificationService;

    @InjectMocks
    private NotificationScheduler scheduler;

    @Test
    void shouldSendReleaseEmailsForGamesReleasingToday() {
        LocalDate today = LocalDate.now();
        Game game = buildGame(UUID.randomUUID(), "Elden Ring 2", today);
        Subscription sub = buildSubscription(game.getId(), "player@example.com");

        when(gameRepository.findAllByStatusAndReleaseDate(GameStatus.UPCOMING, today))
                .thenReturn(List.of(game));
        when(subscriptionRepository.findAllByGameId(game.getId())).thenReturn(List.of(sub));

        scheduler.sendReleaseNotifications();

        verify(emailNotificationService).sendReleaseToday(sub.getEmail(), game, sub.getUnsubscribeToken());
    }

    @Test
    void shouldSendWeeklyReminderEmailsForGamesReleasingInSevenDays() {
        LocalDate inSevenDays = LocalDate.now().plusDays(7);
        Game game = buildGame(UUID.randomUUID(), "Hollow Knight 2", inSevenDays);
        Subscription sub = buildSubscription(game.getId(), "player@example.com");

        when(gameRepository.findAllByStatusAndReleaseDate(GameStatus.UPCOMING, inSevenDays))
                .thenReturn(List.of(game));
        when(subscriptionRepository.findAllByGameId(game.getId())).thenReturn(List.of(sub));

        scheduler.sendWeeklyReminders();

        verify(emailNotificationService).sendReleaseSoon(sub.getEmail(), game, sub.getUnsubscribeToken());
    }

    @Test
    void shouldNotSendEmailsWhenNoSubscribersForGame() {
        LocalDate today = LocalDate.now();
        Game game = buildGame(UUID.randomUUID(), "Lonely Game", today);

        when(gameRepository.findAllByStatusAndReleaseDate(GameStatus.UPCOMING, today))
                .thenReturn(List.of(game));
        when(subscriptionRepository.findAllByGameId(game.getId())).thenReturn(List.of());

        scheduler.sendReleaseNotifications();

        verifyNoInteractions(emailNotificationService);
    }

    @Test
    void shouldNotSendEmailsWhenNoGamesReleasingToday() {
        when(gameRepository.findAllByStatusAndReleaseDate(eq(GameStatus.UPCOMING), any(LocalDate.class)))
                .thenReturn(List.of());

        scheduler.sendReleaseNotifications();

        verifyNoInteractions(subscriptionRepository);
        verifyNoInteractions(emailNotificationService);
    }

    private Game buildGame(UUID id, String title, LocalDate releaseDate) {
        return Game.builder()
                .id(id).title(title).releaseDate(releaseDate)
                .platforms(Set.of(Platform.PC)).status(GameStatus.UPCOMING)
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .build();
    }

    private Subscription buildSubscription(UUID gameId, String email) {
        return Subscription.builder()
                .id(UUID.randomUUID()).gameId(gameId).email(email)
                .unsubscribeToken(UUID.randomUUID()).createdAt(LocalDateTime.now())
                .build();
    }
}
