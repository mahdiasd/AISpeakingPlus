package ir.aispeaking.domain.usecase.user

import ir.aispeaking.domain.model.data_result.DataResult
import ir.aispeaking.domain.model.user.UpdateProfileInput
import ir.aispeaking.domain.model.user.UserProfile
import ir.aispeaking.domain.repository.user.UserProfileRepository
import org.koin.core.annotation.Factory

@Factory
class UpdateUserProfileUseCase(
    private val repository: UserProfileRepository
) {
    suspend operator fun invoke(input: UpdateProfileInput): DataResult<UserProfile> {
        return repository.updateProfile(input)
    }
}
