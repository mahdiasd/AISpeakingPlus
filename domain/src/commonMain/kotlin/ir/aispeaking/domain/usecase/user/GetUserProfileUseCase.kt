package ir.aispeaking.domain.usecase.user

import ir.aispeaking.domain.model.data_result.DataResult
import ir.aispeaking.domain.model.user.UserProfile
import ir.aispeaking.domain.repository.user.UserProfileRepository
import org.koin.core.annotation.Factory

@Factory
class GetUserProfileUseCase(
    private val repository: UserProfileRepository
) {
    suspend operator fun invoke(): DataResult<UserProfile> {
        return repository.getUserProfile()
    }
}
