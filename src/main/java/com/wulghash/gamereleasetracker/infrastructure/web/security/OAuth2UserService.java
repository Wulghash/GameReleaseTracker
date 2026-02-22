package com.wulghash.gamereleasetracker.infrastructure.web.security;

import com.wulghash.gamereleasetracker.domain.model.AppUser;
import com.wulghash.gamereleasetracker.domain.port.out.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String googleId = oAuth2User.getAttribute("sub");
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        AppUser user = userRepository.findByGoogleId(googleId)
                .orElseGet(() -> userRepository.save(AppUser.builder()
                        .id(UUID.randomUUID())
                        .googleId(googleId)
                        .email(email)
                        .name(name)
                        .createdAt(LocalDateTime.now())
                        .build()));

        return new AppUserPrincipal(user);
    }
}
