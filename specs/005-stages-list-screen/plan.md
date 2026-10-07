# Implementation Plan: Stages List Screen

**Branch**: `feature/005-stages-list-screen` | **Date**: 2026-10-07 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from `specs/005-stages-list-screen/spec.md`

## Summary

Migrate the temporary Disney-inspired `PastStagesDialog` modal into a dedicated, rich, and responsive full-page `StagesListScreen` built with Compose Multiplatform and Navigation 3. The screen displays the complete sequence of learning stages, highlights the user's current active stage, shows total and per-stage star counts (0-3 stars), and provides direct replay or stage-start actions with unified navigation.

## Technical Context

**Language/Version**: Kotlin 2.1.0 Multiplatform (KMP)  
**Primary Dependencies**: Compose Multiplatform 1.7.3, Navigation 3, Koin 4.0.0, Coroutines  
**Storage**: Domain repositories (`GetStagesUseCase`, local/remote cache)  
**Testing**: Kotlin Common Unit Tests (`sharedUI/src/commonTest/`)  
**Target Platform**: Android, Desktop, iOS (CMP CommonMain)  
**Project Type**: Mobile / Multiplatform App  
**Performance Goals**: Instant page render (<300ms), 60fps smooth scrolling with `LazyColumn`  
**Constraints**: Follow Clean Architecture & Constitution Principle II, pure RTL design, unified `AppBackButton`  

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- [X] Clean Architecture & layer boundaries respected (no UI leaking into domain)
- [X] Pure Kotlin Multiplatform & Compose Multiplatform commonMain implementation
- [X] Koin dependency injection used cleanly via `@Factory` / `koinViewModel()`
- [X] Navigation 3 route with `@Serializable` polymorphic configuration

## Project Structure

### Documentation (this feature)

```text
specs/005-stages-list-screen/
├── spec.md              # Feature specification
├── plan.md              # This implementation plan
├── research.md          # Architecture decisions & findings
├── data-model.md        # UI and domain data representations
├── quickstart.md        # Verification and testing scenarios
├── contracts/
│   └── contracts.md     # Route, ViewModel, and Screen contracts
└── tasks.md             # Implementation tasks (generated via /speckit-tasks)
```

### Source Code (repository root)

```text
navigation/src/commonMain/kotlin/ir/aispeaking/navigation/
├── Routes.kt                                   # StagesListRoute definition
├── config.kt                                   # SavedState polymorphic registration
└── AppNavigation.kt                            # Entry point registration for StagesListRoute

sharedUI/src/commonMain/kotlin/ir/aispeaking/sharedui/ui/stage/
├── JourneyMapScreen.kt                         # Replace dialog trigger with onNavigateToStagesList
├── stageslist/
│   ├── StagesListScreen.kt                     # Dedicated full-page Compose screen
│   ├── StagesListViewModel.kt                  # State management for stages list
│   └── component/
│       ├── StagesHeaderStatsCard.kt            # Total stars & progress header
│       └── StageListItemCard.kt                # Individual stage item card with star rating
```

## Implementation Phases

- **Phase 1: Navigation Infrastructure**: Define `StagesListRoute` in `navigation` module and wire polymorphic serializer in `config.kt`.
- **Phase 2: UI & State Implementation**: Create `StagesListViewModel`, `StagesListScreen`, and components in `sharedUI`.
- **Phase 3: Integration & Clean-up**: Update `JourneyMapScreen` to navigate to `StagesListRoute`, remove obsolete dialog code, and verify build.
