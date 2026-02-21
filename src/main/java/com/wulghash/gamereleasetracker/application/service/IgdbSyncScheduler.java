package com.wulghash.gamereleasetracker.application.service;

import com.wulghash.gamereleasetracker.domain.model.Game;
import com.wulghash.gamereleasetracker.domain.model.GameStatus;
import com.wulghash.gamereleasetracker.domain.model.Subscription;
import com.wulghash.gamereleasetracker.domain.port.out.GameRepository;
import com.wulghash.gamereleasetracker.domain.port.out.SubscriptionRepository;
import com.wulghash.gamereleasetracker.infrastructure.igdb.IgdbClient;
import com.wulghash.gamereleasetracker.infrastructure.mail.EmailNotificationService;
import com.wulghash.gamereleasetracker.infrastructure.web.dto.GameLookupDetail;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class IgdbSyncScheduler {

    private final GameRepository gameRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final IgdbClient igdbClient;
    private final EmailNotificationService emailNotificationService;

    // Runs at 3 AM daily — before the 9 AM notification job, but only touches past-due games.
    // Games releasing TODAY are intentionally left as UPCOMING so the 9 AM notifications fire.
    @Scheduled(cron = "0 0 3 * * *", zone = "Europe/Warsaw")
    public void syncUpcomingGames() {
        LocalDate today = LocalDate.now();
        log.info("Running IGDB sync for {}", today);

        List<Game> upcomingGames = gameRepository
                .findAll(null, GameStatus.UPCOMING, null, null, Pageable.unpaged())
                .getContent();

        int refreshed = 0;
        int transitioned = 0;

        for (Game game : upcomingGames) {
            // Auto-transition: release date is strictly in the past (not today)
            if (!game.isTba() && game.getReleaseDate().isBefore(today)) {
                autoRelease(game);
                transitioned++;
                continue; // no need to sync a game we just released
            }

            // IGDB date refresh for games that were added via IGDB lookup
            if (game.getIgdbId() != null) {
                if (refreshDateFromIgdb(game)) refreshed++;
            }
        }

        log.info("IGDB sync complete: {} date(s) updated, {} game(s) auto-transitioned to RELEASED",
                refreshed, transitioned);
    }

    private boolean refreshDateFromIgdb(Game game) {
        try {
            GameLookupDetail detail = igdbClient.getDetail(game.getIgdbId());
            if (detail == null || detail.releaseDate() == null) return false;

            LocalDate newDate = LocalDate.parse(detail.releaseDate());
            if (newDate.equals(game.getReleaseDate())) return false;

            LocalDate oldDate = game.getReleaseDate();
            log.info("Release date changed for '{}': {} → {}", game.getTitle(), oldDate, newDate);

            Game updated = game.toBuilder()
                    .releaseDate(newDate)
                    .tba(false)
                    .updatedAt(LocalDateTime.now())
                    .build();
            gameRepository.save(updated);

            List<Subscription> subscribers = subscriptionRepository.findAllByGameId(game.getId());
            for (Subscription sub : subscribers) {
                emailNotificationService.sendDateChanged(
                        sub.getEmail(), updated, oldDate, sub.getUnsubscribeToken());
            }

            return true;
        } catch (Exception e) {
            log.error("Failed to sync '{}' (igdbId={}): {}", game.getTitle(), game.getIgdbId(), e.getMessage());
            return false;
        }
    }

    private void autoRelease(Game game) {
        log.info("Auto-transitioning '{}' to RELEASED (was due {})", game.getTitle(), game.getReleaseDate());
        Game updated = game.toBuilder()
                .status(GameStatus.RELEASED)
                .updatedAt(LocalDateTime.now())
                .build();
        gameRepository.save(updated);
    }
}
