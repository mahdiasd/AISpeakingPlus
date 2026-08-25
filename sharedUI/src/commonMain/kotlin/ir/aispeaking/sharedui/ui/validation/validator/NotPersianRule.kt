package ir.aispeaking.sharedui.validation.validator;


import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.error_persian_characters_not_support
import ir.aispeaking.sharedui.ui.extension.isPersian
import ir.aispeaking.sharedui.validation.ValidationRule
import ir.aispeaking.sharedui.ui.validation.ValidationStatus


class NotPersianRule : ValidationRule<String> {
    override fun validate(value: String): ValidationStatus {
        return if (value.isPersian()) ValidationStatus.Invalid(Res.string.error_persian_characters_not_support)
        else ValidationStatus.Valid
    }
}