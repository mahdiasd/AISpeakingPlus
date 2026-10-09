package ir.aispeaking.stageslist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.aispeaking.domain.model.stage.Stage
import ir.aispeaking.domain.model.stage.StageLockStatus
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.ic_back
import ir.aispeaking.sharedui.ui.game.Game
import ir.aispeaking.sharedui.ui.game.GameButton
import ir.aispeaking.sharedui.ui.game.GameIconButton
import ir.aispeaking.sharedui.ui.game.GameText
import ir.aispeaking.sharedui.ui.game.GlassPanel
import kotlinx.coroutines.delay
import ir.aispeaking.stageslist.component.StageListItemCard
import ir.aispeaking.stageslist.component.StagesHeaderStatsCard

@Composable
fun StagesListScreen(
    viewModel: StagesListViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToStageChat: (stageId: String) -> Unit,
    onNavigateToSubscription: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()

    // Auto-scroll to current active stage when list is loaded
    LaunchedEffect(uiState.isLoading, uiState.stages) {
        if (!uiState.isLoading && uiState.stages.isNotEmpty()) {
            val targetIndex = uiState.currentActiveStageIndex.coerceIn(0, uiState.stages.size - 1)
            listState.animateScrollToItem(targetIndex)
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Game.FallbackBackground)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
            ) {
                // Top bar: back on the start side (right in RTL), centered title
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    GameIconButton(
                        icon = Res.drawable.ic_back,
                        onClick = onNavigateBack,
                        contentDescription = "بازگشت"
                    )
                    GameText(
                        text = "مرحله‌ها",
                        size = 18.sp,
                        bold = true,
                        align = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.size(44.dp))
                }

                if (uiState.stages.isNotEmpty()) {
                    StagesHeaderStatsCard(
                        totalStars = uiState.totalStarsEarned,
                        maxStars = uiState.maxPossibleStars,
                        completedStages = uiState.completedStagesCount,
                        totalStages = uiState.stages.size,
                        progressPercentage = uiState.progressPercentage,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)
                    )
                }

                if (uiState.isLoading && uiState.stages.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CircularProgressIndicator(
                                color = Game.Gold,
                                strokeWidth = 3.dp,
                                modifier = Modifier.size(40.dp)
                            )
                            GameText(text = "در حال بارگذاری مرحله‌ها…", size = 14.sp, color = Game.TextSecondary)
                        }
                    }
                } else if (uiState.stages.isEmpty() && uiState.errorMessage != null) {
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
                                text = uiState.errorMessage ?: "خطایی رخ داده است",
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
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentPadding = PaddingValues(
                            start = 14.dp,
                            end = 14.dp,
                            top = 10.dp,
                            bottom = 28.dp
                        )
                    ) {
                        itemsIndexed(
                            items = uiState.stages,
                            key = { _, stage -> stage.id }
                        ) { index, stage ->
                            StageListItemCard(
                                stage = stage,
                                isActiveCurrentStage = index == uiState.currentActiveStageIndex,
                                isFirst = index == 0,
                                isLast = index == uiState.stages.lastIndex,
                                onStageClick = { clickedStage ->
                                    handleStageClick(
                                        stage = clickedStage,
                                        onNavigateToChat = onNavigateToStageChat,
                                        onNavigateToSubscription = onNavigateToSubscription
                                    )
                                }
                            )
                        }
                    }
                }
            }

            // Temporary error toast (list stays visible)
            val toast = uiState.errorMessage
            if (toast != null && uiState.stages.isNotEmpty()) {
                LaunchedEffect(toast) {
                    delay(3500)
                    viewModel.onAction(StagesListUiAction.ClearError)
                }
                GlassPanel(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .padding(16.dp),
                    shape = RoundedCornerShape(18.dp),
                    color = Game.PanelSolid,
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

private fun handleStageClick(
    stage: Stage,
    onNavigateToChat: (stageId: String) -> Unit,
    onNavigateToSubscription: () -> Unit
) {
    when (stage.lockStatus) {
        StageLockStatus.UNLOCKED -> {
            onNavigateToChat(stage.id)
        }
        StageLockStatus.LOCKED_SUBSCRIPTION -> {
            onNavigateToSubscription()
        }
        StageLockStatus.LOCKED_REGISTRATION -> {
            // Unlocked through registration on main journey or fallback
            onNavigateToChat(stage.id)
        }
        StageLockStatus.LOCKED_PREVIOUS_STAGE -> {
            // Cannot start ahead of sequence
        }
    }
}

/* --------------------------------- Previews --------------------------------- */

@androidx.compose.ui.tooling.preview.Preview
@Composable
private fun StagesListScreenPreview() {
    ir.aispeaking.sharedui.ui.them.AppTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF070B19)),
            contentAlignment = Alignment.Center
        ) {
            GameText(text = "پیش‌نمایش لیست کامل مراحل", size = 16.sp, bold = true)
        }
    }
}
