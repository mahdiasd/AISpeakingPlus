package ir.aispeaking.sharedui.ui.model.ui_message

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

enum class MessageType { Network, Device }
enum class MessageStatus { Success, Failure }

sealed class UiMessageContent {
    // Keep the name "IntMessage" to prevent breaking changes in the app,
    // but change the underlying type to Compose Multiplatform's StringResource
    data class IntMessage(val value: StringResource) : UiMessageContent()
    data class StringMessage(val value: String) : UiMessageContent()
}

/**
 * Extension function to resolve the message content cleanly within a Composable.
 * Removed Android Context to support Compose Multiplatform.
 */
@Composable
fun UiMessageContent.getMessage(): String {
    return when (this) {
        is UiMessageContent.IntMessage -> stringResource(this.value)
        is UiMessageContent.StringMessage -> this.value
    }
}

data class UiMessage(
    val messageType: MessageType,
    val status: MessageStatus,
    val content: UiMessageContent
) {
    // Keep the parameter name "intValue" so ViewModels using named arguments
    // e.g., UiMessage(intValue = Res.string.error_translation) won't break.
    constructor(
        messageType: MessageType = MessageType.Network,
        status: MessageStatus = MessageStatus.Failure,
        intValue: StringResource
    ) : this(
        messageType = messageType,
        status = status,
        content = UiMessageContent.IntMessage(intValue)
    )

    constructor(
        messageType: MessageType = MessageType.Network,
        status: MessageStatus = MessageStatus.Failure,
        stringValue: String
    ) : this(
        messageType = messageType,
        status = status,
        content = UiMessageContent.StringMessage(stringValue)
    )
}