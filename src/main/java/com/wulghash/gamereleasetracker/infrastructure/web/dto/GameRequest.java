package com.wulghash.gamereleasetracker.infrastructure.web.dto;

import com.wulghash.gamereleasetracker.domain.model.Platform;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;

import java.time.LocalDate;
import java.util.Set;

@Builder
public record GameRequest(
        @NotBlank(message = "Title is required")
        String title,

        String description,

        @NotNull(message = "Release date is required")
        LocalDate releaseDate,

        @NotEmpty(message = "At least one platform is required")
        Set<Platform> platforms,

        @Pattern(
                regexp = "^$|https?://.+",
                message = "Shop URL must be a valid http/https URL"
        )
        String shopUrl,

        @Pattern(
                regexp = "^$|https?://.+",
                message = "Image URL must be a valid http/https URL"
        )
        String imageUrl,

        String developer,
        String publisher,
        Long igdbId,
        boolean tba
) {}
