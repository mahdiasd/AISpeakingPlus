package ir.aispeaking.storage.preferences.token

import com.russhwolf.settings.Settings
import ir.aispeaking.storage.SharedKeyConstant
import org.koin.core.annotation.Single

@Single
class TokenPreferencesImpl(private val settings: Settings) : TokenPreferences {

    override fun save(token: String) {
        settings.putString(SharedKeyConstant.TOKEN, token)
    }

    override fun read(): String {
        return settings.getString(SharedKeyConstant.TOKEN, "")
    }
}
