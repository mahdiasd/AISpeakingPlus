package ir.aispeaking.storage.preferences.user

import com.russhwolf.settings.Settings
import ir.aispeaking.storage.SharedKeyConstant
import ir.aispeaking.storage.model.user.SharedPrefUser
import ir.aispeaking.utils.fromJson
import ir.aispeaking.utils.toJson
import org.koin.core.annotation.Single

@Single
class UserPreferencesImpl(private val settings: Settings) : UserPreferences {

    override fun save(user: SharedPrefUser?) {
        settings.putString(SharedKeyConstant.USER, user.toJson() ?: "")
    }

    override fun read(): SharedPrefUser? {
        return settings.getStringOrNull(SharedKeyConstant.USER).fromJson<SharedPrefUser>()
    }

    override fun hasSeenIntroVideo(): Boolean {
        return settings.getBoolean(SharedKeyConstant.HAS_SEEN_INTRO_VIDEO, false)
    }

    override fun setHasSeenIntroVideo(seen: Boolean) {
        settings.putBoolean(SharedKeyConstant.HAS_SEEN_INTRO_VIDEO, seen)
    }
}
