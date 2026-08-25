package ir.aispeaking.domain.repository.welcome_message

import ir.aispeaking.domain.model.config.WelcomeMessage

interface WelcomeMessageRepository {

    suspend fun read(): WelcomeMessage?

    suspend fun save(value: WelcomeMessage?)
}
