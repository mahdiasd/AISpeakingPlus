package ir.aispeaking.chat

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.aispeaking.chat.component.AiChatItem
import ir.aispeaking.chat.component.ChatBottomBar
import ir.aispeaking.chat.component.ChatInputMode
import ir.aispeaking.chat.component.ChatToolbar
import ir.aispeaking.chat.component.UserChatItem
import ir.aispeaking.domain.model.chat.Chat
import ir.aispeaking.domain.model.stage.EvaluationSession
import ir.aispeaking.sharedui.ui.component.AsyncStageBackground
import ir.aispeaking.sharedui.ui.game.Game
import ir.aispeaking.sharedui.ui.game.GameAvatar
import ir.aispeaking.sharedui.ui.game.GameText
import ir.aispeaking.sharedui.ui.game.GlassPanel
import ir.aispeaking.sharedui.ui.them.AppTheme

@Composable
fun ChatScreen(
    stageId: String,
    viewModel: ChatViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    var showHintConfirmDialog by remember { mutableStateOf(false) }
    var showObjectiveSheet by remember { mutableStateOf(false) }
    // Keep the last evaluation so the result card can animate out instead of vanishing.
    var lastEvaluation by remember { mutableStateOf<EvaluationSession?>(null) }
    LaunchedEffect(uiState.evaluationSession) {
        uiState.evaluationSession?.let { lastEvaluation = it }
    }

    LaunchedEffect(stageId) {
        viewModel.initStage(stageId)
    }

    val lastChat = uiState.chats.lastOrNull()
    val lastChatUid = lastChat?.uid
    val lastChatMessageLength = when (lastChat) {
        is Chat.Ai -> lastChat.message.length
        is Chat.User -> lastChat.message.length
        else -> 0
    }

    val bottomAnchorIndex = uiState.chats.size

    LaunchedEffect(uiState.chats.size, lastChatUid) {
        if (uiState.chats.isNotEmpty()) {
            listState.animateScrollToItem(bottomAnchorIndex)
        }
    }

    LaunchedEffect(lastChatMessageLength) {
        if (uiState.chats.isNotEmpty()) {
            listState.scrollToItem(bottomAnchorIndex, scrollOffset = 10000)
        }
    }

    val stage = uiState.stage

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        AsyncStageBackground(
            backgroundUrl = stage?.backgroundUrl,
            modifier = modifier,
            enableFrostedGlass = false
        ) {
            // Readability scrims; the middle of the artwork stays visible.
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0f to Game.ScrimTop,
                            0.22f to Color(0x33050816),
                            0.55f to Color(0x55050816),
                            1f to Game.ScrimBottom
                        )
                    )
            )

            Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
                ChatToolbar(
                    stage = stage,
                    turnsCount = uiState.turnsCount,
                    isFinishing = uiState.isSubmittingEvaluation,
                    onBackClick = onNavigateBack,
                    onObjectiveClick = { showObjectiveSheet = true },
                    onFinishConversationClick = { viewModel.submitEvaluation() }
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    if (uiState.stageLoadError != null) {
                        StageLoadErrorState(
                            errorMessage = uiState.stageLoadError!!,
                            onRetry = { viewModel.retryLoadStage() },
                            onBack = onNavigateBack,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else if (stage == null) {
                        LoadingState(modifier = Modifier.align(Alignment.Center))
                    } else {
                        if (uiState.chats.isEmpty() && stage.initialSpeaker != "Model") {
                            YourTurnState(
                                characterName = stage.characterName,
                                avatarUrl = stage.characterAvatarUrl,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }

                        // Conversation reads left-to-right (English); the HUD around it is RTL.
                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                            LazyColumn(
                                state = listState,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 12.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp),
                                contentPadding = PaddingValues(vertical = 12.dp)
                            ) {
                                items(
                                    items = uiState.chats,
                                    key = { it.uid }
                                ) { chat ->
                                    Box(modifier = Modifier.fillMaxWidth()) {
                                        when (chat) {
                                            is Chat.User -> UserChatItem(
                                                modifier = Modifier.align(Alignment.CenterEnd),
                                                chat = chat,
                                                onRetry = { viewModel.retrySendMessage() }
                                            )

                                            is Chat.Ai -> AiChatItem(
                                                modifier = Modifier
                                                    .fillMaxWidth(0.94f)
                                                    .align(Alignment.CenterStart),
                                                chat = chat,
                                                characterName = stage.characterName,
                                                characterAvatarUrl = stage.characterAvatarUrl,
                                                onPlayVoice = { viewModel.playAiVoice(chat.uid) },
                                                onStopVoice = { viewModel.stopAiVoice(chat.uid) }
                                            )

                                            is Chat.WaitingForAi -> TypingBubble(
                                                characterName = stage.characterName,
                                                avatarUrl = stage.characterAvatarUrl,
                                                modifier = Modifier.align(Alignment.CenterStart)
                                            )
                                        }
                                    }
                                }

                                // Bottom anchor to ensure scrolling reaches the very end of messages
                                item(key = "bottom_anchor") {
                                    Box(modifier = Modifier.height(8.dp))
                                }
                            }
                        }
                    }
                }

                HintSuggestionCue(
                    visible = uiState.currentHintSuggestion != null || uiState.hintError != null,
                    suggestionEn = uiState.currentHintSuggestion,
                    explanationFa = uiState.currentHintExplanation,
                    errorMessage = uiState.hintError,
                    onApplySuggestion = { suggestion ->
                        viewModel.onMessageTextChanged(suggestion)
                        viewModel.setInputMode(ChatInputMode.TEXT)
                        viewModel.dismissHint()
                    },
                    onDismiss = { viewModel.dismissHint() }
                )

                ChatBottomBar(
                    inputMode = uiState.inputMode,
                    text = uiState.messageText,
                    isRecording = uiState.isRecording,
                    hintsUsedCount = uiState.hintsUsedCount,
                    isRequestingHint = uiState.isRequestingHint,
                    onTextChange = { viewModel.onMessageTextChanged(it) },
                    onInputModeChange = { viewModel.setInputMode(it) },
                    onVoiceToggle = { start -> viewModel.toggleRecording(start) },
                    onSendClick = { viewModel.sendMessage() },
                    onHintClick = { showHintConfirmDialog = true }
                )
            }

            ObjectiveSheet(
                visible = showObjectiveSheet,
                stage = stage,
                onDismiss = { showObjectiveSheet = false }
            )

            HintConfirmDialog(
                visible = showHintConfirmDialog,
                hintsUsedCount = uiState.hintsUsedCount,
                onConfirm = {
                    showHintConfirmDialog = false
                    viewModel.requestHint()
                },
                onDismiss = { showHintConfirmDialog = false }
            )

            lastEvaluation?.let { evaluation ->
                FinishConversationDialog(
                    visible = uiState.showFinishConfirmDialog && uiState.evaluationSession != null,
                    evaluation = evaluation,
                    onContinueChatting = { viewModel.dismissFinishConfirmDialog() },
                    onReplayStage = { viewModel.replayStage() },
                    onConfirmAndNext = {
                        viewModel.dismissFinishConfirmDialog()
                        onNavigateBack()
                    },
                    onDismissRequest = { viewModel.dismissFinishConfirmDialog() },
                    onExitWithoutSave = {
                        viewModel.dismissFinishConfirmDialog()
                        onNavigateBack()
                    }
                )
            }
        }
    }
}

