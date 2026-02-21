package com.wulghash.gamereleasetracker.domain.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Subscription {

    @EqualsAndHashCode.Include
    private final UUID id;

    private final UUID gameId;
    private final String email;
    private final UUID unsubscribeToken;
    private final LocalDateTime createdAt;
}
