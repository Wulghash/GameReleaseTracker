package com.wulghash.gamereleasetracker.domain.port.in;

import java.util.UUID;

public interface SubscriptionUseCase {

    void subscribe(UUID gameId, String email);

    void unsubscribe(UUID unsubscribeToken);
}
