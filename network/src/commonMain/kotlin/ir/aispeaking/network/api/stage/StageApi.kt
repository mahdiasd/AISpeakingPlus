package ir.aispeaking.network.api.stage

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import ir.aispeaking.network.BuildConfig
import ir.aispeaking.network.model.NetworkResponse
import ir.aispeaking.network.model.stage.dto.HintRequestDto
import ir.aispeaking.network.model.stage.dto.HintResponseDto
import ir.aispeaking.network.model.stage.dto.StageCatalogDataDto
import ir.aispeaking.network.model.stage.dto.StageDetailDto
import org.koin.core.annotation.Single

import ir.aispeaking.network.model.stage.dto.EvaluationRequestDto
import ir.aispeaking.network.model.stage.dto.EvaluationResponseDto

@Single
class StageApi(
    private val client: HttpClient
) {
    private val baseUrl = BuildConfig.BaseUrl

    suspend fun getStages(): NetworkResponse<StageCatalogDataDto> {
        return client.get("$baseUrl/api/v2/stages").body()
    }

    suspend fun getStageDetail(stageId: String): NetworkResponse<StageDetailDto> {
        return client.get("$baseUrl/api/v2/stages/$stageId").body()
    }

    suspend fun getHint(stageId: String, request: HintRequestDto): NetworkResponse<HintResponseDto> {
        return client.post("$baseUrl/api/v2/stages/$stageId/hint") {
            setBody(request)
        }.body()
    }

    suspend fun submitEvaluation(stageId: String, request: EvaluationRequestDto): NetworkResponse<EvaluationResponseDto> {
        return client.post("$baseUrl/api/v2/stages/$stageId/evaluate") {
            setBody(request)
        }.body()
    }
}
