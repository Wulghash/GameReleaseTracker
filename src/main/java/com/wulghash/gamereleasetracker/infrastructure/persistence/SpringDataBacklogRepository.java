package com.wulghash.gamereleasetracker.infrastructure.persistence;

import com.wulghash.gamereleasetracker.domain.model.BacklogStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataBacklogRepository extends JpaRepository<BacklogEntryJpaEntity, UUID> {

    List<BacklogEntryJpaEntity> findAllByUserId(UUID userId);

    List<BacklogEntryJpaEntity> findAllByUserIdAndBacklogStatus(UUID userId, BacklogStatus backlogStatus);

    boolean existsByUserIdAndIgdbId(UUID userId, Long igdbId);

    Optional<BacklogEntryJpaEntity> findByIdAndUserId(UUID id, UUID userId);

    void deleteByIdAndUserId(UUID id, UUID userId);
}
