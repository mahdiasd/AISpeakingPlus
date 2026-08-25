package ir.aispeaking.data.mapper.welcome_message

import ir.aispeaking.domain.model.config.WelcomeMessage
import ir.aispeaking.network.dto.config.WelcomeMessageResponse
import ir.aispeaking.storage.model.welcome_message.SharedWelcomeMessage

fun WelcomeMessageResponse.toDomain(): WelcomeMessage {
    return WelcomeMessage(
        id = this.id,
        message = this.message,
        isReadByUser = this.isReadByUser,
        imageUrl = this.imageUrl,
        title = this.title
    )
}

fun WelcomeMessageResponse.toSharedPref(): SharedWelcomeMessage {
    return SharedWelcomeMessage(
        id = this.id,
        message = this.message,
        isReadByUser = this.isReadByUser,
        imageUrl = this.imageUrl,
        title = this.title
    )
}

fun WelcomeMessage.toSharedPref(): SharedWelcomeMessage {
    return SharedWelcomeMessage(
        id = this.id,
        message = this.message,
        isReadByUser = this.isReadByUser,
        imageUrl = this.imageUrl,
        title = this.title
    )
}


fun SharedWelcomeMessage.toDomain(): WelcomeMessage {
    return WelcomeMessage(
        id = this.id,
        message = this.message,
        isReadByUser = this.isReadByUser,
        imageUrl = this.imageUrl,
        title = this.title,
    )
}

