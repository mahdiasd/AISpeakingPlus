package ir.aispeaking.domain.repository.user

import ir.aispeaking.domain.model.data_result.DataResult
import ir.aispeaking.domain.model.user.UpdateProfileInput
import ir.aispeaking.domain.model.user.UserProfile

interface UserProfileRepository {
    suspend fun getUserProfile(): DataResult<UserProfile>
    suspend fun updateProfile(input: UpdateProfileInput): DataResult<UserProfile>
    suspend fun getGuestProfile(): UserProfile
}
