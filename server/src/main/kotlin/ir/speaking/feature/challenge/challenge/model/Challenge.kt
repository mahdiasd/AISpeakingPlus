package ir.speaking.feature.challenge.challenge.model

import ir.speaking.feature.challenge.task.model.ChallengeTask
import ir.speaking.feature.chat.model.Role
import ir.speaking.feature.user.model.Gender
import kotlinx.datetime.LocalDate
import java.util.*

data class Challenge(
    val uid: UUID,
    val title: String,
    val description: String,
    val aiRole: String,
    val persianTitle: String,
    val persianDescription: String,
    val imageUrl: String?,
    val aiName: String,
    val aiAvatar: String?,
    val gender: Gender,
    val starter: Role,
    val points: Int,
    val createdAt: kotlinx.datetime.LocalDateTime,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val tasks: List<ChallengeTask> = listOf()
)
