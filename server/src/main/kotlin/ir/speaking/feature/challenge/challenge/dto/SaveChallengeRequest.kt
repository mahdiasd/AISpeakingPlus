package ir.speaking.feature.challenge.challenge.dto

import ir.speaking.feature.challenge.task.dto.ChallengeTaskRequest
import kotlinx.serialization.Serializable

@Serializable
data class SaveChallengeRequest(
    val uid: String? = null,
    val title: String,
    val persianTitle: String,
    val description: String,
    val persianDescription: String,
    val imageUrl: String?,
    val aiName: String,
    val role: String,
    val aiAvatar: String?,
    val gender: String,
    val starter: String,
    val startDate: String,
    val endDate: String,
    val points: Int,
    val tasks: List<ChallengeTaskRequest>
)