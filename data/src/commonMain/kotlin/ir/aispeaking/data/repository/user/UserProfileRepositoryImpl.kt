package ir.aispeaking.data.repository.user

import ir.aispeaking.data.mapper.user.toDomain
import ir.aispeaking.data.mapper.user.toDto
import ir.aispeaking.data.mapper.user.toGuestDomainProfile
import ir.aispeaking.data.source.LocalGuestProgressDataSource
import ir.aispeaking.data.utils.safeCall
import ir.aispeaking.domain.model.data_result.DataResult
import ir.aispeaking.domain.model.user.UpdateProfileInput
import ir.aispeaking.domain.model.user.UserProfile
import ir.aispeaking.domain.repository.user.UserProfileRepository
import ir.aispeaking.network.api.user.UserApi
import ir.aispeaking.storage.model.user.SharedPrefUser
import ir.aispeaking.storage.preferences.token.TokenPreferences
import ir.aispeaking.storage.preferences.user.UserPreferences
import org.koin.core.annotation.Single

@Single
class UserProfileRepositoryImpl(
    private val userApi: UserApi,
    private val tokenPreferences: TokenPreferences,
    private val userPreferences: UserPreferences,
    private val guestProgressDataSource: LocalGuestProgressDataSource
) : UserProfileRepository {

    override suspend fun getUserProfile(): DataResult<UserProfile> {
        val token = tokenPreferences.read()
        if (token.isBlank()) {
            return DataResult.Success(getGuestProfile())
        }

        val result = safeCall { userApi.getProfile() }
        return when (result) {
            is DataResult.Success -> {
                val profile = result.data.toDomain()
                val localUser = userPreferences.read() ?: defaultSharedPrefUser()
                userPreferences.save(
                    localUser.copy(
                        uid = profile.id,
                        mobile = profile.phoneNumber,
                        nickName = profile.nickName,
                        firstName = profile.firstName ?: "",
                        lastName = profile.lastName ?: "",
                        avatar = profile.avatar,
                        score = profile.score
                    )
                )
                DataResult.Success(profile)
            }
            is DataResult.Failure -> {
                val cached = userPreferences.read()
                if (cached != null && cached.uid.isNotBlank()) {
                    DataResult.Success(
                        UserProfile(
                            id = cached.uid,
                            phoneNumber = cached.mobile,
                            nickName = cached.nickName,
                            firstName = cached.firstName,
                            lastName = cached.lastName,
                            avatar = cached.avatar,
                            score = cached.score,
                            totalStars = 0,
                            completedStagesCount = 0,
                            languageLevel = cached.languageLevel,
                            isGuest = false
                        )
                    )
                } else {
                    DataResult.Failure(result.appError)
                }
            }
        }
    }

    override suspend fun updateProfile(input: UpdateProfileInput): DataResult<UserProfile> {
        val token = tokenPreferences.read()
        if (token.isBlank()) {
            val currentGuest = userPreferences.read() ?: defaultSharedPrefUser()
            val updated = currentGuest.copy(
                nickName = input.nickName ?: currentGuest.nickName,
                avatar = input.avatar ?: currentGuest.avatar,
                languageLevel = input.languageLevel ?: currentGuest.languageLevel
            )
            userPreferences.save(updated)
            return DataResult.Success(getGuestProfile())
        }

        val result = safeCall { userApi.updateProfile(input.toDto()) }
        return when (result) {
            is DataResult.Success -> {
                val profile = result.data.toDomain()
                val localUser = userPreferences.read() ?: defaultSharedPrefUser()
                userPreferences.save(
                    localUser.copy(
                        nickName = profile.nickName,
                        avatar = profile.avatar,
                        firstName = profile.firstName ?: "",
                        lastName = profile.lastName ?: "",
                        languageLevel = profile.languageLevel
                    )
                )
                DataResult.Success(profile)
            }
            is DataResult.Failure -> DataResult.Failure(result.appError)
        }
    }

    override suspend fun getGuestProfile(): UserProfile {
        val guestList = guestProgressDataSource.getProgressList()
        val totalStars = guestList.sumOf { it.stars }
        val totalScore = guestList.sumOf { it.score }
        val cached = userPreferences.read() ?: defaultSharedPrefUser()
        return cached.toGuestDomainProfile(guestStars = totalStars, guestScore = totalScore)
    }

    private fun defaultSharedPrefUser() = SharedPrefUser(
        uid = "",
        nickName = "زبان‌آموز مهمان",
        firstName = "",
        lastName = "",
        mobile = "",
        gender = null,
        languageLevel = "A1",
        age = 0,
        score = 0,
        avatar = "avatar_g1"
    )
}
