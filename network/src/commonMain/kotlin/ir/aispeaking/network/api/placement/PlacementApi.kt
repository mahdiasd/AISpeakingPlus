package ir.aispeaking.network.api.placement

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import ir.aispeaking.network.NetworkConfig
import ir.aispeaking.network.model.NetworkResponse
import ir.aispeaking.network.model.placement.dto.PlacementEvaluationResponseDto
import ir.aispeaking.network.model.placement.dto.PlacementQuestionsResponseDto
import ir.aispeaking.network.model.placement.dto.PlacementSubmitRequestDto
import org.koin.core.annotation.Single

@Single
class PlacementApi(
    private val client: HttpClient
) {
    private val baseUrl get() = NetworkConfig.baseUrl

    suspend fun getQuestions(): NetworkResponse<PlacementQuestionsResponseDto> {
        return client.get("$baseUrl/api/v2/placement/questions").body()
    }

    suspend fun evaluate(request: PlacementSubmitRequestDto): NetworkResponse<PlacementEvaluationResponseDto> {
        return client.post("$baseUrl/api/v2/placement/evaluate") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }
}
