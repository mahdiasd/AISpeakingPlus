package ir.aispeaking.data.repository.welcome_message

import ir.aispeaking.data.mapper.welcome_message.toDomain
import ir.aispeaking.data.mapper.welcome_message.toSharedPref
import ir.aispeaking.domain.model.config.WelcomeMessage
import ir.aispeaking.domain.repository.welcome_message.WelcomeMessageRepository
import ir.aispeaking.storage.preferences.welcome_message.WelcomeMessagePreferences
import org.koin.core.annotation.Single

@Single
class WelcomeMessageRepositoryImpl(
    private val preferences: WelcomeMessagePreferences,
) : WelcomeMessageRepository {
    override suspend fun read(): WelcomeMessage? {
        return preferences.read()?.toDomain()
    }

    override suspend fun save(value: WelcomeMessage?) {
        preferences.save(value?.toSharedPref())
    }

}
