package ir.speaking.feature.user.dto.response

import ir.speaking.feature.user.model.User
import kotlinx.serialization.Serializable

@Serializable
data class UserResponse(
    val uid: String,
    val nickName: String,
    val firstName: String,
    val lastName: String,
    val mobile: String,
    val gender: String?,
    val languageLevel: String,
    val age: Int?,
    val score: Int,
    val avatar: String,

    val completedScenarioCount: Long,
    val completedWordCount: Long,
    val completedChallengeCount: Long,
)


// Extension functions to convert User to DTOs
fun User.toResponse(
    completedScenarioCount: Long = 0,
    completedWordCount: Long = 0,
    completedChallengeCount: Long = 0,
) = UserResponse(
    uid = uid.toString(),
    nickName = nickName,
    firstName = firstName ?: "",
    lastName = lastName ?: "",
    mobile = mobile,
    gender = gender?.toString(),
    languageLevel = languageLevel.name,
    age = age,
    score = score,
    avatar = avatar,

    completedScenarioCount = completedScenarioCount,
    completedWordCount = completedWordCount,
    completedChallengeCount = completedChallengeCount,
)
