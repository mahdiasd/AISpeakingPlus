package ir.aispeaking.stageslist

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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
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
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.aispeaking.domain.model.stage.Stage
import ir.aispeaking.domain.model.stage.StageLockStatus
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.ic_back
import ir.aispeaking.sharedui.ic_star
import ir.aispeaking.sharedui.ui.component.AsyncStageBackground
import ir.aispeaking.sharedui.ui.game.Game
import ir.aispeaking.sharedui.ui.game.GameButton
import ir.aispeaking.sharedui.ui.game.GameIconButton
import ir.aispeaking.sharedui.ui.game.GameText
import ir.aispeaking.sharedui.ui.game.GlassPanel
import ir.aispeaking.sharedui.ui.game.fa
import ir.aispeaking.sharedui.ui.them.AppTheme
import ir.aispeaking.sharedui.utils.lifecycle.OnResume
import ir.aispeaking.stageslist.component.PhaseHeaderCard
import ir.aispeaking.stageslist.component.StageListItemCard
import ir.aispeaking.stageslist.component.StagesHeaderStatsCard
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource

/**
 * Apple-grade Stages List Screen with cartoon game richness:
 * - Ambient scenic cartoon background with frosted glass overlay
 * - Translucent floating Apple navigation bar with content scrolling underneath
 * - Apple Frosted Glass cards with specular highlights and spring physics
 * - Journey timeline with glowing active stage node and star collectibles
 */
