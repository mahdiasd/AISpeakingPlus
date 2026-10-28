package ir.aispeaking.storage.preferences.guide

interface GuidePreferences {
    fun isGuideCompleted(screenKey: String): Boolean
    fun markGuideCompleted(screenKey: String)
    fun resetGuide(screenKey: String)
    fun resetAllGuides()
}
