package ir.speaking.feature.stage.dto

import kotlinx.serialization.Serializable

@Serializable
data class StageChatMessageDto(
    val role: String,
    val content: String
)

@Serializable
data class StageChatRequest(
    val message: String? = null,
    val history: List<StageChatMessageDto> = emptyList()
)

@Serializable
data class StageChatResponse(
    val message: String,
    val translatedMessage: String? = null,
    val audioUrl: String? = null,
    val hasGrammarError: Boolean = false,
    val correctedSentence: String? = null,
    val grammarFeedbackFa: String = "",
    val objectiveCompleted: Boolean = false,
    val finishTaskIndexes: List<Int> = emptyList()
)

