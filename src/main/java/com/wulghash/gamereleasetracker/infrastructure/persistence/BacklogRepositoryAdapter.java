package com.wulghash.gamereleasetracker.infrastructure.persistence;

import com.wulghash.gamereleasetracker.domain.model.BacklogEntry;
import com.wulghash.gamereleasetracker.domain.model.BacklogStatus;
import com.wulghash.gamereleasetracker.domain.port.out.BacklogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class BacklogRepositoryAdapter implements BacklogRepository {

    private final SpringDataBacklogRepository jpaRepository;

    @Override
    public BacklogEntry save(BacklogEntry entry) {
        return jpaRepository.save(BacklogEntryJpaEntity.from(entry)).toDomain();
    }

    @Override
    public Optional<BacklogEntry> findByIdAndUserId(UUID id, UUID userId) {
        return jpaRepository.findByIdAndUserId(id, userId).map(BacklogEntryJpaEntity::toDomain);
    }

    @Override
    public List<BacklogEntry> findAllByUserId(UUID userId, BacklogStatus status) {
        if (status == null) {
            return jpaRepository.findAllByUserId(userId).stream()
                    .map(BacklogEntryJpaEntity::toDomain)
                    .toList();
        }
        return jpaRepository.findAllByUserIdAndBacklogStatus(userId, status).stream()
                .map(BacklogEntryJpaEntity::toDomain)
                .toList();
    }

    @Override
    public void deleteByIdAndUserId(UUID id, UUID userId) {
        jpaRepository.deleteByIdAndUserId(id, userId);
    }

    @Override
    public boolean existsByUserIdAndIgdbId(UUID userId, Long igdbId) {
        return jpaRepository.existsByUserIdAndIgdbId(userId, igdbId);
    }
}
