package ir.speaking.feature.user.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import java.util.*

data class User(
    val uid: UUID,
    val nickName: String,
    val firstName: String?,
    val lastName: String?,
    val mobile: String,
    val gender: Gender?,
    val languageLevel: LanguageLevel,
    val age: Int?,
    val score: Int,
    val avatar: String,
    val createdAt: Instant,
    val updatedAt: Instant,
    val active: Boolean,
)

@Serializable
enum class Gender { Man, Woman }

@Serializable
enum class LanguageLevel { A1, A2, B1, B2, C1, C2 }

fun String.toGender(): Gender {
    return when {
        this.equals(Gender.Man.name, true) -> Gender.Man
        this.equals(Gender.Woman.name, true) -> Gender.Woman
        else -> throw Exception("Invalid gender")
    }
}