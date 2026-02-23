package com.wulghash.gamereleasetracker.infrastructure.igdb;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.wulghash.gamereleasetracker.domain.model.Platform;
import com.wulghash.gamereleasetracker.infrastructure.web.dto.GameLookupDetail;
import com.wulghash.gamereleasetracker.infrastructure.web.dto.GameLookupResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class IgdbClient {

    private static final Logger log = LoggerFactory.getLogger(IgdbClient.class);
    private static final String BASE_URL = "https://api.igdb.com/v4";

    // Map IGDB platform IDs to our Platform enum
    private static final Map<Integer, Platform> PLATFORM_MAP = Map.of(
            6, Platform.PC,       // PC (Windows)
            14, Platform.PC,      // Mac
            3, Platform.PC,       // Linux
            167, Platform.PS5,    // PlayStation 5
            169, Platform.XBOX,   // Xbox Series X|S
            130, Platform.SWITCH  // Nintendo Switch
    );

    private final RestClient restClient;
    private final IgdbTokenService tokenService;
    private final String clientId;

    public IgdbClient(IgdbTokenService tokenService,
                      @Value("${app.igdb.client-id:}") String clientId) {
        this.tokenService = tokenService;
        this.clientId = clientId;
        this.restClient = RestClient.create();
    }

    public List<GameLookupResult> search(String query) {
        if (!tokenService.isConfigured()) {
            log.warn("IGDB not configured, skipping search");
            return List.of();
        }

        String apicalypse = String.format(
                "search \"%s\"; fields name,first_release_date,cover.url,platforms.id,status,aggregated_rating; " +
                "where status = null | status != (5,6,7,8); limit 10;",
                query.replace("\"", "\\\""));

        List<IgdbGame> games = callApi("/games", apicalypse);
        return games.stream()
                .map(g -> new GameLookupResult(
                        g.id(),
                        g.name(),
                        g.firstReleaseDateAsString(),
                        g.coverUrl(),
                        mapPlatforms(g.platforms()),
                        roundScore(g.aggregatedRating())))
                .collect(Collectors.toList());
    }

    public GameLookupDetail getDetail(long igdbId) {
        if (!tokenService.isConfigured()) {
            log.warn("IGDB not configured, skipping detail fetch");
            return null;
        }

        String apicalypse = String.format(
                "fields name,first_release_date,cover.url,platforms.id," +
                "involved_companies.company.name,involved_companies.developer,involved_companies.publisher," +
                "summary,aggregated_rating; where id = %d; limit 1;",
                igdbId);

        List<IgdbGame> games = callApi("/games", apicalypse);
        if (games.isEmpty()) return null;

        IgdbGame g = games.get(0);

        String developer = g.involvedCompanies() == null ? null :
                g.involvedCompanies().stream()
                        .filter(IgdbInvolvedCompany::developer)
                        .map(c -> c.company() != null ? c.company().name() : null)
                        .filter(Objects::nonNull)
                        .findFirst().orElse(null);

        String publisher = g.involvedCompanies() == null ? null :
                g.involvedCompanies().stream()
                        .filter(IgdbInvolvedCompany::publisher)
                        .map(c -> c.company() != null ? c.company().name() : null)
                        .filter(Objects::nonNull)
                        .findFirst().orElse(null);

        return new GameLookupDetail(
                g.name(),
                g.firstReleaseDateAsString(),
                g.coverUrl(),
                mapPlatforms(g.platforms()),
                g.summary(),
                developer,
                publisher,
                roundScore(g.aggregatedRating()));
    }

    private List<IgdbGame> callApi(String path, String body) {
        try {
            String token = tokenService.getToken();
            log.debug("IGDB query: {}", body);
            List<IgdbGame> result = restClient.post()
                    .uri(BASE_URL + path)
                    .header("Client-ID", clientId)
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(body)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
            return result != null ? result : List.of();
        } catch (Exception e) {
            log.error("IGDB API error on {}: {}", path, e.getMessage());
            return List.of();
        }
    }

    private List<Platform> mapPlatforms(List<IgdbPlatform> platforms) {
        if (platforms == null) return List.of();
        return platforms.stream()
                .map(p -> PLATFORM_MAP.get(p.id()))
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }

    // ---- IGDB response shapes ----

    private static Integer roundScore(Double v) {
        return v != null ? (int) Math.round(v) : null;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record IgdbGame(
            long id,
            String name,
            @JsonProperty("first_release_date") Long firstReleaseDateEpoch,
            IgdbCover cover,
            List<IgdbPlatform> platforms,
            @JsonProperty("involved_companies") List<IgdbInvolvedCompany> involvedCompanies,
            String summary,
            Integer status,
            @JsonProperty("aggregated_rating") Double aggregatedRating
    ) {
        String firstReleaseDateAsString() {
            if (firstReleaseDateEpoch == null) return null;
            return LocalDate.ofEpochDay(firstReleaseDateEpoch / 86400)
                    .format(DateTimeFormatter.ISO_LOCAL_DATE);
        }

        String coverUrl() {
            if (cover == null || cover.url() == null) return null;
            // IGDB URLs start with "//" and use t_thumb; upgrade to t_cover_big
            return "https:" + cover.url().replace("t_thumb", "t_cover_big");
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record IgdbCover(String url) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record IgdbPlatform(int id) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record IgdbInvolvedCompany(
            IgdbCompany company,
            boolean developer,
            boolean publisher
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record IgdbCompany(String name) {}
}
