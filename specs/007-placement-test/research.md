# Technical Research & Architecture Decisions: Placement Test (آزمون تعیین سطح)

**Feature**: `007-placement-test` | **Date**: 2026-11-13

## 1. Number of Questions & Sampling Strategy

- **Decision**: 20 questions per test session (4 questions each from A1, A2, B1, B2, C1) selected via Stratified Random Sampling from a 100-question seed bank on the server.
- **Rationale**:
  - Global benchmarking (British Council Quick Check, Cambridge English, Duolingo, Busuu) demonstrates that 20 questions take ~5-7 minutes.
  - Less than 15 questions leads to unacceptable statistical guessing variance.
  - More than 25-30 questions leads to steep drop-off rates on mobile devices.
  - Pure random selection from 100 questions could disproportionately favor basic or advanced questions. Stratified sampling guarantees exactly 4 questions per CEFR band.
- **Alternatives Considered**:
  - Pure random sampling: Rejected due to risk of skewed difficulty distributions.
  - 50 questions (Aptis format): Rejected due to 15-20 min test duration which causes high user fatigue in mobile apps.

## 2. Horizontal Slider UX & Interaction (Compose Multiplatform)

- **Decision**: Use `androidx.compose.foundation.pager.HorizontalPager` with an animated pager state.
- **Rationale**:
  - Smooth 60fps horizontal paging across all platforms (Android, Desktop, Wasm, iOS).
  - Clean separation: each slide focuses 100% of user attention on one question and its 4 options.
  - Supports programmatic navigation (Next/Previous buttons) as well as intuitive touch swipe gestures.
  - Animated progress indicator at the top showing "سوال X از ۲۰".
- **Alternatives Considered**:
  - Single vertical scrolling list with 20 questions: Rejected because it overwhelms mobile users, invites skipping, and feels like a paper exam rather than an engaging digital experience.

## 3. Server-side Scoring & CEFR Level Assignment Algorithm

- **Decision**: Server validates answers against the true answer keys stored in PostgreSQL (`placement_questions` table).
- **Formula & Logic**:
  - Each level (A1, A2, B1, B2, C1) has 4 questions (total 20 questions = 100%).
  - Points per question: 5 points (Total 100 points).
  - Level Determination:
    - If user achieves < 40% overall or fails A1/A2: Assigned `A1`.
    - If user passes A1 (>=3/4) and A2 (>=3/4) with overall 40-55%: Assigned `A2`.
    - If user passes A1-A2 and B1 (>=3/4) with overall 55-70%: Assigned `B1`.
    - If user passes A1-B1 and B2 (>=3/4) with overall 70-85%: Assigned `B2`.
    - If user passes A1-B2 and C1 (>=3/4) with overall >= 85%: Assigned `C1`.
  - The calculated level string (`A1`, `A2`, `B1`, `B2`, `C1`) is saved directly to `UserTable.language_level` and returned with full review data.
- **Security**: The correct answer index is never sent in the initial `GET /api/v2/placement/questions` response. It is only revealed in the `POST /api/v2/placement/evaluate` response.

## 4. Question Review Breakdown (کارنامه و مرور سوالات)

- **Decision**: Result payload includes an array of `QuestionReviewItem`:
  - `questionId`: String
  - `questionText`: String
  - `options`: List<String>
  - `selectedOption`: String?
  - `correctOption`: String
  - `isCorrect`: Boolean
  - `level`: LanguageLevel
- **Rationale**: Allows the client to render a clean, color-coded interactive review sheet or dialog showing which questions were right (green badge) and which were wrong (red badge with the right answer highlighted).

## 5. Clean Architecture Layering & Module Organization

- **Domain Layer** (`domain` module):
  - Models: `PlacementQuestion`, `PlacementEvaluationResult`, `QuestionReviewItem`, `PlacementSubmission`.
  - Use Cases: `GetPlacementQuestionsUseCase`, `SubmitPlacementTestUseCase`.
  - Repository Interface: `PlacementTestRepository`.
- **Data Layer** (`data` module):
  - Repository Impl: `PlacementTestRepositoryImpl`.
  - Network DTOs: `PlacementQuestionDto`, `PlacementEvaluationResponseDto`, `PlacementSubmitRequestDto`.
  - Remote Data Source: `PlacementRemoteDataSource` using Ktor HttpClient.
- **Client Presentation Layer** (`feature/placement_test` module):
  - Route: `PlacementTestRoute` registered in `AppNavigation.kt`.
  - Screens & Components: `PlacementTestScreen`, `PlacementPager`, `QuestionSlideCard`, `OptionSelectableCard`, `PlacementResultScreen`, `QuestionReviewDialog`.
  - ViewModel: `PlacementTestViewModel` with state management (loading, in-progress, completed, error).
  - Profile Entry: `PlacementTestCard` in `feature/profile` under `ProfileScreen.kt`.
- **Server Layer** (`server` module):
  - Exposed Table: `PlacementQuestionsTable`, `PlacementSessionsTable`.
  - Route: `placementRouting.kt` under `/api/v2/placement` with full OpenAPI 3.0.3 `.describe { ... }`.
  - Seed Data: 100 curated multiple-choice English questions (20 questions each for A1, A2, B1, B2, C1) seeded on server startup.
