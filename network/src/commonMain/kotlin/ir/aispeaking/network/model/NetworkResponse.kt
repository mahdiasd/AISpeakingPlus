package ir.aispeaking.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NetworkResponse<T>(
    val data: T? = null,
    val message: String? = "",
    @SerialName("pagingMeta")
    val pagingMetaResponse: PagingMetaResponse? = null,
    val errorMessage: String? = null,
    val errorCode: Int? = null,
)