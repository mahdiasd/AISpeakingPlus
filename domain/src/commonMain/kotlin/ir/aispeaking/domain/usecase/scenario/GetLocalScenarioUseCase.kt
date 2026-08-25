package ir.aispeaking.domain.usecase.scenario

import ir.aispeaking.domain.repository.scenario.ScenarioRepository
import org.koin.core.annotation.Single

@Single
class GetLocalScenarioUseCase(private val repo: ScenarioRepository) {
    suspend operator fun invoke() = repo.getSharedPrefScenario()
}