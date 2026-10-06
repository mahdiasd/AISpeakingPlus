package ir.aispeaking.network.model

import kotlinx.serialization.Serializable

@Serializable
data class PagingMetaResponse(
    val totalPages: Int = 0,
    val totalItems: Long = 0,
    val page: Int = 1,
)
