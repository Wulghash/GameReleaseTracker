package com.wulghash.gamereleasetracker.application.service;

import com.wulghash.gamereleasetracker.domain.model.GameAlreadySubscribedException;
import com.wulghash.gamereleasetracker.domain.model.GameNotFoundException;
import com.wulghash.gamereleasetracker.domain.model.Subscription;
import com.wulghash.gamereleasetracker.domain.model.SubscriptionNotFoundException;
import com.wulghash.gamereleasetracker.domain.port.out.GameRepository;
import com.wulghash.gamereleasetracker.domain.port.out.SubscriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private GameRepository gameRepository;

    private SubscriptionService subscriptionService;

    @BeforeEach
    void setUp() {
        subscriptionService = new SubscriptionService(subscriptionRepository, gameRepository);
    }

    @Test
    void subscribeShouldSaveSubscriptionWithGeneratedToken() {
        UUID gameId = UUID.randomUUID();
        when(gameRepository.existsByIdForAnyUser(gameId)).thenReturn(true);
        when(subscriptionRepository.existsByGameIdAndEmail(gameId, "player@example.com")).thenReturn(false);
        when(subscriptionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        subscriptionService.subscribe(gameId, "player@example.com");

        ArgumentCaptor<Subscription> captor = ArgumentCaptor.forClass(Subscription.class);
        verify(subscriptionRepository).save(captor.capture());
        Subscription saved = captor.getValue();
        assertThat(saved.getGameId()).isEqualTo(gameId);
        assertThat(saved.getEmail()).isEqualTo("player@example.com");
        assertThat(saved.getUnsubscribeToken()).isNotNull();
        assertThat(saved.getId()).isNotNull();
    }

    @Test
    void subscribeShouldThrowWhenGameDoesNotExist() {
        UUID gameId = UUID.randomUUID();
        when(gameRepository.existsByIdForAnyUser(gameId)).thenReturn(false);

        assertThatThrownBy(() -> subscriptionService.subscribe(gameId, "player@example.com"))
                .isInstanceOf(GameNotFoundException.class);
    }

    @Test
    void subscribeShouldThrowWhenAlreadySubscribed() {
        UUID gameId = UUID.randomUUID();
        when(gameRepository.existsByIdForAnyUser(gameId)).thenReturn(true);
        when(subscriptionRepository.existsByGameIdAndEmail(gameId, "player@example.com")).thenReturn(true);

        assertThatThrownBy(() -> subscriptionService.subscribe(gameId, "player@example.com"))
                .isInstanceOf(GameAlreadySubscribedException.class)
                .hasMessageContaining("player@example.com");
    }

    @Test
    void unsubscribeShouldDeleteByToken() {
        UUID token = UUID.randomUUID();
        Subscription existing = Subscription.builder()
                .id(UUID.randomUUID()).gameId(UUID.randomUUID())
                .email("player@example.com").unsubscribeToken(token)
                .createdAt(LocalDateTime.now()).build();

        when(subscriptionRepository.findByUnsubscribeToken(token)).thenReturn(Optional.of(existing));

        subscriptionService.unsubscribe(token);

        verify(subscriptionRepository).deleteById(existing.getId());
    }

    @Test
    void unsubscribeShouldThrowWhenTokenNotFound() {
        UUID token = UUID.randomUUID();
        when(subscriptionRepository.findByUnsubscribeToken(token)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> subscriptionService.unsubscribe(token))
                .isInstanceOf(SubscriptionNotFoundException.class);
    }
}
