package ir.speaking.feature.word.word.dto

import kotlinx.serialization.Serializable

@Serializable
data class CrudWordRequest(
    val uid: String? = null,
    val word: String,
    val options: List<String>,
    val answerIndex: Int,
    val points: Int,
    val expiredAt: String,
)
