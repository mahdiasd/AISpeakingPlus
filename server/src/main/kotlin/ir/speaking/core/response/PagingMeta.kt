package ir.speaking.core.response

import kotlinx.serialization.Serializable

@Serializable
data class PagingMeta(
    val totalPages: Int,

    val totalItems: Long,

    val page: Int,
)