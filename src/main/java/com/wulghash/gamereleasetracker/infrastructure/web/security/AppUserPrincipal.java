package com.wulghash.gamereleasetracker.infrastructure.web.security;

import com.wulghash.gamereleasetracker.domain.model.AppUser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AppUserPrincipal implements OAuth2User {

    private final AppUser user;

    public AppUserPrincipal(AppUser user) {
        this.user = user;
    }

    public UUID getUserId() {
        return user.getId();
    }

    public AppUser getUser() {
        return user;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return Map.of(
                "sub", user.getGoogleId(),
                "email", user.getEmail(),
                "name", user.getName()
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getName() {
        return user.getGoogleId();
    }
}
