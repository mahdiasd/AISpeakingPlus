package ir.speaking.feature.challenge.challenge.dto

import ir.speaking.feature.challenge.challenge.model.Challenge
import kotlinx.serialization.Serializable

@Serializable
data class ChallengeSummaryResponse(
    val id: String,
    val title: String,
    val imageUrl: String?,
    val description: String,
    val aiAvatar: String?,
    val score: Int,
)

fun Challenge.toSummaryResponse(fullImagePath: (String?) -> String?) = ChallengeSummaryResponse(
    id = uid.toString(),
    title = title,
    imageUrl = fullImagePath(imageUrl),
    description = description,
    aiAvatar = fullImagePath(aiAvatar),
    score = points,
)