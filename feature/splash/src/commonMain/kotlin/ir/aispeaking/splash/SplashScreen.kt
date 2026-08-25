package ir.aispeaking.splash

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import ir.aispeaking.domain.model.config.UpdateState
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.ic_logo
import ir.aispeaking.sharedui.not_now
import ir.aispeaking.sharedui.syncing_data_from_server
import ir.aispeaking.sharedui.update
import ir.aispeaking.sharedui.update_available
import ir.aispeaking.sharedui.ui.core.dialog.MessageDialog
import ir.aispeaking.sharedui.ui.core.error.ErrorContent
import ir.aispeaking.sharedui.ui.core.text.BodyMediumText
import ir.aispeaking.sharedui.ui.core.ui_message.UiMessageScreen
import ir.aispeaking.sharedui.ui.extension.baseModifier
import ir.aispeaking.sharedui.ui.them.AppTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel


@Composable
fun SplashScreen(
    vm: SplashViewModel = koinViewModel(),
    navigateToAuth: () -> Unit,
    navigateToMain: () -> Unit,
    navigateToOnboarding: () -> Unit,
) {
    val uiState = vm.uiState.collectAsState().value

    AnimatedContent(uiState.splashState) { splashState ->
        when (splashState) {
            SplashState.FetchingData -> {
                NewSplashAnimation(
                    modifier = Modifier
                        .baseModifier(0.dp)
                        .padding(vertical = 16.dp),
                )
            }

            SplashState.FetchingError -> {
                ErrorContent(
                    modifier = Modifier.baseModifier(),
                    onRetry = { vm.onTriggerEvent(SplashUiEvent.OnRefreshClick) }
                )
            }

            is SplashState.DataFetched -> {
                // TODO: Pass actual version code for KMP
                if (splashState.update.getState(0) !is UpdateState.UpToDate) {
                    MessageDialog(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                AppTheme.colors.surfaceContainerLow,
                                shape = AppTheme.shapes.roundMedium
                            )
                            .padding(16.dp),
                        title = stringResource(Res.string.update_available),
                        message = splashState.update.message,
                        properties = DialogProperties(
                            dismissOnBackPress = false,
                            dismissOnClickOutside = false
                        ),
                        positiveText = Res.string.update,
                        negativeText = if (splashState.update.getState(0) is UpdateState.UpdateRecommended) Res.string.not_now else null,
                        onPositive = {
                            vm.onTriggerEvent(SplashUiEvent.UpdateApp)
                        },
                        onNegative = {
                            vm.onTriggerEvent(SplashUiEvent.MoveToMain)
                        },
                        onDismiss = {

                        },
                    )
                }
            }
        }
    }


    UiMessageScreen(shared = vm.uiMessage)

    LaunchedEffect(Unit) {
        vm.uiNavigation.collectLatest {
            when (it) {
                SplashUiNavigation.ToAuth -> navigateToAuth()
                SplashUiNavigation.ToMain -> navigateToMain()
                SplashUiNavigation.ToOnboarding -> navigateToOnboarding()
            }
        }
    }
}


@Composable
fun NewSplashAnimation(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(Modifier)

        Image(
            painter = painterResource(Res.drawable.ic_logo),
            contentDescription = "logo",
            modifier = Modifier
                .size(150.dp)
        )

        AnimatedEllipsisText()
    }
}


@Composable
fun AnimatedEllipsisText(
    dotCount: Int = 3,
    intervalMillis: Long = 400L,
) {
    var dots by remember { mutableIntStateOf(0) }
    val baseText = stringResource(Res.string.syncing_data_from_server)
    val syncingText by remember(dots) {
        mutableStateOf(baseText + ".".repeat(dots) + " ".repeat(dotCount - dots))
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(intervalMillis)
            dots = (dots + 1) % (dotCount + 1)
        }
    }

    BodyMediumText(
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center,
        text = syncingText
    )
}
