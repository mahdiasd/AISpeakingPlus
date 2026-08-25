package ir.aispeaking.network.dto.user

import kotlinx.serialization.Serializable

@Serializable
data class CreateUserRequest(
    val nickName: String?,
    val firstName: String?,
    val lastName: String?,
    val mobile: String,
    val gender: String?,
    val languageLevel: String,
    val age: Int?,
    val avatar: String
)
