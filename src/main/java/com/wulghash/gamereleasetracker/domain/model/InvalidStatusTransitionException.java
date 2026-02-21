package com.wulghash.gamereleasetracker.domain.model;

public class InvalidStatusTransitionException extends RuntimeException {
    public InvalidStatusTransitionException(GameStatus from, GameStatus to) {
        super("Cannot transition game status from " + from + " to " + to);
    }
}
