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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GameServiceTest {

    static final UUID USER_ID = UUID.randomUUID();

    @Mock
    private GameRepository gameRepository;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private EmailNotificationService emailNotificationService;

    private GameService gameService;

    @BeforeEach
    void setUp() {
        gameService = new GameService(gameRepository, subscriptionRepository, emailNotificationService);
    }

    @Test
    void createShouldSaveWithUpcomingStatusAndReturnGame() {
        GameUseCase.GameCommand cmd = new GameUseCase.GameCommand(
                "Elden Ring 2", null,
                LocalDate.of(2026, 6, 15),
                Set.of(Platform.PC, Platform.PS5),
                null, null, "FromSoftware", null, null, false);

        Game savedGame = buildGame(UUID.randomUUID(), "Elden Ring 2", GameStatus.UPCOMING);
        when(gameRepository.save(any(Game.class))).thenReturn(savedGame);

        Game result = gameService.create(USER_ID, cmd);

        assertThat(result.getTitle()).isEqualTo("Elden Ring 2");
        ArgumentCaptor<Game> captor = ArgumentCaptor.forClass(Game.class);
        verify(gameRepository).save(captor.capture());
        assertThat(captor.getValue().getTitle()).isEqualTo("Elden Ring 2");
        assertThat(captor.getValue().getId()).isNotNull();
        assertThat(captor.getValue().getStatus()).isEqualTo(GameStatus.UPCOMING);
        assertThat(captor.getValue().getUserId()).isEqualTo(USER_ID);
    }

    @Test
    void getByIdShouldReturnGameWhenFound() {
        UUID id = UUID.randomUUID();
        Game game = buildGame(id, "Hollow Knight 2", GameStatus.UPCOMING);
        when(gameRepository.findById(id, USER_ID)).thenReturn(Optional.of(game));

        Game result = gameService.getById(id, USER_ID);

        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.getTitle()).isEqualTo("Hollow Knight 2");
    }

    @Test
    void getByIdShouldThrowWhenNotFound() {
        UUID id = UUID.randomUUID();
        when(gameRepository.findById(id, USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> gameService.getById(id, USER_ID))
                .isInstanceOf(GameNotFoundException.class)
                .hasMessageContaining(id.toString());
    }

    @Test
    void listShouldReturnPagedGames() {
        PageRequest pageable = PageRequest.of(0, 10);
        List<Game> games = List.of(
                buildGame(UUID.randomUUID(), "Game A", GameStatus.UPCOMING),
                buildGame(UUID.randomUUID(), "Game B", GameStatus.UPCOMING)
        );
        when(gameRepository.findAll(USER_ID, null, null, null, null, pageable))
                .thenReturn(new PageImpl<>(games, pageable, 2));

        Page<Game> result = gameService.list(USER_ID, null, null, null, null, pageable);

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
    }

    @Test
    void updateShouldPreserveExistingStatus() {
        UUID id = UUID.randomUUID();
        Game existing = buildGame(id, "Old Title", GameStatus.UPCOMING);
        GameUseCase.GameCommand cmd = new GameUseCase.GameCommand(
                "New Title", null,
                LocalDate.of(2027, 1, 1),
                Set.of(Platform.PC),
                null, null, null, null, null, false);

        when(gameRepository.findById(id, USER_ID)).thenReturn(Optional.of(existing));
        when(gameRepository.save(any(Game.class))).thenAnswer(inv -> inv.getArgument(0));

        Game result = gameService.update(id, USER_ID, cmd);

        assertThat(result.getTitle()).isEqualTo("New Title");
        assertThat(result.getId()).isEqualTo(id);
        ArgumentCaptor<Game> captor = ArgumentCaptor.forClass(Game.class);
        verify(gameRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(GameStatus.UPCOMING);
    }

    @Test
    void updateShouldThrowWhenGameNotFound() {
        UUID id = UUID.randomUUID();
        when(gameRepository.findById(id, USER_ID)).thenReturn(Optional.empty());

        GameUseCase.GameCommand cmd = new GameUseCase.GameCommand(
                "Any", null, LocalDate.now(), Set.of(Platform.PC),
                null, null, null, null, null, false);

        assertThatThrownBy(() -> gameService.update(id, USER_ID, cmd))
                .isInstanceOf(GameNotFoundException.class);
    }

    @Test
    void updateStatusShouldChangeStatusAndReturnGame() {
        UUID id = UUID.randomUUID();
        Game existing = buildGame(id, "Elden Ring 2", GameStatus.UPCOMING);
        when(gameRepository.findById(id, USER_ID)).thenReturn(Optional.of(existing));
        when(gameRepository.save(any(Game.class))).thenAnswer(inv -> inv.getArgument(0));

        Game result = gameService.updateStatus(id, USER_ID, GameStatus.RELEASED);

        assertThat(result.getStatus()).isEqualTo(GameStatus.RELEASED);
        assertThat(result.getTitle()).isEqualTo("Elden Ring 2");
    }

    @Test
    void updateStatusShouldThrowWhenGameNotFound() {
        UUID id = UUID.randomUUID();
        when(gameRepository.findById(id, USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> gameService.updateStatus(id, USER_ID, GameStatus.RELEASED))
                .isInstanceOf(GameNotFoundException.class);
    }

    @Test
    void updateStatusShouldThrowOnInvalidTransitionFromReleased() {
        UUID id = UUID.randomUUID();
        Game released = buildGame(id, "Old Game", GameStatus.RELEASED);
        when(gameRepository.findById(id, USER_ID)).thenReturn(Optional.of(released));

        assertThatThrownBy(() -> gameService.updateStatus(id, USER_ID, GameStatus.UPCOMING))
                .isInstanceOf(InvalidStatusTransitionException.class)
                .hasMessageContaining("RELEASED")
                .hasMessageContaining("UPCOMING");
    }

    @Test
    void updateStatusShouldThrowOnInvalidTransitionFromCancelled() {
        UUID id = UUID.randomUUID();
        Game cancelled = buildGame(id, "Dead Game", GameStatus.CANCELLED);
        when(gameRepository.findById(id, USER_ID)).thenReturn(Optional.of(cancelled));

        assertThatThrownBy(() -> gameService.updateStatus(id, USER_ID, GameStatus.RELEASED))
                .isInstanceOf(InvalidStatusTransitionException.class);
    }

    @Test
    void updateStatusToCancelledShouldNotifyAndRemoveSubscribers() {
        UUID id = UUID.randomUUID();
        Game existing = buildGame(id, "Cancelled Game", GameStatus.UPCOMING);

        UUID subId = UUID.randomUUID();
        Subscription subscription = Subscription.builder()
                .id(subId)
                .gameId(id)
                .email("fan@example.com")
                .unsubscribeToken(UUID.randomUUID())
                .createdAt(LocalDateTime.now())
                .build();

        when(gameRepository.findById(id, USER_ID)).thenReturn(Optional.of(existing));
        when(gameRepository.save(any(Game.class))).thenAnswer(inv -> inv.getArgument(0));
        when(subscriptionRepository.findAllByGameId(id)).thenReturn(List.of(subscription));

        Game result = gameService.updateStatus(id, USER_ID, GameStatus.CANCELLED);

        assertThat(result.getStatus()).isEqualTo(GameStatus.CANCELLED);
        verify(emailNotificationService).sendCancellation(eq("fan@example.com"), any(Game.class));
        verify(subscriptionRepository).deleteById(subId);
    }

    @Test
    void deleteShouldDelegateToRepository() {
        UUID id = UUID.randomUUID();
        when(gameRepository.existsById(id, USER_ID)).thenReturn(true);

        gameService.delete(id, USER_ID);

        verify(gameRepository).deleteById(id, USER_ID);
    }

    @Test
    void deleteShouldThrowWhenGameNotFound() {
        UUID id = UUID.randomUUID();
        when(gameRepository.existsById(id, USER_ID)).thenReturn(false);

        assertThatThrownBy(() -> gameService.delete(id, USER_ID))
                .isInstanceOf(GameNotFoundException.class);
    }

    private Game buildGame(UUID id, String title, GameStatus status) {
        LocalDateTime now = LocalDateTime.now();
        return Game.builder()
                .id(id)
                .userId(USER_ID)
                .title(title)
                .releaseDate(LocalDate.of(2026, 6, 15))
                .platforms(Set.of(Platform.PC))
                .status(status)
                .tba(false)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }
}
