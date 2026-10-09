package ir.aispeaking.splash

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.aispeaking.domain.model.user.User
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.ic_logo
import ir.aispeaking.sharedui.ui.core.loading.DotLoading
import ir.aispeaking.sharedui.ui.game.Game
import ir.aispeaking.sharedui.ui.game.GameButton
import ir.aispeaking.sharedui.ui.game.GameText
import ir.aispeaking.sharedui.ui.game.GlassPanel
import ir.aispeaking.sharedui.ui.them.AppTheme
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * Route composable for Splash screen, responsible for ViewModel connection and effects.
 */
@Composable
fun SplashRoute(
    modifier: Modifier = Modifier,
    viewModel: SplashViewModel = koinViewModel(),
    onNavigateToMain: (User) -> Unit = {},
    onNavigateToLogin: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is SplashEffect.NavigateToMain -> onNavigateToMain(effect.user)
                is SplashEffect.NavigateToLogin -> onNavigateToLogin()
                is SplashEffect.ShowToast -> {
                    // Handled if snackbar host provided
                }
            }
        }
    }

    SplashScreen(
        modifier = modifier,
        uiState = uiState,
        onRetry = { viewModel.processIntent(SplashIntent.Retry) }
    )
}

/**
 * Backwards compatible overload for SplashScreen with ViewModel injection.
 */
@Composable
fun SplashScreen(
    modifier: Modifier = Modifier,
    viewModel: SplashViewModel = koinViewModel(),
    onNavigateToMain: (User) -> Unit = {},
    onNavigateToLogin: () -> Unit = {}
) {
    SplashRoute(
        modifier = modifier,
        viewModel = viewModel,
        onNavigateToMain = onNavigateToMain,
        onNavigateToLogin = onNavigateToLogin
    )
}

/**
 * Pure stateless splash: brand mark floating over the dark game backdrop, with either a calm
 * loading state or a clear retry card.
 */
@Composable
fun SplashScreen(
    modifier: Modifier = Modifier,
    uiState: SplashUiState,
    onRetry: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "SplashFloat")
    val floatAnim by infiniteTransition.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "FloatY"
    )

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(listOf(Color(0xFF1B1F4B), Color(0xFF0E1433), Game.Ink))
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .widthIn(max = 440.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.offset(y = floatAnim.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(190.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        listOf(Game.Violet.copy(alpha = 0.45f), Color.Transparent)
                                    )
                                )
                        )
                        val shape = RoundedCornerShape(34.dp)
                        Box(
                            modifier = Modifier
                                .size(116.dp)
                                .clip(shape)
                                .background(Color.White)
                                .border(2.dp, Game.StrokeStrong, shape)
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(Res.drawable.ic_logo),
                                contentDescription = "AI Speaking Plus",
                                modifier = Modifier.size(80.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    GameText(
                        text = "AI Speaking Plus",
                        size = 30.sp,
                        bold = true,
                        latin = true,
                        align = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    GameText(
                        text = "مکالمه انگلیسی، مثل یک ماجراجویی",
                        size = 16.sp,
                        bold = true,
                        color = Game.Mint,
                        align = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(52.dp))

                    AnimatedVisibility(
                        visible = uiState.isLoading,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            DotLoading(dotColor = Game.Gold, dotSize = 12.dp)
                            GameText(
                                text = "در حال آماده‌سازی…",
                                size = 13.sp,
                                color = Game.TextSecondary,
                                align = TextAlign.Center
                            )
                        }
                    }

                    AnimatedVisibility(
                        visible = uiState.errorMessage != null,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        GlassPanel(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            color = Game.PanelSolid,
                            border = Game.Coral.copy(alpha = 0.5f)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                GameText(
                                    text = "اتصال برقرار نشد",
                                    size = 16.sp,
                                    bold = true,
                                    color = Game.Coral,
                                    align = TextAlign.Center
                                )
                                GameText(
                                    text = uiState.errorMessage ?: "",
                                    size = 13.sp,
                                    lineHeight = 21.sp,
                                    color = Game.TextSecondary,
                                    align = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                GameButton(
                                    text = "تلاش دوباره",
                                    onClick = onRetry,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/* --------------------------------- Previews --------------------------------- */

@PreviewLightDark
@Composable
private fun SplashScreenLoadingPreview() {
    AppTheme {
        SplashScreen(
            uiState = SplashUiState(isLoading = true),
            onRetry = {}
        )
    }
}

@Preview
@Composable
private fun SplashScreenErrorPreview() {
    AppTheme {
        SplashScreen(
            uiState = SplashUiState(
                isLoading = false,
                errorMessage = "خطا در اتصال به سرور"
            ),
            onRetry = {}
        )
    }
}
