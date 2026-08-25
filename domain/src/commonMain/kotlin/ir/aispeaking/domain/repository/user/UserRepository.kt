package ir.aispeaking.domain.repository.user

import ir.aispeaking.domain.model.data_result.DataResult
import ir.aispeaking.domain.model.paging.Paging
import ir.aispeaking.domain.model.user.RegisterParam
import ir.aispeaking.domain.model.user.User
import ir.aispeaking.domain.model.user.UserSummary
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun getSharedPreferencesUser(): Flow<DataResult<User>>
    suspend fun saveSharedUser(user: User?)

    suspend fun getToken(): Flow<DataResult<String>>

    suspend fun sendOtp(mobile: String): Flow<DataResult<String>>

    suspend fun verifyOtp(mobile: String, otpCode: String): Flow<DataResult<User>>

    suspend fun createUser(registerParam: RegisterParam): Flow<DataResult<Pair<User, Int>>>

    suspend fun updateUser(user: User): Flow<DataResult<User>>

    suspend fun getUsers(
        search: String,
        page: Int,
        limit: Int?
    ): Flow<DataResult<Paging<UserSummary>>>

    suspend fun getTops(): Flow<DataResult<List<UserSummary>>>

    suspend fun getServerUser(): Flow<DataResult<User>>
}