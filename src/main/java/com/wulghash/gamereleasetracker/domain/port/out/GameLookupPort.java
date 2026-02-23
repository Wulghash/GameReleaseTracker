package com.wulghash.gamereleasetracker.domain.port.out;

import java.time.LocalDate;
import java.util.Optional;

public interface GameLookupPort {

    Optional<LocalDate> findReleaseDateByIgdbId(long igdbId);
}
