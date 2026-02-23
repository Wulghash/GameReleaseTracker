package com.wulghash.gamereleasetracker.domain.model;

import java.util.UUID;

public class BacklogEntryNotFoundException extends RuntimeException {
    public BacklogEntryNotFoundException(UUID id) {
        super("Backlog entry not found with id: " + id);
    }
}
