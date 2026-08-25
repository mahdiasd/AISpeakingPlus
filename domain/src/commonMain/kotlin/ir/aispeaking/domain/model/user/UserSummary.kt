package ir.aispeaking.domain.model.user

data class UserSummary(
    val uid: String,
    val score: Int,
    val nickName: String,
    val avatar: String
)