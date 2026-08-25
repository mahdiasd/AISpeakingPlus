package ir.aispeaking.onboarding

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ir.aispeaking.onboarding.component.OnboardingBottomBar
import ir.aispeaking.onboarding.component.OnboardingItem
import ir.aispeaking.sharedui.ui.core.indicator.PageIndicator
import ir.aispeaking.sharedui.ui.core.text.BodyMediumBoldText
import ir.aispeaking.sharedui.ui.core.ui_message.UiMessageScreen
import ir.aispeaking.sharedui.ui.extension.animateClickable
import ir.aispeaking.sharedui.ui.them.AppTheme
import kotlinx.collections.immutable.ImmutableList
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun OnboardingScreen(
    vm: OnboardingViewModel = koinViewModel(),
    navigateToMain: () -> Unit,
) {
    val uiState = vm.uiState.collectAsState().value
    val uiNavigation by vm.uiNavigation.collectAsStateWithLifecycle(null)

    OnboardingScreenContent(
        modifier = Modifier.fillMaxSize(),
        onboardingPages = uiState.onboardingPages,
        selectedPage = uiState.selectedPage,
        onAction = { vm.onTriggerEvent(it) }
    )

    UiMessageScreen(shared = vm.uiMessage)

    LaunchedEffect(uiNavigation) {
        when (uiNavigation) {
            is OnboardingUiNavigation.ToMain -> navigateToMain()
        }
    }
}


@Composable
fun OnboardingScreenContent(
    modifier: Modifier = Modifier,
    onboardingPages: ImmutableList<OnboardingPage>,
    selectedPage: OnboardingPage,
    onAction: OnAction,
) {
    val pagerState = rememberPagerState(pageCount = { onboardingPages.size })

    LaunchedEffect(pagerState.currentPage) {
        onAction(OnboardingUiEvent.OnChangePage(onboardingPages[pagerState.currentPage]))
    }

    LaunchedEffect(selectedPage) {
        if (onboardingPages[pagerState.currentPage] != selectedPage)
            pagerState.animateScrollToPage(onboardingPages.indexOf(selectedPage))
    }

    val animateContainerColor by animateColorAsState(
        targetValue = when (selectedPage) {
            OnboardingPage.First -> selectedPage.containerColor
            OnboardingPage.Second -> selectedPage.containerColor
            OnboardingPage.Third -> selectedPage.containerColor
        },
        label = "containerColorOfPage"
    )

    Scaffold(
        modifier = modifier,
        containerColor = animateContainerColor,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .statusBarsPadding(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(Modifier)
                BodyMediumBoldText(
                    modifier = Modifier.animateClickable { onAction(OnboardingUiEvent.OnNavigateToMain) },
                    text = stringResource(Res.string.onboarding_skip),
                    color = Color.White
                )
            }
        },
        bottomBar = {
            OnboardingBottomBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(24.dp),
                onboardingPages = onboardingPages,
                selectedPage = selectedPage,
                onAction = onAction
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .statusBarsPadding()
                    .navigationBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically)
            ) {
                HorizontalPager(
                    modifier = Modifier.fillMaxWidth(),
                    state = pagerState,
                    pageContent = { page ->
                        val item = onboardingPages[page]
                        OnboardingItem(
                            modifier = Modifier,
                            item = item,
                            onAction = onAction
                        )
                    }
                )
                PageIndicator(
                    modifier = Modifier,
                    numberOfPages = onboardingPages.size,
                    selectedPage = onboardingPages.indexOf(selectedPage),
                    defaultRadius = 10.dp,
                    defaultColor = Color(0xFF8a9296),
                    selectedColor = Color.White,
                    selectedLength = 20.dp,
                    space = 8.dp,
                    animationDurationInMillis = 300,
                )
            }
        }
    )

}


@Composable
private fun Preview() {
    AppTheme {
    }
}



