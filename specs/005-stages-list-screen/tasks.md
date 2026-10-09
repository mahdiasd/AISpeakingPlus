# Tasks: Stages List Screen

**Feature**: `005-stages-list-screen`  
**Input**: [spec.md](./spec.md), [plan.md](./plan.md), [data-model.md](./data-model.md), [contracts/contracts.md](./contracts/contracts.md)

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Navigation route registration and configuration

- [X] T001 Define `StagesListRoute` in `navigation/src/commonMain/kotlin/ir/aispeaking/navigation/Routes.kt`
- [X] T002 Register `StagesListRoute` polymorphic serializer in `navigation/src/commonMain/kotlin/ir/aispeaking/navigation/config.kt`

---

## Phase 2: Foundational (State & ViewModel)

**Purpose**: ViewModel and state definition for stages listing and progress tracking

- [X] T003 Create `StagesListContract` and `StagesListUiState` in `sharedUI/src/commonMain/kotlin/ir/aispeaking/sharedui/ui/stage/stageslist/StagesListContract.kt`
- [X] T004 Implement `StagesListViewModel` injecting `GetStagesUseCase` in `sharedUI/src/commonMain/kotlin/ir/aispeaking/sharedui/ui/stage/stageslist/StagesListViewModel.kt`

---

## Phase 3: User Story 1 & 2 - Stages List & Stars Presentation (Priority: P1) 🎯 MVP

**Goal**: Full-page dedicated screen showing all stages, active stage badge, stars earned per stage, and summary stats header

**Independent Test**: Navigate to `StagesListRoute`, observe header summary with total stars, all stages listed in order, active stage highlighted, and back navigation working.

- [X] T005 [P] [US1] Create `StagesHeaderStatsCard` in `sharedUI/src/commonMain/kotlin/ir/aispeaking/sharedui/ui/stage/stageslist/component/StagesHeaderStatsCard.kt` displaying total earned stars and completion progress
- [X] T006 [P] [US1] Create `StageListItemCard` in `sharedUI/src/commonMain/kotlin/ir/aispeaking/sharedui/ui/stage/stageslist/component/StageListItemCard.kt` with order badge, Persian & English titles, active glowing border, and star rating (0-3 stars)
- [X] T007 [US1] Build `StagesListScreen` with `LazyColumn`, top app bar (`AppBackButton`), and auto-scroll to active stage in `sharedUI/src/commonMain/kotlin/ir/aispeaking/sharedui/ui/stage/stageslist/StagesListScreen.kt`
- [X] T008 [US1] Register `entry<StagesListRoute>` in `navigation/src/commonMain/kotlin/ir/aispeaking/navigation/AppNavigation.kt`

---

## Phase 4: User Story 3 - Stage Interaction & JourneyMap Integration (Priority: P2)

**Goal**: Connect JourneyMapScreen to StagesListScreen and handle stage click actions (play, replay, or paywall)

**Independent Test**: Click "مرحله‌های گذشته" on `JourneyMapScreen` to open `StagesListScreen`; clicking on a stage triggers direct navigation to chat or subscription.

- [X] T009 [US2] Update `JourneyMapScreen` in `sharedUI/src/commonMain/kotlin/ir/aispeaking/sharedui/ui/stage/JourneyMapScreen.kt` to replace `PastStagesDialog` modal trigger with `onNavigateToStagesList: () -> Unit`
- [X] T010 [US2] Wire `onNavigateToStagesList = { backStack.add(StagesListRoute) }` in `navigation/src/commonMain/kotlin/ir/aispeaking/navigation/AppNavigation.kt`
- [X] T011 [US3] Connect item click handling in `StagesListScreen` to `onNavigateToStageChat` and `onNavigateToSubscription`

---

## Phase 5: Polish & Cross-Cutting Concerns

**Purpose**: Cleanup obsolete dialog and verify multiplatform compilation

- [X] T012 Remove obsolete `PastStagesDialog` and dead sheet states from `sharedUI/src/commonMain/kotlin/ir/aispeaking/sharedui/ui/stage/JourneyMapScreen.kt`
- [X] T013 Verify project compilation across commonMain and run Gradle checks
