package com.wulghash.gamereleasetracker.domain.port.out;

import com.wulghash.gamereleasetracker.domain.model.BacklogEntry;
import com.wulghash.gamereleasetracker.domain.model.BacklogStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BacklogRepository {

    BacklogEntry save(BacklogEntry entry);

    Optional<BacklogEntry> findByIdAndUserId(UUID id, UUID userId);

    List<BacklogEntry> findAllByUserId(UUID userId, BacklogStatus status);

    void deleteByIdAndUserId(UUID id, UUID userId);

    boolean existsByUserIdAndIgdbId(UUID userId, Long igdbId);
}
