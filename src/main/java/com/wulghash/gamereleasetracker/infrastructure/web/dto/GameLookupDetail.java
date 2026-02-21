package com.wulghash.gamereleasetracker.infrastructure.web.dto;

import com.wulghash.gamereleasetracker.domain.model.Platform;

import java.util.List;

public record GameLookupDetail(
        String title,
        String releaseDate,
        String imageUrl,
        List<Platform> platforms,
        String description,
        String developer,
        String publisher
) {}
