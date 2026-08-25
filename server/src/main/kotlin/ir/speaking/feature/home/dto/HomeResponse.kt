package ir.speaking.feature.home.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class HomeResponse(
    val type: String,
    val title: String? = null,
    val data: JsonElement,
)

@Serializable
enum class HomeType {
    Banner,
    Categories,
    Scenarios
}
