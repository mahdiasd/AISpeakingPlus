package ir.aispeaking.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.aispeaking.domain.model.stage.AccessTier
import ir.aispeaking.domain.model.stage.Stage
import ir.aispeaking.domain.model.stage.StageLockStatus
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.ic_crown
import ir.aispeaking.sharedui.ic_history
import ir.aispeaking.sharedui.ic_play
import ir.aispeaking.sharedui.ic_profile
import ir.aispeaking.sharedui.ic_star
import ir.aispeaking.sharedui.ui.component.AsyncStageBackground
import ir.aispeaking.sharedui.ui.game.Game
import ir.aispeaking.sharedui.ui.game.GameAvatar
import ir.aispeaking.sharedui.ui.game.GameButton
import ir.aispeaking.sharedui.ui.game.GameButtonStyle
import ir.aispeaking.sharedui.ui.game.GameChip
import ir.aispeaking.sharedui.ui.game.GameIconButton
import ir.aispeaking.sharedui.ui.game.GameStars
import ir.aispeaking.sharedui.ui.game.GameStatPill
import ir.aispeaking.sharedui.ui.game.GameText
import ir.aispeaking.sharedui.ui.game.GlassPanel
import ir.aispeaking.sharedui.ui.game.fa
import ir.aispeaking.sharedui.utils.lifecycle.OnResume
import kotlinx.coroutines.delay

@Composable
fun JourneyMapScreen(
    viewModel: JourneyMapViewModel,
    onNavigateToChat: (String) -> Unit,
    onNavigateToProfile: () -> Unit = {},
    onNavigateToSubscription: () -> Unit = {},
    onNavigateToStagesList: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    LaunchedEffect(Unit) {
        viewModel.refreshStages()
    }

    OnResume {
        viewModel.refreshStages()
    }

    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.selectedStageForPaywall) {
        if (uiState.selectedStageForPaywall != null) {
            viewModel.dismissPaywall()
            onNavigateToSubscription()
        }
    }

    // Keep the last stage around so overlays can animate out instead of disappearing.
    var briefingStage by remember { mutableStateOf<Stage?>(null) }
    LaunchedEffect(uiState.selectedStageForBriefing) {
        uiState.selectedStageForBriefing?.let { briefingStage = it }
    }
    var registerStage by remember { mutableStateOf<Stage?>(null) }
    LaunchedEffect(uiState.selectedStageForRegister) {
        uiState.selectedStageForRegister?.let { registerStage = it }
    }

    LaunchedEffect(uiState.snackbarMessage) {
        if (uiState.snackbarMessage != null) {
            delay(3500)
            viewModel.clearSnackbar()
        }
    }

    val currentStage = uiState.currentStage

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Game.Ink)
        ) {
            when {
                uiState.isLoading && currentStage == null -> {
                    CenterMessage(loading = true, text = "در حال بارگذاری ماجراجویی…")
                }

                currentStage == null -> {
                    CenterMessage(
                        loading = false,
                        text = "مرحله‌ها بارگذاری نشدند. اتصال اینترنت را بررسی کن.",
                        actionText = "تلاش دوباره",
                        onAction = { viewModel.refreshStages() }
                    )
                }

                else -> {
                    AsyncStageBackground(
                        backgroundUrl = currentStage.backgroundUrl,
                        modifier = Modifier.fillMaxSize(),
                        enableFrostedGlass = false
                    ) {
                        // Top and bottom scrims keep the HUD readable; the artwork stays the hero.
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                                .align(Alignment.TopCenter)
                                .background(
                                    Brush.verticalGradient(listOf(Game.ScrimTop, Color.Transparent))
                                )
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(0.5f)
                                .align(Alignment.BottomCenter)
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color.Transparent, Color(0x99050816), Game.ScrimBottom)
                                    )
                                )
                        )

                        TopHud(
                            totalStars = uiState.totalStarsEarned,
                            isSubscriber = uiState.currentTier == AccessTier.SUBSCRIBER,
                            onProfile = onNavigateToProfile,
                            onSubscribe = onNavigateToSubscription,
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .statusBarsPadding()
                        )

                        AnimatedVisibility(
                            visible = uiState.isViewingPastStage,
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .statusBarsPadding()
                                .padding(top = 64.dp),
                            enter = fadeIn() + slideInVertically { -it / 2 },
                            exit = fadeOut() + slideOutVertically { -it / 2 }
                        ) {
                            GameChip(
                                text = "در حال مرور مرحله قبلی · بازگشت به مرحله فعلی",
                                icon = Res.drawable.ic_history,
                                accent = Game.Sky,
                                container = Game.Panel,
                                active = true,
                                onClick = { viewModel.resetToActiveStage() }
                            )
                        }

                        StageCard(
                            stage = currentStage,
                            stageNumber = currentStage.orderIndex,
                            totalStages = uiState.stages.size,
                            isReplay = uiState.isViewingPastStage,
                            pastCount = uiState.pastStages.size,
                            onStart = { viewModel.onStartCurrentStage() },
                            onOpenList = onNavigateToStagesList,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .navigationBarsPadding()
                        )
                    }
                }
            }

            // Toast
            AnimatedVisibility(
                visible = uiState.snackbarMessage != null,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 66.dp, start = 16.dp, end = 16.dp),
                enter = fadeIn() + slideInVertically { -it },
                exit = fadeOut() + slideOutVertically { -it }
            ) {
                GlassPanel(
                    shape = RoundedCornerShape(18.dp),
                    color = Game.PanelSolid,
                    border = Game.Sky.copy(alpha = 0.6f)
                ) {
                    GameText(
                        text = uiState.snackbarMessage ?: "",
                        size = 13.sp,
                        lineHeight = 20.sp,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                    )
                }
            }

            briefingStage?.let { stage ->
                StageBriefingDialog(
                    visible = uiState.selectedStageForBriefing != null,
                    stage = stage,
                    onDismiss = { viewModel.dismissBriefing() },
                    onStartMission = {
                        viewModel.dismissBriefing()
                        onNavigateToChat(stage.id)
                    }
                )
            }

            registerStage?.let { stage ->
                RegisterBottomSheet(
                    visible = uiState.selectedStageForRegister != null,
                    stage = stage,
                    onDismiss = { viewModel.dismissRegister() },
                    onRegisterSuccess = { viewModel.onRegisterSuccess() }
                )
            }
        }
    }
}

