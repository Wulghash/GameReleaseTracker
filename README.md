# Game Release Tracker

A Spring Boot REST API for tracking upcoming video game releases. Add games manually, filter by platform or status, and subscribe to email notifications before a game drops.

---

## Features

- **Game catalog** — create, update, and delete upcoming games with release dates, platforms, shop URLs, and cover images
- **Status tracking** — games move through `UPCOMING → RELEASED` (or `CANCELLED`)
- **Filtering** — filter the game list by platform, status, and release date range with pagination
- **Email subscriptions** — subscribe to a game with an email address and receive:
  - A reminder **7 days before** release
  - A notification **on release day**
- **Unsubscribe** — one-click unsubscribe link included in every email

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.5 |
| Persistence | PostgreSQL + Spring Data JPA + Flyway |
| Email | Spring Mail (MailHog for local dev) |
| Build | Maven |
| Testing | JUnit 5, Mockito, Testcontainers |

---

## Architecture

Hexagonal (Ports & Adapters) — the domain has zero framework dependencies.

```
src/main/java/com/wulghash/gamereleasetracker/
├── domain/
│   ├── model/          # Game, GameStatus, Platform, Subscription (pure Java)
│   └── port/
│       ├── in/         # GameUseCase, SubscriptionUseCase (input ports)
│       └── out/        # GameRepository, SubscriptionRepository (output ports)
├── application/
│   └── service/        # GameService, SubscriptionService, NotificationScheduler
└── infrastructure/
    ├── persistence/    # JPA entities, Spring Data repos, adapters
    ├── web/            # REST controllers, DTOs, GlobalExceptionHandler
    └── mail/           # EmailNotificationService
```

---

## Getting Started

### Prerequisites

- Java 17+
- Docker (for PostgreSQL and MailHog)

### Run locally

```bash
# Start PostgreSQL and MailHog
docker-compose up -d

# Start the application
./mvnw spring-boot:run
```

The API is available at `http://localhost:8080`.
Sent emails are visible at `http://localhost:8025` (MailHog UI).

---

## API Reference

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

**Create / Update game body:**

```json
{
  "title": "Elden Ring 2",
  "description": "Sequel to the 2022 action RPG",
  "releaseDate": "2026-06-15",
  "platforms": ["PC", "PS5", "XBOX"],
  "shopUrl": "https://store.steampowered.com/app/example",
  "imageUrl": "https://example.com/cover.jpg",
  "developer": "FromSoftware",
  "publisher": "Bandai Namco"
}
```

**Update status body:**

```json
{ "status": "RELEASED" }
```

### Subscriptions

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/v1/games/{id}/subscribe` | Subscribe to a game |
| `GET` | `/api/v1/unsubscribe/{token}` | Unsubscribe via email link |

**Subscribe body:**

```json
{ "email": "player@example.com" }
```

### Response codes

| Code | Meaning |
|------|---------|
| `200` | OK |
| `201` | Created |
| `204` | No Content |
| `400` | Validation error — `{"errors": [...]}` |
| `404` | Not found — `{"message": "..."}` |
| `409` | Already subscribed — `{"message": "..."}` |

---

## Running Tests

```bash
# All tests (requires Docker for Testcontainers)
./mvnw test
```

Tests are split into four layers:

| Test class | Type | Count |
|---|---|---|
| `GameTest`, `SubscriptionTest` | Domain unit | 6 |
| `GameServiceTest`, `SubscriptionServiceTest`, `NotificationSchedulerTest` | Service unit (Mockito) | 19 |
| `GameRepositoryAdapterTest`, `SubscriptionRepositoryAdapterTest` | JPA integration (Testcontainers) | 17 |
| `GameControllerTest`, `SubscriptionControllerTest` | Web slice (`@WebMvcTest`) | 18 |
| `GameIntegrationTest`, `GameReleaseTrackerApplicationTests` | End-to-end (Testcontainers) | 5 |

---

## Email Notifications

Notifications are sent daily at **9 AM** by a scheduled job:

- **7-day reminder** — sent to all subscribers of a game releasing in exactly 7 days
- **Release day** — sent to all subscribers of a game releasing today

Every email contains an unsubscribe link: `GET /api/v1/unsubscribe/{token}`

In production, configure a real SMTP provider by overriding:

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
