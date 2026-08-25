package ir.aispeaking.sharedui.ui.core.dialog.translate

import androidx.compose.runtime.Stable
import ir.aispeaking.domain.model.translate.Translation
import ir.aispeaking.sharedui.*
import ir.aispeaking.sharedui.viewmodel.UiEvent
import ir.aispeaking.sharedui.viewmodel.UiNavigation

import ir.aispeaking.sharedui.viewmodel.UiState

@Stable
data class TranslateUiState(
    val translateLoading: Boolean = false,
    val addLightenerLoading: Boolean = false,
    val translation: Translation? = null,
    val isSaved: Boolean = false,
) : UiState

sealed class TranslateUiNavigation : UiNavigation {
    data object ToDismiss : TranslateUiNavigation()
}


sealed class TranslateUiEvent : UiEvent {
    data object AddToLightener : TranslateUiEvent()
    data class Translate(val text: String) : TranslateUiEvent()
}
