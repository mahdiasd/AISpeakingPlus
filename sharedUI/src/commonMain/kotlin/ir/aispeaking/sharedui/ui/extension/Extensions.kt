package ir.aispeaking.sharedui.ui.extension


import ir.aispeaking.sharedui.validation.ValidationRule
import ir.aispeaking.sharedui.ui.validation.ValidationStatus
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableSet


fun <T> immutableListOf(): ImmutableList<T> = listOf<T>().toImmutableList()

fun <T> immutableListOf(vararg elements: T): ImmutableList<T> = elements.toList().toImmutableList()


fun <T> immutableSetOf(): ImmutableSet<T> = listOf<T>().toImmutableSet()

fun <T> immutableSetOf(vararg elements: T): ImmutableSet<T> = elements.toList().toImmutableSet()



/**
 * Validates a given value against a list of validation rules.
 *
 * @param value The value to validate.
 * @return The validation status. If any rule returns an Invalid status, the function
 * immediately returns that status. Otherwise, if all rules return Valid, the function
 * returns Valid.
 */
fun <T> List<ValidationRule<T>>.validate(value: T): ValidationStatus {
    for (rule in this) {
        val result = rule.validate(value)
        if (result is ValidationStatus.Invalid) return result
    }
    return ValidationStatus.Valid
}

