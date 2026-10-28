package ir.aispeaking.domain.repository.guide

interface GuideRepository {
    fun isGuideCompleted(screenKey: String): Boolean
    fun markGuideCompleted(screenKey: String)
    fun resetGuide(screenKey: String)
    fun resetAllGuides()
}
