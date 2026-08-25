package ir.speaking.feature.config

import ir.speaking.core.redis.message.MessageDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Config(
    val update: Update,
    val tokenAlive: Boolean,
    val welcomeMessage: MessageDto?,
)

@Serializable
data class Update(
    @SerialName("force_version")
    val forceVersion: Int = 0,

    @SerialName("last_version")
    val lastVersion: Int = 0,

    @SerialName("suggest_version")
    val suggestVersion: Int = 0,

    val link: String = "",

    val message: String = ""
)