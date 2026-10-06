package ir.aispeaking.sharedui.ui.stage

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.aispeaking.domain.model.chat.Chat
import ir.aispeaking.sharedui.ui.component.AsyncStageBackground
import ir.aispeaking.sharedui.ui.stage.component.*
import ir.aispeaking.sharedui.ui.them.AppTheme
import kotlinx.coroutines.delay

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

    LaunchedEffect(uiState.chats.size, lastChatUid) {
        if (uiState.chats.isNotEmpty()) {
            listState.animateScrollToItem(uiState.chats.size - 1)
        }
    }

    LaunchedEffect(lastChatMessageLength) {
        if (uiState.chats.isNotEmpty()) {
            listState.scrollToItem(uiState.chats.size - 1)
        }
    }

    AsyncStageBackground(
        backgroundUrl = uiState.stage?.backgroundUrl,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Header Bar
            ChatToolbar(
                stage = uiState.stage,
                isFinishing = uiState.isSubmittingEvaluation,
                onBackClick = onNavigateBack,
                onFinishConversationClick = { viewModel.submitEvaluation() }
            )

            // Conversation Messages Area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(vertical = 12.dp, horizontal = 4.dp)
                ) {
                    items(
                        items = uiState.chats,
                        key = { it.uid }
                    ) { chat ->
                        Box(modifier = Modifier.fillMaxWidth()) {
                            when (chat) {
                                is Chat.User -> {
                                    UserChatItem(
                                        modifier = Modifier.align(Alignment.CenterEnd),
                                        chat = chat,
                                        onRetry = { viewModel.retrySendMessage() }
                                    )
                                }

                                is Chat.Ai -> {
                                    AiChatItem(
                                        modifier = Modifier
                                            .fillMaxWidth(0.88f)
                                            .align(Alignment.CenterStart),
                                        chat = chat,
                                        onPlayVoice = { viewModel.playAiVoice(chat.uid) },
                                        onStopVoice = { viewModel.stopAiVoice(chat.uid) }
                                    )
                                }

                                is Chat.WaitingForAi -> {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .align(Alignment.CenterStart)
                                            .background(
                                                color = AppTheme.colors.aiChatContainer.copy(alpha = 0.85f),
                                                shape = RoundedCornerShape(16.dp)
                                            )
                                            .padding(horizontal = 12.dp, vertical = 8.dp)
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            strokeWidth = 2.dp,
                                            color = AppTheme.colors.primary
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "${uiState.stage?.characterName ?: "AI"} is typing...",
                                            color = AppTheme.colors.onSurface.copy(alpha = 0.8f),
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Live Transcribed Text Bubble (Tooltip pointing down to mic)
                val showBubbleVoiceText = uiState.inputMode == ChatInputMode.VOICE && uiState.messageText.isNotBlank()
                androidx.compose.animation.AnimatedVisibility(
                    visible = showBubbleVoiceText,
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 8.dp),
                    enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
                    exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 })
                ) {
                    BubbleVoiceText(
                        modifier = Modifier.fillMaxWidth(),
                        text = uiState.messageText,
                        onEditClick = { viewModel.setInputMode(ChatInputMode.TEXT) },
                        onClearClick = { viewModel.clearMessageText() }
                    )
                }
            }

            // Hint Suggestion Cue Banner (if requested)
            HintSuggestionCue(
                visible = uiState.currentHintSuggestion != null,
                suggestionEn = uiState.currentHintSuggestion,
                explanationFa = uiState.currentHintExplanation,
                onApplySuggestion = { suggestion ->
                    viewModel.onMessageTextChanged(suggestion)
                    viewModel.setInputMode(ChatInputMode.TEXT)
                    viewModel.dismissHint()
                },
                onDismiss = { viewModel.dismissHint() }
            )

            // Hint Button + Bottom Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    onClick = { showHintConfirmDialog = true },
                    enabled = !uiState.isRequestingHint,
                    shape = CircleShape,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFFFBBF24)
                    )
                ) {
                    if (uiState.isRequestingHint) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            strokeWidth = 2.dp,
                            color = Color(0xFFFBBF24)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("دریافت راهنما...", fontSize = 12.sp)
                    } else {
                        Text("💡 راهنما ${uiState.hintsUsedCount}", fontSize = 12.sp)
                    }
                }
            }

            // Bottom Input Bar (Voice Recorder / Text Editor)
            ChatBottomBar(
                inputMode = uiState.inputMode,
                text = uiState.messageText,
                isRecording = uiState.isRecording,
                onTextChange = { viewModel.onMessageTextChanged(it) },
                onInputModeChange = { viewModel.setInputMode(it) },
                onVoiceToggle = { start -> viewModel.toggleRecording(start) },
                onSendClick = { viewModel.sendMessage() }
            )
        }

        // Hint Confirmation & Score Calculation Dialog
        if (showHintConfirmDialog) {
            HintConfirmDialog(
                hintsUsedCount = uiState.hintsUsedCount,
                onConfirm = {
                    showHintConfirmDialog = false
                    viewModel.requestHint()
                },
                onDismiss = {
                    showHintConfirmDialog = false
                }
            )
        }

        // Finish Conversation Dialog with Stars & Calculation Breakdown
        if (uiState.showFinishConfirmDialog && uiState.evaluationSession != null) {
            FinishConversationDialog(
                evaluation = uiState.evaluationSession!!,
                onContinueChatting = { viewModel.dismissFinishConfirmDialog() },
                onReplayStage = { viewModel.replayStage() },
                onConfirmAndNext = {
                    viewModel.dismissFinishConfirmDialog()
                    onNavigateBack()
                },
                onDismissRequest = { viewModel.dismissFinishConfirmDialog() }
            )
        }
    }
}
