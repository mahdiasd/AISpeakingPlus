# Research & Architecture Decisions: Stages List Screen

**Feature**: `005-stages-list-screen`  
**Date**: 2026-10-07

## 1. Screen Architecture & State Management

- **Decision**: Implement `StagesListScreen` with dedicated `StagesListViewModel` (or leverage clean MVI state pattern in `sharedUI/src/commonMain/kotlin/ir/aispeaking/sharedui/ui/stage/stageslist/`).
- **Rationale**:
  - Conforms to Clean Architecture & Constitution Principle II (Pure KMP & CMP Client).
  - Keeps UI decoupled from `JourneyMapViewModel` while allowing direct navigation to/from stages.
  - Follows existing conventions used in `ProfileScreen` and `SubscriptionScreen`.
- **Alternatives considered**:
  - Reusing `JourneyMapViewModel`: Rejected because `JourneyMapViewModel` manages 2D map camera, prologue, briefing dialogs, and map artwork state, which introduces unnecessary coupling and state leakage. A focused ViewModel provides better testability and lifecycle management.

## 2. Navigation Integration (Navigation 3)

- **Decision**: Register `StagesListRoute : AppRoute` with polymorphic serializer in `navigation/src/commonMain/kotlin/ir/aispeaking/navigation/Routes.kt` and `config.kt`.
- **Rationale**:
  - Navigation 3 is the established navigation standard in this codebase (see `MainRoute`, `ProfileRoute`, `SubscriptionRoute`, `StageChatRoute`).
  - Supports deep linking and predictable backstack handling (`backStack.add(StagesListRoute)`, `backStack.removeLastOrNull()`).
- **Alternatives considered**:
  - Modal BottomSheet / Dialog: Rejected per explicit user requirement ("الان داریم ولی دیالوگ هست. باید بره توی صفحه").

## 3. UI/UX Design & Star Badges

- **Decision**:
  - Modern dark Disney/gamified theme (`#070B19` / `#0F172A`) matching `JourneyMapScreen`.
  - Top app bar with `AppBackButton`, title "مسیر یادگیری و مراحل" (Learning Journey & Stages), and overall star statistics chip.
  - Summary Header Card showing total stars earned (`totalStarsEarned`), completed stages count, and overall progress percentage.
  - `LazyColumn` of Stage Cards. Each card displays:
    - Stage order badge (`orderIndex`)
    - Title in Persian (`titleFa`) and English (`title`)
    - Target objective preview
    - Star Rating indicator (0 to 3 stars using `ic_star` and `ic_star_outline`)
    - Active Stage indicator ("مرحله فعلی شما") with distinctive glowing border (`Color(0xFFFFD700)`)
    - Action button ("ادامه مرحله", "تکرار مرحله", "قفل")
  - Automatic scroll to active stage on first entry using `LazyListState.scrollToItem()`.
- **Rationale**:
  - Seamless visual consistency with `JourneyMapScreen` and `AppBackButton`.
  - Satisfies all user acceptance criteria and provides high-engagement feedback.

## 4. Reusability of Domain & Use Cases

- **Decision**: Utilize existing `GetStagesUseCase` and `GetCurrentAccessTierUseCase` from `domain` module.
- **Rationale**:
  - Domain layer already possesses all necessary models (`Stage`, `UserProgress`, `AccessTier`, `StageLockStatus`).
  - Zero modifications required to backend or network layer.
