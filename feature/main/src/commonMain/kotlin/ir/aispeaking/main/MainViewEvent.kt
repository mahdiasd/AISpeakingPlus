package ir.aispeaking.main

import androidx.compose.runtime.Stable
import ir.aispeaking.main.model.BottomBarItem
import ir.aispeaking.sharedui.ui.extension.immutableListOf
import ir.aispeaking.sharedui.ui.model.theme_mode.ThemeMode
import ir.aispeaking.sharedui.ui.permission.AppPermission
import ir.aispeaking.sharedui.permission.PostNotificationPermission
import ir.aispeaking.sharedui.viewmodel.UiEvent
import ir.aispeaking.sharedui.viewmodel.UiNavigation
import ir.aispeaking.sharedui.viewmodel.UiState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

@Stable
data class MainUiState(
    val isLoading: Boolean = true,
    val items: ImmutableList<BottomBarItem> = BottomBarItem.entries.toImmutableList(),
    val currentBottomBarItem: BottomBarItem = BottomBarItem.Scenarios(),
    val showExitDialog: Boolean = false,
    val permissions: ImmutableList<AppPermission> = immutableListOf(PostNotificationPermission()),
    val themeMode: ThemeMode = ThemeMode.System()
) : UiState


sealed class MainUiEvent : UiEvent {
    data object CheckTheme : MainUiEvent()
    data class OnChangeTab(val bottomBarItem: BottomBarItem) : MainUiEvent()
    data class ShowExitAppDialog(val show: Boolean) : MainUiEvent()
}


sealed class MainUiNavigation : UiNavigation {
    data class ToPage(val bottomBarItem: BottomBarItem) : MainUiNavigation()
}

typealias OnAction = (MainUiEvent) -> Unit