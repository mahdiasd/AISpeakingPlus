package ir.speaking.feature.scenario.scenario.dto.response

import ir.speaking.feature.scenario.scenario.model.Scenario
import ir.speaking.feature.scenario.task.dto.ScenarioTaskResponse
import ir.speaking.feature.scenario.task.dto.toResponse
import kotlinx.serialization.Serializable

@Serializable
data class ScenarioResponse(
    val id: String,
    val categoryId: String,
    val title: String,
    val persianTitle: String,
    val persianDescription: String,
    val description: String,
    val imageUrl: String?,
    val aiName: String?,
    val aiAvatar: String?,
    val points: Int,
    val gender: String,
    val createdAt: String,
    val starter: String,
    val tasks: List<ScenarioTaskResponse>
)

fun Scenario.toResponse(fullImagePath: (String?) -> String?) = ScenarioResponse(
    id = id.toString(),
    categoryId = categoryId.toString(),
    title = title,
    description = description,
    imageUrl = fullImagePath(imageUrl),
    aiName = aiName,
    aiAvatar = fullImagePath(aiAvatar),
    points = points,
    createdAt = createdAt.toString(),
    persianDescription = persianDescription,
    persianTitle = persianTitle,
    gender = gender.name,
    starter = starter.name,
    tasks = this.tasks.map { it.toResponse() }
)