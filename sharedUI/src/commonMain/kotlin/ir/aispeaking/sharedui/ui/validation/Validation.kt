package ir.aispeaking.sharedui.validation

import ir.aispeaking.sharedui.ui.validation.ValidationStatus

interface Validation {
    fun validate(): ValidationStatus
}