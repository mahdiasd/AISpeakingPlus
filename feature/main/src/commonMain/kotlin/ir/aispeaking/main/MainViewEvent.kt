package ir.aispeaking.main

import androidx.compose.runtime.Stable
import ir.aispeaking.domain.model.user.User
import ir.aispeaking.main.model.BottomBarItem
import ir.aispeaking.sharedui.permission.PostNotificationPermission
import ir.aispeaking.sharedui.ui.extension.immutableListOf
import ir.aispeaking.sharedui.ui.model.theme_mode.ThemeMode
import ir.aispeaking.sharedui.ui.permission.AppPermission
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
    val themeMode: ThemeMode = ThemeMode.System(),
    val user: User? = null,
    val level: Int = 3,
    val currentXp: Int = 750,
    val maxXp: Int = 1000,
    val streakDays: Int = 5,
    val gemsCount: Int = 120,
    val activeLevelTitle: String = "مرحله ۴",
    val speechText: String = "سلام علی! آماده‌ای برای ماجراجویی مرحله ۴؟",
) : UiState


sealed class MainUiEvent : UiEvent {
    data object CheckTheme : MainUiEvent()
    data object FetchUser : MainUiEvent()
    data class OnChangeTab(val bottomBarItem: BottomBarItem) : MainUiEvent()
    data class ShowExitAppDialog(val show: Boolean) : MainUiEvent()
}


sealed class MainUiNavigation : UiNavigation {
    data class ToPage(val bottomBarItem: BottomBarItem) : MainUiNavigation()
}

typealias OnAction = (MainUiEvent) -> Unit