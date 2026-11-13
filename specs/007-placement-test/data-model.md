# Data Model: Placement Test (آزمون تعیین سطح)

**Feature**: `007-placement-test` | **Date**: 2026-11-13

## Database Schema (PostgreSQL via Exposed)

### 1. `placement_questions` Table
Stores the bank of 100 CEFR-graded multiple-choice questions.

| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | VARCHAR(64) | PRIMARY KEY | Unique ID (e.g., `pq_a1_01`) |
| `question_text` | TEXT | NOT NULL | English question prompt or sentence with blank |
| `options_json` | TEXT | NOT NULL | JSON array of 4 options (e.g. `["am", "is", "are", "be"]`) |
| `correct_option` | VARCHAR(255) | NOT NULL | The correct answer string matching one of the options |
| `level` | VARCHAR(16) | NOT NULL | CEFR Level (`A1`, `A2`, `B1`, `B2`, `C1`) |
| `category` | VARCHAR(64) | NOT NULL DEFAULT 'grammar' | Topic (e.g., 'grammar', 'vocabulary', 'comprehension') |
| `created_at` | TIMESTAMP | NOT NULL | Record creation timestamp |

### 2. `placement_sessions` Table
Tracks user test attempts and their calculated results.

| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | VARCHAR(64) | PRIMARY KEY | Session UUID |
| `user_id` | VARCHAR(64) | NOT NULL | References `users.id` (or anonymous ID for guest) |
| `question_ids_json` | TEXT | NOT NULL | JSON array of the 20 selected question IDs |
| `score_percent` | INT | NOT NULL DEFAULT 0 | Total percentage score (0-100) |
| `assigned_level` | VARCHAR(16) | NULLABLE | Calculated CEFR level (`A1`..`C1`) |
| `correct_count` | INT | NOT NULL DEFAULT 0 | Count of correct answers (0-20) |
| `wrong_count` | INT | NOT NULL DEFAULT 0 | Count of incorrect answers (0-20) |
| `is_completed` | BOOLEAN | NOT NULL DEFAULT FALSE | True when user completes test |
| `created_at` | TIMESTAMP | NOT NULL | Session start timestamp |
| `completed_at` | TIMESTAMP | NULLABLE | Session completion timestamp |

---

## Domain Models (KMP Shared)

### 1. `PlacementQuestion`
```kotlin
package ir.aispeaking.domain.model.placement

import ir.aispeaking.domain.model.level.LanguageLevel
import kotlinx.serialization.Serializable

@Serializable
data class PlacementQuestion(
    val id: String,
    val questionText: String,
    val options: List<String>,
    val level: LanguageLevel,
    val category: String = "general"
)
```

### 2. `PlacementEvaluationResult`
```kotlin
package ir.aispeaking.domain.model.placement

import ir.aispeaking.domain.model.level.LanguageLevel
import kotlinx.serialization.Serializable

@Serializable
data class PlacementEvaluationResult(
    val sessionId: String,
    val assignedLevel: LanguageLevel,
    val scorePercent: Int,
    val correctCount: Int,
    val wrongCount: Int,
    val totalCount: Int = 20,
    val levelTitleFa: String,
    val descriptionFa: String,
    val reviewItems: List<QuestionReviewItem>
)

@Serializable
data class QuestionReviewItem(
    val questionId: String,
    val questionText: String,
    val options: List<String>,
    val selectedOption: String?,
    val correctOption: String,
    val isCorrect: Boolean,
    val level: LanguageLevel
)
```

### 3. `PlacementSubmission`
```kotlin
package ir.aispeaking.domain.model.placement

import kotlinx.serialization.Serializable

@Serializable
data class PlacementSubmission(
    val sessionId: String,
    val answers: Map<String, String> // questionId -> selectedOption
)
```
