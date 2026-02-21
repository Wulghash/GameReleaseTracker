package com.wulghash.gamereleasetracker.domain.model;

import java.util.UUID;

public class GameAlreadySubscribedException extends RuntimeException {
    public GameAlreadySubscribedException(UUID gameId, String email) {
        super("Email " + email + " is already subscribed to game " + gameId);
    }
}
