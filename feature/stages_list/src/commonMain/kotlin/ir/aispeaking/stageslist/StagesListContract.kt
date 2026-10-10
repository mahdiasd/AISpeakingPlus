package ir.aispeaking.stageslist

import ir.aispeaking.domain.model.stage.Stage

data class StagesListUiState(
    val isLoading: Boolean = true,
    val stages: List<Stage> = emptyList(),
    val errorMessage: String? = null
) {
    /**
     * Active current stage that user is on (first uncompleted stage with 0 stars, or last available stage).
     */
    val currentStage: Stage?
        get() = stages.firstOrNull { (it.userProgress?.stars ?: 0) == 0 } ?: stages.lastOrNull()

    /**
     * Index of the current active stage in the stages list.
     */
    val currentActiveStageIndex: Int
        get() {
            val curr = currentStage ?: return 0
            val idx = stages.indexOfFirst { it.id == curr.id }
            return if (idx >= 0) idx else 0
        }

    /**
     * Total stars earned across all stages.
     */
    val totalStarsEarned: Int
        get() = stages.sumOf { it.userProgress?.stars ?: 0 }

    /**
     * Maximum possible stars in the curriculum (3 stars per stage).
     */
    val maxPossibleStars: Int
        get() = stages.size * 3

    /**
     * Count of completed stages (where user received at least 1 star).
     */
    val completedStagesCount: Int
        get() = stages.count { (it.userProgress?.stars ?: 0) > 0 }

    /**
     * Percentage of journey completed (0.0f - 1.0f).
     */
    val progressPercentage: Float
        get() = if (stages.isNotEmpty()) completedStagesCount.toFloat() / stages.size else 0f
}

sealed interface StagesListUiAction {
    data class StageClicked(val stage: Stage) : StagesListUiAction
    data object RefreshRequested : StagesListUiAction
    data object ClearError : StagesListUiAction
}
