package ir.aispeaking.network.api.announcement

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import ir.aispeaking.network.NetworkConfig
import ir.aispeaking.network.model.NetworkResponse
import ir.aispeaking.network.model.announcement.dto.AnnouncementDto
import org.koin.core.annotation.Single

@Single
class AnnouncementApi(
    private val client: HttpClient
) {
    private val baseUrl get() = NetworkConfig.baseUrl

    suspend fun getActiveAnnouncement(): AnnouncementDto? {
        val response: NetworkResponse<AnnouncementDto> = client.get("$baseUrl/api/announcements/active").body()
        return response.data
    }
}
