package com.wulghash.gamereleasetracker.domain.port.out;

import com.wulghash.gamereleasetracker.domain.model.AppUser;

import java.util.Optional;

public interface UserRepository {

    AppUser save(AppUser user);

    Optional<AppUser> findByGoogleId(String googleId);
}
