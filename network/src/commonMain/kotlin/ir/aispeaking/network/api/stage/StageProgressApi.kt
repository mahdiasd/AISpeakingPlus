package ir.aispeaking.network.api.stage

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import ir.aispeaking.network.BuildConfig
import ir.aispeaking.network.NetworkConfig
import ir.aispeaking.network.model.NetworkResponse
import ir.aispeaking.network.model.stage.dto.SyncProgressRequestDto
import ir.aispeaking.network.model.stage.dto.SyncProgressResponseDto
import org.koin.core.annotation.Single

@Single
class StageProgressApi(
    private val client: HttpClient
) {
    private val baseUrl get() = NetworkConfig.baseUrl

    suspend fun syncProgress(request: SyncProgressRequestDto): NetworkResponse<SyncProgressResponseDto> {
        return client.post("$baseUrl/api/v2/progress/sync") {
            setBody(request)
        }.body()
    }
}
