package ir.aispeaking.domain.model.guide

data class GuideCompletionStatus(
    val register: Boolean,
    val challenge: Boolean,
    val lightener: Boolean,
    val scenario: Boolean,
    val roadmap: Boolean,
    val chat: Boolean,
    val profile: Boolean,
)
