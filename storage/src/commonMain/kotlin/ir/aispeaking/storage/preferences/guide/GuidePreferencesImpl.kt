package ir.aispeaking.storage.preferences.guide

import com.russhwolf.settings.Settings
import ir.aispeaking.storage.SharedKeyConstant
import ir.aispeaking.storage.model.guide.SharedGuide
import ir.aispeaking.utils.fromJson
import ir.aispeaking.utils.toJson
import org.koin.core.annotation.Single

@Single
class GuidePreferencesImpl(private val settings: Settings) : GuidePreferences {
    override fun read(): SharedGuide {
        return settings.getStringOrNull(SharedKeyConstant.GUIDE).fromJson<SharedGuide>() ?: SharedGuide()
    }

    override fun saveRegisterGuideModel(value: Boolean) {
        val guide = read()
        settings.putString(SharedKeyConstant.GUIDE, guide.copy(register = true).toJson() ?: "")
    }

    override fun saveChallengeGuideModel(value: Boolean) {
        val guide = read()
        settings.putString(SharedKeyConstant.GUIDE, guide.copy(challenge = true).toJson() ?: "")
    }

    override fun saveLightenerGuideModel(value: Boolean) {
        val guide = read()
        settings.putString(SharedKeyConstant.GUIDE, guide.copy(lightener = true).toJson() ?: "")
    }

    override fun saveScenarioGuideModel(value: Boolean) {
        val guide = read()
        settings.putString(SharedKeyConstant.GUIDE, guide.copy(scenario = true).toJson() ?: "")
    }

    override fun saveRoadmapGuideModel(value: Boolean) {
        val guide = read()
        settings.putString(SharedKeyConstant.GUIDE, guide.copy(roadmap = true).toJson() ?: "")
    }

    override fun saveChatGuideModel(value: Boolean) {
        val guide = read()
        settings.putString(SharedKeyConstant.GUIDE, guide.copy(chat = true).toJson() ?: "")
    }

    override fun saveProfileGuideModel(value: Boolean) {
        val guide = read()
        settings.putString(SharedKeyConstant.GUIDE, guide.copy(profile = true).toJson() ?: "")
    }
}
