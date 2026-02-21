package com.wulghash.gamereleasetracker.domain.model;

import java.util.Set;

public enum GameStatus {
    UPCOMING,
    RELEASED,
    CANCELLED;

    private static final java.util.Map<GameStatus, Set<GameStatus>> ALLOWED_TRANSITIONS = java.util.Map.of(
            UPCOMING,   Set.of(RELEASED, CANCELLED),
            RELEASED,   Set.of(),
            CANCELLED,  Set.of()
    );

    public boolean canTransitionTo(GameStatus next) {
        return ALLOWED_TRANSITIONS.get(this).contains(next);
    }
}
