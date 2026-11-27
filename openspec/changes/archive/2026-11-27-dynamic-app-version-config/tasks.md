# Tasks

## 1. Backend Persistence & Service (`server/`)

- [x] 1.1 Create `AppVersionConfigTable` in `server/src/main/kotlin/ir/speaking/feature/config/db/AppVersionConfigTable.kt` and register it in `Databases.kt` (with default `"android"` seeding) and `configureTestDatabases.kt`
- [x] 1.2 Add `AppVersionConfigDto` and `UpdateAppVersionConfigRequest` to `ConfigDtos.kt` while keeping `UpdateResponseDto` wire-compatible
- [x] 1.3 Implement `AppVersionConfigRepository` and `AppConfigService` (Redis cache + PostgreSQL + safe defaults + input validation) and verify with `AppConfigServiceTest`

## 2. Backend Routing & OpenAPI (`server/`)

- [x] 2.1 Update `ConfigRouting.kt` (`/api/v1/config` and `/api/v2/config` GET/POST) to read `UpdateResponseDto` from `AppConfigService` with complete `.describe { ... }` OpenAPI blocks
- [x] 2.2 Implement `AdminConfigRouting.kt` (`GET` and `PUT /api/admin/config/app-version` under `ADMIN_JWT_NAME`) with `AuditLogService` logging (`APP_VERSION_CONFIG_UPDATE`), mount it in `AdminRouting.kt`, add Apidog example in `Routing.kt`, and verify with `ConfigRoutingTest` and `OpenApiRoutingTest`

## 3. Admin Panel UI (`admin-web/`)

- [x] 3.1 Add `AppVersionConfig` and `UpdateAppVersionConfigPayload` types to `admin-web/src/types/index.ts` and API methods to `admin-web/src/api/client.ts`
- [x] 3.2 Create `admin-web/src/pages/AppVersionConfigPage.tsx`, wire `/admin/app-version` route in `App.tsx`, add sidebar menu item in `AdminSidebar.tsx`, and add `CONFIG` filter in `AuditLogsPage.tsx`

## 4. Verification

- [x] 4.1 Run `./gradlew :server:test` and confirm all server unit, routing, and OpenAPI tests pass
- [x] 4.2 Run `cd admin-web && npm run build` and confirm TypeScript check and Vite production build succeed

## Workflow follow-up

- Sync delta specs to `openspec/specs/02-splash-config-announcements/spec.md` and archive the change to `openspec/changes/archive/2026-11-27-dynamic-app-version-config`.
- Stage and commit changes on `feature/dynamic-app-version-config`.
