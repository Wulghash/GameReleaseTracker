package com.wulghash.gamereleasetracker.infrastructure.web.dto;

import com.wulghash.gamereleasetracker.domain.model.Platform;

import java.util.List;

public record GameLookupResult(
        long rawgId,
        String title,
        String releaseDate,
        String imageUrl,
        List<Platform> platforms
) {}
