package com.wulghash.gamereleasetracker.infrastructure.web;

import com.wulghash.gamereleasetracker.domain.model.Platform;
import com.wulghash.gamereleasetracker.infrastructure.rawg.RawgClient;
import com.wulghash.gamereleasetracker.infrastructure.web.dto.GameLookupDetail;
import com.wulghash.gamereleasetracker.infrastructure.web.dto.GameLookupResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/games")
@RequiredArgsConstructor
public class RawgLookupController {

    private static final Map<String, Platform> SLUG_TO_PLATFORM = Map.of(
            "pc",              Platform.PC,
            "playstation5",    Platform.PS5,
            "xbox-series-x",   Platform.XBOX,
            "xbox-series-s-x", Platform.XBOX,
            "xbox-one",        Platform.XBOX,
            "xbox360",         Platform.XBOX,
            "xbox",            Platform.XBOX,
            "nintendo-switch", Platform.SWITCH
    );

    private final RawgClient rawgClient;

    @GetMapping("/lookup")
    public List<GameLookupResult> lookup(@RequestParam String q) {
        LocalDate today = LocalDate.now();
        return rawgClient.search(q).stream()
                .filter(r -> r.released() == null || LocalDate.parse(r.released()).isAfter(today))
                .map(r -> new GameLookupResult(
                        r.id(),
                        r.name(),
                        r.released(),
                        r.backgroundImage(),
                        mapPlatforms(r.platforms())
                ))
                .toList();
    }

    @GetMapping("/lookup/{rawgId}")
    public GameLookupDetail lookupDetail(@PathVariable long rawgId) {
        RawgClient.RawgGameDetail detail = rawgClient.detail(rawgId);
        if (detail == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "RAWG game not found");
        }
        String developer = detail.developers() != null && !detail.developers().isEmpty()
                ? detail.developers().get(0).name() : null;
        String publisher = detail.publishers() != null && !detail.publishers().isEmpty()
                ? detail.publishers().get(0).name() : null;
        return new GameLookupDetail(
                detail.name(),
                detail.released(),
                detail.backgroundImage(),
                mapPlatforms(detail.platforms()),
                detail.descriptionRaw(),
                developer,
                publisher
        );
    }

    private List<Platform> mapPlatforms(List<RawgClient.RawgPlatformWrapper> wrappers) {
        if (wrappers == null) return List.of();
        Set<Platform> seen = new LinkedHashSet<>();
        for (RawgClient.RawgPlatformWrapper w : wrappers) {
            if (w.platform() != null) {
                Platform p = SLUG_TO_PLATFORM.get(w.platform().slug());
                if (p != null) seen.add(p);
            }
        }
        return List.copyOf(seen);
    }
}
