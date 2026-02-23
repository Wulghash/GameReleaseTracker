package com.wulghash.gamereleasetracker.domain.model;

import java.util.Map;
import java.util.Set;

public enum GameStatus {
    UPCOMING,
    RELEASED,
    CANCELLED;

    private static final Map<GameStatus, Set<GameStatus>> ALLOWED_TRANSITIONS = Map.of(
            UPCOMING,   Set.of(RELEASED, CANCELLED),
            RELEASED,   Set.of(),
            CANCELLED,  Set.of()
    );

    public boolean canTransitionTo(GameStatus next) {
        return ALLOWED_TRANSITIONS.get(this).contains(next);
    }
}
