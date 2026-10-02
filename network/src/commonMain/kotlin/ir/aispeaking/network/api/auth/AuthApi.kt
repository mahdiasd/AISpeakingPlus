package ir.aispeaking.network.api.auth

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import ir.aispeaking.network.BuildConfig
import ir.aispeaking.network.model.NetworkResponse
import ir.aispeaking.network.model.auth.dto.AuthResponseDto
import ir.aispeaking.network.model.auth.dto.SendOtpRequestDto
import ir.aispeaking.network.model.auth.dto.SendOtpResponseDto
import ir.aispeaking.network.model.auth.dto.UserProfileDto
import ir.aispeaking.network.model.auth.dto.VerifyOtpRequestDto
import org.koin.core.annotation.Single

@Single
class AuthApi(
    private val client: HttpClient
) {
    private val baseUrl = BuildConfig.BaseUrl

    suspend fun sendOtp(request: SendOtpRequestDto): NetworkResponse<SendOtpResponseDto> {
        return client.post("$baseUrl/api/v2/auth/otp/send") {
            setBody(request)
        }.body()
    }

    suspend fun verifyOtp(request: VerifyOtpRequestDto): NetworkResponse<AuthResponseDto> {
        return client.post("$baseUrl/api/v2/auth/otp/verify") {
            setBody(request)
        }.body()
    }

    suspend fun getMe(): NetworkResponse<UserProfileDto> {
        return client.get("$baseUrl/api/v2/auth/me").body()
    }
}
