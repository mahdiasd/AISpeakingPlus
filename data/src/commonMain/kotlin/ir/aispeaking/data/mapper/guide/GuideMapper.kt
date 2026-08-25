package ir.aispeaking.data.mapper.guide

import ir.aispeaking.domain.model.guide.GuideCompletionStatus
import ir.aispeaking.storage.model.guide.SharedGuide

fun SharedGuide.toDomain(): GuideCompletionStatus {
    return GuideCompletionStatus(
        register = register,
        challenge = challenge,
        lightener = lightener,
        scenario = scenario,
        roadmap = roadmap,
        chat = chat,
        profile = profile
    )
}