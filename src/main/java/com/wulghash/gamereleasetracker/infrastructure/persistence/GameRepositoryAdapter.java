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
    public Optional<Game> findById(UUID id) {
        return jpaRepository.findById(id).map(GameJpaEntity::toDomain);
    }

    @Override
    public Page<Game> findAll(Platform platform, GameStatus status, LocalDate from, LocalDate to, Pageable pageable) {
        return jpaRepository.findAll(GameSpecification.withFilters(platform, status, from, to), pageable)
                .map(GameJpaEntity::toDomain);
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }
}
