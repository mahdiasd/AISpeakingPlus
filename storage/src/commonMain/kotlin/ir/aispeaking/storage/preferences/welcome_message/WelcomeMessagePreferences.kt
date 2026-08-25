package ir.aispeaking.storage.preferences.welcome_message

import ir.aispeaking.storage.model.welcome_message.SharedWelcomeMessage

interface WelcomeMessagePreferences {
    fun save(value: SharedWelcomeMessage?)

    fun read(): SharedWelcomeMessage?
}
