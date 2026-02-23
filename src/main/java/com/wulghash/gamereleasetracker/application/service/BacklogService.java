package com.wulghash.gamereleasetracker.application.service;

import com.wulghash.gamereleasetracker.domain.model.BacklogEntry;
import com.wulghash.gamereleasetracker.domain.model.BacklogEntryNotFoundException;
import com.wulghash.gamereleasetracker.domain.model.BacklogStatus;
import com.wulghash.gamereleasetracker.domain.model.GameAlreadyInBacklogException;
import com.wulghash.gamereleasetracker.domain.port.in.BacklogUseCase;
import com.wulghash.gamereleasetracker.domain.port.out.BacklogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BacklogService implements BacklogUseCase {

    private final BacklogRepository backlogRepository;

    @Override
    @Transactional
    public BacklogEntry add(UUID userId, BacklogAddCommand cmd) {
        if (backlogRepository.existsByUserIdAndIgdbId(userId, cmd.igdbId())) {
            throw new GameAlreadyInBacklogException(cmd.igdbId());
        }
        LocalDateTime now = LocalDateTime.now();
        BacklogEntry entry = BacklogEntry.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .igdbId(cmd.igdbId())
                .name(cmd.name())
                .coverUrl(cmd.coverUrl())
                .releaseDate(cmd.releaseDate())
                .backlogStatus(cmd.backlogStatus() != null ? cmd.backlogStatus() : BacklogStatus.WANT_TO_PLAY)
                .igdbScore(cmd.igdbScore())
                .rating(cmd.rating())
                .notes(cmd.notes())
                .createdAt(now)
                .updatedAt(now)
                .build();
        return backlogRepository.save(entry);
    }

    @Override
    public List<BacklogEntry> list(UUID userId, BacklogStatus statusFilter) {
        return backlogRepository.findAllByUserId(userId, statusFilter);
    }

    @Override
    @Transactional
    public BacklogEntry update(UUID entryId, UUID userId, BacklogUpdateCommand cmd) {
        BacklogEntry existing = backlogRepository.findByIdAndUserId(entryId, userId)
                .orElseThrow(() -> new BacklogEntryNotFoundException(entryId));

        BacklogEntry updated = existing.toBuilder()
                .backlogStatus(cmd.backlogStatus() != null ? cmd.backlogStatus() : existing.getBacklogStatus())
                .igdbScore(cmd.igdbScore())
                .rating(cmd.rating())
                .notes(cmd.notes())
                .updatedAt(LocalDateTime.now())
                .build();

        return backlogRepository.save(updated);
    }

    @Override
    @Transactional
    public void delete(UUID entryId, UUID userId) {
        backlogRepository.findByIdAndUserId(entryId, userId)
                .orElseThrow(() -> new BacklogEntryNotFoundException(entryId));
        backlogRepository.deleteByIdAndUserId(entryId, userId);
    }
}