@Composable
fun StagesListScreen(
    viewModel: StagesListViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToStageChat: (stageId: String) -> Unit,
    onNavigateToSubscription: () -> Unit,
    onNavigateToLogin: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val isDark = AppTheme.isDark

    OnResume {
        viewModel.loadStages()
    }

    // Auto-scroll to current active stage when list is loaded
    LaunchedEffect(uiState.isLoading, uiState.stages) {
        if (!uiState.isLoading && uiState.stages.isNotEmpty()) {
            val curr = uiState.currentStage
            if (curr != null) {
                val phaseCountBefore = (curr.phaseNumber - 1).coerceAtLeast(0)
                val stageIdx = uiState.stages.indexOfFirst { it.id == curr.id }
                val targetIndex = (stageIdx + phaseCountBefore + 1).coerceIn(0, uiState.stages.size + 10)
                listState.animateScrollToItem(targetIndex)
            }
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        // Atmospheric Cartoon Background with Apple Frosted Glass Layer
        AsyncStageBackground(
            backgroundUrl = uiState.currentStage?.backgroundUrl,
            dimColor = if (isDark) Color(0xFF080A14).copy(alpha = 0.85f) else Color(0xFFF3F4F6).copy(alpha = 0.90f),
            enableFrostedGlass = true,
            modifier = modifier.fillMaxSize()
        ) {
            // Subtle ambient color bloom overlays for rich cartoon lighting
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                if (isDark) Game.Blue.copy(alpha = 0.15f) else Game.Blue.copy(alpha = 0.08f),
                                Color.Transparent
                            ),
                            radius = 900f
                        )
                    )
            )

            Column(modifier = Modifier.fillMaxSize()) {
                // Floating Translucent Apple Navigation Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (isDark) Color(0xCC0E101A) else Color(0xD9FFFFFF)
                        )
                        .border(
                            1.dp,
                            if (isDark) Color(0x28FFFFFF) else Color(0x1F000000)
                        )
                        .statusBarsPadding()
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // RTL Start anchor (Right): Back Button
                        GameIconButton(
                            icon = Res.drawable.ic_back,
                            onClick = onNavigateBack,
                            contentDescription = "بازگشت"
                        )

                        // Center: Title & Subtitle
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(1.dp)
                        ) {
                            GameText(
                                text = "مرحله‌های ماجراجویی",
                                size = 17.sp,
                                bold = true,
                                align = TextAlign.Center
                            )
                            GameText(
                                text = "مسیر مکالمه زبان انگلیسی",
                                size = 11.sp,
                                color = Game.TextSecondary,
                                align = TextAlign.Center
                            )
                        }

                        // RTL End anchor (Left): Stars HUD Pill
                        if (uiState.stages.isNotEmpty()) {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(Game.Gold.copy(alpha = if (isDark) 0.18f else 0.12f))
                                    .border(1.dp, Game.Gold.copy(alpha = 0.45f), RoundedCornerShape(50))
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    painter = painterResource(Res.drawable.ic_star),
                                    contentDescription = null,
                                    tint = Game.Gold,
                                    modifier = Modifier.size(14.dp)
                                )
                                GameText(
                                    text = uiState.totalStarsEarned.fa(),
                                    size = 12.sp,
                                    bold = true,
                                    color = Game.Gold
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.size(44.dp))
                        }
                    }
                }

                // Body content
                when {
                    uiState.isLoading && uiState.stages.isEmpty() -> {
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
                                ir.aispeaking.sharedui.ui.core.loading.AppLoadingIndicator(
                                    color = Game.Blue,
                                    size = 42.dp
                                )
                                GameText(
                                    text = "در حال بارگذاری مرحله‌های ماجراجویی…",
                                    size = 14.sp,
                                    color = Game.TextSecondary
                                )
                            }
                        }
                    }
                    uiState.stages.isEmpty() && uiState.errorMessage != null -> {
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
                                    text = uiState.errorMessage ?: "خطایی در دریافت مرحله‌ها رخ داده است",
                                    size = 14.sp,
                                    lineHeight = 22.sp,
                                    color = Game.TextSecondary,
                                    align = TextAlign.Center
                                )
                                GameButton(
                                    text = "تلاش دوباره",
                                    onClick = { viewModel.loadStages() },
                                    modifier = Modifier.width(200.dp)
                                )
                            }
                        }
                    }
                    else -> {
                        val seasonTitle = uiState.stages.firstOrNull()?.seasonTitleFa ?: "فصل اول: روزهای آغازین در لندن"
                        val phaseGroups = uiState.stages.groupBy { it.phaseNumber }

                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentPadding = PaddingValues(
                                start = 14.dp,
                                end = 14.dp,
                                top = 12.dp,
                                bottom = 32.dp
                            )
                        ) {
                            // Pinned Apple Stats & Season Header
                            item(key = "header_stats_card") {
                                StagesHeaderStatsCard(
                                    totalStars = uiState.totalStarsEarned,
                                    maxStars = uiState.maxPossibleStars,
                                    completedStages = uiState.completedStagesCount,
                                    totalStages = uiState.stages.size,
                                    progressPercentage = uiState.progressPercentage,
                                    seasonTitle = seasonTitle,
                                    modifier = Modifier.padding(bottom = 10.dp)
                                )
                            }

                            // Phase Groups & Stages
                            val firstPhaseKey = phaseGroups.keys.firstOrNull()
                            phaseGroups.forEach { (phaseNum, stagesInPhase) ->
                                item(key = "phase_header_$phaseNum") {
                                    val completedCount = stagesInPhase.count { (it.userProgress?.stars ?: 0) > 0 }
                                    val phaseTitle = stagesInPhase.firstOrNull()?.phaseTitleFa ?: "فاز $phaseNum"
                                    val isFirstPhase = phaseNum == firstPhaseKey
                                    PhaseHeaderCard(
                                        phaseNumber = phaseNum,
                                        phaseTitleFa = phaseTitle,
                                        completedStages = completedCount,
                                        totalStages = stagesInPhase.size,
                                        modifier = Modifier.padding(
                                            top = if (isFirstPhase) 6.dp else 18.dp,
                                            bottom = 10.dp
                                        )
                                    )
                                }

                                itemsIndexed(
                                    items = stagesInPhase,
                                    key = { _, stage -> stage.id }
                                ) { idxInPhase, stage ->
                                    val isFirstInPhase = idxInPhase == 0
                                    val isLastInPhase = idxInPhase == stagesInPhase.lastIndex
                                    val globalIdx = uiState.stages.indexOf(stage)
                                    StageListItemCard(
                                        stage = stage,
                                        isActiveCurrentStage = globalIdx == uiState.currentActiveStageIndex,
                                        isFirst = isFirstInPhase,
                                        isLast = isLastInPhase,
                                        onStageClick = { clickedStage ->
                                            viewModel.onAction(StagesListUiAction.StageClicked(clickedStage))
                                            handleStageClick(
                                                stage = clickedStage,
                                                onNavigateToChat = onNavigateToStageChat,
                                                onNavigateToSubscription = onNavigateToSubscription,
                                                onNavigateToLogin = onNavigateToLogin
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Floating Toast notification
            val toast = uiState.errorMessage
            if (toast != null && uiState.stages.isNotEmpty()) {
                LaunchedEffect(toast) {
                    delay(3500)
                    viewModel.onAction(StagesListUiAction.ClearError)
                }
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn() + slideInVertically { it },
                    exit = fadeOut() + slideOutVertically { it },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .padding(16.dp)
                ) {
                    GlassPanel(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isDark) Color(0xF21C1C24) else Color(0xF2FFFFFF),
                        border = Game.Coral.copy(alpha = 0.6f)
                    ) {
                        GameText(
                            text = toast,
                            size = 13.sp,
                            lineHeight = 20.sp,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun handleStageClick(
    stage: Stage,
    onNavigateToChat: (stageId: String) -> Unit,
    onNavigateToSubscription: () -> Unit,
    onNavigateToLogin: () -> Unit = {}
) {
    when (stage.lockStatus) {
        StageLockStatus.UNLOCKED -> {
            onNavigateToChat(stage.id)
        }
        StageLockStatus.LOCKED_SUBSCRIPTION -> {
            onNavigateToSubscription()
        }
        StageLockStatus.LOCKED_REGISTRATION -> {
            onNavigateToLogin()
        }
        StageLockStatus.LOCKED_PREVIOUS_STAGE -> {
            // Error toast handled by ViewModel
        }
    }
}
