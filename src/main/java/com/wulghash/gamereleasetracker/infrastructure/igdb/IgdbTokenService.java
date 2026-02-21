package com.wulghash.gamereleasetracker.infrastructure.igdb;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Instant;

@Service
public class IgdbTokenService {

    private static final Logger log = LoggerFactory.getLogger(IgdbTokenService.class);

    private final RestClient restClient;
    private final String clientId;
    private final String clientSecret;

    private String cachedToken;
    private Instant tokenExpiry = Instant.EPOCH;

    public IgdbTokenService(
            @Value("${app.igdb.client-id:}") String clientId,
            @Value("${app.igdb.client-secret:}") String clientSecret) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.restClient = RestClient.create();
    }

    public boolean isConfigured() {
        return !clientId.isBlank() && !clientSecret.isBlank();
    }

    public synchronized String getToken() {
        if (cachedToken != null && Instant.now().isBefore(tokenExpiry.minusSeconds(300))) {
            return cachedToken;
        }
        log.info("Fetching new IGDB access token");
        try {
            TokenResponse response = restClient.post()
                    .uri("https://id.twitch.tv/oauth2/token?client_id={id}&client_secret={secret}&grant_type=client_credentials",
                            clientId, clientSecret)
                    .retrieve()
                    .body(TokenResponse.class);
            if (response == null) throw new RuntimeException("Empty token response");
            cachedToken = response.accessToken();
            tokenExpiry = Instant.now().plusSeconds(response.expiresIn());
            log.info("IGDB token fetched, expires in {}s", response.expiresIn());
            return cachedToken;
        } catch (Exception e) {
            log.error("Failed to fetch IGDB token: {}", e.getMessage());
            throw new RuntimeException("Failed to fetch IGDB token", e);
        }
    }

    record TokenResponse(
            @JsonProperty("access_token") String accessToken,
            @JsonProperty("expires_in") long expiresIn
    ) {}
}
