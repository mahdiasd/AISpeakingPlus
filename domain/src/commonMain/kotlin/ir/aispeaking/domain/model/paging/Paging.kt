package ir.aispeaking.domain.model.paging

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

const val DefaultPageIndex = 1

data class Paging<T>(
    val content: ImmutableList<T>,
    val page: Int = DefaultPageIndex,

    val totalPages: Int = 0,
    val totalItems: Long = 0,
    val pageItemCount: Int = 0,

    val isFirst: Boolean = false,
    val isLast: Boolean = false,


    /* ↓ ------------ For ui --------------- ↓*/
    val isRefreshing: Boolean = false,
    val isLoadMore: Boolean = false,
    val apiErrorHappened: Boolean = false
)


fun <T> Paging<T>.addMore(paging: Paging<T>): Paging<T> {
    val newContent =
        if (this.page == DefaultPageIndex) paging.content else this.content.toMutableSet() + paging.content.toMutableSet()

    return paging
        .copy(
            content = newContent.toImmutableList(),
            isLoadMore = false,
            isRefreshing = false
        )
}


/**
 * Prepares the Paging instance for loading the next page by incrementing the page number
 * and setting the `isLoadMore` flag.
 *
 * @return A new Paging instance for the next page.
 */
fun <T> Paging<T>.nextPage(): Paging<T> {
    return copy(apiErrorHappened = false, isRefreshing = false, isLoadMore = true, page = page + 1)
}


/**
 * Resets the Paging instance to the first page and sets the `isRefreshing` flag.
 *
 * @return A new Paging instance for the first page.
 */
fun <T> Paging<T>.firstPage(): Paging<T> {
    return copy(
        apiErrorHappened = false,
        isRefreshing = true,
        isLoadMore = false,
        page = DefaultPageIndex
    )
}

/**
 * Handles an error during paging by resetting flags and decrementing the page if needed.
 *
 * @return A new Paging instance with the error state updated.
 */
fun <T> Paging<T>.errorHappened(): Paging<T> {
    return copy(
        isRefreshing = false,
        isLoadMore = false,
        page = if (page > DefaultPageIndex) page - 1 else page,
        apiErrorHappened = true
    )
}