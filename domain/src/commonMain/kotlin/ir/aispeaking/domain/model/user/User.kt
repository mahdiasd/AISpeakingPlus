package ir.aispeaking.domain.model.user

import ir.aispeaking.domain.model.level.LanguageLevel
import kotlinx.serialization.Serializable

data class User(
    val uid: String,
    val nickName: String,
    val firstName: String,
    val lastName: String,
    val mobile: String,
    val gender: Gender?,
    val languageLevel: LanguageLevel,
    val age: Int,
    val score: Int,
    val avatar: String,

    // show in profile ↓
    val completedScenarioCount: Long = 0,
    val completedWordCount: Long = 0,
    val completedChallengeCount: Long = 0,
) {
    fun fullName(): String {
        return "$firstName $lastName"
    }
}

@Serializable
enum class Gender { Man, Woman }

