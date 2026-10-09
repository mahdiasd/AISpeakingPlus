package ir.aispeaking.sharedui.ui.stage.stageslist

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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import ir.aispeaking.sharedui.ui.core.button.AppBackButton
import ir.aispeaking.sharedui.ui.stage.stageslist.component.StageListItemCard
import ir.aispeaking.sharedui.ui.stage.stageslist.component.StagesHeaderStatsCard

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
                .background(Color(0xFF070B19))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
            ) {
                // Top App Bar: Back button on the RIGHT (Start in RTL), Centered title
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Right button (Start in RTL): Back button pointing right
                    AppBackButton(onClick = onNavigateBack)

                    // Symmetric Center-Aligned Screen Title
                    Text(
                        text = "مراحل و پیشرفت یادگیری",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )

                    // Balanced spacer equal to AppBackButton size (38dp) for true center alignment
                    Spacer(modifier = Modifier.size(38.dp))
                }

                // Pinned Header Status Card: Always pinned at the top above the scrolling list
                if (uiState.stages.isNotEmpty()) {
                    StagesHeaderStatsCard(
                        totalStars = uiState.totalStarsEarned,
                        maxStars = uiState.maxPossibleStars,
                        completedStages = uiState.completedStagesCount,
                        totalStages = uiState.stages.size,
                        progressPercentage = uiState.progressPercentage,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }

                if (uiState.isLoading && uiState.stages.isEmpty()) {
                    // Loading State
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(
                                color = Color(0xFFFFD700),
                                strokeWidth = 3.dp,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "در حال بارگذاری مراحل...",
                                color = Color(0xFFA5B4FC),
                                fontSize = 14.sp
                            )
                        }
                    }
                } else if (uiState.stages.isEmpty() && uiState.errorMessage != null) {
                    // Error State
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Text(
                                text = uiState.errorMessage ?: "خطایی رخ داده است",
                                color = Color.White,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { viewModel.loadStages() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1))
                            ) {
                                Text("تلاش مجدد", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    // Content List: Compact stages items
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            top = 6.dp,
                            bottom = 24.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        itemsIndexed(
                            items = uiState.stages,
                            key = { _, stage -> stage.id }
                        ) { index, stage ->
                            val isActive = index == uiState.currentActiveStageIndex

                            StageListItemCard(
                                stage = stage,
                                isActiveCurrentStage = isActive,
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

            // Snackbar for temporary error/notification
            uiState.errorMessage?.let { message ->
                if (uiState.stages.isNotEmpty()) {
                    Snackbar(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(16.dp),
                        action = {
                            TextButton(onClick = { viewModel.onAction(StagesListUiAction.ClearError) }) {
                                Text("متوجه شدم", color = Color.White)
                            }
                        }
                    ) {
                        Text(message)
                    }
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
