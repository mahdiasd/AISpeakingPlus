package ir.aispeaking.storage.model.user

import kotlinx.serialization.Serializable

@Serializable
data class SharedPrefUser(
    val uid: String,
    val nickName: String,
    val firstName: String,
    val lastName: String,
    val mobile: String,
    val gender: String?,
    val languageLevel: String,
    val age: Int,
    val score: Int,
    val avatar: String,
    val completedScenarioCount: Long = 0,
    val completedWordCount: Long = 0,
    val completedChallengeCount: Long = 0,
)
