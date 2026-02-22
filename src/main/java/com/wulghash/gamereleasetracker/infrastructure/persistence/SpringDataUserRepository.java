package com.wulghash.gamereleasetracker.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

interface SpringDataUserRepository extends JpaRepository<AppUserJpaEntity, UUID> {

    Optional<AppUserJpaEntity> findByGoogleId(String googleId);
}
