package ir.aispeaking.storage.model.user

import kotlinx.serialization.Serializable

@Serializable
data class SharedPrefUser(
    val uid: String = "",
    val nickName: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val mobile: String = "",
    val gender: String? = null,
    val languageLevel: String = "A1",
    val age: Int = 0,
    val score: Int = 0,
    val avatar: String = "avatar_g1",
    val completedScenarioCount: Long = 0,
    val completedWordCount: Long = 0,
    val completedChallengeCount: Long = 0,
)
