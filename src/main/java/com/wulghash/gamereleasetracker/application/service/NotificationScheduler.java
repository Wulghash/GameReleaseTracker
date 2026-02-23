package com.wulghash.gamereleasetracker.application.service;

import com.wulghash.gamereleasetracker.domain.model.Game;
import com.wulghash.gamereleasetracker.domain.model.GameStatus;
import com.wulghash.gamereleasetracker.domain.model.Subscription;
import com.wulghash.gamereleasetracker.domain.port.out.GameRepository;
import com.wulghash.gamereleasetracker.domain.port.out.SubscriptionRepository;
import com.wulghash.gamereleasetracker.infrastructure.mail.EmailNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationScheduler {

    private final GameRepository gameRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final EmailNotificationService emailNotificationService;

    @Scheduled(cron = "0 0 9 * * *", zone = "Europe/Warsaw")
    public void sendReleaseNotifications() {
        LocalDate today = LocalDate.now();
        log.info("Running release-day notifications for {}", today);

        List<Game> games = gameRepository.findAllByStatusAndReleaseDate(GameStatus.UPCOMING, today);

        for (Game game : games) {
            List<Subscription> subscribers = subscriptionRepository.findAllByGameId(game.getId());
            for (Subscription sub : subscribers) {
                emailNotificationService.sendReleaseToday(sub.getEmail(), game, sub.getUnsubscribeToken());
            }
        }

        log.info("Release-day notifications sent for {} game(s)", games.size());
    }

    @Scheduled(cron = "0 0 9 * * *", zone = "Europe/Warsaw")
    public void sendWeeklyReminders() {
        LocalDate inSevenDays = LocalDate.now().plusDays(7);
        log.info("Running 7-day reminder notifications for release date {}", inSevenDays);

        List<Game> games = gameRepository.findAllByStatusAndReleaseDate(GameStatus.UPCOMING, inSevenDays);

        for (Game game : games) {
            List<Subscription> subscribers = subscriptionRepository.findAllByGameId(game.getId());
            for (Subscription sub : subscribers) {
                emailNotificationService.sendReleaseSoon(sub.getEmail(), game, sub.getUnsubscribeToken());
            }
        }

        log.info("7-day reminders sent for {} game(s)", games.size());
    }
}
