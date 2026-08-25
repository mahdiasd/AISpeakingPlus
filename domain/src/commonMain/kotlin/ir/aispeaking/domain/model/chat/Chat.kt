package ir.aispeaking.domain.model.chat

import kotlinx.collections.immutable.ImmutableList
import kotlin.uuid.Uuid


sealed class Chat(
    open val uid: Uuid,
) {
    data class Ai(
        override val uid: Uuid = Uuid.random(),
        val message: String,
        val translatedMessage: String? = null,
        val voiceState: AiVoiceState,
        val grammar: String = "",
        val finishTaskIndexes: ImmutableList<Int>,
        val suggests: ImmutableList<String>? = null,
        val fetchingSuggest: Boolean = false,
        val audioUrl: String? = null,
    ) : Chat(uid = uid)

    data class User(
        override val uid: Uuid = Uuid.random(),
        val message: String,
        val status: ChatStatus = ChatStatus.Sending,
    ) : Chat(uid = uid)

    data object WaitingForAi : Chat(Uuid.random())
}

sealed class AiVoiceState {
    data object Stopped : AiVoiceState()
    data object PendingToPlay : AiVoiceState()
    data object Playing : AiVoiceState()
}
