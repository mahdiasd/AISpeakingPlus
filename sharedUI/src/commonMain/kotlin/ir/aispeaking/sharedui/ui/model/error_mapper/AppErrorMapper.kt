package ir.aispeaking.sharedui.ui.model.error_mapper

import ir.aispeaking.domain.model.error.AppError
import ir.aispeaking.domain.model.error.DeviceError
import ir.aispeaking.domain.model.error.NetworkError
import ir.aispeaking.sharedui.ui.model.ui_message.MessageType
import ir.aispeaking.sharedui.ui.model.ui_message.UiMessage

fun AppError.toUiMessage(): UiMessage {
    return when (this) {
        is DeviceError -> {
            UiMessage(
                messageType = MessageType.Device,
                stringValue = this.message
            )
        }

        is NetworkError -> {
            UiMessage(
                messageType = MessageType.Network,
                stringValue = this.message
            )
        }
    }
}