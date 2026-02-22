package com.wulghash.gamereleasetracker.infrastructure.persistence;

import com.wulghash.gamereleasetracker.domain.model.GameStatus;
import com.wulghash.gamereleasetracker.domain.model.Platform;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

class GameSpecification {

    static Specification<GameJpaEntity> withFilters(UUID userId, Platform platform, GameStatus status, LocalDate from, LocalDate to) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("userId"), userId));

            if (platform != null) {
                predicates.add(cb.isMember(platform, root.get("platforms")));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (from != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("releaseDate"), from));
            }
            if (to != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("releaseDate"), to));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    static Specification<GameJpaEntity> withIdAndUserId(UUID id, UUID userId) {
        return (root, query, cb) -> cb.and(
                cb.equal(root.get("id"), id),
                cb.equal(root.get("userId"), userId)
        );
    }

    static Specification<GameJpaEntity> withStatus(GameStatus status) {
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }
}
