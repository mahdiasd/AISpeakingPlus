package ir.aispeaking.domain.model.user

import ir.aispeaking.domain.model.level.LanguageLevel

data class RegisterParam(
    val nickName: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val mobile: String = "",
    val languageLevel: LanguageLevel? = null,
    val avatar: String = "",
    val gender: Gender? = null,
    val age: Int? = null,
)
