package com.wulghash.gamereleasetracker.infrastructure.web;

import com.wulghash.gamereleasetracker.domain.model.AppUser;
import com.wulghash.gamereleasetracker.infrastructure.web.security.AppUserPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class UserController {

    @GetMapping("/api/v1/me")
    public MeResponse me(@AuthenticationPrincipal AppUserPrincipal principal) {
        AppUser user = principal.getUser();
        return new MeResponse(user.getId(), user.getEmail(), user.getName());
    }

    record MeResponse(UUID id, String email, String name) {}
}
