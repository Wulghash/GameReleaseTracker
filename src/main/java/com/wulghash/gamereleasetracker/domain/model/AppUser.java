package com.wulghash.gamereleasetracker.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class AppUser {

    private final UUID id;
    private final String googleId;
    private final String email;
    private final String name;
    private final LocalDateTime createdAt;
}
