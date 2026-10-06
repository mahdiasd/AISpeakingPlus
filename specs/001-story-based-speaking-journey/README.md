# Story-Based Gamified Speaking Journey (Tehran to London)

## Overview
The Story-Based Gamified Speaking Journey is an episodic English conversation experience taking learners from Tehran Imam Khomeini Airport through Heathrow Border Control to daily life in London across 15+ narrative stages.

## Architectural Components

### Domain Layer (`:domain`)
- **Models**:
  - `Stage`, `StageProgress`, `LocalGuestProgress`
  - `AccessTier`: `GUEST` (Stage 1), `REGISTERED_FREE` (Stages 1-2), `SUBSCRIBER` (Stages 1-15+)
  - `StageLockStatus`: `UNLOCKED`, `LOCKED_REGISTRATION`, `LOCKED_SUBSCRIPTION`, `LOCKED_PREVIOUS_STAGE`
  - `EvaluationSession`, `GrammarErrorDetail`, `HintSuggestion`
  - `SubscriptionPlan`, `SubscriptionStatus`
  - `LeaderboardEntry`, `JourneyLeaderboard`
- **Use Cases**:
  - `GetStagesUseCase`, `GetStageDetailUseCase`
  - `RequestStageHintUseCase`, `SubmitStageEvaluationUseCase`
  - `SyncGuestProgressUseCase`
  - `GetSubscriptionPlansUseCase`, `CheckSubscriptionStatusUseCase`
  - `GetJourneyLeaderboardUseCase`

### Network Layer (`:network`)
- **Endpoints**:
  - `GET /api/v2/stages` - Fetch catalog with lock status per access tier.
  - `GET /api/v2/stages/{stageId}` - Fetch stage details with tier enforcement (401 / 402).
  - `POST /api/v2/stages/{stageId}/hint` - Generate English suggestion with Persian guidance.
  - `POST /api/v2/stages/{stageId}/evaluate` - Evaluate transcript, penalize hints/grammar, award stars.
  - `POST /api/v2/progress/sync` - Sync local guest progress preserving highest stars and scores.
  - `GET /api/v2/subscriptions/plans` - List available subscription plans (1, 3, 6 months).
  - `GET /api/v2/subscriptions/status` - Current user active subscription status.
  - `GET /api/v2/leaderboard/journey` - Global journey leaderboard with pagination and user ranking.

### Data Layer (`:data`)
- `StageRepositoryImpl` - Caching, offline guest progress merging.
- `StageProgressRepositoryImpl` - Conflict resolution and sync.
- `SubscriptionRepositoryImpl` - Plan and entitlement querying.
- `LeaderboardRepositoryImpl` - Journey rankings and score aggregation.
- `LocalGuestProgressDataSource` - Persistent key-value storage for guest progress.

### Presentation Layer (`:sharedUI`)
- `JourneyMapScreen` & `JourneyMapViewModel` - 15-node vertical journey path with status badges and animations.
- `StageBriefingDialog` - Bilingual mission objectives and character introduction.
- `ChatScreen` & `ChatViewModel` - Conversation loop with real-time speech-to-text, TTS, and hint cues.
- `HintSuggestionCue` - Contextual English starter with Persian explanation.
- `EvaluationResultCard` - 0–3 animated glowing star rating, penalty breakdown, and constructive Persian feedback.
- `RegisterBottomSheet` - Iranian mobile OTP verification modal with automatic progress sync.
- `SubscriptionPaywallSheet` - Toman pricing, discount badges, and promo code redemption.
- `JourneyLeaderboardSheet` - Global leaderboard and current user ranking card.

### Server Layer (`:server`)
- **Tables**: `stages`, `stage_progress`, `subscriptions`, `users`.
- **Seed Data**: 15 complete stages with bilingual briefings and target objectives.
- **Evaluation Rubric**: `totalPenalties = grammarErrors + hintsUsed`.
  - 0 penalties: 3 Stars ⭐⭐⭐
  - 1 penalty: 2 Stars ⭐⭐
  - 2 penalties: 1 Star ⭐
  - 3+ penalties or !objectiveCompleted: 0 Stars ⭕
- **Replay Preservation**: `stars_saved = max(existing_stars, new_stars)`.

## Verification Commands
```bash
# Domain tests
./gradlew :domain:jvmTest

# Server tests
./gradlew :server:test

# Multiplatform build
./gradlew :server:compileKotlin :desktopApp:compileKotlin
```
