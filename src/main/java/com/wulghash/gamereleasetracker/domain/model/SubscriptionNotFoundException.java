package com.wulghash.gamereleasetracker.domain.model;

import java.util.UUID;

public class SubscriptionNotFoundException extends RuntimeException {
    public SubscriptionNotFoundException(UUID token) {
        super("Subscription not found for token: " + token);
    }
}
