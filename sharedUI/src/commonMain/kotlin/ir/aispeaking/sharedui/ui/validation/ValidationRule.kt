package ir.aispeaking.sharedui.validation

import ir.aispeaking.sharedui.ui.validation.ValidationStatus

interface ValidationRule<T> {
    fun validate(value: T): ValidationStatus
}