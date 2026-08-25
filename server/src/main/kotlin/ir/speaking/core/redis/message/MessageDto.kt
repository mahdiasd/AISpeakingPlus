package ir.speaking.core.redis.message

import ir.speaking.core.utils.ImageData
import kotlinx.serialization.Serializable

@Serializable
data class MessageDto(
    val id: String,
    val message: String,
    val title: String,
    val imageUrl: String? = null,
)