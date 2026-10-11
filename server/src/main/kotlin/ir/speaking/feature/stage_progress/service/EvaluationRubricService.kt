package ir.speaking.feature.stage_progress.service

import org.koin.core.annotation.Single

data class RubricResult(
    val totalPenalties: Int,
    val starsEarned: Int,
    val score: Int
)

@Single
class EvaluationRubricService {

    /**
     * Rubric:
     * totalPenalties = grammarErrorsCount + hintsUsedCount
     * stars = 3 if totalPenalties == 0 and objectiveCompleted
     * stars = 2 if totalPenalties == 1 and objectiveCompleted
     * stars = 1 if totalPenalties == 2 and objectiveCompleted
     * stars = 0 if totalPenalties >= 3 or !objectiveCompleted
     */
    fun calculateRubric(
        grammarErrorsCount: Int,
        hintsUsedCount: Int,
        objectiveCompleted: Boolean
    ): RubricResult {
        val totalPenalties = grammarErrorsCount + hintsUsedCount

        val stars = when {
            !objectiveCompleted -> 0
            totalPenalties == 0 -> 3
            totalPenalties == 1 -> 2
            totalPenalties == 2 -> 1
            else -> 0
        }

        val score = when (stars) {
            3 -> 100
            2 -> 85
            1 -> 70
            else -> if (objectiveCompleted) 50 else 0
        }

        return RubricResult(
            totalPenalties = totalPenalties,
            starsEarned = stars,
            score = score
        )
    }
}
