package ir.aispeaking.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import ir.aispeaking.main.component.BottomBarItemScreen
import ir.aispeaking.main.model.BottomBarItem
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.are_you_sure_to_exit_the_app
import ir.aispeaking.sharedui.exit
import ir.aispeaking.sharedui.exit_app_dialog_title
import ir.aispeaking.sharedui.not_now
import ir.aispeaking.sharedui.ui.core.dialog.MessageDialog
import ir.aispeaking.sharedui.ui.core.permission.PerPermissionScreen
import ir.aispeaking.sharedui.ui.core.ui_message.UiMessageScreen
import ir.aispeaking.sharedui.ui.extension.baseModifier
import ir.aispeaking.sharedui.ui.model.theme_mode.ThemeMode
import ir.aispeaking.sharedui.ui.remember.rememberPermissionGrant
import ir.aispeaking.sharedui.ui.them.AppTheme
import kotlinx.collections.immutable.toImmutableList
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun MainScreen(
    vm: MainViewModel = koinViewModel(),
    navHost: @Composable () -> Unit,
    navigateToPage: (BottomBarItem) -> Unit,
) {
    val uiState = vm.uiState.collectAsState().value
    val uiNavigation by vm.uiNavigation.collectAsState(null)
    val permissionGrant = rememberPermissionGrant(uiState.permissions)
    var userDeniedPermission by rememberSaveable { mutableStateOf(false) }

    val navState = rememberNavigationEventState(
        currentInfo = NavigationEventInfo.None,
    )

    NavigationBackHandler(
        state = navState,
        isBackEnabled = !uiState.showExitDialog,
        onBackCompleted = {
            vm.onTriggerEvent(MainUiEvent.ShowExitAppDialog(true))
        },
    )

    MainScreenContent(
        modifier = Modifier.fillMaxSize(),
        bottomBarItems = uiState.items,
        navHost = navHost,
        currentBottomBar = uiState.currentBottomBarItem,
        themeMode = uiState.themeMode,
        onAction = { vm.onTriggerEvent(it) }
    )

    UiMessageScreen(shared = vm.uiMessage)

    LaunchedEffect(uiNavigation) {
        when (uiNavigation) {
            is MainUiNavigation.ToPage -> navigateToPage((uiNavigation as MainUiNavigation.ToPage).bottomBarItem)
        }
    }

    if (uiState.showExitDialog) {
        MessageDialog(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppTheme.colors.surfaceContainerLow, AppTheme.shapes.roundMedium)
                .padding(16.dp),
            title = stringResource(Res.string.exit_app_dialog_title),
            message = stringResource(Res.string.are_you_sure_to_exit_the_app),
            positiveText = Res.string.not_now,
            negativeText = Res.string.exit,
            onPositive = {
                vm.onTriggerEvent(MainUiEvent.ShowExitAppDialog(false))
            },
            onNegative = {
                // TODO: Platform specific - Call exit/close instead of activity.finishAffinity()
            },
            onDismiss = {
                vm.onTriggerEvent(MainUiEvent.ShowExitAppDialog(false))
            }
        )
    }


    if (!permissionGrant && !userDeniedPermission) {
        PerPermissionScreen(
            permissions = uiState.permissions,
            onDismissDialog = {
                userDeniedPermission = true
            }
        )
    }

}

@Composable
fun MainScreenContent(
    modifier: Modifier = Modifier,
    bottomBarItems: List<BottomBarItem>,
    navHost: @Composable () -> Unit,
    themeMode: ThemeMode = ThemeMode.System(),
    currentBottomBar: BottomBarItem = BottomBarItem.Scenarios(),
    onAction: OnAction,
) {
    Scaffold(
        modifier = modifier,
        bottomBar = {
            BottomBarItemScreen(
                bottomBarItems = bottomBarItems.toImmutableList(),
                currentBottomBar = currentBottomBar,
                themeMode = themeMode,
                onAction = onAction,
            )
        }
    ) {
        Box(
            Modifier
                .background(AppTheme.colors.surface)
                .padding(it)
        ) {
            navHost()
        }
    }
}