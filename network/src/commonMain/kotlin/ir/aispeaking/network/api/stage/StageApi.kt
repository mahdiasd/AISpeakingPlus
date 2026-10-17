package ir.aispeaking.network.api.stage

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.sse.sse
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.HttpMethod
import ir.aispeaking.network.BuildConfig
import ir.aispeaking.network.NetworkConfig
import ir.aispeaking.network.model.NetworkResponse
import ir.aispeaking.network.model.stage.dto.HintRequestDto
import ir.aispeaking.network.model.stage.dto.HintResponseDto
import ir.aispeaking.network.model.stage.dto.StageCatalogDataDto
import ir.aispeaking.network.model.stage.dto.StageDetailDto
import ir.aispeaking.network.model.stage.dto.StageChatStreamEvent
import ir.aispeaking.network.model.stage.dto.StageChatStreamEventDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Single

import ir.aispeaking.network.model.stage.dto.EvaluationRequestDto
import ir.aispeaking.network.model.stage.dto.EvaluationResponseDto

@Single
class StageApi(
    private val client: HttpClient
) {
    private val baseUrl get() = NetworkConfig.baseUrl

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

    suspend fun sendStageChatMessage(stageId: String, request: ir.aispeaking.network.model.stage.dto.StageChatRequestDto): NetworkResponse<ir.aispeaking.network.model.stage.dto.StageChatResponseDto> {
        return client.post("$baseUrl/api/v2/stages/$stageId/chat") {
            setBody(request)
        }.body()
    }

    fun streamStageChatMessage(
        stageId: String,
        request: ir.aispeaking.network.model.stage.dto.StageChatRequestDto
    ): Flow<StageChatStreamEvent> = flow {
        val sseJson = Json { ignoreUnknownKeys = true }
        client.sse(
            urlString = "$baseUrl/api/v2/stages/$stageId/chat/stream",
            request = {
                method = HttpMethod.Post
                setBody(request)
            }
        ) {
            emit(StageChatStreamEvent.Connected)
            incoming.collect { event ->
                val data = event.data?.trim()
                if (!data.isNullOrEmpty()) {
                    try {
                        val eventDto = sseJson.decodeFromString<StageChatStreamEventDto>(data)
                        when (eventDto.type) {
                            "chunk" -> {
                                val delta = eventDto.delta.orEmpty()
                                if (delta.isNotEmpty()) {
                                    emit(StageChatStreamEvent.Chunk(delta))
                                }
                            }
                            "done" -> {
                                eventDto.payload?.let { payload ->
                                    emit(StageChatStreamEvent.Done(payload))
                                }
                            }
                            "error" -> {
                                emit(StageChatStreamEvent.Error(eventDto.message ?: "Server streaming error"))
                            }
                        }
                    } catch (_: Exception) {
                    }
                }
            }
        }
    }
}
