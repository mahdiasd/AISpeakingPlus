package ir.aispeaking.network.dto.home

import kotlinx.serialization.Serializable

import kotlinx.serialization.json.JsonElement

@Serializable
data class HomeResponse(
    val type: String,
    val title: String? = null,
    val data: JsonElement,
)
