package ir.aispeaking.profile.placement

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.aispeaking.profile.placement.component.OptionSelectableCard
import ir.aispeaking.profile.placement.component.PlacementExitConfirmDialog
import ir.aispeaking.profile.placement.component.PlacementProgressBar
import ir.aispeaking.profile.placement.component.PlacementResultView
import ir.aispeaking.profile.placement.component.QuestionReviewDialog
import ir.aispeaking.profile.placement.component.QuestionSlideCard
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.ic_back
import ir.aispeaking.sharedui.ui.core.loading.AppLoadingIndicator
import ir.aispeaking.sharedui.ui.game.Game
import ir.aispeaking.sharedui.ui.game.GameButton
import ir.aispeaking.sharedui.ui.game.GameButtonStyle
import ir.aispeaking.sharedui.ui.game.GameText
import ir.aispeaking.sharedui.ui.them.AppTheme
import org.jetbrains.compose.resources.painterResource

@Composable
fun PlacementTestScreen(
    viewModel: PlacementTestViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val isDark = AppTheme.isDark
    val screenBg = if (isDark) Color(0xFF0F1015) else Color(0xFFF2F4F7)

    val pagerState = rememberPagerState(pageCount = { uiState.totalQuestions })

    // Sync pager state with ViewModel
    LaunchedEffect(pagerState.currentPage) {
        if (uiState.currentQuestionIndex != pagerState.currentPage) {
            viewModel.onEvent(PlacementTestUiEvent.PageChanged(pagerState.currentPage))
        }
    }

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is PlacementTestUiEffect.ScrollToPage -> {
                    if (effect.pageIndex in 0 until uiState.totalQuestions) {
                        pagerState.animateScrollToPage(effect.pageIndex)
                    }
                }
                is PlacementTestUiEffect.Finished -> {
                    // Result will be displayed automatically via uiState.evaluationResult
                }
            }
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(screenBg)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
            ) {
                // Top Navigation Bar matching Apple sleek Liquid Glass style
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Right in RTL: Back button (chevron points right > per Constitution)
                    val backInteraction = remember { MutableInteractionSource() }
                    val backPressed by backInteraction.collectIsPressedAsState()
                    val backScale by animateFloatAsState(
                        targetValue = if (backPressed) 0.92f else 1f,
                        animationSpec = spring(dampingRatio = 0.82f, stiffness = Spring.StiffnessMediumLow),
                        label = "placement_back_btn_press"
                    )

                    val backBg = if (isDark) Color(0x1AFFFFFF) else Color(0xB8FFFFFF)
                    val backBorder = Game.Stroke
                    val backTint = if (isDark) Game.TextPrimary else Color(0xFF111318)

                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .graphicsLayer {
                                scaleX = backScale
                                scaleY = backScale
                            }
                            .clip(CircleShape)
                            .background(backBg)
                            .border(1.dp, backBorder, CircleShape)
                            .clickable(
                                interactionSource = backInteraction,
                                indication = null,
                                onClick = {
                                    if (uiState.evaluationResult != null || uiState.questions.isEmpty()) {
                                        onNavigateBack()
                                    } else {
                                        viewModel.onEvent(PlacementTestUiEvent.ShowExitConfirm)
                                    }
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_back),
                            contentDescription = "بازگشت",
                            tint = backTint,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Symmetrical Center-Aligned Title
                    GameText(
                        text = "آزمون تعیین سطح انگلیسی",
                        size = 17.sp,
                        bold = true,
                        color = if (isDark) Game.TextPrimary else Color(0xFF111318),
                        align = TextAlign.Center
                    )

                    // Balance spacer on left opposite the back button for symmetrical symmetry
                    Spacer(modifier = Modifier.size(44.dp))
                }

                // Main Content Body
                when {
                    uiState.isLoading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                AppLoadingIndicator(
                                    color = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7),
                                    size = 38.dp
                                )
                                GameText(
                                    text = "در حال بارگذاری سوالات آزمون…",
                                    size = 13.sp,
                                    color = if (isDark) Game.TextSecondary else Color(0xFF6B7280)
                                )
                            }
                        }
                    }

                    uiState.errorMessage != null && uiState.questions.isEmpty() -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                modifier = Modifier.padding(32.dp)
                            ) {
                                GameText(
                                    text = uiState.errorMessage ?: "خطایی رخ داد",
                                    size = 14.sp,
                                    lineHeight = 22.sp,
                                    color = if (isDark) Game.TextSecondary else Color(0xFF6B7280),
                                    align = TextAlign.Center
                                )
                                GameButton(
                                    text = "تلاش دوباره",
                                    onClick = { viewModel.onEvent(PlacementTestUiEvent.RetryLoad) },
                                    modifier = Modifier.width(200.dp)
                                )
                            }
                        }
                    }

                    uiState.evaluationResult != null -> {
                        // Results View
                        PlacementResultView(
                            result = uiState.evaluationResult!!,
                            onReviewClick = { viewModel.onEvent(PlacementTestUiEvent.ToggleReviewDialog) },
                            onRetakeClick = { viewModel.onEvent(PlacementTestUiEvent.RetakeTest) },
                            onFinishClick = onNavigateBack,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    uiState.questions.isNotEmpty() -> {
                        // In-Progress Test Pager View
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            // Top Animated Progress Bar
                            PlacementProgressBar(
                                currentIndex = pagerState.currentPage,
                                totalQuestions = uiState.totalQuestions,
                                answeredCount = uiState.answeredCount
                            )

                            // Horizontal Pager for Questions
                            HorizontalPager(
                                state = pagerState,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                            ) { page ->
                                val question = uiState.questions[page]
                                val selectedOption = uiState.selectedAnswers[question.id]

                                QuestionSlideCard(
                                    questionIndex = page,
                                    question = question,
                                    selectedOption = selectedOption,
                                    onOptionSelected = { option ->
                                        viewModel.onEvent(PlacementTestUiEvent.SelectOption(question.id, option))
                                    }
                                )
                            }

                            // Bottom Navigation Actions Row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .navigationBarsPadding()
                                    .padding(horizontal = 20.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Previous Question Button
                                if (pagerState.currentPage > 0) {
                                    GameButton(
                                        text = "قبلی",
                                        style = GameButtonStyle.Glass,
                                        onClick = { viewModel.onEvent(PlacementTestUiEvent.PreviousQuestion) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                // Next or Submit Button
                                if (pagerState.currentPage == uiState.totalQuestions - 1) {
                                    GameButton(
                                        text = if (uiState.isEvaluating) "در حال تصحیح…" else "ثبت و مشاهده نتیجه",
                                        onClick = { viewModel.onEvent(PlacementTestUiEvent.SubmitTest) },
                                        enabled = !uiState.isEvaluating,
                                        modifier = Modifier.weight(if (pagerState.currentPage > 0) 1.6f else 1f)
                                    )
                                } else {
                                    GameButton(
                                        text = "بعدی",
                                        onClick = { viewModel.onEvent(PlacementTestUiEvent.NextQuestion) },
                                        modifier = Modifier.weight(if (pagerState.currentPage > 0) 1.6f else 1f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Exit Confirmation Dialog
            PlacementExitConfirmDialog(
                visible = uiState.showExitConfirmDialog,
                onConfirmExit = {
                    viewModel.onEvent(PlacementTestUiEvent.DismissExitConfirm)
                    onNavigateBack()
                },
                onDismiss = { viewModel.onEvent(PlacementTestUiEvent.DismissExitConfirm) }
            )

            // Question Review Dialog
            if (uiState.evaluationResult != null) {
                QuestionReviewDialog(
                    visible = uiState.showReviewDialog,
                    reviewItems = uiState.evaluationResult!!.reviewItems,
                    onDismiss = { viewModel.onEvent(PlacementTestUiEvent.ToggleReviewDialog) }
                )
            }
        }
    }
}
