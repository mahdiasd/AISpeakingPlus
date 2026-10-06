package ir.aispeaking.domain.model.user

data class User(
    val uid: String,
    val nickName: String,
    val firstName: String = "",
    val lastName: String = "",
    val mobile: String,
    val gender: String? = null,
    val languageLevel: String = "",
    val score: Int = 0,
    val avatar: String = ""
)
