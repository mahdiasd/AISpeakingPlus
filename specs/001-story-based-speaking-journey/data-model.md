# Data Model: Story-Based Gamified Speaking Journey

**Feature Branch**: `001-story-based-speaking-journey`  
**Date**: 2026-09-18  
**Status**: Draft  

---

## 1. Relational Entities & Database Schema

### `stages` (Database Table: `StageTable`)
Represents an individual episodic story scenario along the journey from Tehran to London.

| Column | Type | Nullable | Constraints / Defaults | Description |
|---|---|---|---|---|
| `id` | `VARCHAR(64)` | No | PRIMARY KEY | Unique stage identifier (e.g. `stage-01-tehran-departure`) |
| `order_index` | `INT` | No | UNIQUE, INDEX | Linear progression order (1 to 15+) |
| `title` | `VARCHAR(255)` | No | | English title (e.g. "Tehran Airport Departure") |
| `title_fa` | `VARCHAR(255)` | No | | Persian title (e.g. "خروج از فرودگاه امام تهران") |
| `briefing` | `TEXT` | No | | English scenario briefing and context |
| `briefing_fa` | `TEXT` | No | | Persian scenario briefing explaining the learner's mission |
| `target_objective` | `TEXT` | No | | Clear goal required for AI evaluation (e.g. "Obtain boarding pass and request window seat") |
| `background_url` | `VARCHAR(512)` | No | | URL to 2D illustrated background image |
| `character_name` | `VARCHAR(128)` | No | | Name of NPC roleplay partner (e.g. "Check-in Agent Sarah") |
| `character_avatar_url`| `VARCHAR(512)` | Yes | | Avatar illustration for the NPC |
| `character_gender` | `VARCHAR(16)` | No | DEFAULT `'Woman'` | Gender for TTS synthesis (`'Man'`, `'Woman'`) |
| `voice_id` | `VARCHAR(64)` | Yes | | Kokoro / Sherpa TTS voice identifier |
| `initial_speaker` | `VARCHAR(16)` | No | DEFAULT `'Model'` | Who initiates the conversation (`'Model'` or `'User'`) |
| `max_turns` | `INT` | No | DEFAULT 12 | Maximum conversational turns before mission auto-evaluates |
| `created_at` | `TIMESTAMP` | No | DEFAULT NOW() | Record creation timestamp |

---

### `stage_progress` (Database Table: `StageProgressTable`)
Tracks an individual user's completion status, best star rating, and replay statistics for a stage.

| Column | Type | Nullable | Constraints / Defaults | Description |
|---|---|---|---|---|
| `id` | `UUID` | No | PRIMARY KEY | Unique progress record ID |
| `user_id` | `UUID` | No | FOREIGN KEY (`users.uid`) ON DELETE CASCADE | Reference to authenticated user |
| `stage_id` | `VARCHAR(64)` | No | FOREIGN KEY (`stages.id`) ON DELETE CASCADE | Reference to stage |
| `stars` | `INT` | No | CHECK (`stars` BETWEEN 0 AND 3) | Best historical stars achieved (0 to 3) |
| `best_score` | `INT` | No | DEFAULT 0 | Best numeric score earned (0 to 100) |
| `repeat_count` | `INT` | No | DEFAULT 1 | Number of times this stage was completed |
| `completed_at` | `TIMESTAMP` | No | DEFAULT NOW() | Timestamp of first successful completion ($\ge 1$ star) |
| `updated_at` | `TIMESTAMP` | No | DEFAULT NOW() | Timestamp of latest attempt |

*Unique Index*: `(user_id, stage_id)` ensures a single historical record per user per stage.

---

### `subscriptions` (Database Table: `SubscriptionTable`)
Tracks premium subscription status, validity period, and plan entitlements.

| Column | Type | Nullable | Constraints / Defaults | Description |
|---|---|---|---|---|
| `id` | `UUID` | No | PRIMARY KEY | Subscription unique ID |
| `user_id` | `UUID` | No | FOREIGN KEY (`users.uid`) ON DELETE CASCADE | Subscribed user |
| `plan_type` | `VARCHAR(32)` | No | | `'MONTHLY'`, `'QUARTERLY'`, `'BIANNUAL'` |
| `started_at` | `TIMESTAMP` | No | DEFAULT NOW() | Subscription activation time |
| `expires_at` | `TIMESTAMP` | No | INDEX | Expiration time |
| `status` | `VARCHAR(32)` | No | DEFAULT `'ACTIVE'` | `'ACTIVE'`, `'EXPIRED'`, `'CANCELLED'` |
| `created_at` | `TIMESTAMP` | No | DEFAULT NOW() | Transaction time |

---

