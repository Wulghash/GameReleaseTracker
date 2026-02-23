package com.wulghash.gamereleasetracker.infrastructure.persistence;

import com.wulghash.gamereleasetracker.domain.model.Game;
import com.wulghash.gamereleasetracker.domain.model.GameStatus;
import com.wulghash.gamereleasetracker.domain.model.Platform;
import com.wulghash.gamereleasetracker.domain.port.out.GameRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class GameRepositoryAdapter implements GameRepository {

    private final SpringDataGameRepository jpaRepository;

    @Override
    public Game save(Game game) {
        return jpaRepository.save(GameJpaEntity.from(game)).toDomain();
    }

    @Override
    public Optional<Game> findById(UUID id, UUID userId) {
        return jpaRepository.findOne(GameSpecification.withIdAndUserId(id, userId))
                .map(GameJpaEntity::toDomain);
    }

    @Override
    public Page<Game> findAll(UUID userId, Platform platform, GameStatus status, LocalDate from, LocalDate to, Pageable pageable) {
        return jpaRepository.findAll(GameSpecification.withFilters(userId, platform, status, from, to), pageable)
                .map(GameJpaEntity::toDomain);
    }

    @Override
    public void deleteById(UUID id, UUID userId) {
        jpaRepository.findOne(GameSpecification.withIdAndUserId(id, userId))
                .ifPresent(jpaRepository::delete);
    }

    @Override
    public boolean existsById(UUID id, UUID userId) {
        return jpaRepository.exists(GameSpecification.withIdAndUserId(id, userId));
    }

    @Override
    public boolean existsByIdForAnyUser(UUID id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public List<Game> findAllByStatus(GameStatus status) {
        return jpaRepository.findByStatus(status).stream()
                .map(GameJpaEntity::toDomain)
                .toList();
    }

    @Override
    public List<Game> findAllByStatusAndReleaseDate(GameStatus status, LocalDate releaseDate) {
        return jpaRepository.findByStatusAndReleaseDate(status, releaseDate).stream()
                .map(GameJpaEntity::toDomain)
                .toList();
    }
}
