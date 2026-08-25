package ir.aispeaking.domain.model.challenge

data class ChallengeSummary(
    val id: String,
    val title: String,
    val imageUrl: String?,
    val description: String,
    val aiAvatar: String?,
    val score: Int,
)
