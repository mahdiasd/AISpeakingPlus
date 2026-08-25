package ir.aispeaking.domain.model.config

data class ConfigRequest(
    val deviceName: String?,
    val androidVersion: String?,
    val firebaseToken: String?,
)