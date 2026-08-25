package ir.aispeaking.network.api.user

import ir.aispeaking.network.platformBaseUrl

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.http.Parameters
import ir.aispeaking.network.dto.user.AuthResponse
import ir.aispeaking.network.dto.user.CreateUserRequest
import ir.aispeaking.network.dto.user.UpdateUserRequest
import ir.aispeaking.network.dto.user.UserResponse
import ir.aispeaking.network.dto.user.UserSummaryResponse
import ir.aispeaking.network.model.NetworkResponse
import org.koin.core.annotation.Single

@Single
class UserApiServiceImpl(private val httpClient: HttpClient) : UserApiService {

    override suspend fun getUser() =
        httpClient.get {
            url(platformBaseUrl() + "api/v1/user")
        }.body<NetworkResponse<UserResponse>>()

    override suspend fun sendOtp(username: String) =
        httpClient.post {
            url(platformBaseUrl() + "api/v1/user/auth/otp/send")
            setBody(FormDataContent(Parameters.build {
                append("mobile", username)
            }))
        }.body<NetworkResponse<String>>()

    override suspend fun verifyOtp(mobile: String, otpCode: String) = httpClient.post {
        url(platformBaseUrl() + "api/v1/user/auth/otp/verify")
        setBody(FormDataContent(Parameters.build {
            append("mobile", mobile)
            append("otpCode", otpCode)
        }))
    }.body<NetworkResponse<AuthResponse>>()

    override suspend fun createUser(createUserRequest: CreateUserRequest) = httpClient.post {
        url(platformBaseUrl() + "api/v1/user")
        setBody(createUserRequest)
    }.body<NetworkResponse<AuthResponse>>()

    override suspend fun updateUser(createUserRequest: UpdateUserRequest) = httpClient.put {
        url(platformBaseUrl() + "api/v1/user")
        setBody(createUserRequest)
    }.body<NetworkResponse<UserResponse>>()

    override suspend fun getUsers(search: String, page: Int, limit: Int?) =
        httpClient.get {
            url("${platformBaseUrl()}v1/users")
            parameter("search", search)
            parameter("page", page)
            parameter("limit", limit)
        }.body<NetworkResponse<List<UserSummaryResponse>>>()

    override suspend fun getTops() =
        httpClient.get {
            url("${platformBaseUrl()}v1/user/tops")
        }.body<NetworkResponse<List<UserSummaryResponse>>>()

}