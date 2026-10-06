# Phase 0 Research: User Profile & Account Overview

**Feature**: `003-user-profile`  
**Date**: 2026-10-06  
**Status**: Completed  

---

## 1. Consolidated Profile API vs Multiple Client Calls

### Context & Problem
The Profile screen displays identity (nickname, phone, avatar), subscription metrics (active status, plan title, remaining days, expiration date), and learning stats (total stars, completed stages count, total XP/score, language level).
Currently, the server has `/api/v2/auth/me` (returning only basic identity) and `/api/v2/subscriptions/status` (returning subscription status), but no aggregated stars or completed stages endpoint.

### Decision
Implement a dedicated, consolidated endpoint `GET /api/v2/user/profile` (and `PUT /api/v2/user/profile` for updates) on the Ktor backend.

### Rationale
- **Latency & Mobile Battery**: Fulfills Constitution Principle V (Ultra-Low Latency & High Performance). Consolidating into a single SQL join / parallel coroutine read achieves sub-50ms response times and eliminates 3 sequential mobile network round-trips.
- **Transactional Consistency**: Eliminates inconsistent UI states (e.g., subscription state out of sync with user identity).
- **Ktor OpenAPI Compliance**: Generates a clear, self-documenting contract with full OpenAPI 3.0.3 schema and examples (`.describe { ... }`).

### Alternatives Considered
- *Option B: Multiple client-side requests (`/auth/me`, `/subscriptions/status`, and stages list)*: Rejected because it increases mobile latency (3 round-trips), risks partial loading failures, and forces the client to calculate total stars from full stage objects.
- *Option C: Expand `/api/v2/auth/me`*: Rejected because `/auth/me` is an authentication handshake token validation endpoint; mixing gamification and subscription analytics into auth violates Single Responsibility Principle.

---

## 2. Profile Update & Avatar Selection Architecture

### Context & Problem
Users need to update basic profile information (nickname and avatar). Avatars in the application are currently curated vector/raster drawables stored in `sharedUI/src/commonMain/composeResources/drawable/` (`avatar_g1.png` through `avatar_g12.png`, `avatar_b8.png`, `avatar_b9.png`, etc.).

### Decision
- `PUT /api/v2/user/profile` receives `UpdateProfileRequestDto(nickName: String?, avatar: String?, firstName: String?, lastName: String?)`.
- Server validates that `nickName` length is between 2 and 50 characters, strips unwanted control characters, and persists to `UserTable`.
- Avatars use predefined string keys matching client drawable resources (e.g. `"avatar_g1"`, `"avatar_b8"`). A picker dialog on the client displays available avatars and lets the user select one with immediate visual feedback.

### Rationale
- Storing identifier keys rather than raw binary uploads keeps backend storage lightweight, fast, and secure.
- Instant rendering on Compose Multiplatform using existing local resource painters without network bandwidth consumption.

### Alternatives Considered
- *Option B: Custom image upload (multipart)*: Deemed out of scope for v1 since the application design relies on consistent gamified 3D avatars.

---

## 3. Guest Mode Profile Architecture & Progression Sync

### Context & Problem
Guest users can browse and earn stars on Stage 1 before authenticating. When a guest opens the profile screen, what should they see?

### Decision
- Profile screen detects `AccessTier.GUEST` (or empty token).
- Instead of showing a network 401 error or blocking the user, `ProfileViewModel` reads local data from `LocalGuestProgressDataSource` and `UserPreferences`.
- Displays "زبان‌آموز مهمان" (Guest Learner), local stars earned, local score, and a prominent call-to-action banner ("ثبت‌نام و ذخیره دائمی پیشرفت").
- Tapping the banner triggers navigation to `LoginRoute`. Upon successful login, the existing `SyncGuestProgressUseCase` merges local stars into the server database and `ProfileViewModel` refreshes with the registered user's profile.

### Rationale
- Maximizes conversion while providing a friction-free, motivating experience.
- Satisfies requirements FR-009, SC-005, and Acceptance Scenario 4.1 & 4.2.

### Alternatives Considered
- *Option B: Blocking guests from opening profile (redirecting immediately to Login)*: Rejected because it breaks user flow and feels jarring.
- *Option C: Making guest profile purely empty*: Rejected because guests want to see the stars they just earned.

---

## 4. Client Presentation & Navigation Integration

### Context & Problem
The app uses Jetpack Compose Multiplatform and Navigation 3 (`androidx.navigation3`).
Navigation routes are defined in `navigation/src/commonMain/kotlin/ir/aispeaking/navigation/Routes.kt`.
The home screen is `JourneyMapScreen.kt` with a top header containing Total Stars and Subscription badges.

### Decision
- Define `ProfileRoute : AppRoute` with `override val screenName: String = "Profile"` in `Routes.kt`.
- In `AppNavigation.kt`, register `entry<ProfileRoute>` rendering `ProfileRoute` composable with `onNavigateBack = { backStack.removeLastOrNull() }`, `onNavigateToLogin = { backStack.add(LoginRoute) }`, and `onNavigateToSubscription = { ... }`.
- In `JourneyMapScreen.kt`, add a clickable Profile action button in the top bar (icon `Res.drawable.ic_profile`) that invokes `onNavigateToProfile: () -> Unit`.
- `ProfileViewModel` utilizes Kotlin Coroutines `StateFlow<ProfileUiState>` with MVI unidirectional data flow, loading data on `init` and supporting manual refresh (`pull-to-refresh` or `refresh()`).

### Rationale
- Completely aligns with the project's existing Clean Architecture, Navigation 3 setup, and Koin injection.
