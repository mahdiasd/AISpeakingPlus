package ir.aispeaking.data.repository.guide

import ir.aispeaking.domain.repository.guide.GuideRepository
import ir.aispeaking.storage.preferences.guide.GuidePreferences
import org.koin.core.annotation.Single

@Single
class GuideRepositoryImpl(
    private val preferences: GuidePreferences
) : GuideRepository {

    override fun isGuideCompleted(screenKey: String): Boolean {
        return preferences.isGuideCompleted(screenKey)
    }

    override fun markGuideCompleted(screenKey: String) {
        preferences.markGuideCompleted(screenKey)
    }

    override fun resetGuide(screenKey: String) {
        preferences.resetGuide(screenKey)
    }

    override fun resetAllGuides() {
        preferences.resetAllGuides()
    }
}
