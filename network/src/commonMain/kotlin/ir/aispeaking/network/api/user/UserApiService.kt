package ir.aispeaking.network.api.user

import ir.aispeaking.network.dto.user.AuthResponse
import ir.aispeaking.network.dto.user.CreateUserRequest
import ir.aispeaking.network.dto.user.UpdateUserRequest
import ir.aispeaking.network.dto.user.UserResponse
import ir.aispeaking.network.dto.user.UserSummaryResponse
import ir.aispeaking.network.model.NetworkResponse


interface UserApiService {
    suspend fun getUser(): NetworkResponse<UserResponse>

    suspend fun sendOtp(username: String): NetworkResponse<String>

    suspend fun verifyOtp(mobile: String, otpCode: String): NetworkResponse<AuthResponse>

    suspend fun createUser(createUserRequest: CreateUserRequest): NetworkResponse<AuthResponse>

    suspend fun updateUser(createUserRequest: UpdateUserRequest): NetworkResponse<UserResponse>

    suspend fun getUsers(
        search: String,
        page: Int,
        limit: Int?
    ): NetworkResponse<List<UserSummaryResponse>>

    suspend fun getTops(): NetworkResponse<List<UserSummaryResponse>>
}