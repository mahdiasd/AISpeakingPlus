package ir.aispeaking.domain.usecase.challenge

import ir.aispeaking.domain.repository.challenge.ChallengeRepository
import org.koin.core.annotation.Single

@Single
class GetChallengeUseCase(private val repo: ChallengeRepository) {
    suspend operator fun invoke() = repo.getChallenge()
}