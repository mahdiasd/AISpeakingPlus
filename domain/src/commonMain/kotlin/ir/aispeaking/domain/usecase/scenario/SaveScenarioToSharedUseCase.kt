package ir.aispeaking.domain.usecase.scenario

import ir.aispeaking.domain.model.scenario.Scenario
import ir.aispeaking.domain.repository.scenario.ScenarioRepository
import org.koin.core.annotation.Single

@Single
class SaveScenarioToSharedUseCase(private val repo: ScenarioRepository) {
    suspend operator fun invoke(scenario: Scenario) = repo.saveScenarioToSharedPref(scenario = scenario)
}