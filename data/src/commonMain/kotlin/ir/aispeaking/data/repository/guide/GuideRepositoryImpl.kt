package ir.aispeaking.data.repository.guide

import ir.aispeaking.data.mapper.guide.toDomain
import ir.aispeaking.domain.model.guide.GuideCompletionStatus
import ir.aispeaking.domain.repository.guide.GuideRepository
import ir.aispeaking.storage.preferences.guide.GuidePreferences
import org.koin.core.annotation.Single

@Single
class GuideRepositoryImpl(
    private val preferences: GuidePreferences,
) : GuideRepository {
    override suspend fun getGuidesCompletionStatus(): GuideCompletionStatus {
        return preferences.read().toDomain()
    }

    override suspend fun setRegister() {
        preferences.saveRegisterGuideModel(true)
    }

    override suspend fun setChallenge() {
        preferences.saveChallengeGuideModel(true)
    }

    override suspend fun setLightener() {
        preferences.saveLightenerGuideModel(true)
    }

    override suspend fun setScenario() {
        preferences.saveScenarioGuideModel(true)
    }

    override suspend fun setRoadmap() {
        preferences.saveRoadmapGuideModel(true)
    }

    override suspend fun setChat() {
        preferences.saveChatGuideModel(true)
    }

    override suspend fun setProfile() {
        preferences.saveProfileGuideModel(true)
    }
}
