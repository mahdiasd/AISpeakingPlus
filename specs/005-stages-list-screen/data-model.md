# Data Model: Stages List Screen

**Feature**: `005-stages-list-screen`  
**Date**: 2026-10-07

## 1. Existing Domain Entities (Reused)

### `Stage` (`ir.aispeaking.domain.model.stage.Stage`)
- `id: String` - Unique stage identifier (e.g., `"stage-1"`, `"stage-london-02"`).
- `orderIndex: Int` - Progression order (1..15+).
- `title: String` - English title (e.g., `"Coffee at Heathrow"`).
- `titleFa: String` - Persian title (e.g., `"سفارش قهوه در فرودگاه هیترو"`).
- `descriptionFa: String` - Persian description/scenario.
- `characterName: String` - Conversational AI persona.
- `targetObjective: String` - Target English learning objective.
- `targetObjectiveFa: String` - Persian learning objective.
- `lockStatus: StageLockStatus` - Status (`UNLOCKED`, `LOCKED_REGISTRATION`, `LOCKED_SUBSCRIPTION`, `LOCKED_PREVIOUS_STAGE`).
- `userProgress: UserProgress?` - Progress details if played.

### `UserProgress` (`ir.aispeaking.domain.model.stage.UserProgress`)
- `stageId: String`
- `stars: Int` - Stars earned (0, 1, 2, or 3).
- `highScore: Int` - Score points.
- `completedAt: String?` - Completion timestamp.

## 2. Presentation State Model

### `StagesListUiState`
```kotlin
data class StagesListUiState(
    val isLoading: Boolean = true,
    val stages: List<Stage> = emptyList(),
    val currentActiveStageIndex: Int = 0,
    val totalStarsEarned: Int = 0,
    val maxPossibleStars: Int = 0,
    val completedStagesCount: Int = 0,
    val errorMessage: String? = null
)
```

### Derived UI Metrics:
- `currentStage: Stage? = stages.firstOrNull { (it.userProgress?.stars ?: 0) == 0 } ?: stages.lastOrNull()`
- `totalStarsEarned: Int = stages.sumOf { it.userProgress?.stars ?: 0 }`
- `maxPossibleStars: Int = stages.size * 3`
- `progressPercentage: Float = if (maxPossibleStars > 0) totalStarsEarned.toFloat() / maxPossibleStars else 0f`
