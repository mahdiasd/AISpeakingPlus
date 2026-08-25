package ir.aispeaking.domain.model.data_result

import ir.aispeaking.domain.model.error.AppError
import ir.aispeaking.domain.model.paging.PagingMeta
import kotlinx.serialization.Serializable

@Serializable
sealed class DataResult<T> {
    @Serializable
    data class Success<T>(val data: T, val pagingMeta: PagingMeta? = null, val message: String = "") : DataResult<T>()

    @Serializable
    data class Failure<T>(val appError: AppError) : DataResult<T>()
}

suspend fun <T> DataResult<T>.onSuccess(
    executable: suspend (T) -> Unit
): DataResult<T> = apply {
    if (this is DataResult.Success)
        executable(this.data)
}

suspend fun <T> DataResult<T>.onFailure(
    executable: suspend (AppError) -> Unit
): DataResult<T> = apply {
    if (this is DataResult.Failure)
        executable(this.appError)
}