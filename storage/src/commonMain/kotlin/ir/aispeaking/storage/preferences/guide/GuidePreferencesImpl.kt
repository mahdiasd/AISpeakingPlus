package ir.aispeaking.storage.preferences.guide

import com.russhwolf.settings.Settings
import ir.aispeaking.storage.SharedKeyConstant
import org.koin.core.annotation.Single

@Single
class GuidePreferencesImpl(
    private val settings: Settings
) : GuidePreferences {

    override fun isGuideCompleted(screenKey: String): Boolean {
        val key = "${SharedKeyConstant.GUIDE_COMPLETED_PREFIX}$screenKey"
        return settings.getBoolean(key, false)
    }

    override fun markGuideCompleted(screenKey: String) {
        val key = "${SharedKeyConstant.GUIDE_COMPLETED_PREFIX}$screenKey"
        settings.putBoolean(key, true)
    }

    override fun resetGuide(screenKey: String) {
        val key = "${SharedKeyConstant.GUIDE_COMPLETED_PREFIX}$screenKey"
        settings.remove(key)
    }

    override fun resetAllGuides() {
        listOf("journey_map", "chat", "stages_list", "profile").forEach { key ->
            resetGuide(key)
        }
    }
}
