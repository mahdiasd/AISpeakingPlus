package ir.aispeaking.network.dto.paginate

import kotlinx.serialization.Serializable

@Serializable
data class PagingMetaResponse(
    val totalPages: Int,

    val totalItems: Long,

    val page: Int,
)