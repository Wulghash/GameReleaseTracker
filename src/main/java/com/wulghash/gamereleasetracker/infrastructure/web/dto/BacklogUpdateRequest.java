package com.wulghash.gamereleasetracker.infrastructure.web.dto;

import com.wulghash.gamereleasetracker.domain.model.BacklogStatus;

public record BacklogUpdateRequest(
        BacklogStatus backlogStatus,
        Integer igdbScore,
        Integer rating,
        String notes
) {}
