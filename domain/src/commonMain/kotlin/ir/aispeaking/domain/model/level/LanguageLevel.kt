package ir.aispeaking.domain.model.level

import kotlinx.serialization.Serializable

@Serializable
enum class LanguageLevel { A1, A2, B1, B2, C1, C2 }

fun LanguageLevel?.isInLevelGroup(languageGroupLevel: LanguageGroupLevel): Boolean {
    return when (this) {
        LanguageLevel.A1 -> (languageGroupLevel is LanguageGroupLevel.Basic)
        LanguageLevel.A2 -> (languageGroupLevel is LanguageGroupLevel.Basic)
        LanguageLevel.B1 -> (languageGroupLevel is LanguageGroupLevel.Intermediate)
        LanguageLevel.B2 -> (languageGroupLevel is LanguageGroupLevel.Intermediate)
        LanguageLevel.C1 -> (languageGroupLevel is LanguageGroupLevel.Advanced)
        LanguageLevel.C2 -> (languageGroupLevel is LanguageGroupLevel.Advanced)
        null -> false
    }
}

fun LanguageLevel.getGroup(): LanguageGroupLevel {
    return when (this) {
        LanguageLevel.A1 -> LanguageGroupLevel.Basic()
        LanguageLevel.A2 -> LanguageGroupLevel.Basic()
        LanguageLevel.B1 -> LanguageGroupLevel.Intermediate()
        LanguageLevel.B2 -> LanguageGroupLevel.Intermediate()
        LanguageLevel.C1 -> LanguageGroupLevel.Advanced()
        LanguageLevel.C2 -> LanguageGroupLevel.Advanced()
    }
}