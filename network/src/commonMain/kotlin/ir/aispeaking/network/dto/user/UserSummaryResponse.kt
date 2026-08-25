package ir.aispeaking.network.dto.user

import kotlinx.serialization.Serializable

@Serializable
data class UserSummaryResponse(
    val uid: String,
    val nickName: String? = null,
    val avatar: String? = null,
    val score: Int? = null,
)
