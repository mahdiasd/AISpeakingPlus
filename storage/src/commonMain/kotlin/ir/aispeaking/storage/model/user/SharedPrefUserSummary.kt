package ir.aispeaking.storage.model.user

import kotlinx.serialization.Serializable

@Serializable
data class SharedPrefUserSummary(
    val uid: String,
    val score: Int = 0,
    val nickName: String? = null,
    val avatar: String? = null
)