package ir.aispeaking.domain.model.scenario

data class ScenarioSummary(
    val id: String,
    val title: String,
    val imageUrl: String?,
    val aiAvatar: String?,
    val score: Int = 0,
)