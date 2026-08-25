package ir.aispeaking.storage.preferences.firebase

import com.russhwolf.settings.Settings
import ir.aispeaking.storage.SharedKeyConstant
import org.koin.core.annotation.Single

@Single
class FirebaseTokenPreferencesImpl(private val settings: Settings) : FirebaseTokenPreferences {

    override fun save(value: String) {
        settings.putString(SharedKeyConstant.FIREBASE_TOKEN, value)
    }

    override fun read(): String {
        return settings.getString(SharedKeyConstant.FIREBASE_TOKEN, "")
    }
}
