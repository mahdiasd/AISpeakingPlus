package ir.aispeaking.domain.usecase.scenario_progress

import ir.aispeaking.domain.repository.scenario.ScenarioRepository
import org.koin.core.annotation.Single

@Single
class CreateScenarioProgressUseCase(private val repo: ScenarioRepository) {
    suspend operator fun invoke(scenarioId: String, score: Int) =
        repo.createScenarioProgress(scenarioId = scenarioId, score = score)
}