package com.wulghash.gamereleasetracker.infrastructure.web.dto;

import com.wulghash.gamereleasetracker.domain.model.BacklogStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record BacklogAddRequest(
        @NotNull Long igdbId,
        @NotBlank String name,
        String coverUrl,
        LocalDate releaseDate,
        BacklogStatus backlogStatus,
        Integer igdbScore,
        Integer rating,
        String notes
) {}
