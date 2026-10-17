package ir.aispeaking.network.model.stage.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StageChatStreamDonePayloadDto(
    @SerialName("message") val message: String = "",
    @SerialName("translated_message") val translatedMessage: String? = null,
    @SerialName("has_grammar_error") val hasGrammarError: Boolean = false,
    @SerialName("corrected_sentence") val correctedSentence: String? = null,
    @SerialName("grammar_feedback_fa") val grammarFeedbackFa: String = "",
    @SerialName("objective_completed") val objectiveCompleted: Boolean = false,
    @SerialName("audio_url") val audioUrl: String? = null,
    @SerialName("finish_task_indexes") val finishTaskIndexes: List<Int> = emptyList()
)

@Serializable
data class StageChatStreamEventDto(
    val type: String,
    val delta: String? = null,
    val payload: StageChatStreamDonePayloadDto? = null,
    val message: String? = null
)

sealed class StageChatStreamEvent {
    data object Connected : StageChatStreamEvent()
    data class Chunk(val delta: String) : StageChatStreamEvent()
    data class Done(val payload: StageChatStreamDonePayloadDto) : StageChatStreamEvent()
    data class Error(val message: String) : StageChatStreamEvent()
}
