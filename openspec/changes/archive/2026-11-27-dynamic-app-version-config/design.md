# Design

## Context

See `proposal.md` for motivation. Currently `ConfigRouting.kt` constructs `UpdateResponseDto` inline with hardcoded constants. Public endpoints `/api/v1/config` and `/api/v2/config` must remain wire-compatible (`force_version`, `last_version`, `suggest_version`, `link`, `message`).

## Goals / Non-Goals

**Goals:**
- Store platform-keyed (`android` default) app version settings in PostgreSQL using JetBrains Exposed (`app_version_configs`).
- Serve reads with low latency via Redis (`RedissonClient` via `RedisClientHolder`) with 10-minute TTL and safe fallback to PostgreSQL and default constants when Redis or DB rows are absent.
- Provide `ADMIN_JWT_NAME`-protected `GET` and `PUT /api/admin/config/app-version` endpoints with validation, `AuditLogService` recording, and OpenAPI 3.0.3 `.describe { ... }` metadata.
- Provide an RTL Persian settings page in `admin-web` (`/admin/app-version`) with live update preview.

**Non-Goals:**
- Modifying KMP client modules (`data/`, `domain/`, `network/`, `feature/`, `sharedUI/`, `webApp/`, `androidApp/`).
- Multi-platform (iOS/Web) version configuration UI (table is keyed by `platform` for future extensibility, defaulting to `"android"`).

## Decisions

1. **Single-row-per-platform table (`app_version_configs`, PK `platform`)**:
   - Chosen over a generic key-value `app_settings` table so columns (`last_version`, `min_version`, `suggest_version`, `link`, `message`) are strongly typed, schema-checked, and easy to query/upsert in a single row.
2. **Redis write-through + TTL (`app:config:version:android`, 10 min) without non-shared in-memory cache**:
   - Chosen because `AdminRouting` and `ConfigRouting` may resolve or run in environments where `RedissonClient` is `null` (e.g., unit/integration tests); avoiding a per-instance `ConcurrentHashMap` prevents stale reads across services/tests while PostgreSQL/H2 PK lookup remains sub-millisecond.
3. **Lazy `appConfigServiceProvider: () -> AppConfigService` in `AdminConfigRouting` and `by inject<AppConfigService>()` in `ConfigRouting`**:
   - Chosen so `OpenApiRoutingTest` (which boots routing with an empty Koin module) and `AdminApiIntegrationTest` (which boots `adminRouting()` without Koin) both work without eager Koin resolution errors.

## Risks / Trade-offs

- [Redis temporarily unavailable] -> Logged as warning and bypassed; falls back directly to PostgreSQL and safe defaults.
- [Admin sets `minVersion` higher than `lastVersion`] -> Prevented by explicit validation in `AppConfigService.updateAppVersionConfig` (`400 Bad Request`).
