package ir.aispeaking.storage.preferences.guide

import ir.aispeaking.storage.model.guide.SharedGuide

interface GuidePreferences {
    fun saveRegisterGuideModel(value: Boolean)
    fun saveChallengeGuideModel(value: Boolean)

    fun saveLightenerGuideModel(value: Boolean)

    fun saveScenarioGuideModel(value: Boolean)

    fun saveRoadmapGuideModel(value: Boolean)

    fun saveChatGuideModel(value: Boolean)

    fun saveProfileGuideModel(value: Boolean)
    fun read(): SharedGuide
}
