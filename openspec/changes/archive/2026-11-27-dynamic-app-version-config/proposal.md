# Proposal

## Why

Currently, `ConfigRouting.kt` returns hardcoded `UpdateResponseDto` values (`forceVersion = 10`, `lastVersion = 10`, `suggestVersion = 10`, CafeBazaar link, and Persian update message), requiring a backend code change and redeployment every time an Android app release or force-update threshold changes. Making these settings dynamic in PostgreSQL with Redis caching and exposing them in `admin-web` enables administrators to manage app update behavior immediately without server deployments.

## What Changes

- Persist Android app version and update settings (`lastVersion`, `minVersion` / `forceVersion`, `suggestVersion`, `link`, `message`) in a PostgreSQL table (`app_version_configs`) via JetBrains Exposed.
- Update `/api/v1/config` and `/api/v2/config` (GET/POST) to read `UpdateResponseDto` dynamically from PostgreSQL with Redis caching and safe fallback defaults when no record exists.
- Add admin endpoints `GET /api/admin/config/app-version` and `PUT /api/admin/config/app-version` protected by `ADMIN_JWT_NAME`, with input validation, Redis cache refresh/invalidation, `AuditLogService` logging (`APP_VERSION_CONFIG_UPDATE`), and OpenAPI 3.0.3 `.describe { ... }` metadata.
- Add an App Version Settings page (`/admin/app-version`) in `admin-web` with sidebar navigation so administrators can view and edit version thresholds, CafeBazaar link, and Persian update message.
- Out of scope: KMP client modules (`data/`, `domain/`, `network/`, `feature/`, `sharedUI/`, `webApp/`, `androidApp/`) remain untouched.

## Capabilities

### New Capabilities
<!-- None; extends existing splash/config capability -->

### Modified Capabilities
- `02-splash-config-announcements`: Dynamic persistence, Redis caching, and admin management of app version and update configuration (`UpdateResponseDto` served by `/api/v1/config` and `/api/v2/config`, managed via `/api/admin/config/app-version` and `admin-web`).

## Impact

- **Backend (`server/`)**: New `AppVersionConfigTable`, `AppVersionConfigRepository`, `AppConfigService`, `AdminConfigRouting`, and updates to `ConfigDtos.kt`, `ConfigRouting.kt`, `AdminRouting.kt`, `Databases.kt`, `Routing.kt`, and test suites.
- **Admin Web (`admin-web/`)**: New `AppVersionConfigPage.tsx`, updates to `types/index.ts`, `api/client.ts`, `AdminSidebar.tsx`, `App.tsx`, and `AuditLogsPage.tsx`.
- **APIs**: No breaking changes to `/api/v1/config` or `/api/v2/config` wire contracts (`force_version`, `last_version`, `suggest_version`, `link`, `message` preserved).
