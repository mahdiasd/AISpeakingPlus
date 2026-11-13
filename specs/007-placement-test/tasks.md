# Tasks: Placement Test (آزمون تعیین سطح)

**Input**: Design documents from `/specs/007-placement-test/`
**Prerequisites**: `plan.md`, `spec.md`, `research.md`, `data-model.md`, `contracts/contracts.md`

## Format: `- [X] [TaskID] [P?] [Story?] Description with file path`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Maps to user stories: [US1], [US2], [US3]

---

## Phase 1: Setup (Shared Domain Infrastructure)

**Purpose**: Core Domain models, repository interfaces, and use cases.

- [X] T001 [P] Create placement domain models (`PlacementQuestion`, `PlacementEvaluationResult`, `QuestionReviewItem`, `PlacementSubmission`) in `domain/src/commonMain/kotlin/ir/aispeaking/domain/model/placement/PlacementModels.kt`
- [X] T002 [P] Create `PlacementTestRepository` interface in `domain/src/commonMain/kotlin/ir/aispeaking/domain/repository/placement/PlacementTestRepository.kt`
- [X] T003 Create use cases `GetPlacementQuestionsUseCase` and `SubmitPlacementTestUseCase` in `domain/src/commonMain/kotlin/ir/aispeaking/domain/usecase/placement/PlacementUseCases.kt`
- [X] T004 Register placement use cases in `domain/src/commonMain/kotlin/ir/aispeaking/domain/di/DomainKoinModule.kt`

---

## Phase 2: Foundational (Backend Data, Seed & REST APIs)

**Purpose**: Database tables, 100-question seed bank, scoring service, and OpenAPI-annotated Ktor endpoints.

- [X] T005 [P] Create Exposed tables `PlacementQuestionsTable` and `PlacementSessionsTable` in `server/src/main/kotlin/ir/speaking/feature/placement/db/PlacementTables.kt`
- [X] T006 [P] Create comprehensive seed data with 100 curated CEFR questions (20 each for A1, A2, B1, B2, C1) in `server/src/main/kotlin/ir/speaking/feature/placement/db/PlacementSeedData.kt`
- [X] T007 Register placement tables and seed runner in `server/src/main/kotlin/ir/speaking/core/Databases.kt`
- [X] T008 Implement `PlacementServerRepository` and scoring algorithm `PlacementEvaluationService` in `server/src/main/kotlin/ir/speaking/feature/placement/service/PlacementEvaluationService.kt`
- [X] T009 Implement Ktor endpoints `GET /api/v2/placement/questions` and `POST /api/v2/placement/evaluate` with full OpenAPI 3.0.3 `.describe { ... }` in `server/src/main/kotlin/ir/speaking/feature/placement/routing/placementRouting.kt`
- [X] T010 Wire `placementRouting` in `server/src/main/kotlin/ir/speaking/core/Routing.kt`
- [X] T011 [P] Create network DTOs and client remote data source in `network/src/commonMain/kotlin/ir/aispeaking/network/model/placement/dto/PlacementDtos.kt` and `network/src/commonMain/kotlin/ir/aispeaking/network/api/placement/PlacementApi.kt`
- [X] T012 Implement `PlacementTestRepositoryImpl` in `data/src/commonMain/kotlin/ir/aispeaking/data/repository/placement/PlacementTestRepositoryImpl.kt` and register in `data/src/commonMain/kotlin/ir/aispeaking/data/di/DataKoinModule.kt`

---

## Phase 3: User Story 1 (P1) - Profile Card & Horizontal Pager Placement Test UI 🎯 MVP

**Goal**: Deliver the interactive 20-question horizontal slider test experience accessible directly from `ProfileScreen`.

**Independent Test**: User opens profile, taps "آزمون تعیین سطح", slides horizontally through 20 questions, and selects 4-option answers with tactile highlight.

- [X] T013 [P] [US1] Create Apple-styled `PlacementTestCard` in `feature/profile/src/commonMain/kotlin/ir/aispeaking/profile/component/PlacementTestCard.kt`
- [X] T014 [US1] Wire `PlacementTestCard` and `onNavigateToPlacementTest` callback in `feature/profile/src/commonMain/kotlin/ir/aispeaking/profile/ProfileScreen.kt`
- [X] T015 [P] [US1] Define `PlacementTestRoute` in `navigation/src/commonMain/kotlin/ir/aispeaking/navigation/Routes.kt` and register in `navigation/src/commonMain/kotlin/ir/aispeaking/navigation/config.kt`
- [X] T016 [US1] Create UI contract `PlacementTestContract.kt` and `PlacementTestViewModel.kt` managing session, current page, and user answers
- [X] T017 [P] [US1] Implement UI components `QuestionSlideCard.kt`, `OptionSelectableCard.kt`, and `PlacementProgressBar.kt`
- [X] T018 [US1] Implement `PlacementTestScreen.kt` with `HorizontalPager`, smooth slide animation, exit confirmation dialog, and `AppBackButton` on top-right pointing right (`>`)
- [X] T019 [US1] Register `entry<PlacementTestRoute>` in `navigation/src/commonMain/kotlin/ir/aispeaking/navigation/AppNavigation.kt`

---

## Phase 4: User Story 2 (P1) - Evaluation, Correct/Wrong Breakdown & Review Modal

**Goal**: Evaluate answers upon submission, show CEFR level and correct/wrong stats, and allow full question review.

**Independent Test**: Complete question 20, tap submit, see level result card with breakdown (e.g. 15 correct, 5 wrong), and open review dialog to inspect every question and correct answers.

- [X] T020 [US2] Implement `PlacementResultView.kt` displaying CEFR badge (`A1`..`C1`), percentage score, breakdown pills (correct vs wrong), and action buttons
- [X] T021 [US2] Implement `QuestionReviewDialog.kt` showcasing list of 20 questions with user answers, green/red status badges, and correct answer indicators
- [X] T022 [US2] Integrate submit action in `PlacementTestViewModel` to call `SubmitPlacementTestUseCase`, update `UserTable` language level, and present results
- [X] T023 [US2] Handle test retake flow in `PlacementTestViewModel` (reset state, fetch fresh 20-question randomized batch)

---

## Phase 5: Polish & Cross-Cutting Concerns

**Purpose**: Testing, error handling, design verification, and build validation.

- [X] T024 Write Ktor integration tests for placement endpoints in `server/src/test/kotlin/ir/speaking/feature/placement/PlacementRoutingTest.kt`
- [X] T025 Verify RTL navigation, back button alignment (`ic_back` pointing right), and centered TopAppBar headers per Constitution
- [X] T026 Compile and test all affected modules (`:server:compileKotlin`, `:domain:compileKotlinJvm`, `:data:compileKotlinJvm`, `:feature:profile:compileKotlinJvm`, `:navigation:compileKotlinJvm`)
- [X] T027 Commit code cleanly following Conventional Commits (`feat(placement): ...`)
