package ir.aispeaking.sharedui.ui.validation

import org.jetbrains.compose.resources.StringResource

sealed class ValidationStatus {
    data object Valid : ValidationStatus()
    data class Invalid( val errorMessage: StringResource) : ValidationStatus()
}

fun ValidationStatus.errorMessage(): StringResource? {
    return when (this) {
        is ValidationStatus.Invalid -> this.errorMessage
        ValidationStatus.Valid -> null
    }
}