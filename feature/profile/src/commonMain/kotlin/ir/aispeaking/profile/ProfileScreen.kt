package ir.aispeaking.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ir.aispeaking.domain.fake_data.FakeData
import ir.aispeaking.domain.model.purchase.Purchase
import ir.aispeaking.domain.model.user.User
import ir.aispeaking.profile.component.DataSection
import ir.aispeaking.profile.component.InfoSection
import ir.aispeaking.profile.component.PurchaseSection
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.app_theme
import ir.aispeaking.sharedui.exit
import ir.aispeaking.sharedui.exit_dialog_message
import ir.aispeaking.sharedui.exit_dialog_title
import ir.aispeaking.sharedui.guide
import ir.aispeaking.sharedui.ic_arrow_left
import ir.aispeaking.sharedui.ic_exit
import ir.aispeaking.sharedui.ic_guide
import ir.aispeaking.sharedui.ic_history
import ir.aispeaking.sharedui.ic_support
import ir.aispeaking.sharedui.ic_theme
import ir.aispeaking.sharedui.log_out
import ir.aispeaking.sharedui.not_now
import ir.aispeaking.sharedui.payment_history
import ir.aispeaking.sharedui.support
import ir.aispeaking.sharedui.ui.core.dialog.MessageDialog
import ir.aispeaking.sharedui.ui.core.dialog.theme.ThemeDialog
import ir.aispeaking.sharedui.ui.core.guide.GuideDialog
import ir.aispeaking.sharedui.ui.core.icon.AppIcon
import ir.aispeaking.sharedui.ui.core.loading.PageLoading
import ir.aispeaking.sharedui.ui.core.text.BodyMediumText
import ir.aispeaking.sharedui.ui.core.ui_message.UiMessageScreen
import ir.aispeaking.sharedui.ui.core.unauthorized.UnauthorizedContent
import ir.aispeaking.sharedui.ui.extension.LightDarkPreview
import ir.aispeaking.sharedui.ui.extension.animateClickable
import ir.aispeaking.sharedui.ui.extension.baseModifier
import ir.aispeaking.sharedui.ui.extension.immutableListOf
import ir.aispeaking.sharedui.ui.them.AppTheme
import ir.aispeaking.sharedui.utils.lifecycle.OnResume
import kotlinx.collections.immutable.ImmutableList
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ProfileScreen(
    vm: ProfileViewModel = koinViewModel(),
    navigateBack: () -> Unit,
    navigateToLogin: () -> Unit,
    navigatePurchase: () -> Unit,
    navigatePaymentHistory: () -> Unit,
    navigateEditProfile: () -> Unit,
) {
    val uiState = vm.uiState.collectAsState().value
    val uiNavigation by vm.uiNavigation.collectAsStateWithLifecycle(null)

    OnResume {
        vm.onTriggerEvent(ProfileUiEvent.FetchUser)
    }

    if (uiState.isFetchingUser) {
        PageLoading(modifier = Modifier.baseModifier())
    } else if (uiState.user != null) {
        ProfileScreenContent(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 8.dp)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            user = uiState.user,
            isFetchingPurchases = uiState.isFetchingPurchases,
            subscriptions = uiState.purchases,
            subscriptionFetchingError = uiState.subscriptionFetchingError,
            onAction = { vm.onTriggerEvent(it) }
        )
    } else {
        UnauthorizedContent(
            modifier = Modifier.baseModifier(),
            navigateToLogin = { vm.onTriggerEvent(ProfileUiEvent.NavigateToLogin) }
        )
    }

    UiMessageScreen(shared = vm.uiMessage)

    if (uiState.showThemeDialog) {
        ThemeDialog(
            onDismiss = {
                vm.onTriggerEvent(ProfileUiEvent.ShowThemeDialog(false))
            },
        )
    }

    if (uiState.showExitDialog) {
        MessageDialog(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppTheme.colors.surfaceContainerLow, shape = AppTheme.shapes.roundMedium)
                .padding(16.dp),
            title = stringResource(Res.string.exit_dialog_title),
            message = stringResource(Res.string.exit_dialog_message),
            positiveText = Res.string.not_now,
            negativeText = Res.string.exit,
            onPositive = {
                vm.onTriggerEvent(ProfileUiEvent.ShowExitDialog(false))
            },
            onNegative = {
                vm.onTriggerEvent(ProfileUiEvent.OnExit)
            },
            onDismiss = {
                vm.onTriggerEvent(ProfileUiEvent.ShowExitDialog(false))
            }
        )
    }

    if (uiState.guideList.isNotEmpty()) {
        GuideDialog(
            modifier = Modifier.fillMaxWidth(),
            onDismiss = { vm.onTriggerEvent(ProfileUiEvent.GuideTour(false)) },
            list = uiState.guideList
        )
    }

    LaunchedEffect(uiNavigation) {
        when (uiNavigation) {
            is ProfileUiNavigation.ToBack -> navigateBack()
            is ProfileUiNavigation.ToLogin -> navigateToLogin()
            is ProfileUiNavigation.ToPurchase -> navigatePurchase()
            is ProfileUiNavigation.ToPurchaseHistory -> navigatePaymentHistory()
            is ProfileUiNavigation.ToEditProfile -> navigateEditProfile()
        }
    }
}


