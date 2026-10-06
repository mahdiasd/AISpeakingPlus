# Implementation Plan: User Profile & Account Overview

**Branch**: `003-user-profile` | **Date**: 2026-10-06 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from [`specs/003-user-profile/spec.md`](./spec.md)

---

## Summary

Build an end-to-end, Clean Architecture-driven User Profile & Account Overview feature for AISpeakingPlus.
The feature includes:
1. **Server Layer (Ktor)**: Consolidated `GET /api/v2/user/profile` and `PUT /api/v2/user/profile` endpoints exposing complete user identity, total stars earned, completed stage count, language level, and active subscription details with OpenAPI 3.0.3 metadata.
2. **Client Architecture (KMP & CMP)**:
   - **Domain Layer**: `UserProfile`, `SubscriptionSummary` domain models, `UserProfileRepository` contract, and use cases (`GetUserProfileUseCase`, `UpdateUserProfileUseCase`).
   - **Data & Network Layer**: `UserApi`, `UserProfileResponseDto`, `UpdateProfileRequestDto`, `UserProfileMappers`, and `UserProfileRepositoryImpl` combining remote queries with local guest fallbacks.
   - **Presentation Layer (SharedUI)**: `ProfileViewModel` (MVI / StateFlow), `ProfileScreen` with full RTL layout, avatar selection dialog, subscription remaining-days card, achievements statistics grid, and safe sign-out dialog.
   - **Navigation Integration**: Navigation 3 `ProfileRoute` registration and Profile icon action in `JourneyMapScreen` top bar.

---

## Technical Context

**Language/Version**: Kotlin 2.x (Kotlin Multiplatform & JVM)  
**Primary Dependencies**:
- Client: Compose Multiplatform, Navigation 3 (`androidx.navigation3`), Koin 4.x, kotlinx.coroutines, kotlinx.serialization, Coil 3 (for images)
- Server: Ktor Server 3.x (Netty, OpenAPI, ContentNegotiation, JWT Auth), JetBrains Exposed, HikariCP, PostgreSQL  
**Storage**:
- Server: PostgreSQL (`users`, `subscriptions`, `stage_progress` tables)
- Client: Multiplatform Preferences (`UserPreferences`, `TokenPreferences`, `LocalGuestProgressDataSource`)  
**Testing**:
- Ktor `testApplication` for routing and API contract validation
- Kotlin Multiplatform unit tests with mock/fake repositories for domain use cases  
**Target Platform**: Android, Desktop (JVM), iOS (KMP), Web (WASM/JS), Server (Linux/JVM)  
**Project Type**: Multi-module Mobile/Desktop Application + Ktor Backend Service  
**Performance Goals**:
- Sub-50ms response time for `GET /api/v2/user/profile`
- Instant transition (<1s) from Journey Map to Profile Screen
- Sub-2s initial render under standard mobile connectivity  
**Constraints**:
- Strict Clean Architecture boundaries (Domain has zero UI or Ktor dependencies)
- Full Persian/RTL layout orientation and typography (`persian-writing` standard)
- OpenAPI 3.0.3 schema compatibility on all routes (`.describe { ... }`)  
**Scale/Scope**: 1 new destination screen, 4 modular presentation components, 2 server endpoints, 2 use cases, 1 repository interface.

---

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-checked after Phase 1 design.*

| Constitution Principle | Requirement | Compliance Analysis | Status |
|---|---|---|---|
| **I. Clean Architecture Everywhere** | Strict layer separation (Domain, Data, Presentation) with inward dependencies | Domain models, use cases, and repository interfaces in `domain`; implementations in `data` and `server`; UI strictly in `sharedUI`. Zero framework leaks into domain. | **PASS** ✅ |
| **II. Pure KMP & CMP Client** | Shared code across platforms; platform actuals only for OS-specific needs | All profile UI, state management, networking, and navigation reside in `commonMain` shared modules. | **PASS** ✅ |
| **III. Ktor Server & Async Coroutines** | Non-blocking coroutines on Netty; Dispatchers.IO for database queries | All database queries executed inside `suspendTransaction` with Dispatchers.IO. Zero blocking operations on server threads. | **PASS** ✅ |
| **IV. Dependency Injection via Koin** | Constructor injection; per-feature module declarations | Feature dependencies injected via Koin (`userProfileModule`, `@Single`, `@Factory`). No service locators in domain entities. | **PASS** ✅ |
| **V. Ultra-Low Latency & High Performance** | Sub-100ms response times, efficient queries, kotlinx.serialization | Consolidated single endpoint eliminates N+1 and mobile waterfall queries. Direct Exposed indexed joins. | **PASS** ✅ |
| **VI. Living OpenAPI Specification** | Mandatory `.describe { ... }` with schemas, requestBody, and examples | All routes define complete request/response schemas and example payloads compatible with OpenAPI 3.0.3 for Apidog Live Sync. | **PASS** ✅ |

