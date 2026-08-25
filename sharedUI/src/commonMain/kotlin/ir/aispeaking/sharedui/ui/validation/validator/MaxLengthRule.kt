package ir.aispeaking.sharedui.ui.validation.validator


import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.error_max_length
import ir.aispeaking.sharedui.validation.ValidationRule
import ir.aispeaking.sharedui.ui.validation.ValidationStatus

class MaxLengthRule(private val max: Int) : ValidationRule<String> {
    override fun validate(value: String): ValidationStatus {
        return if (value.length > max) ValidationStatus.Invalid(Res.string.error_max_length)
        else ValidationStatus.Valid
    }
}