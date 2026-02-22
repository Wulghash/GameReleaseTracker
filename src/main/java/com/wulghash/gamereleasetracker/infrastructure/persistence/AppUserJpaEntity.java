package com.wulghash.gamereleasetracker.infrastructure.persistence;

import com.wulghash.gamereleasetracker.domain.model.AppUser;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "app_users")
@Getter
@Setter
@NoArgsConstructor
public class AppUserJpaEntity {

    @Id
    private UUID id;

    @Column(name = "google_id", nullable = false, unique = true)
    private String googleId;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String name;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public static AppUserJpaEntity from(AppUser user) {
        AppUserJpaEntity entity = new AppUserJpaEntity();
        entity.id = user.getId();
        entity.googleId = user.getGoogleId();
        entity.email = user.getEmail();
        entity.name = user.getName();
        entity.createdAt = user.getCreatedAt();
        return entity;
    }

    public AppUser toDomain() {
        return AppUser.builder()
                .id(id)
                .googleId(googleId)
                .email(email)
                .name(name)
                .createdAt(createdAt)
                .build();
    }
}
