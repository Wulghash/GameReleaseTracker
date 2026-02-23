# Game Release Tracker

A full-stack web app for tracking upcoming video game releases. Browse and search games via IGDB, maintain a personal backlog with status tracking and critic scores, and subscribe to email reminders before a game drops.

---

## Features

- **Google OAuth2 login** — sign in with your Google account; all data is user-specific
- **Game catalog** — create, update, and delete upcoming games with release dates, platforms, shop URLs, and cover images
- **IGDB search** — look up games from IGDB's database and prefill the form automatically
- **Status tracking** — games move through `UPCOMING → RELEASED` (or `CANCELLED`)
- **Automatic release sync** — a daily job (3 AM) refreshes release dates from IGDB and auto-transitions past-due games to RELEASED
- **Filtering** — filter games by platform, status, and release date range with pagination
- **Personal backlog** — track games with statuses: `WANT_TO_PLAY`, `PLAYING`, `COMPLETED`, `DROPPED`
- **Critic score badge** — shows IGDB `aggregated_rating` (0–100) on each backlog card, colour-coded by tier; refresh on demand
- **Email subscriptions** — subscribe to a game to receive:
  - A reminder **7 days before** release
  - A notification **on release day**
  - A notice when a **release date changes**
- **Unsubscribe** — one-click unsubscribe link included in every email

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.5 |
| Persistence | PostgreSQL + Spring Data JPA + Flyway |
| Security | Spring Security + Google OAuth2 |
| External API | IGDB (via Twitch OAuth2 client credentials) |
| Email | Spring Mail — optional; MailHog for local dev |
| Build | Maven |
| Testing | JUnit 5, Mockito, Testcontainers |
| Frontend | React 18, TypeScript, Vite, Tailwind CSS, TanStack Query |

---

## Architecture

Hexagonal (Ports & Adapters) — the domain has zero framework dependencies.

```
src/main/java/com/wulghash/gamereleasetracker/
├── domain/
│   ├── model/          # Game, GameStatus, Platform, Subscription, BacklogEntry,
│   │                   # AppUser, BacklogStatus (pure Java)
│   └── port/
│       ├── in/         # GameUseCase (+ GameCommand), SubscriptionUseCase, BacklogUseCase
│       └── out/        # GameRepository, SubscriptionRepository, BacklogRepository,
│                       # UserRepository, GameLookupPort
├── application/
│   └── service/        # GameService, SubscriptionService, BacklogService,
│                       # NotificationScheduler, IgdbSyncScheduler
└── infrastructure/
    ├── igdb/           # IgdbClient (implements GameLookupPort), IgdbTokenService
    ├── persistence/    # JPA entities, Spring Data repos, adapters
    ├── security/       # SecurityConfig, AppUserPrincipal, OAuth2UserService
    ├── web/            # REST controllers, DTOs, GlobalExceptionHandler
    └── mail/           # EmailNotificationService (optional — skipped if SMTP not configured)
```

---

## Getting Started

### Prerequisites

