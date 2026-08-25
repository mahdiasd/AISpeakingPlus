package ir.aispeaking.domain.usecase.user

import ir.aispeaking.domain.repository.user.UserRepository
import org.koin.core.annotation.Single

@Single
class GetTopUsersUseCase(private val userRepository: UserRepository) {
    suspend operator fun invoke() = userRepository.getTops()
}