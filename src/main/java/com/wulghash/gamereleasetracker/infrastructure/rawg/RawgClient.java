package com.wulghash.gamereleasetracker.infrastructure.rawg;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Collections;
import java.util.List;

@Component
public class RawgClient {

    private static final Logger log = LoggerFactory.getLogger(RawgClient.class);

    private final RestClient restClient;
    private final String apiKey;

    public RawgClient(@Value("${app.rawg.api-key:}") String apiKey) {
        this.apiKey = apiKey;
        this.restClient = RestClient.builder()
                .baseUrl("https://api.rawg.io/api")
                .build();
    }

    public List<RawgSearchResult> search(String query) {
        if (apiKey.isBlank()) return Collections.emptyList();
        try {
            RawgSearchResponse response = restClient.get()
                    .uri(b -> b.path("/games")
                            .queryParam("key", apiKey)
                            .queryParam("search", query)
                            .queryParam("page_size", 10)
                            .build())
                    .retrieve()
                    .body(RawgSearchResponse.class);
            return response != null && response.results() != null ? response.results() : Collections.emptyList();
        } catch (RestClientException e) {
            log.error("RAWG search failed: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    public RawgGameDetail detail(long rawgId) {
        if (apiKey.isBlank()) return null;
        try {
            return restClient.get()
                    .uri(b -> b.path("/games/{id}")
                            .queryParam("key", apiKey)
                            .build(rawgId))
                    .retrieve()
                    .body(RawgGameDetail.class);
        } catch (RestClientException e) {
            log.error("RAWG detail failed for id {}: {}", rawgId, e.getMessage());
            return null;
        }
    }

    // --- Internal DTOs ---

    public record RawgSearchResponse(List<RawgSearchResult> results) {}

    public record RawgSearchResult(
            long id,
            String name,
            String released,
            @JsonProperty("background_image") String backgroundImage,
            List<RawgPlatformWrapper> platforms
    ) {}

    public record RawgPlatformWrapper(RawgPlatform platform) {}

    public record RawgPlatform(long id, String name, String slug) {}

    public record RawgGameDetail(
            long id,
            String name,
            String released,
            @JsonProperty("background_image") String backgroundImage,
            @JsonProperty("description_raw") String descriptionRaw,
            List<RawgPlatformWrapper> platforms,
            List<RawgCompany> developers,
            List<RawgCompany> publishers
    ) {}

    public record RawgCompany(long id, String name) {}
}
