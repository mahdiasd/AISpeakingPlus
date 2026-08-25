package ir.aispeaking.chat.input

import androidx.compose.runtime.Stable
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.chat_screen_message_hint
import ir.aispeaking.sharedui.ui.extension.validate
import ir.aispeaking.sharedui.ui.model.input_fields.InputField
import ir.aispeaking.sharedui.ui.validation.ValidationStatus
import ir.aispeaking.sharedui.ui.validation.validator.NotEmptyRule
import ir.aispeaking.sharedui.validation.validator.NotPersianRule
import org.jetbrains.compose.resources.StringResource

@Stable
data class Message(
    override val hint: StringResource = Res.string.chat_screen_message_hint,
    override val value: String = "",
    override val shouldValidate: Boolean = false
) : InputField<String> {

    override fun validate(): ValidationStatus {
        if (!shouldValidate) return ValidationStatus.Valid
        return listOf(
            NotEmptyRule(),
            NotPersianRule(),
        ).validate(value)
    }
}

