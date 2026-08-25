package ir.aispeaking.sharedui.validation.validator;


import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.error_space_detect
import ir.aispeaking.sharedui.ui.extension.isSpaceDetect
import ir.aispeaking.sharedui.validation.ValidationRule
import ir.aispeaking.sharedui.ui.validation.ValidationStatus

class NoSpacesRule : ValidationRule<String> {
    override fun validate(value: String): ValidationStatus {
        return if (value.isSpaceDetect()) ValidationStatus.Invalid(Res.string.error_space_detect)
        else ValidationStatus.Valid
    }
}