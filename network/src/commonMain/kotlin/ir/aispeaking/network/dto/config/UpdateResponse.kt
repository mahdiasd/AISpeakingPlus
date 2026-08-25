package ir.aispeaking.network.dto.config

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class UpdateResponse(
    @SerialName("force_version")
    val forceVersion: Int = 0,

    @SerialName("last_version")
    val lastVersion: Int = 0,

    @SerialName("suggest_version")
    val suggestVersion: Int = 0,

    val link: String = "",

    val message: String = ""
)