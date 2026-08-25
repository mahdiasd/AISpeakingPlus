package ir.aispeaking.domain.model.level

import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.Serializable

@Serializable
sealed class LanguageGroupLevel(
    open val selected: Boolean,
    open val haveAccess: Boolean
) {
    data class Basic(
        override val selected: Boolean = false,
        override val haveAccess: Boolean = false,
    ) : LanguageGroupLevel(selected = selected, haveAccess = haveAccess)

    data class Intermediate(
        override val selected: Boolean = false,
        override val haveAccess: Boolean = false,
    ) : LanguageGroupLevel(selected = selected, haveAccess = haveAccess)

    data class Advanced(
        override val selected: Boolean = false,
        override val haveAccess: Boolean = false,
    ) : LanguageGroupLevel(selected = selected, haveAccess = haveAccess)

    companion object {
        val entries = listOf(
            LanguageGroupLevel.Basic(selected = false, haveAccess = false),
            LanguageGroupLevel.Intermediate(selected = false, haveAccess = false),
            LanguageGroupLevel.Advanced(selected = false, haveAccess = false),
        ).toImmutableList()
    }
}

fun LanguageGroupLevel.copy(
    selected: Boolean = this.selected,
    haveAccess: Boolean = this.haveAccess,
): LanguageGroupLevel {
   return when (this) {
        is LanguageGroupLevel.Advanced -> this.copy(selected = selected, haveAccess = haveAccess)
        is LanguageGroupLevel.Basic -> this.copy(selected = selected, haveAccess = haveAccess)
        is LanguageGroupLevel.Intermediate -> this.copy(selected = selected, haveAccess = haveAccess)
    }
}