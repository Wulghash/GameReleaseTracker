package com.wulghash.gamereleasetracker.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

interface SpringDataGameRepository extends JpaRepository<GameJpaEntity, UUID>,
        JpaSpecificationExecutor<GameJpaEntity> {
}
