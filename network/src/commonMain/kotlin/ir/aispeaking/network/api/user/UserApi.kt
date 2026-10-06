package ir.aispeaking.network.api.user

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import ir.aispeaking.network.BuildConfig
import ir.aispeaking.network.model.NetworkResponse
import ir.aispeaking.network.model.user.dto.UpdateProfileRequestDto
import ir.aispeaking.network.model.user.dto.UserProfileResponseDto
import org.koin.core.annotation.Single

@Single
class UserApi(
    private val client: HttpClient
) {
    private val baseUrl = BuildConfig.BaseUrl

    suspend fun getProfile(): NetworkResponse<UserProfileResponseDto> {
        return client.get("$baseUrl/api/v2/user/profile").body()
    }

    suspend fun updateProfile(request: UpdateProfileRequestDto): NetworkResponse<UserProfileResponseDto> {
        return client.put("$baseUrl/api/v2/user/profile") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }
}
