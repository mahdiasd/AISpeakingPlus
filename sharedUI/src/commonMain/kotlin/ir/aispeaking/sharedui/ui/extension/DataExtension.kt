package ir.aispeaking.sharedui.ui.extension


import ir.aispeaking.domain.model.level.LanguageGroupLevel
import ir.aispeaking.domain.model.level.LanguageLevel
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.language_level_a1
import ir.aispeaking.sharedui.language_level_a1_desc
import ir.aispeaking.sharedui.language_level_a2
import ir.aispeaking.sharedui.language_level_a2_desc
import ir.aispeaking.sharedui.language_level_b1
import ir.aispeaking.sharedui.language_level_b1_desc
import ir.aispeaking.sharedui.language_level_b2
import ir.aispeaking.sharedui.language_level_b2_desc
import ir.aispeaking.sharedui.language_level_c1
import ir.aispeaking.sharedui.language_level_c1_desc
import ir.aispeaking.sharedui.language_level_c2
import ir.aispeaking.sharedui.language_level_c2_desc
import ir.aispeaking.sharedui.language_level_group_advanced
import ir.aispeaking.sharedui.language_level_group_basic
import ir.aispeaking.sharedui.language_level_group_intermediate
import org.jetbrains.compose.resources.StringResource

fun LanguageLevel.name(): StringResource {
    return when (this) {
        LanguageLevel.A1 -> Res.string.language_level_a1
        LanguageLevel.A2 -> Res.string.language_level_a2
        LanguageLevel.B1 -> Res.string.language_level_b1
        LanguageLevel.B2 -> Res.string.language_level_b2
        LanguageLevel.C1 -> Res.string.language_level_c1
        LanguageLevel.C2 -> Res.string.language_level_c2
    }
}

fun LanguageLevel.descriptionToPersian(): StringResource {
    return when (this) {
        LanguageLevel.A1 -> Res.string.language_level_a1_desc
        LanguageLevel.A2 -> Res.string.language_level_a2_desc
        LanguageLevel.B1 -> Res.string.language_level_b1_desc
        LanguageLevel.B2 -> Res.string.language_level_b2_desc
        LanguageLevel.C1 -> Res.string.language_level_c1_desc
        LanguageLevel.C2 -> Res.string.language_level_c2_desc
    }
}

fun LanguageGroupLevel.name(): StringResource {
    return when (this) {
        is LanguageGroupLevel.Basic -> Res.string.language_level_group_basic
        is LanguageGroupLevel.Intermediate -> Res.string.language_level_group_intermediate
        is LanguageGroupLevel.Advanced -> Res.string.language_level_group_advanced
    }
}
