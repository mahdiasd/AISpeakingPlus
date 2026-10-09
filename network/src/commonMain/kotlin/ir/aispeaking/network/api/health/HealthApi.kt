package ir.aispeaking.network.api.health

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import ir.aispeaking.network.BuildConfig
import ir.aispeaking.network.NetworkConfig
import ir.aispeaking.network.model.health.dto.HealthResponseDto
import org.koin.core.annotation.Single

@Single
class HealthApi(
    private val client: HttpClient
) {
    private val baseUrl get() = NetworkConfig.baseUrl

    suspend fun getHealth(): HealthResponseDto {
        return client.get("$baseUrl/health").body()
    }
}
