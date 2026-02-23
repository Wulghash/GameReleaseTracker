package com.wulghash.gamereleasetracker.domain.model;

public class GameAlreadyInBacklogException extends RuntimeException {
    public GameAlreadyInBacklogException(Long igdbId) {
        super("Game with IGDB id " + igdbId + " is already in your backlog");
    }
}
