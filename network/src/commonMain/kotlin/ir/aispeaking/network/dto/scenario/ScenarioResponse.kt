package ir.aispeaking.network.dto.scenario

import kotlinx.serialization.Serializable

@Serializable
data class ScenarioResponse(
    val id: String,
    val categoryId: String,
    val persianTitle: String,
    val title: String,
    val persianDescription: String,
    val description: String,
    val imageUrl: String?,
    val aiName: String?,
    val aiAvatar: String?,
    val points: Int,
    val createdAt: String,
    val gender: String,
    val starter: String,
    val tasks: List<ScenarioTaskResponse>
)