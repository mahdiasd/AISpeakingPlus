package ir.speaking.feature.user.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class UpdateUserRequest(
    val uid: String,
    val nickName: String,
    val firstName: String?,
    val lastName: String?,
    val mobile: String,
    val gender: String?,
    val languageLevel: String,
    val age: Int?,
    val avatar: String
)