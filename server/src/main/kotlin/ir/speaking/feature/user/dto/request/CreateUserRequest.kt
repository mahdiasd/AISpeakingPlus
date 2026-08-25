package ir.speaking.feature.user.dto.request

import ir.speaking.feature.user.model.Gender
import ir.speaking.feature.user.model.LanguageLevel
import ir.speaking.feature.user.model.User
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class CreateUserRequest(
    val nickName: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val mobile: String,
    val gender: String? = null,
    val languageLevel: String,
    val age: Int? = null,
    val avatar: String
)

fun CreateUserRequest.toUser(): User {
    return User(
        uid = UUID.randomUUID(),
        nickName = this.nickName ?: "",
        firstName = this.firstName ?: "",
        lastName = this.lastName ?: "",
        mobile = this.mobile,
        gender = if (this.gender.isNullOrEmpty()) null else Gender.valueOf(this.gender),
        languageLevel = LanguageLevel.valueOf(this.languageLevel),
        age = this.age ?: 0,
        score = 0,
        avatar = this.avatar,
        active = true,
        createdAt = Clock.System.now(),
        updatedAt = Clock.System.now()
    )
}
