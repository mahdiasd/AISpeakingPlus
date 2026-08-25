package ir.aispeaking.domain.model.challenge

data class ChallengeDetail(
    val challenge: Challenge,
    val userHaveSubscription: Boolean,
    val progress: ChallengeProgress?
)
