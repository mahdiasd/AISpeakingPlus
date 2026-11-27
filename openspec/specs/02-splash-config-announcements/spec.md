# 02-splash-config-announcements Specification

## Purpose

Defines the splash screen initialization flow, server health and app update configuration endpoints, intro video onboarding, and broadcast announcement dialogs.

## Requirements

### Requirement: Splash Initialization and Routing
The system SHALL display branding in `SplashScreen`, evaluate `HasSeenIntroVideoUseCase` and `CheckAuthStatusUseCase` after an initial delay, and navigate to `IntroVideoRoute` (for first-time users) or `MainRoute` (for returning users), supporting retry on failure.

#### Scenario: First-time user onboarding redirect
- **WHEN** the app opens on `SplashScreen` and `hasSeenIntro == false`
- **THEN** the system navigates the user to `IntroVideoRoute` after checking state

#### Scenario: Server communication error on splash
- **WHEN** initial status checking throws an error
- **THEN** `SplashUiState` exposes the error message and allows dispatching `SplashIntent.Retry`

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

### Requirement: Android Update Link and Force-Update Blocking
When the config service indicates an available app update on Android, the client SHALL provide a direct link to the application's CafeBazaar page (`link`); if the update is mandatory (`currentVersion < forceVersion`), the client SHALL block navigation to subsequent screens.

#### Scenario: Optional or suggested update on Android
- **WHEN** the config response indicates a newer version (`suggestVersion` or `lastVersion`) on Android without forcing (`currentVersion >= forceVersion`)
- **THEN** the client displays the update prompt with a link to the CafeBazaar page and allows the user to proceed

#### Scenario: Force update blocking navigation
- **WHEN** the config response indicates a mandatory update (`currentVersion < forceVersion`)
- **THEN** the client prevents navigation to `IntroVideoRoute` or `MainRoute` and keeps the user on the update prompt with the CafeBazaar link (on Android)

### Requirement: Active Broadcast Announcement Display and Suppression
The system SHALL fetch the latest active announcement from `/api/announcements/active` on app startup, render `AnnouncementDialog` styled by `type` (`INFO`, `WARNING`, `ERROR`, `SUCCESS`), enforce `isBlocking`, and allow 24-hour suppression for non-blocking announcements.

#### Scenario: Non-blocking announcement with 24-hour suppression
- **WHEN** the server returns an active announcement with `isBlocking = false` and the user enables the 24-hour suppression checkbox and dismisses it
- **THEN** the dialog closes and `SuppressAnnouncementUseCase` suppresses it for 24 hours

#### Scenario: Executing announcement action buttons
- **WHEN** the user clicks a primary or secondary button with action `OPEN_URL`, `NAVIGATE`, `RELOAD`, `EXIT_APP`, or `CLOSE`
- **THEN** `AppNavigation` executes the corresponding action (opening the URI, navigating to `subscription`/`profile`/`stages`/`leitner`, reloading, or exiting)
