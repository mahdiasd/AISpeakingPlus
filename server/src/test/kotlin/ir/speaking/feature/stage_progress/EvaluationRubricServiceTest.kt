package ir.speaking.feature.stage_progress

import ir.speaking.feature.stage_progress.service.EvaluationRubricService
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class EvaluationRubricServiceTest {

    private val rubricService = EvaluationRubricService()

    @Test
    fun `0 penalties and completed objective awards 3 stars`() {
        val result = rubricService.calculateRubric(
            grammarErrorsCount = 0,
            hintsUsedCount = 0,
            objectiveCompleted = true
        )
        assertEquals(3, result.starsEarned)
        assertEquals(0, result.totalPenalties)
        assertEquals(100, result.score)
    }

    @Test
    fun `1 penalty from grammar error awards 2 stars`() {
        val result = rubricService.calculateRubric(
            grammarErrorsCount = 1,
            hintsUsedCount = 0,
            objectiveCompleted = true
        )
        assertEquals(2, result.starsEarned)
        assertEquals(1, result.totalPenalties)
        assertEquals(85, result.score)
    }

    @Test
    fun `1 penalty from hint awards 2 stars`() {
        val result = rubricService.calculateRubric(
            grammarErrorsCount = 0,
            hintsUsedCount = 1,
            objectiveCompleted = true
        )
        assertEquals(2, result.starsEarned)
        assertEquals(1, result.totalPenalties)
        assertEquals(85, result.score)
    }

    @Test
    fun `2 penalties awards 1 star`() {
        val result = rubricService.calculateRubric(
            grammarErrorsCount = 1,
            hintsUsedCount = 1,
            objectiveCompleted = true
        )
        assertEquals(1, result.starsEarned)
        assertEquals(2, result.totalPenalties)
        assertEquals(70, result.score)
    }

    @Test
    fun `3 or more penalties awards 0 stars`() {
        val result = rubricService.calculateRubric(
            grammarErrorsCount = 2,
            hintsUsedCount = 1,
            objectiveCompleted = true
        )
        assertEquals(0, result.starsEarned)
        assertEquals(3, result.totalPenalties)
    }

    @Test
    fun `uncompleted objective always awards 0 stars regardless of penalties`() {
        val result = rubricService.calculateRubric(
            grammarErrorsCount = 0,
            hintsUsedCount = 0,
            objectiveCompleted = false
        )
        assertEquals(0, result.starsEarned)
    }
}
