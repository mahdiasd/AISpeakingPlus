package ir.aispeaking.storage.model.guide

import kotlinx.serialization.Serializable

@Serializable
data class SharedGuide(
    val register: Boolean = false,
    val challenge: Boolean = false,
    val lightener: Boolean = false,
    val scenario: Boolean = false,
    val roadmap: Boolean = false,
    val chat: Boolean = false,
    val profile: Boolean = false,
)