package ir.aispeaking.domain.usecase.challenge_progress

import ir.aispeaking.domain.repository.challenge.ChallengeRepository
import org.koin.core.annotation.Single

@Single
class CreateChallengeProgressUseCase(private val repo: ChallengeRepository) {
    suspend operator fun invoke(challengeId: String, score: Int) =
        repo.createProgress(challengeId = challengeId, score = score)
}