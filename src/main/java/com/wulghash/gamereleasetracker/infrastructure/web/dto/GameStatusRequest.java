package com.wulghash.gamereleasetracker.infrastructure.web.dto;

import com.wulghash.gamereleasetracker.domain.model.GameStatus;
import jakarta.validation.constraints.NotNull;

public record GameStatusRequest(
        @NotNull(message = "Status is required")
        GameStatus status
) {}
