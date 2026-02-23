package com.wulghash.gamereleasetracker.domain.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class AppUser {

    @EqualsAndHashCode.Include
    private final UUID id;
    private final String googleId;
    private final String email;
    private final String name;
    private final LocalDateTime createdAt;
}
