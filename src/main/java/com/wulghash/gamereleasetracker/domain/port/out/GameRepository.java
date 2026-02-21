package com.wulghash.gamereleasetracker.domain.port.out;

import com.wulghash.gamereleasetracker.domain.model.Game;
import com.wulghash.gamereleasetracker.domain.model.GameStatus;
import com.wulghash.gamereleasetracker.domain.model.Platform;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface GameRepository {

    Game save(Game game);

    Optional<Game> findById(UUID id);

    Page<Game> findAll(Platform platform, GameStatus status, LocalDate from, LocalDate to, Pageable pageable);

    void deleteById(UUID id);

    boolean existsById(UUID id);
}
