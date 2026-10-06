# Implementation Plan: Story-Based Gamified Speaking Journey with Tiered Access & Evaluation Rubric

**Branch**: `001-story-based-speaking-journey` | **Date**: 2026-09-18 | **Spec**: [spec.md](file:///Users/mahdi/StudioProjects/AISpeakingPlus/specs/001-story-based-speaking-journey/spec.md)

**Input**: Feature specification from `/specs/001-story-based-speaking-journey/spec.md`

---

## Summary

Build an episodic spoken English roleplay journey featuring 15+ thematic stages tracking the protagonist from Tehran to London.
The system features:
1. **Frictionless Stage 1 Immersion**: Playable in Guest Mode with local device persistence and zero initial registration friction.
2. **3-Tiered Access Gating**: Stage 1 is open to guests, Stage 2 requires mobile SMS OTP registration with cloud progress synchronization, and Stages 3+ require an active paid subscription.
3. **Strict 0–3 Star Evaluation Rubric**: Objective grading deducting for hints requested and grammar errors, alongside constructive Persian feedback.
4. **Replay & High-Score Preservation**: Learners can replay stages for fluency without ever degrading their best historical rating.
5. **Remote 2D Visual Delivery**: Dynamic server delivery of full-screen WebP stage illustrations with persistent local disk caching via Coil 3.
6. **Journey Leaderboard**: Real-time ranking based on aggregate stars and completed stages.

---

## Technical Context

**Language/Version**: Kotlin 2.1.0, JVM 17/21  
**Primary Dependencies**:
- **Server**: Ktor Server 3.x (Netty engine), Exposed 0.58.x (PostgreSQL), Koin 4.x (DI), Redis (Redisson / Lettuce), Kokoro & Sherpa-ONNX (TTS / STT), `kotlinx.serialization`, `kotlinx.datetime`.
- **Client**: Compose Multiplatform (CMP), Ktor Client, Koin 4.x, Coil 3.x (Multiplatform image loader & cache), Multiplatform Settings / DataStore, `kotlinx.coroutines`.

**Storage**:
- **Server**: PostgreSQL for durable storage (`StageTable`, `StageProgressTable`, `SubscriptionTable`, `UserTable`), Redis for high-throughput rate-limiting, OTP codes, session states, and leaderboard rankings.
- **Client**: Multiplatform Key-Value storage (`MultiplatformSettings` / `DataStore`) for Guest Mode Stage 1 progress.

**Testing**: JUnit 5, Ktor `testApplication`, MockK / fake repositories for domain use cases, Compose UI preview/tests.  
**Target Platform**: Server (Linux/macOS JVM), Client: Android, Desktop (JVM), iOS, Web (WasmJs).  
**Project Type**: Full-Stack Mobile & Desktop Client with Asynchronous Micro-Monolith Server.  
**Performance Goals**:
- Spoken dialogue initial feedback: < 2.5s (STT transcription + AI streaming).
- Cached 2D stage background render: < 150ms.
- REST API response time: < 100ms for database queries, < 10ms for cached responses.

**Constraints**:
- Strict Clean Architecture: Domain layer remains pure Kotlin with 0 Ktor, Exposed, or UI dependencies.
- Structured asynchronous coroutines on Netty: zero blocking calls on request threads.
- Defense-in-depth: Server-side route authorization enforces guest, OTP, and subscription gates independently of client UI.

**Scale/Scope**: 15+ linear story stages, hundreds of concurrent conversational sessions, global leaderboard.

---

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Requirement | Status | Verification / Justification |
|---|---|---|---|
| **I. Clean Architecture Everywhere** | Strict separation of Domain, Data, and Presentation/API layers. Domain has zero framework dependencies. | **PASS** | Domain defines pure entities (`Stage`, `StageProgress`, `EvaluationSession`), interfaces (`StageRepository`), and use cases. Data implements repositories and mappers. Presentation handles CMP UI and Ktor routing. |
| **II. Pure KMP & CMP Client** | Client logic and UI built with KMP and CMP. Platform code restricted to actual hardware hooks. | **PASS** | Stage journey map, chat overlay, hints, and paywall modals are built purely with CMP in `feature:main`, `feature:chat`, and `sharedUI`. |
| **III. Ktor Server & Async Coroutines** | Non-blocking Netty engine. All DB, Redis, and AI calls dispatched properly (`Dispatchers.IO`). | **PASS** | Exposed transactions and AI requests run asynchronously on appropriate coroutine dispatchers; no thread blocking in route handlers. |
| **IV. Dependency Injection via Koin** | Constructor injection only. Per-feature Koin modules. No service locator anti-patterns. | **PASS** | `StageKoinModule`, `StageProgressKoinModule`, `SubscriptionKoinModule` provide singletons and ViewModels via constructor injection. |
| **V. Ultra-Low Latency & High Performance** | Sub-100ms REST queries, aggressive Redis caching, streaming chunking, `kotlinx.serialization`. | **PASS** | Leaderboard and subscription statuses cached in Redis; stage backgrounds served with HTTP immutable caching; `kotlinx.serialization` used end-to-end. |

**Gate Status**: **PASSED** (0 violations).

---

## Project Structure

### Documentation (this feature)

```text
specs/001-story-based-speaking-journey/
├── plan.md              # This file
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output
├── quickstart.md        # Phase 1 output
├── contracts/           # Phase 1 output
│   ├── stages-api.md
│   ├── chat-evaluation-api.md
│   ├── subscription-api.md
│   └── leaderboard-api.md
└── tasks.md             # Phase 2 output (/speckit-tasks command)
```

### Source Code (Concrete Repository Layout)

```text
# Server Modules (Ktor Backend)
server/src/main/kotlin/ir/speaking/
├── core/
│   ├── Databases.kt                      # Registers StageTable, StageProgressTable, SubscriptionTable
│   └── Routing.kt                        # Mounts stageRouting, subscriptionRouting, leaderboardRouting
├── feature/
│   ├── stage/
│   │   ├── db/StageTable.kt              # Exposed table for 15+ stages
│   │   ├── dto/                          # StageResponse, StageDetailResponse
│   │   ├── repository/StageRepository.kt # Stage repository & Exposed implementation
│   │   ├── routing/stageRouting.kt       # GET /api/v2/stages, GET /api/v2/stages/{id}
│   │   └── di/StageModule.kt
│   ├── stage_progress/
│   │   ├── db/StageProgressTable.kt      # Exposed table for user stage completion & stars
│   │   ├── dto/                          # SyncProgressRequest, StageEvaluationResponse
│   │   ├── repository/StageProgressRepo.kt
│   │   ├── routing/progressRouting.kt    # POST /api/v2/progress/sync, POST /api/v2/stages/{id}/evaluate
│   │   └── di/StageProgressModule.kt
│   ├── subscription/
│   │   ├── db/SubscriptionTable.kt       # Exposed table for active plans
│   │   ├── dto/                          # PlanResponse, SubscriptionStatusResponse
│   │   ├── repository/SubscriptionRepo.kt
│   │   ├── routing/subscriptionRouting.kt # GET /api/v2/subscriptions/plans, GET /status
│   │   └── di/SubscriptionModule.kt
│   └── leaderboard/
│       ├── dto/LeaderboardResponse.kt
│       ├── repository/LeaderboardRepo.kt # Redis + Exposed leaderboard aggregation
│       ├── routing/leaderboardRouting.kt # GET /api/v2/leaderboard/journey
│       └── di/LeaderboardModule.kt

# Client Modules (Kotlin Multiplatform & Compose Multiplatform)
domain/src/commonMain/kotlin/ir/aispeaking/domain/
├── model/stage/
│   ├── Stage.kt
│   ├── StageProgress.kt
│   ├── StageLockStatus.kt
│   ├── AccessTier.kt
│   ├── EvaluationSession.kt
│   └── LeaderboardEntry.kt
├── repository/stage/
│   ├── StageRepository.kt
│   ├── StageProgressRepository.kt
│   └── SubscriptionRepository.kt
└── usecase/stage/
    ├── GetStagesUseCase.kt
    ├── GetStageDetailUseCase.kt
    ├── RequestStageHintUseCase.kt
    ├── SubmitStageEvaluationUseCase.kt
    ├── SyncGuestProgressUseCase.kt
    └── GetJourneyLeaderboardUseCase.kt

data/src/commonMain/kotlin/ir/aispeaking/data/
├── repository/stage/
│   ├── StageRepositoryImpl.kt
│   ├── StageProgressRepositoryImpl.kt
│   └── SubscriptionRepositoryImpl.kt
├── source/
│   ├── LocalGuestProgressDataSource.kt   # MultiplatformSettings persistence for Stage 1
│   └── RemoteStageDataSource.kt
└── mapper/stage/StageMappers.kt

network/src/commonMain/kotlin/ir/aispeaking/network/
├── api/stage/
│   ├── StageApi.kt
│   ├── StageProgressApi.kt
│   ├── SubscriptionApi.kt
│   └── LeaderboardApi.kt
└── model/stage/dto/                      # Serializable DTOs

sharedUI/src/commonMain/kotlin/ir/aispeaking/sharedui/
├── ui/stage/
│   ├── StarRatingBadge.kt                # 0-3 star display component with glow/particle effects
│   ├── StageBriefingDialog.kt            # 2D story briefing dialog with Persian context
│   └── EvaluationResultCard.kt           # End-of-mission card showing stars, penalties, Persian feedback
└── ui/component/AsyncStageBackground.kt  # Coil 3 full-screen background loader with blur/glass overlay

feature/main/src/commonMain/kotlin/ir/aispeaking/main/
├── screen/
│   ├── JourneyMapScreen.kt               # 2D illustrated linear journey map (15+ stage nodes)
│   └── CharacterHomeScreen.kt            # Enhanced dashboard with active stage node & CTA
└── viewmodel/JourneyMapViewModel.kt

feature/chat/src/commonMain/kotlin/ir/aispeaking/chat/
├── ChatScreen.kt                         # In-mission chat with 2D background, "💡 Hint" button, evaluation dialog
├── ChatViewModel.kt                      # Integrated with Stage mission, hint penalty, evaluation use case
└── component/HintSuggestionCue.kt        # Contextual bottom hint banner

feature/register/src/commonMain/kotlin/ir/aispeaking/register/
└── RegisterBottomSheet.kt                # Frictionless Stage 2 gate modal (Mobile + OTP + auto-sync)

feature/purchases/src/commonMain/kotlin/ir/aispeaking/purchases/
└── SubscriptionPaywallSheet.kt           # Stage 3+ paywall modal with tiered plans
```

---

## Complexity Tracking

> **Fill ONLY if Constitution Check has violations that must be justified**

*No violations identified. The architecture strictly follows all constitutional principles.*
