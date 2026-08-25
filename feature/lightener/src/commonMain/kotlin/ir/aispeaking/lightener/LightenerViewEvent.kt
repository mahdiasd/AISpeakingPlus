package ir.aispeaking.lightener

import androidx.compose.runtime.Stable
import ir.aispeaking.domain.model.paging.Paging
import ir.aispeaking.domain.model.translate.Translation
import ir.aispeaking.domain.model.user.User
import ir.aispeaking.sharedui.ui.extension.immutableListOf
import ir.aispeaking.sharedui.viewmodel.UiEvent
import ir.aispeaking.sharedui.viewmodel.UiNavigation
import ir.aispeaking.sharedui.viewmodel.UiState

@Stable
data class LightenerUiState(
    val searchText: String = "",

    val isExpandedSearch: Boolean = false,

    val translationPaging: Paging<Translation> = Paging(immutableListOf(), isRefreshing = true),

    val deletingId: String = "",

    val deleteDialogUid: String? = null,

    var user: User? = null

) : UiState

sealed class LightenerUiEvent : UiEvent {
    data object OnBtnClick : LightenerUiEvent()
    data object CheckForFetching : LightenerUiEvent()

    data object OnRefresh : LightenerUiEvent()
    data object OnLoadMore : LightenerUiEvent()

    data object OnSearchIconClick : LightenerUiEvent()
    data object OnShowAllTranslations : LightenerUiEvent()
    data class OnSearchTextChange(val text: String) : LightenerUiEvent()

    data class OnChangeTranslation(val translation: Translation) : LightenerUiEvent()

    data class DeleteTranslation(val uid: String) : LightenerUiEvent()
    data class OnShowDeleteDialog(val uid: String?) : LightenerUiEvent()
}

sealed class LightenerUiNavigation : UiNavigation {
    data object ToBack : LightenerUiNavigation()
}

typealias OnAction = (LightenerUiEvent) -> Unit

