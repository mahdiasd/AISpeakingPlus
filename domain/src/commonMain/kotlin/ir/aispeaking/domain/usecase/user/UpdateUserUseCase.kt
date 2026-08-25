package ir.aispeaking.domain.usecase.user

import ir.aispeaking.domain.model.user.User
import ir.aispeaking.domain.repository.user.UserRepository
import org.koin.core.annotation.Single

@Single
class UpdateUserUseCase(private val userRepository: UserRepository) {
    suspend operator fun invoke(user: User) = userRepository.updateUser(user)
}