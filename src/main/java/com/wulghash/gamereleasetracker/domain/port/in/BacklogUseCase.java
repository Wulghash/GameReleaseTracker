package com.wulghash.gamereleasetracker.domain.port.in;

import com.wulghash.gamereleasetracker.domain.model.BacklogEntry;
import com.wulghash.gamereleasetracker.domain.model.BacklogStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface BacklogUseCase {

    BacklogEntry add(UUID userId, BacklogAddCommand cmd);

    List<BacklogEntry> list(UUID userId, BacklogStatus statusFilter);

    BacklogEntry update(UUID entryId, UUID userId, BacklogUpdateCommand cmd);

    void delete(UUID entryId, UUID userId);

    record BacklogAddCommand(
            Long igdbId,
            String name,
            String coverUrl,
            LocalDate releaseDate,
            BacklogStatus backlogStatus,
            Integer igdbScore,
            Integer rating,
            String notes
    ) {}

    record BacklogUpdateCommand(
            BacklogStatus backlogStatus,
            Integer igdbScore,
            Integer rating,
            String notes
    ) {}
}
