package ir.aispeaking.storage.preferences.welcome_message

import com.russhwolf.settings.Settings
import ir.aispeaking.storage.SharedKeyConstant
import ir.aispeaking.storage.model.welcome_message.SharedWelcomeMessage
import ir.aispeaking.utils.dLog
import ir.aispeaking.utils.fromJson
import ir.aispeaking.utils.toJson
import org.koin.core.annotation.Single

@Single
class WelcomeMessagePreferencesImpl(private val settings: Settings) : WelcomeMessagePreferences {

    override fun save(value: SharedWelcomeMessage?) {
        settings.putString(SharedKeyConstant.WELCOME_MESSAGE, value.toJson() ?: "")
        value.dLog("Welcome Message")
    }

    override fun read(): SharedWelcomeMessage? {
        return settings.getStringOrNull(SharedKeyConstant.WELCOME_MESSAGE).fromJson<SharedWelcomeMessage>()
    }
}
