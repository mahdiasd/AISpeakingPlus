package ir.aispeaking.domain.usecase.challenge

import ir.aispeaking.domain.repository.scenario.ScenarioRepository
import org.koin.core.annotation.Single

@Single
class GetChallengeDetailUseCase(private val repo: ScenarioRepository) {
    suspend operator fun invoke(challengeId: String) = repo.getChallengeDetail(challengeId = challengeId)
}