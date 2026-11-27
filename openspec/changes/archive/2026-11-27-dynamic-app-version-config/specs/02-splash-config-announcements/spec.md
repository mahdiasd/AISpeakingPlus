## MODIFIED Requirements

### Requirement: Server Health and App Config Endpoint
The server SHALL expose `/health` for unauthenticated liveness checks and `/api/v1/config` and `/api/v2/config` (GET/POST) returning `ConfigResponseDto` (`update`, `welcomeMessage`, `tokenAlive`) while optionally registering a provided `firebaseToken`, reading `UpdateResponseDto` dynamically from persisted configuration with Redis caching and safe defaults when no record exists.

#### Scenario: Fetching app config and registering device token
- **WHEN** a client sends `POST /api/v2/config` with optional device info or `firebaseToken`
- **THEN** the server registers the token in `DeviceTokenService` if present and returns `UpdateResponseDto` (`force_version` from `minVersion`, `last_version` from `lastVersion`, `suggest_version` from `suggestVersion`, `link`, `message`)

#### Scenario: Returning dynamic config values after admin update
- **WHEN** an administrator updates app version settings and a client requests `GET /api/v1/config` or `GET /api/v2/config`
- **THEN** the server returns the updated `UpdateResponseDto` values from cache or PostgreSQL

#### Scenario: Safe default fallback when no database record exists
- **WHEN** no app version configuration record exists in the database
- **THEN** the server returns safe default `UpdateResponseDto` values (`force_version = 10`, `last_version = 10`, `suggest_version = 10`, `link = "https://cafebazaar.ir/app/ir.aispeaking"`, and a Persian update message) without error

## ADDED Requirements

### Requirement: Admin App Version Configuration Management
The server SHALL expose `GET /api/admin/config/app-version` and `PUT /api/admin/config/app-version` under `ADMIN_JWT_NAME` authentication to retrieve and update Android app version settings (`lastVersion`, `minVersion`, `suggestVersion`, `link`, `message`), validating inputs, refreshing cache, recording `AuditLogService` entries, and exposing an `admin-web` management page.

#### Scenario: Authenticated admin fetches and updates app version configuration
- **WHEN** an authenticated admin sends `PUT /api/admin/config/app-version` with valid `lastVersion`, `minVersion`, `suggestVersion`, `link`, and `message`
- **THEN** the server persists the settings, refreshes the cached configuration, records an `APP_VERSION_CONFIG_UPDATE` entry in `AuditLogService`, and returns `200 OK`

#### Scenario: Rejecting invalid version or link values
- **WHEN** an admin submits `minVersion > lastVersion`, `suggestVersion > lastVersion`, a non-HTTP(S) link, or a blank message
- **THEN** the server rejects the request with `400 Bad Request` and leaves persisted settings unchanged

#### Scenario: Unauthenticated access to admin app version endpoints
- **WHEN** a client calls `GET` or `PUT /api/admin/config/app-version` without a valid `ADMIN_JWT_NAME` token
- **THEN** the server rejects the request with `401 Unauthorized`
