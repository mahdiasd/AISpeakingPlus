package ir.aispeaking.domain.repository.guide

import ir.aispeaking.domain.model.guide.GuideCompletionStatus // Assuming GuideSaved is in this package

interface GuideRepository {
    /**
     * Retrieves the saved state of all guides.
     */
    suspend fun getGuidesCompletionStatus(): GuideCompletionStatus

    suspend fun setRegister()
    suspend fun setChallenge()
    suspend fun setLightener()
    suspend fun setScenario()
    suspend fun setRoadmap()
    suspend fun setChat()
    suspend fun setProfile()
}
