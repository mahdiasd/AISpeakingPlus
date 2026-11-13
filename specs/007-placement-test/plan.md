# Implementation Plan: Placement Test (آزمون تعیین سطح)

**Branch**: `feature/007-placement-test` | **Date**: 2026-11-13 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from `/specs/007-placement-test/spec.md`

## Summary

Build a 20-question stratified adaptive English placement test feature for AISpeakingPlus.
The test draws 20 questions (4 per CEFR level: A1, A2, B1, B2, C1) from a 100-question seed bank on the server.
In the CMP client, questions are presented one-by-one in an Apple-styled horizontal slider (`HorizontalPager`) with options loaded beneath each question.
Upon completion, the server evaluates answers, computes the CEFR level, updates the user's profile, and returns an educational review report showing correct and incorrect answers.
The feature is accessible via a new dedicated card in `ProfileScreen`.

## Technical Context

**Language/Version**: Kotlin 2.x (Multiplatform & JVM 17)

**Primary Dependencies**:
- Client: Compose Multiplatform (Foundation Pager, Material 3, Animation, Icons), Koin 4.x, kotlinx.serialization, kotlinx.coroutines
- Server: Ktor Server 3.x (Netty, ContentNegotiation, StatusPages, JWT Auth, OpenAPI), Exposed SQL, HikariCP, PostgreSQL

**Storage**: PostgreSQL (`placement_questions` and `placement_sessions` tables)

**Testing**: Ktor `testApplication`, Kotlin Test, Coroutines Test

**Target Platform**: Android, Web (Wasm/JS fallback), Desktop (JVM), Server (Linux/Docker)

**Project Type**: Multiplatform Mobile & Web App + Backend REST API

**Performance Goals**:
- Pager 60fps horizontal slide animation
- API response time < 300ms for test generation and evaluation

**Constraints**:
- Clean Architecture (Domain, Data, Presentation)
- RTL layout rules: `AppBackButton` on top-right pointing right (`>`), centered titles
- OpenAPI 3.0.3 `.describe { ... }` documentation for all server routes

**Scale/Scope**:
- 100 questions in database seed
- 20 questions presented per test session
- 2 new REST endpoints (`GET /api/v2/placement/questions`, `POST /api/v2/placement/evaluate`)
- 1 new feature module or package (`feature/placement_test` or `feature/placement`) + integration into `feature/profile` and `navigation`

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Compliance Check | Status |
|---|---|---|
| I. Clean Architecture | Pure Kotlin Domain models (`PlacementQuestion`, `PlacementEvaluationResult`, UseCases), Data repo implementation, isolated Presentation | **PASS** |
| II. Pure KMP/CMP Client | UI built purely in CMP, RTL layout with `AppBackButton` on top-right with `>` chevron and centered title | **PASS** |
| III. Ktor Coroutine Pipeline | Non-blocking database calls using `suspendTransaction` or `Dispatchers.IO` | **PASS** |
| IV. Compile-Time DI via Koin | Constructor injection with Koin modules (`PlacementKoinModule`, `AppModule`) | **PASS** |
| V. Low Latency API | Instant in-memory / indexed DB query for 20 questions, sub-100ms response | **PASS** |
| VI. Living OpenAPI 3.0.3 Contract | Both GET and POST endpoints fully annotated with `.describe { ... }` with realistic examples | **PASS** |

## Project Structure

### Documentation (this feature)

```text
specs/007-placement-test/
├── plan.md              # This file
├── research.md          # Benchmark and architecture decisions
├── data-model.md        # DB schemas & domain models
├── quickstart.md        # E2E validation scenarios
├── contracts/
│   └── contracts.md     # Route, API and UI contracts
└── tasks.md             # Implementation tasks
```

### Source Code

```text
# Domain Layer
domain/src/commonMain/kotlin/ir/aispeaking/domain/
├── model/placement/
│   ├── PlacementQuestion.kt
│   ├── PlacementEvaluationResult.kt
│   └── PlacementSubmission.kt
├── repository/placement/
│   └── PlacementRepository.kt
└── usecase/placement/
    ├── GetPlacementQuestionsUseCase.kt
    └── SubmitPlacementTestUseCase.kt

# Data Layer
data/src/commonMain/kotlin/ir/aispeaking/data/
├── repository/placement/
│   └── PlacementRepositoryImpl.kt
├── datasource/placement/
│   └── PlacementRemoteDataSource.kt
└── dto/placement/
    └── PlacementDtos.kt

# Server Layer
server/src/main/kotlin/ir/speaking/feature/placement/
├── db/
│   ├── PlacementQuestionsTable.kt
│   ├── PlacementSessionsTable.kt
│   └── PlacementSeedData.kt
├── dto/
│   └── PlacementServerDtos.kt
├── repository/
│   └── PlacementServerRepository.kt
├── service/
│   └── PlacementEvaluationService.kt
└── routing/
    └── placementRouting.kt

# Client Presentation Layer
feature/placement_test/ (or inside shared UI / feature)
├── di/
│   └── PlacementTestKoinModule.kt
├── component/
│   ├── QuestionSlideCard.kt
│   ├── OptionSelectableCard.kt
│   ├── PlacementProgressBar.kt
│   └── QuestionReviewDialog.kt
├── PlacementTestContract.kt
├── PlacementTestViewModel.kt
└── PlacementTestScreen.kt

# Profile Screen Integration
feature/profile/src/commonMain/kotlin/ir/aispeaking/profile/
├── component/
│   └── PlacementTestCard.kt
└── ProfileScreen.kt

# Navigation Module
navigation/src/commonMain/kotlin/ir/aispeaking/navigation/
├── Routes.kt
├── config.kt
└── AppNavigation.kt
```

## Complexity Tracking

No violations. Standard project architecture strictly followed.
