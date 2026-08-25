package ir.aispeaking.sharedui.ui.model.input_fields

import ir.aispeaking.sharedui.validation.Validation
import ir.aispeaking.sharedui.ui.validation.ValidationStatus
import org.jetbrains.compose.resources.StringResource

interface InputField<T> : Validation {
    val value: T
    val hint: StringResource
    val shouldValidate: Boolean
    override fun validate(): ValidationStatus
}