- Java 17+
- Docker (for PostgreSQL; MailHog optional)
- A Google OAuth2 client ID + secret ([Google Cloud Console](https://console.cloud.google.com/))
- An IGDB / Twitch API client ID + secret ([Twitch Developer Portal](https://dev.twitch.tv/console))

### Configuration

Create `src/main/resources/application-local.properties` (gitignored) with your credentials:

```properties
# Google OAuth2
spring.security.oauth2.client.registration.google.client-id=YOUR_GOOGLE_CLIENT_ID
spring.security.oauth2.client.registration.google.client-secret=YOUR_GOOGLE_CLIENT_SECRET

# IGDB (Twitch)
app.igdb.client-id=YOUR_TWITCH_CLIENT_ID
app.igdb.client-secret=YOUR_TWITCH_CLIENT_SECRET
```

### Run locally

```bash
# Start PostgreSQL (and optional MailHog)
docker-compose up -d

# Start the backend (activates the "local" profile automatically)
./mvnw spring-boot:run

# Start the frontend dev server (in a separate terminal)
cd frontend
npm install
npm run dev
```

| Service | URL |
|---------|-----|
| Frontend | `http://localhost:5173` |
| Backend API | `http://localhost:8080` |
| MailHog UI | `http://localhost:8025` |

---

## API Reference

All `/api/**` endpoints require authentication (session cookie from Google OAuth2).

### Auth

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/v1/me` | Returns the current user (`id`, `email`, `name`) |
| `GET` | `/oauth2/authorization/google` | Initiates Google OAuth2 login |

### Games

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/v1/games` | List games (paginated) |
| `GET` | `/api/v1/games/{id}` | Get a single game |
| `POST` | `/api/v1/games` | Create a game |
| `PUT` | `/api/v1/games/{id}` | Update a game |
| `PATCH` | `/api/v1/games/{id}/status` | Update game status |
| `DELETE` | `/api/v1/games/{id}` | Delete a game |

**List query parameters:**

| Parameter | Type | Description |
|-----------|------|-------------|
| `platform` | `PC \| PS5 \| XBOX \| SWITCH` | Filter by platform |
| `status` | `UPCOMING \| RELEASED \| CANCELLED` | Filter by status |
| `from` | `YYYY-MM-DD` | Release date from |
| `to` | `YYYY-MM-DD` | Release date to |
| `page` | integer | Page number (default 0) |
| `size` | integer | Page size (default 20) |
| `sort` | string | Sort field (default `releaseDate,asc`) |

### IGDB Lookup

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/v1/games/lookup?q={query}` | Search IGDB for games |
| `GET` | `/api/v1/games/lookup/{igdbId}` | Fetch full detail for an IGDB game |

### Backlog

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/v1/backlog` | List backlog entries (optional `?status=` filter) |
| `POST` | `/api/v1/backlog` | Add a game to the backlog |
| `PUT` | `/api/v1/backlog/{id}` | Update a backlog entry (status, score, rating, notes) |
| `DELETE` | `/api/v1/backlog/{id}` | Remove a game from the backlog |

**Backlog statuses:** `WANT_TO_PLAY`, `PLAYING`, `COMPLETED`, `DROPPED`

### Subscriptions

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/v1/games/{id}/subscribe` | Subscribe to release notifications |
| `GET` | `/api/v1/unsubscribe/{token}` | Unsubscribe via email link (public) |

### Response codes

| Code | Meaning |
|------|---------|
| `200` | OK |
| `201` | Created |
| `204` | No Content |
| `400` | Validation error — `{"errors": [...]}` |
| `401` | Unauthenticated |
| `404` | Not found — `{"message": "..."}` |
| `409` | Conflict (duplicate) — `{"message": "..."}` |

---

## Running Tests

```bash
# All tests (requires Docker for Testcontainers)
./mvnw test
```

**69 tests** across five layers:

| Test class | Type | Count |
|---|---|---|
| `GameTest`, `SubscriptionTest` | Domain unit | 6 |
| `GameServiceTest`, `SubscriptionServiceTest`, `NotificationSchedulerTest` | Service unit (Mockito) | 22 |
| `GameRepositoryAdapterTest`, `SubscriptionRepositoryAdapterTest` | JPA integration (Testcontainers) | 17 |
| `GameControllerTest`, `SubscriptionControllerTest` | Web slice (`@WebMvcTest`) | 19 |
| `GameIntegrationTest`, `GameReleaseTrackerApplicationTests` | End-to-end (Testcontainers) | 5 |

---

## Scheduled Jobs

| Job | Schedule | What it does |
|-----|----------|--------------|
| `NotificationScheduler` | 9 AM daily | Sends release-day and 7-day-reminder emails to subscribers |
| `IgdbSyncScheduler` | 3 AM daily | Refreshes release dates from IGDB; auto-transitions past-due games to RELEASED; emails subscribers on date change |

---

## Email Notifications

Email is **optional** — the app runs without an SMTP server configured (notifications are silently skipped).

For local development, MailHog captures all outgoing mail at `http://localhost:8025`.

For production, configure via environment variables or properties:

```properties
spring.mail.host=smtp.your-provider.com
spring.mail.port=587
spring.mail.username=your-username
spring.mail.password=your-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
app.mail.from=noreply@yourdomain.com
app.base-url=https://yourdomain.com
```