@Composable
fun ProfileScreenContent(
    modifier: Modifier = Modifier,
    user: User,
    subscriptions: ImmutableList<Purchase>? = null,
    isFetchingPurchases: Boolean,
    subscriptionFetchingError: Boolean = false,
    onAction: OnAction,
) {

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp, alignment = Alignment.CenterVertically)
    ) {
        InfoSection(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppTheme.colors.surfaceContainerLow, shape = AppTheme.shapes.roundMedium)
                .border(width = 1.dp, color = AppTheme.colors.outline, shape = AppTheme.shapes.roundMedium)
                .padding(12.dp),
            user = user,
            onAction = onAction
        )

        DataSection(
            modifier = Modifier.fillMaxWidth(),
            user = user,
            onAction = onAction
        )

        PurchaseSection(
            modifier = Modifier.fillMaxWidth(),
            subscriptions = subscriptions,
            isFetching = isFetchingPurchases,
            isErrorFetching = subscriptionFetchingError,
            onAction = onAction,
        )

        ProfileItem(
            modifier = Modifier
                .fillMaxWidth()
                .animateClickable {
                    onAction(ProfileUiEvent.NavigateToPurchaseHistory)
                },
            brush = Brush.horizontalGradient(immutableListOf(AppTheme.colors.surfaceContainerLow, AppTheme.colors.surfaceContainerLow)),
            icon = Res.drawable.ic_history,
            title = Res.string.payment_history,
        )

        ProfileItem(
            modifier = Modifier
                .animateClickable {
                    onAction(ProfileUiEvent.ShowThemeDialog(true))
                }
                .fillMaxWidth(),
            brush = Brush.horizontalGradient(immutableListOf(AppTheme.colors.surfaceContainerLow, AppTheme.colors.surfaceContainerLow)),
            icon = Res.drawable.ic_theme,
            title = Res.string.app_theme,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(
                8.dp,
                alignment = Alignment.CenterHorizontally
            )
        ) {
            ProfileItem(
                modifier = Modifier
                    .weight(1f)
                    .animateClickable {
                        onAction(ProfileUiEvent.Support)
                    }
                    .fillMaxWidth(),
                brush = Brush.horizontalGradient(immutableListOf(AppTheme.colors.surfaceContainerLow, AppTheme.colors.surfaceContainerLow)),
                icon = Res.drawable.ic_support,
                title = Res.string.support,
            )
            ProfileItem(
                modifier = Modifier
                    .weight(1f)
                    .animateClickable {
                        onAction(ProfileUiEvent.GuideTour(true))
                    }
                    .fillMaxWidth(),
                brush = Brush.horizontalGradient(immutableListOf(AppTheme.colors.surfaceContainerLow, AppTheme.colors.surfaceContainerLow)),
                icon = Res.drawable.ic_guide,
                title = Res.string.guide,
            )
        }


        ProfileItem(
            modifier = Modifier
                .fillMaxWidth()
                .animateClickable { onAction(ProfileUiEvent.ShowExitDialog(true)) },
            brush = Brush.linearGradient(
                immutableListOf(
                    AppTheme.colors.surfaceContainerLow,
                    AppTheme.colors.error.copy(alpha = 0.2f),
                    AppTheme.colors.error
                )
            ),
            borderColor = AppTheme.colors.error,
            icon = Res.drawable.ic_exit,
            title = Res.string.log_out
        )

    }
}

@Composable
private fun ProfileItem(
    modifier: Modifier = Modifier,
    brush: Brush,
    icon: DrawableResource,
    title: StringResource,
    borderColor: Color = AppTheme.colors.outline,
) {
    Row(
        modifier = modifier
            .background(
                brush = brush,
                shape = AppTheme.shapes.roundMedium
            )
            .border(1.dp, color = borderColor, shape = AppTheme.shapes.roundMedium)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp, alignment = Alignment.CenterHorizontally)
    ) {
        AppIcon(
            icon = icon
        )

        BodyMediumText(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            text = stringResource(title)
        )

        AppIcon(
            modifier = Modifier.rotate(180f),
            size = 16.dp,
            icon = Res.drawable.ic_arrow_left
        )
    }
}

@LightDarkPreview
@Composable
private fun ProfilePreview() {
    AppTheme {
        ProfileScreenContent(
            modifier = Modifier.baseModifier(),
            user = FakeData.provideUsers().first(), onAction = {}, isFetchingPurchases = false
        )
    }
}

