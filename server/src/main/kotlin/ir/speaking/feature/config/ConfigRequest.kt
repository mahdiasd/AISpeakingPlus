package ir.speaking.feature.config

import kotlinx.serialization.Serializable

@Serializable
data class ConfigRequest(
    val deviceName: String?,
    val androidVersion: String?,
    val firebaseToken: String?,
)