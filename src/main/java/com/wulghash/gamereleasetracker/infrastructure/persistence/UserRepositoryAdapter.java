package com.wulghash.gamereleasetracker.infrastructure.persistence;

import com.wulghash.gamereleasetracker.domain.model.AppUser;
import com.wulghash.gamereleasetracker.domain.port.out.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserRepositoryAdapter implements UserRepository {

    private final SpringDataUserRepository jpaRepository;

    @Override
    public AppUser save(AppUser user) {
        return jpaRepository.save(AppUserJpaEntity.from(user)).toDomain();
    }

    @Override
    public Optional<AppUser> findByGoogleId(String googleId) {
        return jpaRepository.findByGoogleId(googleId).map(AppUserJpaEntity::toDomain);
    }
}
