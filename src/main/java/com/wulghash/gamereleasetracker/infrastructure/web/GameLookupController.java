package com.wulghash.gamereleasetracker.infrastructure.web;

import com.wulghash.gamereleasetracker.infrastructure.igdb.IgdbClient;
import com.wulghash.gamereleasetracker.infrastructure.web.dto.GameLookupDetail;
import com.wulghash.gamereleasetracker.infrastructure.web.dto.GameLookupResult;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/games/lookup")
public class GameLookupController {

    private final IgdbClient igdbClient;

    public GameLookupController(IgdbClient igdbClient) {
        this.igdbClient = igdbClient;
    }

    @GetMapping
    public List<GameLookupResult> search(@RequestParam String q) {
        return igdbClient.search(q);
    }

    @GetMapping("/{igdbId}")
    public ResponseEntity<GameLookupDetail> detail(@PathVariable long igdbId) {
        GameLookupDetail detail = igdbClient.getDetail(igdbId);
        return detail != null ? ResponseEntity.ok(detail) : ResponseEntity.notFound().build();
    }
}
