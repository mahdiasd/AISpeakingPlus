package ir.aispeaking.network.dto.config

import kotlinx.serialization.Serializable

@Serializable
data class ConfigRequest(
    val deviceName: String?,
    val androidVersion: String?,
    val firebaseToken: String?,
)