package ir.speaking.feature.scenario.scenario.model

import ir.speaking.feature.chat.model.Role
import ir.speaking.feature.scenario.task.model.ScenarioTask
import ir.speaking.feature.user.model.Gender
import kotlinx.datetime.LocalDateTime
import java.util.*

data class Scenario(
    val id: UUID,
    val categoryId: UUID,
    val title: String,
    val description: String,
    val aiRole: String,
    val persianTitle: String,
    val persianDescription: String,
    val imageUrl: String?,
    val aiName: String,
    val aiAvatar: String?,
    val points: Int,
    val gender: Gender,
    val createdAt: LocalDateTime,
    val starter: Role,
    val tasks: List<ScenarioTask> = listOf()
)
