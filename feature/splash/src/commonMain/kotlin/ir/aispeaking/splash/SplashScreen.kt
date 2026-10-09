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
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.aispeaking.domain.model.user.User
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.ic_logo
import ir.aispeaking.sharedui.ui.core.loading.DotLoading
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
 * Pure stateless Composable for Splash Screen with Disney-inspired playful styling.
 */
@Composable
fun SplashScreen(
    modifier: Modifier = Modifier,
    uiState: SplashUiState,
    onRetry: () -> Unit
) {
    // Gentle floating/breathing animation for Disney-style brand icon
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

    // Outer background with subtle Disney sky gradient
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        AppTheme.colors.surface,
                        AppTheme.colors.surfaceContainerLowest,
                        AppTheme.colors.surface
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Enforce mobile-sized width when rendered on wide screens (desktop/laptop/tablet)
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
                // Floating Disney-styled Brand Logo Card
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.offset(y = floatAnim.dp)
                ) {
                    // Soft glowing aura behind the logo
                    Box(
                        modifier = Modifier
                            .size(136.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        AppTheme.colors.primary.copy(alpha = 0.25f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )

                    // Logo Card Container
                    Card(
                        modifier = Modifier
                            .size(112.dp)
                            .shadow(
                                elevation = 16.dp,
                                shape = RoundedCornerShape(32.dp),
                                spotColor = AppTheme.colors.primary.copy(alpha = 0.35f)
                            )
                            .border(
                                width = 2.dp,
                                color = AppTheme.colors.primaryContainer.copy(alpha = 0.8f),
                                shape = RoundedCornerShape(32.dp)
                            ),
                        shape = RoundedCornerShape(32.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = AppTheme.colors.surfaceContainerLowest
                        )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(Res.drawable.ic_logo),
                                contentDescription = "AI Speaking Logo",
                                modifier = Modifier.size(76.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // App Title
                Text(
                    text = "AI Speaking Plus",
                    style = AppTheme.typography.headingLargeBold,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = AppTheme.colors.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Persian Subtitle
                Text(
                    text = "یادگیری هوشمند مکالمه زبان انگلیسی",
                    style = AppTheme.typography.bodyLargeBold.copy(
                        fontFamily = AppTheme.typography.persianBold
                    ),
                    fontSize = 16.sp,
                    color = AppTheme.colors.primary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Playful Disney-style Tag
                Row(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(AppTheme.colors.primaryContainer.copy(alpha = 0.6f))
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "✨ ماجراجویی تعاملی با هوش مصنوعی",
                        style = AppTheme.typography.labelMedium.copy(
                            fontFamily = AppTheme.typography.persianRegular
                        ),
                        fontSize = 12.sp,
                        color = AppTheme.colors.onPrimaryContainer,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(56.dp))

                // Loading State Indicator
                AnimatedVisibility(
                    visible = uiState.isLoading,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        DotLoading(
                            dotColor = AppTheme.colors.primary,
                            dotSize = 12.dp
                        )
                        Text(
                            text = "در حال ورود به دنیای مکالمه...",
                            style = AppTheme.typography.labelMedium.copy(
                                fontFamily = AppTheme.typography.persianRegular
                            ),
                            fontSize = 13.sp,
                            color = AppTheme.colors.outline,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Error and Retry State
                AnimatedVisibility(
                    visible = uiState.errorMessage != null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = AppTheme.colors.surfaceContainerLowest
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Text(
                                text = "⚠️ مشکلی در برقراری ارتباط رخ داد",
                                style = AppTheme.typography.titleBold.copy(
                                    fontFamily = AppTheme.typography.persianBold
                                ),
                                fontSize = 15.sp,
                                color = AppTheme.colors.error,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = uiState.errorMessage ?: "",
                                style = AppTheme.typography.bodyMedium.copy(
                                    fontFamily = AppTheme.typography.persianRegular
                                ),
                                color = AppTheme.colors.outline,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(18.dp))

                            Button(
                                onClick = onRetry,
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AppTheme.colors.primary
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(46.dp)
                            ) {
                                Text(
                                    text = "تلاش مجدد",
                                    style = AppTheme.typography.bodyMediumBold.copy(
                                        fontFamily = AppTheme.typography.persianBold
                                    ),
                                    color = AppTheme.colors.onPrimary
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
