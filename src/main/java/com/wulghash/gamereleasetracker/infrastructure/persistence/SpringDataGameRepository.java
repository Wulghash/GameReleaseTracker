package com.wulghash.gamereleasetracker.infrastructure.persistence;

import com.wulghash.gamereleasetracker.domain.model.GameStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;

interface SpringDataGameRepository extends JpaRepository<GameJpaEntity, UUID>,
        JpaSpecificationExecutor<GameJpaEntity> {

    List<GameJpaEntity> findByStatus(GameStatus status);
}
