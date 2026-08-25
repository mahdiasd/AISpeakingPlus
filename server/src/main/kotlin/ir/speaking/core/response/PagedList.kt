package ir.speaking.core.response

import kotlinx.serialization.Serializable

@Serializable
data class PagedList<T>(
    val pagingMeta: PagingMeta,
    val items: List<T>
)