## 2. Domain Models & In-Memory Entities

### Kotlin Multiplatform (Common Domain)

```kotlin
package ir.aispeaking.domain.model.stage

import kotlin.time.Instant

/**
 * Access tier for the speaking journey.
 */
enum class AccessTier {
    GUEST,           // Tier 1: Can play Stage 1 only
    REGISTERED_FREE, // Tier 2: Can play Stage 1 and Stage 2
    SUBSCRIBER       // Tier 3: Can play all stages (Stage 1..15+)
}

/**
 * Locking state for a stage item on the journey map.
 */
enum class StageLockStatus {
    UNLOCKED,
    LOCKED_PREVIOUS_STAGE,    // Needs >= 1 star in previous stage
    LOCKED_REGISTRATION,      // Stage 2 gate (requires free SMS OTP signup)
    LOCKED_SUBSCRIPTION       // Stage 3+ gate (requires active paid subscription)
}

/**
 * Core stage definition.
 */
data class Stage(
    val id: String,
    val orderIndex: Int,
    val title: String,
    val titleFa: String,
    val briefing: String,
    val briefingFa: String,
    val targetObjective: String,
    val backgroundUrl: String,
    val characterName: String,
    val characterAvatarUrl: String?,
    val characterGender: String,
    val voiceId: String?,
    val initialSpeaker: String,
    val maxTurns: Int,
    val lockStatus: StageLockStatus = StageLockStatus.UNLOCKED,
    val userProgress: StageProgress? = null
)

/**
 * Persistent progress record for a stage.
 */
data class StageProgress(
    val id: String,
    val userId: String,
    val stageId: String,
    val stars: Int, // 0 to 3
    val bestScore: Int,
    val repeatCount: Int,
    val completedAt: Instant,
    val updatedAt: Instant
)

/**
 * In-memory state of an active speaking session.
 */
data class EvaluationSession(
    val stageId: String,
    val hintsUsedCount: Int,
    val grammarErrorsCount: Int,
    val objectiveCompleted: Boolean,
    val grammarErrors: List<GrammarErrorDetail>,
    val calculatedStars: Int,
    val feedbackFa: String
)

data class GrammarErrorDetail(
    val original: String,
    val correction: String,
    val explanationFa: String
)

/**
 * Local guest progress entity for unauthenticated persistence.
 */
data class LocalGuestProgress(
    val stageId: String,
    val stars: Int,
    val score: Int,
    val completedAt: Instant
)

/**
 * Leaderboard entry projection.
 */
data class LeaderboardEntry(
    val userId: String,
    val displayName: String,
    val avatarUrl: String?,
    val totalStars: Int,
    val completedStages: Int,
    val rank: Int
)
```

---

## 3. Validation Rules & State Transitions

### Stage Progression Rules
1. **Stage 1 (Guest)**:
   - Always unlocked by default.
   - Requires 0 authentication.
   - Earned stars stored in `LocalGuestProgress` if user is unauthenticated.
2. **Stage 2 (OTP Gate)**:
   - Unlocked if and only if:
     - Stage 1 has $\ge 1$ star, **AND**
     - User is authenticated (`AccessTier.REGISTERED_FREE` or `AccessTier.SUBSCRIBER`).
   - If user is Guest, clicking Stage 2 prompts registration dialog.
3. **Stage $N$ ($N \ge 3$, Paywall Gate)**:
   - Unlocked if and only if:
     - Stage $N-1$ has $\ge 1$ star, **AND**
     - User has an active subscription (`AccessTier.SUBSCRIBER` with `expires_at > NOW()`).
   - If user does not have active subscription, clicking Stage $N$ opens subscription paywall.

### Star Evaluation Rubric Formula
$$\text{penalties} = \text{grammarErrorsCount} + \text{hintsUsedCount}$$
$$\text{stars} = \begin{cases}
0 & \text{if } \neg\text{objectiveCompleted} \lor \text{penalties} \ge 3 \\
3 & \text{if } \text{penalties} = 0 \\
2 & \text{if } \text{penalties} = 1 \\
1 & \text{if } \text{penalties} = 2
\end{cases}$$

### High-Score Preservation State Machine
When a stage is re-evaluated:
- New record is saved with:
  $$\text{stars}_{\text{saved}} = \max(\text{stars}_{\text{existing}}, \text{stars}_{\text{new}})$$
  $$\text{score}_{\text{saved}} = \max(\text{score}_{\text{existing}}, \text{score}_{\text{new}})$$
  $$\text{repeatCount}_{\text{saved}} = \text{repeatCount}_{\text{existing}} + 1$$
- Leaderboard aggregates are dynamically updated or invalidated in Redis cache.
