package ir.aispeaking.domain.usecase.user.auth

import ir.aispeaking.domain.model.user.RegisterParam
import ir.aispeaking.domain.repository.user.UserRepository
import org.koin.core.annotation.Single

@Single
class RegisterUserUseCase(private val userRepository: UserRepository) {
    suspend operator fun invoke(registerParam: RegisterParam) = userRepository.createUser(registerParam = registerParam)
}