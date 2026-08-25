package ir.speaking.feature.user.dto.response

import ir.speaking.feature.user.model.User
import kotlinx.serialization.Serializable

@Serializable
data class UserSummaryResponse(
    val uid: String,
    val score: Int,
    val nickName: String,
    val avatar: String
)


fun User.toSummaryResponse() = UserSummaryResponse(
    uid = uid.toString(),
    nickName = nickName,
    score = score,
    avatar = avatar
)