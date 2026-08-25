package ir.aispeaking.storage.preferences.scenario

import com.russhwolf.settings.Settings
import ir.aispeaking.storage.SharedKeyConstant
import ir.aispeaking.storage.model.scenario.SharedScenario
import ir.aispeaking.utils.fromJson
import ir.aispeaking.utils.toJson
import org.koin.core.annotation.Single

@Single
class ScenarioPreferencesImpl(private val settings: Settings) : ScenarioPreferences {

    override fun save(user: SharedScenario) {
        settings.putString(SharedKeyConstant.SCENARIO, user.toJson() ?: "")
    }

    override fun read(): SharedScenario? {
        return settings.getStringOrNull(SharedKeyConstant.SCENARIO).fromJson<SharedScenario>()
    }
}