---

## Project Structure

### Documentation (this feature)

```text
specs/003-user-profile/
├── plan.md              # This implementation plan
├── research.md          # Phase 0 architectural decisions
├── data-model.md        # Phase 1 data entities and schemas
├── quickstart.md        # Phase 1 verification and testing guide
├── contracts/           # Phase 1 API specifications
│   └── user-profile-api.md
└── checklists/
    └── requirements.md  # Requirements quality checklist
```

### Source Code (repository root)

```text
server/
└── src/main/kotlin/ir/speaking/feature/user/
    ├── dto/
    │   └── UserProfileDtos.kt          # UserProfileResponseDto, UpdateProfileRequestDto
    ├── repository/
    │   └── UserRepo.kt                 # Aggregation of profile, stars & subscription
    ├── service/
    │   └── UserService.kt              # Business logic & validations
    └── routing/
        └── UserRouting.kt              # GET / PUT /api/v2/user/profile with .describe

network/
└── src/commonMain/kotlin/ir/aispeaking/network/
    ├── api/user/
    │   └── UserApi.kt                  # Ktor client API interface
    └── model/user/dto/
        └── UserProfileDtos.kt          # Serializable DTOs

domain/
└── src/commonMain/kotlin/ir/aispeaking/domain/
    ├── model/user/
    │   └── UserProfile.kt              # Rich domain models
    ├── repository/user/
    │   └── UserProfileRepository.kt    # Repository contract
    └── usecase/user/
        ├── GetUserProfileUseCase.kt    # Fetches profile / guest fallback
        └── UpdateUserProfileUseCase.kt # Validates & saves profile edits

data/
└── src/commonMain/kotlin/ir/aispeaking/data/
    ├── mapper/user/
    │   └── UserProfileMappers.kt       # DTO <-> Domain transformations
    └── repository/user/
        └── UserProfileRepositoryImpl.kt# Aggregates remote API & local storage

sharedUI/
└── src/commonMain/kotlin/ir/aispeaking/sharedui/ui/
    ├── profile/
    │   ├── ProfileViewModel.kt         # MVI StateFlow ViewModel
    │   ├── ProfileUiState.kt           # Screen UI state
    │   ├── ProfileScreen.kt            # Main composable screen
    │   ├── component/
    │   │   ├── ProfileHeaderCard.kt    # Avatar, nickname, mobile
    │   │   ├── SubscriptionCard.kt     # Remaining days, plan badge, CTA
    │   │   ├── AchievementStatsCard.kt # Stars, XP, stages completed
    │   │   ├── AvatarPickerDialog.kt   # Preset avatar selector
    │   │   └── SignOutConfirmDialog.kt # Safe logout confirmation
    │   └── di/
    │       └── ProfileUiModule.kt      # Koin presentation module
    └── stage/
        └── JourneyMapScreen.kt         # Top bar Profile action button

navigation/
└── src/commonMain/kotlin/ir/aispeaking/navigation/
    ├── Routes.kt                       # ProfileRoute : AppRoute
    └── AppNavigation.kt                # entry<ProfileRoute> registration
```

**Structure Decision**: Multi-module Clean Architecture cleanly segregates concerns: Server endpoints in `server`, Network DTOs in `network`, Core business rules in `domain`, Data caching and mappers in `data`, Declarative Compose UI in `sharedUI`, and App destination routing in `navigation`.

---

## Complexity Tracking

| Issue / Design Choice | Why Needed | Simpler Alternative Rejected Because |
|---|---|---|
| Dedicated `UserProfileRepository` vs reusing `AuthRepository` | Separation of Concerns: Profile encompasses gamification, achievements, and subscription metrics, whereas `AuthRepository` manages tokens, credentials, and OTP verification. | Mixing achievement aggregation and profile updates into `AuthRepository` bloats authentication interfaces and violates Single Responsibility. |
| Consolidated `/api/v2/user/profile` endpoint | Mobile performance: delivers avatar, identity, subscription remaining days, and total stars in a single HTTP call. | Client making 3 separate calls (`/auth/me`, `/subscriptions/status`, and stages list) creates high network latency and complex partial loading error states. |
