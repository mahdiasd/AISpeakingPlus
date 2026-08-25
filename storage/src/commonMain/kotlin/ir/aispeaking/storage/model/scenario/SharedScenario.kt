package ir.aispeaking.storage.model.scenario

import kotlinx.serialization.Serializable

@Serializable
data class SharedScenario(
    val id: String,
    val categoryId: String,
    val title: String,
    val description: String,
    val imageUrl: String?,
    val aiName: String?,
    val aiAvatar: String?,
    val points: Int,
    val gender: String,
    val tasks: List<SharedScenarioTask>,
    val starter: String,
    val createdAt: String,
    val isChallenge: Boolean,
)