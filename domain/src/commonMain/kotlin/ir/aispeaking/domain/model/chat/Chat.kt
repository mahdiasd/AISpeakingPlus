package ir.aispeaking.domain.model.chat

import kotlinx.collections.immutable.ImmutableList

sealed class Chat(
    open val uid: String,
) {
    data class Ai(
        override val uid: String,
        val message: String,
        val translatedMessage: String? = null,
        val voiceState: AiVoiceState = AiVoiceState.Stopped,
        val audioUrl: String? = null,
        val objectiveCompleted: Boolean = false,
        val finishTaskIndexes: ImmutableList<Int>? = null,
        val suggests: ImmutableList<String>? = null,
        val fetchingSuggest: Boolean = false,
    ) : Chat(uid = uid)

    data class User(
        override val uid: String,
        val message: String,
        val status: ChatStatus = ChatStatus.Sending,
    ) : Chat(uid = uid)

    data object WaitingForAi : Chat("waiting_for_ai")
}

sealed class AiVoiceState {
    data object Stopped : AiVoiceState()
    data object PendingToPlay : AiVoiceState()
    data object Playing : AiVoiceState()
}

sealed class ChatStatus {
    data object Failed : ChatStatus()
    data object Sending : ChatStatus()
    data class Answered(
        val grammar: String,
        val correctedSentence: String? = null,
        val hasGrammarError: Boolean = grammar.isNotBlank()
    ) : ChatStatus()
}

data class StageChatTurnResult(
    val message: String,
    val translatedMessage: String? = null,
    val audioUrl: String? = null,
    val hasGrammarError: Boolean = false,
    val correctedSentence: String? = null,
    val grammarFeedbackFa: String = "",
    val objectiveCompleted: Boolean = false,
    val finishTaskIndexes: List<Int> = emptyList()
)

