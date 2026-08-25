package ir.aispeaking.register.input

import androidx.compose.runtime.Stable
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.register_screen_input_first_name
import ir.aispeaking.sharedui.ui.extension.validate
import ir.aispeaking.sharedui.ui.model.input_fields.InputField
import ir.aispeaking.sharedui.ui.validation.validator.MaxLengthRule
import ir.aispeaking.sharedui.ui.validation.validator.NotEmptyRule
import ir.aispeaking.sharedui.ui.validation.ValidationStatus
import org.jetbrains.compose.resources.StringResource

@Stable
data class FirstName(
    override val hint: StringResource = Res.string.register_screen_input_first_name,
    override val value: String = "",
    override val shouldValidate: Boolean = false
) : InputField<String> {

    override fun validate(): ValidationStatus {
        if (!shouldValidate) return ValidationStatus.Valid
        return listOf(
            NotEmptyRule(),
            MaxLengthRule(20)
        ).validate(value)
    }
}
