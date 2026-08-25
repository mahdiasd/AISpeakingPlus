package ir.aispeaking.network.model

import ir.aispeaking.network.dto.paginate.PagingMetaResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NetworkResponse<T>(

    /**
     * When API call is successful:
     * - 'data' contains the response payload
     * - 'message' contains any success message
     */
    val data: T? = null,
    val message: String? = "",
    @SerialName("pagingMeta")
    val pagingMetaResponse: PagingMetaResponse? = null,

    /**
     * When API call fails:
     * - 'errorMessage' contains the error description
     * - 'errorCode' contains the specific error code
     */
    val errorMessage: String? = null,

    val errorCode: Int? = null,
)