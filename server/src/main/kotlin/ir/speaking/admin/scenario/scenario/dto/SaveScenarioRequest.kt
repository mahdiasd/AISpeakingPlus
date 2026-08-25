package ir.speaking.admin.scenario.scenario.dto

import ir.speaking.core.utils.FileUpload
import ir.speaking.core.utils.ImageData
import ir.speaking.core.utils.toUUID
import ir.speaking.feature.chat.model.toRole
import ir.speaking.feature.scenario.scenario.model.Scenario
import ir.speaking.feature.user.model.toGender
import kotlinx.datetime.toKotlinLocalDateTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.time.LocalDateTime
import java.util.*

@Serializable
data class SaveScenarioRequest(
    val id: String? = null,
    val categoryId: String,
    val title: String,
    val persianTitle: String,
    val description: String,
    val persianDescription: String,
    val imageUrl: String? = null,
    val aiName: String,
    val aiRole: String,
    val gender: String,
    val aiAvatar: String? = null,
    val starter: String,
    val points: Int,
    val tasks: List<SaveScenarioTaskRequest> = emptyList()
) {
    @Transient
    @FileUpload("imageUrl")
    var imageFile: ImageData? = null
}

fun SaveScenarioRequest.toScenario() = Scenario(
    id = if (id.isNullOrEmpty()) UUID.randomUUID() else id.toUUID(),
    categoryId = UUID.fromString(categoryId),
    title = title,
    description = description,
    imageUrl = imageUrl,
    aiName = aiName,
    aiAvatar = aiAvatar,
    points = points,
    persianDescription = persianDescription,
    persianTitle = persianTitle,
    createdAt = LocalDateTime.now().toKotlinLocalDateTime(),
    aiRole = aiRole,
    gender = gender.toGender(),
    tasks = tasks.map { it.toScenarioTask(if (id.isNullOrEmpty()) UUID.randomUUID() else UUID.fromString(id)) },
    starter = starter.toRole()
)