@Composable
private fun LoadingState(modifier: Modifier = Modifier) {
    GlassPanel(modifier = modifier) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = Game.Mint
            )
            GameText(text = "در حال آماده‌سازی گفتگو…", size = 14.sp, color = Game.TextSecondary)
        }
    }
}

@Composable
private fun StageLoadErrorState(
    errorMessage: String,
    onRetry: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassPanel(modifier = modifier.padding(horizontal = 32.dp)) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            GameText(text = "خطا در باز کردن مرحله", size = 16.sp, bold = true, color = Game.Coral)
            GameText(
                text = errorMessage,
                size = 13.sp,
                lineHeight = 21.sp,
                color = Game.TextSecondary,
                align = androidx.compose.ui.text.style.TextAlign.Center
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ir.aispeaking.sharedui.ui.game.GameChip(
                    text = "تلاش مجدد",
                    accent = Game.Mint,
                    active = true,
                    onClick = onRetry
                )
                ir.aispeaking.sharedui.ui.game.GameChip(
                    text = "بازگشت",
                    accent = Game.TextSecondary,
                    active = false,
                    onClick = onBack
                )
            }
        }
    }
}

/** Shown when the learner is the one who has to open the conversation. */
@Composable
private fun YourTurnState(
    characterName: String,
    avatarUrl: String?,
    modifier: Modifier = Modifier
) {
    GlassPanel(modifier = modifier.padding(horizontal = 32.dp)) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            GameAvatar(name = characterName, imageUrl = avatarUrl, size = 56.dp)
            GameText(text = "نوبت توست!", size = 18.sp, bold = true)
            GameText(
                text = "$characterName منتظر توست. اولین جمله را به انگلیسی بگو.",
                size = 13.sp,
                lineHeight = 21.sp,
                color = Game.TextSecondary,
                align = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
private fun TypingBubble(
    characterName: String,
    avatarUrl: String?,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "typing")
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        GameAvatar(name = characterName, imageUrl = avatarUrl, size = 34.dp)
        val shape = RoundedCornerShape(topStart = 6.dp, topEnd = 20.dp, bottomEnd = 20.dp, bottomStart = 20.dp)
        Row(
            modifier = Modifier
                .background(Game.BubbleAi, shape)
                .border(1.dp, Game.Stroke, shape)
                .padding(horizontal = 16.dp, vertical = 15.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(3) { index ->
                val alpha by transition.animateFloat(
                    initialValue = 0.3f,
                    targetValue = 0.3f,
                    animationSpec = infiniteRepeatable(
                        animation = keyframes {
                            durationMillis = 1200
                            0.3f at index * 160 using LinearEasing
                            1f at index * 160 + 260 using LinearEasing
                            0.3f at index * 160 + 520 using LinearEasing
                        },
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "dot_$index"
                )
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .alpha(alpha)
                        .background(Game.TextPrimary, CircleShape)
                )
            }
        }
        GameText(text = "در حال پاسخ…", size = 11.sp, color = Game.TextSecondary)
    }
}

/* --------------------------------- Previews --------------------------------- */

@androidx.compose.ui.tooling.preview.Preview
@Composable
private fun ChatScreenPreview() {
    AppTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .fillMaxHeight()
                .background(Game.Ink),
            contentAlignment = Alignment.Center
        ) {
            TypingBubble(characterName = "Emily", avatarUrl = null)
        }
    }
}
