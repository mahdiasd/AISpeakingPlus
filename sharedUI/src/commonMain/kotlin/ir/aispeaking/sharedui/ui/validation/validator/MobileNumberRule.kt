package ir.aispeaking.sharedui.ui.validation.validator


import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.error_mobile_format_invalid
import ir.aispeaking.sharedui.error_mobile_length_invalid
import ir.aispeaking.sharedui.error_mobile_start_invalid
import ir.aispeaking.sharedui.validation.ValidationRule
import ir.aispeaking.sharedui.ui.validation.ValidationStatus

class MobileNumberRule : ValidationRule<String> {
    override fun validate(value: String): ValidationStatus {

        return when {
            value.length != 11 -> ValidationStatus.Invalid(Res.string.error_mobile_length_invalid)

            !value.startsWith("09") -> ValidationStatus.Invalid(Res.string.error_mobile_start_invalid)

            !value.matches(Regex("^09\\d{9}$")) -> {
                ValidationStatus.Invalid(Res.string.error_mobile_format_invalid)
            }

            else -> {
                ValidationStatus.Valid
            }
        }
    }
}