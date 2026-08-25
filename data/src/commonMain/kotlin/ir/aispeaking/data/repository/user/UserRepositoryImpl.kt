package ir.aispeaking.data.repository.user

import ir.aispeaking.data.mapper.paginate.toPaging
import ir.aispeaking.data.mapper.user.toCreateUserRequest
import ir.aispeaking.data.mapper.user.toDomain
import ir.aispeaking.data.mapper.user.toSharedPref
import ir.aispeaking.data.mapper.user.toUpdateUserRequest
import ir.aispeaking.data.utils.safeCall
import ir.aispeaking.domain.model.data_result.DataResult
import ir.aispeaking.domain.model.error.DeviceError
import ir.aispeaking.domain.model.paging.Paging
import ir.aispeaking.domain.model.user.RegisterParam
import ir.aispeaking.domain.model.user.User
import ir.aispeaking.domain.model.user.UserSummary
import ir.aispeaking.domain.repository.user.UserRepository
import ir.aispeaking.network.api.user.UserApiService
import ir.aispeaking.storage.preferences.token.TokenPreferences
import ir.aispeaking.storage.preferences.user.UserPreferences

import ir.aispeaking.utils.constant.AppConstant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.koin.core.annotation.Single

@Single
class UserRepositoryImpl(
    private val apiService: UserApiService,
    private val tokenPreferences: TokenPreferences,
    private val userPreferences: UserPreferences,
) : UserRepository {

    override suspend fun getSharedPreferencesUser(): Flow<DataResult<User>> = flow {
        val result = userPreferences.read()
        if (result == null) emit(DataResult.Failure(DeviceError.NotAuthenticated))
        else emit(DataResult.Success(result.toDomain()))
    }

    override suspend fun saveSharedUser(user: User?) {
        userPreferences.save(user?.toSharedPref())
    }

    override suspend fun getToken(): Flow<DataResult<String>> = flow {
        val result = tokenPreferences.read()
        if (result.isEmpty()) emit(DataResult.Failure(DeviceError.NotAuthenticated))
        else emit(DataResult.Success(result))
    }

    override suspend fun sendOtp(mobile: String): Flow<DataResult<String>> = flow {
        when (val result = safeCall { apiService.sendOtp(mobile) }) {
            is DataResult.Success -> emit(DataResult.Success(result.data))

            is DataResult.Failure -> emit(DataResult.Failure(result.appError))
        }
    }

    override suspend fun verifyOtp(mobile: String, otpCode: String): Flow<DataResult<User>> = flow {
        when (val result = safeCall { apiService.verifyOtp(mobile, otpCode) }) {
            is DataResult.Success -> {
                saveSharedUser(result.data.user.toDomain())
                saveTokenOnSharedPref(result.data.token)
                AppConstant.needToFetchUser = true
                emit(DataResult.Success(result.data.user.toDomain()))
            }

            is DataResult.Failure -> emit(DataResult.Failure(result.appError))
        }
    }

    override suspend fun createUser(registerParam: RegisterParam): Flow<DataResult<Pair<User, Int>>> = flow {
        when (val result = safeCall { apiService.createUser(registerParam.toCreateUserRequest()) }) {
            is DataResult.Success -> {
                saveSharedUser(result.data.user.toDomain())
                saveTokenOnSharedPref(result.data.token)
                emit(DataResult.Success(Pair(result.data.user.toDomain(), result.data.giftPurchase ?: 0)))
            }

            is DataResult.Failure -> emit(DataResult.Failure(result.appError))
        }
    }

    override suspend fun updateUser(user: User): Flow<DataResult<User>> = flow {
        when (val result = safeCall { apiService.updateUser(user.toUpdateUserRequest()) }) {
            is DataResult.Success -> {
                AppConstant.needToFetchUser = true
                saveSharedUser(result.data.toDomain())
                emit(DataResult.Success(result.data.toDomain()))
            }

            is DataResult.Failure -> emit(DataResult.Failure(result.appError))
        }
    }

    override suspend fun getServerUser(): Flow<DataResult<User>> = flow {
        when (val result = safeCall { apiService.getUser() }) {
            is DataResult.Success -> {
                AppConstant.needToFetchUser = false
                saveSharedUser(result.data.toDomain())
                emit(DataResult.Success(result.data.toDomain()))
            }

            is DataResult.Failure -> emit(DataResult.Failure(result.appError))
        }
    }

    override suspend fun getUsers(
        search: String,
        page: Int,
        limit: Int?,
    ): Flow<DataResult<Paging<UserSummary>>> = flow {
        when (val result =
            safeCall { apiService.getUsers(search = search, page = page, limit = limit) }) {
            is DataResult.Success -> {
                result.pagingMeta?.let { pagingMeta ->
                    emit(DataResult.Success(pagingMeta.toPaging(result.data.map { it.toDomain() })))
                }
                    ?: emit(DataResult.Failure(DeviceError.MetadataNotFound))
            }

            is DataResult.Failure -> {
                emit(DataResult.Failure(result.appError))
            }
        }
    }


    override suspend fun getTops(): Flow<DataResult<List<UserSummary>>> = flow {
        when (val result =
            safeCall { apiService.getTops() }) {
            is DataResult.Success -> emit(DataResult.Success(result.data.map { it.toDomain() }))

            is DataResult.Failure -> emit(DataResult.Failure(result.appError))
        }
    }

    private fun saveTokenOnSharedPref(accessToken: String) {
        tokenPreferences.save(accessToken)
    }
}