package ir.aispeaking.data.repository.config

import ir.aispeaking.data.mapper.config.toDomain
import ir.aispeaking.data.mapper.config.toRequest
import ir.aispeaking.data.mapper.welcome_message.toSharedPref
import ir.aispeaking.data.utils.safeCall
import ir.aispeaking.domain.model.config.ConfigRequest
import ir.aispeaking.domain.model.data_result.DataResult
import ir.aispeaking.domain.repository.config.ConfigRepository
import ir.aispeaking.domain.repository.user.UserRepository
import ir.aispeaking.network.api.config.ConfigApiService
import ir.aispeaking.storage.preferences.token.TokenPreferences
import ir.aispeaking.storage.preferences.welcome_message.WelcomeMessagePreferences
import kotlinx.coroutines.flow.flow
import org.koin.core.annotation.Single

@Single
class ConfigRepositoryImpl(
    private val configApiService: ConfigApiService,
    private val tokenPreferences: TokenPreferences,
    private val userRepository: UserRepository,
    private val welcomeMessagePreferences: WelcomeMessagePreferences
) : ConfigRepository {

    override suspend fun getConfig(configRequest: ConfigRequest) = flow {
        when (val result = safeCall { configApiService.getConfig(configRequest.toRequest()) }) {
            is DataResult.Success -> {
                emit(DataResult.Success(result.data.toDomain()))
                if (!result.data.tokenAlive) {
                    tokenPreferences.save("")
                    userRepository.saveSharedUser(null)
                }

                if (result.data.welcomeMessage != null) {
                    val localMessage = welcomeMessagePreferences.read()
                    if (localMessage?.id != result.data.welcomeMessage?.id) {
                        welcomeMessagePreferences.save(result.data.welcomeMessage!!.toSharedPref())
                    }
                }
            }

            is DataResult.Failure -> emit(DataResult.Failure(result.appError))
        }
    }

}