@Composable
private fun TopHud(
    totalStars: Int,
    isSubscriber: Boolean,
    onProfile: () -> Unit,
    onSubscribe: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        GameIconButton(
            icon = Res.drawable.ic_profile,
            onClick = onProfile,
            contentDescription = "پروفایل من"
        )
        GameStatPill(
            text = "${totalStars.fa()} ستاره",
            icon = Res.drawable.ic_star,
            accent = Game.Gold
        )
        Spacer(modifier = Modifier.weight(1f))
        if (isSubscriber) {
            GameStatPill(
                text = "اشتراک ویژه",
                icon = Res.drawable.ic_crown,
                accent = Game.Violet
            )
        } else {
            GameButton(
                text = "اشتراک ویژه",
                onClick = onSubscribe,
                modifier = Modifier.width(140.dp),
                style = GameButtonStyle.Gold,
                icon = Res.drawable.ic_crown,
                height = 40.dp,
                textSize = 13.sp
            )
        }
    }
}

@Composable
private fun StageCard(
    stage: Stage,
    stageNumber: Int,
    totalStages: Int,
    isReplay: Boolean,
    pastCount: Int,
    onStart: () -> Unit,
    onOpenList: () -> Unit,
    modifier: Modifier = Modifier
) {
    val stars = stage.userProgress?.stars ?: 0
    val (ctaText, ctaStyle) = when (stage.lockStatus) {
        StageLockStatus.UNLOCKED -> (if (isReplay || stars > 0) "تکرار مرحله" else "شروع مرحله") to GameButtonStyle.Primary
        StageLockStatus.LOCKED_REGISTRATION -> "ورود و باز کردن مرحله" to GameButtonStyle.Violet
        StageLockStatus.LOCKED_SUBSCRIPTION -> "باز کردن با اشتراک" to GameButtonStyle.Gold
        StageLockStatus.LOCKED_PREVIOUS_STAGE -> "ابتدا مرحله قبل را تمام کن" to GameButtonStyle.Glass
    }

    GlassPanel(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 12.dp),
        shape = RoundedCornerShape(26.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                GameText(
                    text = "مرحله ${stageNumber.fa()} از ${totalStages.fa()}",
                    size = 12.sp,
                    bold = true,
                    color = Game.Mint
                )
                GameStars(stars = stars, size = 22.dp, spacing = 2.dp)
            }

            GameText(
                text = stage.titleFa,
                size = 19.sp,
                lineHeight = 28.sp,
                bold = true,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                GameAvatar(
                    name = stage.characterName,
                    imageUrl = stage.characterAvatarUrl,
                    size = 28.dp
                )
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    GameText(
                        text = stage.characterName,
                        size = 13.sp,
                        latin = true,
                        color = Game.TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            GameButton(
                text = ctaText,
                onClick = onStart,
                modifier = Modifier.fillMaxWidth(),
                style = ctaStyle,
                icon = if (stage.lockStatus == StageLockStatus.UNLOCKED) Res.drawable.ic_play else null
            )

            GameButton(
                text = if (pastCount > 0) "همه مرحله‌ها و پیشرفت (${pastCount.fa()} انجام شده)" else "همه مرحله‌ها و پیشرفت",
                onClick = onOpenList,
                modifier = Modifier.fillMaxWidth(),
                style = GameButtonStyle.Glass,
                height = 44.dp,
                textSize = 13.sp
            )
        }
    }
}

@Composable
private fun CenterMessage(
    loading: Boolean,
    text: String,
    actionText: String? = null,
    onAction: () -> Unit = {}
) {
    Box(modifier = Modifier.fillMaxSize().background(Game.FallbackBackground), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (loading) {
                CircularProgressIndicator(color = Game.Gold, strokeWidth = 3.dp, modifier = Modifier.size(44.dp))
            }
            GameText(text = text, size = 15.sp, lineHeight = 24.sp, color = Game.TextSecondary, align = TextAlign.Center)
            if (actionText != null) {
                GameButton(text = actionText, onClick = onAction, modifier = Modifier.width(200.dp))
            }
        }
    }
}

/* --------------------------------- Previews --------------------------------- */

@androidx.compose.ui.tooling.preview.Preview
@Composable
private fun JourneyMapPreview() {
    ir.aispeaking.sharedui.ui.them.AppTheme {
        Box(modifier = Modifier.fillMaxSize().background(Game.Ink)) {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                StageCard(
                    stage = Stage(
                        id = "1", orderIndex = 1, title = "In-flight", titleFa = "پرواز به سوی آینده",
                        briefing = "", briefingFa = "", targetObjective = "", backgroundUrl = "",
                        characterName = "Flight Attendant Emily"
                    ),
                    stageNumber = 1,
                    totalStages = 15,
                    isReplay = false,
                    pastCount = 0,
                    onStart = {},
                    onOpenList = {},
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
    }
}
