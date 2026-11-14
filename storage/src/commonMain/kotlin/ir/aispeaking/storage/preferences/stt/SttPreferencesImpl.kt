package ir.aispeaking.storage.preferences.stt

import com.russhwolf.settings.Settings
import ir.aispeaking.storage.SharedKeyConstant
import org.koin.core.annotation.Single

@Single
class SttPreferencesImpl(private val settings: Settings) : SttPreferences {

    override fun saveSttEngine(engine: String) {
        settings.putString(SharedKeyConstant.STT_ENGINE, engine)
    }

    override fun getSttEngine(): String {
        return settings.getString(SharedKeyConstant.STT_ENGINE, "app")
    }
}
