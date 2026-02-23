package com.wulghash.gamereleasetracker.infrastructure.web;

import com.wulghash.gamereleasetracker.domain.model.BacklogStatus;
import com.wulghash.gamereleasetracker.domain.port.in.BacklogUseCase;
import com.wulghash.gamereleasetracker.infrastructure.web.dto.BacklogAddRequest;
import com.wulghash.gamereleasetracker.infrastructure.web.dto.BacklogEntryResponse;
import com.wulghash.gamereleasetracker.infrastructure.web.dto.BacklogUpdateRequest;
import com.wulghash.gamereleasetracker.infrastructure.web.security.AppUserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/backlog")
@RequiredArgsConstructor
public class BacklogController {

    private final BacklogUseCase backlogUseCase;

    @GetMapping
    public List<BacklogEntryResponse> list(
            @RequestParam(required = false) BacklogStatus status,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        return backlogUseCase.list(principal.getUserId(), status).stream()
                .map(BacklogEntryResponse::from)
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BacklogEntryResponse add(
            @Valid @RequestBody BacklogAddRequest request,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        BacklogUseCase.BacklogAddCommand cmd = new BacklogUseCase.BacklogAddCommand(
                request.igdbId(),
                request.name(),
                request.coverUrl(),
                request.releaseDate(),
                request.backlogStatus(),
                request.igdbScore(),
                request.rating(),
                request.notes()
        );
        return BacklogEntryResponse.from(backlogUseCase.add(principal.getUserId(), cmd));
    }

    @PutMapping("/{id}")
    public BacklogEntryResponse update(
            @PathVariable UUID id,
            @RequestBody BacklogUpdateRequest request,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        BacklogUseCase.BacklogUpdateCommand cmd = new BacklogUseCase.BacklogUpdateCommand(
                request.backlogStatus(),
                request.igdbScore(),
                request.rating(),
                request.notes()
        );
        return BacklogEntryResponse.from(backlogUseCase.update(id, principal.getUserId(), cmd));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable UUID id,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        backlogUseCase.delete(id, principal.getUserId());
    }
}
