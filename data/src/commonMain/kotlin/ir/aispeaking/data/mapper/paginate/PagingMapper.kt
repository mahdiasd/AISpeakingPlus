package ir.aispeaking.data.mapper.paginate

import ir.aispeaking.domain.model.paging.DefaultPageIndex
import ir.aispeaking.domain.model.paging.Paging
import ir.aispeaking.domain.model.paging.PagingMeta
import ir.aispeaking.network.dto.paginate.PagingMetaResponse
import kotlinx.collections.immutable.toImmutableList

fun PagingMetaResponse.toDomain(): PagingMeta {
    return PagingMeta(
        totalPages = totalPages,
        totalItems = totalItems,
        page = page
    )
}

fun <T> PagingMeta.toPaging(content: List<T>): Paging<T> {
    return Paging(
        content = content.toImmutableList(),
        totalPages = totalPages,
        totalItems = totalItems,
        isFirst = page == DefaultPageIndex,
        isLast = page == totalPages,
        isRefreshing = false,
        isLoadMore = false,
        apiErrorHappened = false
    )
}

