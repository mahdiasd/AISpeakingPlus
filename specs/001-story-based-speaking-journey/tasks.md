---
description: "Task list for Story-Based Gamified Speaking Journey implementation"
---

# Tasks: Story-Based Gamified Speaking Journey with Tiered Access & Evaluation Rubric

**Input**: Design documents from `/specs/001-story-based-speaking-journey/`  
**Prerequisites**: [plan.md](file:///Users/mahdi/StudioProjects/AISpeakingPlus/specs/001-story-based-speaking-journey/plan.md), [spec.md](file:///Users/mahdi/StudioProjects/AISpeakingPlus/specs/001-story-based-speaking-journey/spec.md), [research.md](file:///Users/mahdi/StudioProjects/AISpeakingPlus/specs/001-story-based-speaking-journey/research.md), [data-model.md](file:///Users/mahdi/StudioProjects/AISpeakingPlus/specs/001-story-based-speaking-journey/data-model.md), [contracts/](file:///Users/mahdi/StudioProjects/AISpeakingPlus/specs/001-story-based-speaking-journey/contracts/)

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story increment.

## Format: `- [ ] [TaskID] [P?] [Story?] Description with file path`

- **[P]**: Can run in parallel (independent files, no blocking dependencies)
- **[Story]**: Which user story this task belongs to ([US1], [US2], [US3], [US4], [US5])
- Setup, Foundational, and Polish phases have NO story label

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project initialization, directory structure, seed data, and shared asset caching pipeline

- [ ] T001 Create feature directory structure for stage, progress, subscription, and leaderboard modules in `server/src/main/kotlin/ir/speaking/feature/` and `domain/src/commonMain/kotlin/ir/aispeaking/domain/`
- [ ] T002 Seed 15+ narrative journey stages (Tehran Airport to London) with English and Persian briefings in `server/src/main/kotlin/ir/speaking/feature/stage/db/StageSeedData.kt`
- [ ] T003 [P] Setup Ktor static resource routing to serve 2D stage background WebP images under `/resources/stages/` with `Cache-Control: public, max-age=2592000, immutable` in `server/src/main/kotlin/ir/speaking/core/Routing.kt`
- [ ] T004 [P] Configure Coil 3 multiplatform image loader with 100MB disk cache and memory cache in `sharedUI/src/commonMain/kotlin/ir/aispeaking/sharedui/di/CoilModule.kt`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core Exposed database tables, base domain models, and API routing infrastructure that MUST be complete before ANY user story can be implemented

**⚠️ CRITICAL**: No user story work can begin until this phase is complete.

- [ ] T005 Create `StageTable` Exposed definition in `server/src/main/kotlin/ir/speaking/feature/stage/db/StageTable.kt` with fields verbatim from data-model.md: `id VARCHAR(64) PRIMARY KEY`, `order_index INT UNIQUE INDEX`, `title VARCHAR(255) NOT NULL`, `title_fa VARCHAR(255) NOT NULL`, `briefing TEXT NOT NULL`, `briefing_fa TEXT NOT NULL`, `target_objective TEXT NOT NULL`, `background_url VARCHAR(512) NOT NULL`, `character_name VARCHAR(128) NOT NULL`, `character_avatar_url VARCHAR(512) NULL`, `character_gender VARCHAR(16) DEFAULT 'Woman'`, `voice_id VARCHAR(64) NULL`, `initial_speaker VARCHAR(16) DEFAULT 'Model'`, `max_turns INT DEFAULT 12`, `created_at TIMESTAMP DEFAULT NOW()`
- [ ] T006 [P] Create `StageProgressTable` Exposed definition in `server/src/main/kotlin/ir/speaking/feature/stage_progress/db/StageProgressTable.kt` with constraints verbatim from data-model.md: `id UUID PRIMARY KEY`, `user_id UUID FOREIGN KEY (users.uid) ON DELETE CASCADE`, `stage_id VARCHAR(64) FOREIGN KEY (stages.id) ON DELETE CASCADE`, `stars INT CHECK (stars BETWEEN 0 AND 3)`, `best_score INT DEFAULT 0`, `repeat_count INT DEFAULT 1`, `completed_at TIMESTAMP DEFAULT NOW()`, `updated_at TIMESTAMP DEFAULT NOW()`, and unique index `(user_id, stage_id)`
- [ ] T007 [P] Create `SubscriptionTable` Exposed definition in `server/src/main/kotlin/ir/speaking/feature/subscription/db/SubscriptionTable.kt` with constraints verbatim from data-model.md: `id UUID PRIMARY KEY`, `user_id UUID FOREIGN KEY (users.uid) ON DELETE CASCADE`, `plan_type VARCHAR(32) ('MONTHLY', 'QUARTERLY', 'BIANNUAL')`, `started_at TIMESTAMP DEFAULT NOW()`, `expires_at TIMESTAMP INDEX`, `status VARCHAR(32) DEFAULT 'ACTIVE' ('ACTIVE', 'EXPIRED', 'CANCELLED')`, `created_at TIMESTAMP DEFAULT NOW()`
- [ ] T008 Register `StageTable`, `StageProgressTable`, and `SubscriptionTable` in database initialization list in `server/src/main/kotlin/ir/speaking/core/Databases.kt`
- [ ] T009 [P] Create core domain enums `AccessTier` (`GUEST`, `REGISTERED_FREE`, `SUBSCRIBER`) and `StageLockStatus` (`UNLOCKED`, `LOCKED_PREVIOUS_STAGE`, `LOCKED_REGISTRATION`, `LOCKED_SUBSCRIPTION`) in `domain/src/commonMain/kotlin/ir/aispeaking/domain/model/stage/AccessTier.kt` and `domain/src/commonMain/kotlin/ir/aispeaking/domain/model/stage/StageLockStatus.kt`
- [ ] T010 [P] Define serializable DTOs for stage list, stage detail, evaluation, subscription, and leaderboard in `network/src/commonMain/kotlin/ir/aispeaking/network/model/stage/dto/StageDtos.kt`
- [ ] T011 Configure Koin DI modules for stage, progress, subscription, and leaderboard dependencies in `server/src/main/kotlin/ir/speaking/di/KoinModule.kt`

**Checkpoint**: Foundation ready - user story implementation can now begin.

---

## Phase 3: User Story 1 - Frictionless Guest Entry & Stage 1 Immersion (Priority: P1) 🎯 MVP

**Goal**: Enable a first-time learner to open the app, skip/watch the story prologue, explore the illustrated 2D journey map, enter Stage 1 in Guest Mode without login friction, converse in real-time with NPC Sarah, receive an initial 0-3 star evaluation, and save progress locally.

**Independent Test**: Launch the app with clean storage, observe prologue and journey map, enter Stage 1 ("Tehran Airport Departure"), conduct dialogue, verify 3 stars awarded, and verify stars persist across app restarts without server authentication.

### Tests for User Story 1 ⚠️

> **NOTE: Write these tests FIRST, ensure they FAIL before implementation**

- [ ] T012 [P] [US1] Unit test for `GetStagesUseCase` with Guest mode and lock evaluation in `domain/src/commonTest/kotlin/ir/aispeaking/domain/usecase/stage/GetStagesUseCaseTest.kt`
- [ ] T013 [P] [US1] Integration test for `GET /api/v2/stages` guest access and Stage 1 retrieval in `server/src/test/kotlin/ir/speaking/feature/stage/StageRoutingTest.kt`

### Implementation for User Story 1

- [ ] T014 [P] [US1] Create domain models `Stage`, `StageProgress`, and `LocalGuestProgress` in `domain/src/commonMain/kotlin/ir/aispeaking/domain/model/stage/Stage.kt`
- [ ] T015 [P] [US1] Define `StageRepository` interface in `domain/src/commonMain/kotlin/ir/aispeaking/domain/repository/stage/StageRepository.kt`
- [ ] T016 [US1] Implement `GetStagesUseCase` and `GetStageDetailUseCase` resolving lock status per tier in `domain/src/commonMain/kotlin/ir/aispeaking/domain/usecase/stage/GetStagesUseCase.kt` and `domain/src/commonMain/kotlin/ir/aispeaking/domain/usecase/stage/GetStageDetailUseCase.kt`
- [ ] T017 [P] [US1] Implement `LocalGuestProgressDataSource` using MultiplatformSettings for Stage 1 guest progress in `data/src/commonMain/kotlin/ir/aispeaking/data/source/LocalGuestProgressDataSource.kt`
- [ ] T018 [P] [US1] Implement `StageApi` with Ktor client for `GET /api/v2/stages` and `GET /api/v2/stages/{stageId}` in `network/src/commonMain/kotlin/ir/aispeaking/network/api/stage/StageApi.kt`
- [ ] T019 [US1] Implement `StageRepositoryImpl` and entity mappers connecting remote API and local guest storage in `data/src/commonMain/kotlin/ir/aispeaking/data/repository/stage/StageRepositoryImpl.kt` and `data/src/commonMain/kotlin/ir/aispeaking/data/mapper/stage/StageMappers.kt`
- [ ] T020 [P] [US1] Implement server `StageRepository` querying `StageTable` with guest lock status logic in `server/src/main/kotlin/ir/speaking/feature/stage/repository/StageRepository.kt`
- [ ] T021 [US1] Implement `GET /api/v2/stages` and `GET /api/v2/stages/{stageId}` endpoints allowing guest access for Stage 1 in `server/src/main/kotlin/ir/speaking/feature/stage/routing/stageRouting.kt`
- [ ] T022 [P] [US1] Build `AsyncStageBackground` Compose component with Coil 3 image loading, crossfade, and frosted glass overlay in `sharedUI/src/commonMain/kotlin/ir/aispeaking/sharedui/ui/component/AsyncStageBackground.kt`
- [ ] T023 [P] [US1] Build `StageBriefingDialog` displaying Persian scenario background and clear mission target objective in `sharedUI/src/commonMain/kotlin/ir/aispeaking/sharedui/ui/stage/StageBriefingDialog.kt`
- [ ] T024 [US1] Build `JourneyMapScreen` with 15+ scrollable stage nodes, active node indicator, and skip-enabled intro prologue in `feature/main/src/commonMain/kotlin/ir/aispeaking/main/screen/JourneyMapScreen.kt` and `feature/main/src/commonMain/kotlin/ir/aispeaking/main/viewmodel/JourneyMapViewModel.kt`
- [ ] T025 [US1] Connect `ChatScreen` and `ChatViewModel` to Stage 1 mission parameters and local guest progress save in `feature/chat/src/commonMain/kotlin/ir/aispeaking/chat/ChatScreen.kt` and `feature/chat/src/commonMain/kotlin/ir/aispeaking/chat/ChatViewModel.kt`

**Checkpoint**: User Story 1 is fully functional and testable independently as the project MVP!

---

## Phase 4: User Story 2 - Star Evaluation Rubric & In-Chat Hint Penalty (Priority: P1)

**Goal**: Provide an objective 0–3 star grading system that deducts stars for hints and grammar errors ($0 \rightarrow 3\text{ stars}, 1 \rightarrow 2\text{ stars}, 2 \rightarrow 1\text{ star}, \ge 3 \lor \neg\text{objective} \rightarrow 0\text{ stars}$), alongside an on-demand in-chat hint generator delivering English prompts with Persian guidance.

**Independent Test**: Conduct test dialogue sessions simulating 0, 1, 2, and 3+ combined errors/hints, and verify exact star ratings, grammar correction cards, and Persian constructive feedback match the rubric.

### Tests for User Story 2 ⚠️

- [ ] T026 [P] [US2] Unit test for star rubric calculation logic across all error/hint boundary conditions in `server/src/test/kotlin/ir/speaking/feature/stage_progress/EvaluationRubricServiceTest.kt`
- [ ] T027 [P] [US2] Integration test for `POST /api/v2/stages/{stageId}/hint` and `POST /api/v2/stages/{stageId}/evaluate` in `server/src/test/kotlin/ir/speaking/feature/stage_progress/EvaluationRoutingTest.kt`

### Implementation for User Story 2

- [ ] T028 [P] [US2] Define domain models `EvaluationSession` and `GrammarErrorDetail` in `domain/src/commonMain/kotlin/ir/aispeaking/domain/model/stage/EvaluationSession.kt`
- [ ] T029 [US2] Implement `RequestStageHintUseCase` and `SubmitStageEvaluationUseCase` in `domain/src/commonMain/kotlin/ir/aispeaking/domain/usecase/stage/RequestStageHintUseCase.kt` and `domain/src/commonMain/kotlin/ir/aispeaking/domain/usecase/stage/SubmitStageEvaluationUseCase.kt`
- [ ] T030 [P] [US2] Implement `POST /api/v2/stages/{stageId}/hint` endpoint using LLM gateway to generate English starter and Persian explanation in `server/src/main/kotlin/ir/speaking/feature/stage/routing/stageRouting.kt`
- [ ] T031 [US2] Implement `EvaluationRubricService` calculating penalties verbatim: `totalPenalties = grammarErrorsCount + hintsUsedCount`, returning 3 stars for 0 penalties, 2 stars for 1 penalty, 1 star for 2 penalties, and 0 stars for 3+ penalties or `!objectiveCompleted` in `server/src/main/kotlin/ir/speaking/feature/stage_progress/service/EvaluationRubricService.kt`
- [ ] T032 [US2] Implement `POST /api/v2/stages/{stageId}/evaluate` endpoint evaluating transcript and returning structured feedback in `server/src/main/kotlin/ir/speaking/feature/stage_progress/routing/progressRouting.kt`
- [ ] T033 [P] [US2] Build `HintSuggestionCue` bottom banner component displaying English suggestion and Persian translation in `feature/chat/src/commonMain/kotlin/ir/aispeaking/chat/component/HintSuggestionCue.kt`
- [ ] T034 [P] [US2] Build `StarRatingBadge` component displaying 0 to 3 animated glowing stars in `sharedUI/src/commonMain/kotlin/ir/aispeaking/sharedui/ui/stage/StarRatingBadge.kt`
- [ ] T035 [US2] Build `EvaluationResultCard` dialog displaying earned stars, penalty breakdown, grammar corrections, and Persian feedback in `sharedUI/src/commonMain/kotlin/ir/aispeaking/sharedui/ui/stage/EvaluationResultCard.kt`
- [ ] T036 [US2] Wire "💡 Hint" button, session hint counter increment, and evaluation submission flow in `feature/chat/src/commonMain/kotlin/ir/aispeaking/chat/ChatViewModel.kt`

**Checkpoint**: User Stories 1 AND 2 work independently with full pedagogical evaluation feedback.

---

## Phase 5: User Story 3 - Stage 2 Gate: Mobile OTP Registration & Local Progress Sync (Priority: P2)

**Goal**: Block unauthenticated access to Stage 2 with an informative registration modal, authenticate Iranian mobile numbers via SMS OTP, and synchronize local Stage 1 progress to the cloud preserving maximum stars: $\max(\text{local\_stars}, \text{cloud\_stars})$.

**Independent Test**: Complete Stage 1 as a guest, tap Stage 2, verify registration modal appears, complete mobile OTP verification, verify Stage 1 stars appear on cloud profile, and confirm Stage 2 is unlocked.

### Tests for User Story 3 ⚠️

- [ ] T037 [P] [US3] Unit test for guest progress conflict resolution preserving highest stars in `server/src/test/kotlin/ir/speaking/feature/stage_progress/StageProgressSyncTest.kt`
- [ ] T038 [P] [US3] Integration test for Stage 2 HTTP 401 unauthorized barrier and `POST /api/v2/progress/sync` in `server/src/test/kotlin/ir/speaking/feature/stage_progress/ProgressSyncRoutingTest.kt`

### Implementation for User Story 3

- [ ] T039 [P] [US3] Define `StageProgressRepository` interface and `SyncGuestProgressUseCase` in `domain/src/commonMain/kotlin/ir/aispeaking/domain/repository/stage/StageProgressRepository.kt` and `domain/src/commonMain/kotlin/ir/aispeaking/domain/usecase/stage/SyncGuestProgressUseCase.kt`
- [ ] T040 [P] [US3] Implement `POST /api/v2/progress/sync` network call in `network/src/commonMain/kotlin/ir/aispeaking/network/api/stage/StageProgressApi.kt`
- [ ] T041 [US3] Implement server `StageProgressRepo.syncProgress` running conflict resolution inside database transaction: `persisted_stars = max(local_stars, cloud_stars)` and `persisted_score = max(local_score, cloud_score)` in `server/src/main/kotlin/ir/speaking/feature/stage_progress/repository/StageProgressRepo.kt`
- [ ] T042 [US3] Implement `POST /api/v2/progress/sync` routing and enforce HTTP 401 Unauthorized (`code: AUTH_REQUIRED`) on Stage 2 for unauthenticated requests in `server/src/main/kotlin/ir/speaking/feature/stage_progress/routing/progressRouting.kt` and `server/src/main/kotlin/ir/speaking/feature/stage/routing/stageRouting.kt`
- [ ] T043 [US3] Implement `StageProgressRepositoryImpl` reading `LocalGuestProgressDataSource`, syncing via network API, and clearing guest flag upon success in `data/src/commonMain/kotlin/ir/aispeaking/data/repository/stage/StageProgressRepositoryImpl.kt`
- [ ] T044 [US3] Implement `RegisterBottomSheet` modal triggering mobile number submission, SMS OTP verification, and automatic progress sync in `feature/register/src/commonMain/kotlin/ir/aispeaking/register/RegisterBottomSheet.kt`
- [ ] T045 [US3] Handle Stage 2 click interception on `JourneyMapScreen` to launch `RegisterBottomSheet` when `lockStatus == StageLockStatus.LOCKED_REGISTRATION` in `feature/main/src/commonMain/kotlin/ir/aispeaking/main/screen/JourneyMapScreen.kt`

**Checkpoint**: User Stories 1, 2, and 3 are complete with seamless guest-to-registered account conversion.

---

## Phase 6: User Story 4 - Stage 3+ Paywall & Subscription Access Gating (Priority: P2)

**Goal**: Protect Stage 3+ narrative content behind an active subscription paywall, enforcing HTTP 402 Payment Required on the server and presenting tiered plans (1, 3, 6 months) with promo code entry on the client.

**Independent Test**: Attempt to access Stage 3 with a registered free user, confirm paywall sheet opens and server returns HTTP 402, activate subscription, and verify Stage 3 unlocks immediately.

### Tests for User Story 4 ⚠️

- [ ] T046 [P] [US4] Integration test for Stage 3+ HTTP 402 Payment Required barrier and subscription status check in `server/src/test/kotlin/ir/speaking/feature/subscription/SubscriptionRoutingTest.kt`

### Implementation for User Story 4

- [ ] T047 [P] [US4] Define domain models `SubscriptionPlan` and `SubscriptionStatus` in `domain/src/commonMain/kotlin/ir/aispeaking/domain/model/stage/SubscriptionPlan.kt`
- [ ] T048 [P] [US4] Define `SubscriptionRepository` interface and `GetSubscriptionPlansUseCase`, `CheckSubscriptionStatusUseCase` in `domain/src/commonMain/kotlin/ir/aispeaking/domain/repository/stage/SubscriptionRepository.kt` and `domain/src/commonMain/kotlin/ir/aispeaking/domain/usecase/stage/SubscriptionUseCases.kt`
- [ ] T049 [P] [US4] Implement `SubscriptionApi` client for `GET /api/v2/subscriptions/plans` and `GET /api/v2/subscriptions/status` in `network/src/commonMain/kotlin/ir/aispeaking/network/api/stage/SubscriptionApi.kt`
- [ ] T050 [P] [US4] Implement server `SubscriptionRepo` querying `SubscriptionTable` and caching active entitlement in Redis in `server/src/main/kotlin/ir/speaking/feature/subscription/repository/SubscriptionRepo.kt`
- [ ] T051 [US4] Implement `requireSubscription` Ktor route interceptor rejecting unsubscribed requests with HTTP 402 (`code: SUBSCRIPTION_REQUIRED`) for `stageId >= 3` in `server/src/main/kotlin/ir/speaking/feature/subscription/interceptor/SubscriptionGate.kt`
- [ ] T052 [US4] Implement `GET /api/v2/subscriptions/plans` and `GET /api/v2/subscriptions/status` endpoints in `server/src/main/kotlin/ir/speaking/feature/subscription/routing/subscriptionRouting.kt`
- [ ] T053 [US4] Implement `SubscriptionRepositoryImpl` in `data/src/commonMain/kotlin/ir/aispeaking/data/repository/stage/SubscriptionRepositoryImpl.kt`
- [ ] T054 [US4] Implement `SubscriptionPaywallSheet` displaying 1, 3, 6-month plans, Toman pricing, discount badges, and promo code redemption in `feature/purchases/src/commonMain/kotlin/ir/aispeaking/purchases/SubscriptionPaywallSheet.kt`
- [ ] T055 [US4] Handle Stage 3+ click interception on `JourneyMapScreen` to launch `SubscriptionPaywallSheet` when `lockStatus == StageLockStatus.LOCKED_SUBSCRIPTION` in `feature/main/src/commonMain/kotlin/ir/aispeaking/main/screen/JourneyMapScreen.kt`

**Checkpoint**: Monetization paywall and server-side subscription defense-in-depth are fully operational.

---

## Phase 7: User Story 5 - Replay & High Score Preservation (Priority: P3)

**Goal**: Allow learners to replay any completed stage to practice speaking without degrading their best historical star rating ($\text{stars}_{\text{saved}} = \max(\text{existing}, \text{new})$), updating repeat counts and real-time global leaderboard rankings.

**Independent Test**: Complete a stage with 3 stars, replay and score 1 star, verify profile and server retain 3 stars, and inspect the journey leaderboard to ensure ranking reflects highest scores.

### Tests for User Story 5 ⚠️

- [ ] T056 [P] [US5] Unit test for replay high-score preservation logic in `server/src/test/kotlin/ir/speaking/feature/stage_progress/ReplayHighScoreTest.kt`
- [ ] T057 [P] [US5] Integration test for `GET /api/v2/leaderboard/journey` pagination and ranking in `server/src/test/kotlin/ir/speaking/feature/leaderboard/LeaderboardRoutingTest.kt`

### Implementation for User Story 5

- [ ] T058 [P] [US5] Define `LeaderboardEntry` domain model and `GetJourneyLeaderboardUseCase` in `domain/src/commonMain/kotlin/ir/aispeaking/domain/model/stage/LeaderboardEntry.kt` and `domain/src/commonMain/kotlin/ir/aispeaking/domain/usecase/stage/GetJourneyLeaderboardUseCase.kt`
- [ ] T059 [P] [US5] Implement `LeaderboardApi` client for `GET /api/v2/leaderboard/journey` in `network/src/commonMain/kotlin/ir/aispeaking/network/api/stage/LeaderboardApi.kt`
- [ ] T060 [US5] Update server `StageProgressRepo` replay logic to save `stars = max(existing.stars, new.stars)`, `best_score = max(existing.best_score, new.best_score)`, and increment `repeat_count` in `server/src/main/kotlin/ir/speaking/feature/stage_progress/repository/StageProgressRepo.kt`
- [ ] T061 [US5] Implement server `LeaderboardRepo` computing score formula `(total_stars * 1000) + (completed_stages * 100) + aggregate_points` with Redis sorted set caching (`ZADD leaderboard:journey`) in `server/src/main/kotlin/ir/speaking/feature/leaderboard/repository/LeaderboardRepo.kt`
- [ ] T062 [US5] Implement `GET /api/v2/leaderboard/journey` endpoint returning top ranked learners and current user rank position in `server/src/main/kotlin/ir/speaking/feature/leaderboard/routing/leaderboardRouting.kt`
- [ ] T063 [US5] Build `JourneyLeaderboardSheet` Compose component displaying rank, avatar, user name, stars, stages completed, and current user ranking banner in `feature/main/src/commonMain/kotlin/ir/aispeaking/main/screen/JourneyLeaderboardSheet.kt`

**Checkpoint**: All 5 user stories are complete, independently functional, and verified.

---

## Phase 8: Polish & Cross-Cutting Concerns

**Purpose**: Verification against quickstart scenarios, end-to-end compilation, and documentation

- [ ] T064 [P] Run end-to-end quickstart validation scenarios from `specs/001-story-based-speaking-journey/quickstart.md`
- [ ] T065 [P] Update technical documentation and API catalog in `specs/001-story-based-speaking-journey/README.md`
- [ ] T066 Run full multiplatform build verification via `./gradlew :server:compileKotlin :desktopApp:compileKotlin` and resolve any compiler warnings
- [ ] T067 Execute automated test suites via `./gradlew :domain:test :server:test` and verify 100% test passage
