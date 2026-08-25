package ir.aispeaking.sharedui.ui.validation.validator


import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.error_no_digit_format
import ir.aispeaking.sharedui.validation.ValidationRule
import ir.aispeaking.sharedui.ui.validation.ValidationStatus


class IsDigitRule : ValidationRule<String> {
    override fun validate(value: String): ValidationStatus {
        value.forEach {
            if (!it.isDigit()) return ValidationStatus.Invalid(Res.string.error_no_digit_format)
        }
        return ValidationStatus.Valid
    }
}