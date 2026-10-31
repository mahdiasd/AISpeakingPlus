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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.aispeaking.chat.component.AiChatItem
import ir.aispeaking.chat.component.AppleChatTheme
import ir.aispeaking.chat.component.ChatBottomBar
import ir.aispeaking.chat.component.ChatInputMode
import ir.aispeaking.chat.component.ChatToolbar
import ir.aispeaking.chat.component.UserChatItem
import ir.aispeaking.domain.model.chat.Chat
import ir.aispeaking.domain.model.stage.EvaluationSession
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.ic_done
import ir.aispeaking.sharedui.ic_lamp
import ir.aispeaking.sharedui.ic_microphone
import ir.aispeaking.sharedui.ic_spell_check
import ir.aispeaking.sharedui.ui.component.AsyncStageBackground
import ir.aispeaking.sharedui.ui.game.Game
import ir.aispeaking.sharedui.ui.game.GameAvatar
import ir.aispeaking.sharedui.ui.game.GameChip
import ir.aispeaking.sharedui.ui.game.GameText
import ir.aispeaking.sharedui.ui.guide.AppGuideHost
import ir.aispeaking.sharedui.ui.guide.GuideAlignment
import ir.aispeaking.sharedui.ui.guide.GuideStep
import ir.aispeaking.sharedui.ui.guide.guideTarget
import ir.aispeaking.sharedui.ui.guide.rememberGuideState
import ir.aispeaking.sharedui.ui.them.AppTheme
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource

/**
 * 2026 Apple Liquid Glass Chat Screen for AISpeakingPlus.
 *
 * Implements:
 * - Frosted Liquid Glass floating HUD header with real-time turn progress and level badge.
 * - Left-to-right English hero transcript with expandable translations, audio playback,
 *   and Siri iridescent typing state.
 * - Right-aligned user messages with rich Royal Azure to Electric Indigo gradient,
 *   with inline Serene Cyan grammar feedback (strictly NO green).
 * - Floating Liquid Glass bottom dock with breathing voice record pill and Apple input capsule.
 * - Ambient visionOS frosted glass cards for empty state, hint suggestions, and objective modals.
 */
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
    var toastMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(toastMessage) {
        if (toastMessage != null) {
            delay(2000)
            toastMessage = null
        }
    }

    // Keep the last evaluation so the result card can animate out smoothly
    var lastEvaluation by remember { mutableStateOf<EvaluationSession?>(null) }
    LaunchedEffect(uiState.evaluationSession) {
        uiState.evaluationSession?.let { lastEvaluation = it }
    }

    LaunchedEffect(Unit) {
        viewModel.checkMicrophonePermission()
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
        if (uiState.chats.isNotEmpty() && lastChatMessageLength > 0) {
            // Throttle scroll updates during streaming so Skiko Canvas maintains a smooth 60fps on mobile
            delay(120)
            listState.scrollToItem(bottomAnchorIndex, scrollOffset = 10000)
        }
    }

    val stage = uiState.stage

    val guideState = rememberGuideState(screenKey = "chat") { key ->
        viewModel.markGuideCompleted(key)
    }

    LaunchedEffect(stage) {
        if (stage != null && !viewModel.isGuideCompleted("chat")) {
            delay(800)
            guideState.start(
                listOf(
                    GuideStep(
                        key = "chat_mic",
                        title = "ضبط و ارسال صدا",
                        description = "برای صحبت کردن به زبان انگلیسی، این دکمه را لمس کنید. هوش مصنوعی صدای شما را می‌شنود و تحلیل می‌کند.",
                        icon = Res.drawable.ic_microphone,
                        alignment = GuideAlignment.Top
                    ),
                    GuideStep(
                        key = "chat_objective",
                        title = "اهداف مرحله",
                        description = "از اینجا ماموریت‌ها و اهداف این مکالمه را مشاهده کنید تا بدانید سناریو درباره چیست.",
                        icon = Res.drawable.ic_lamp,
                        alignment = GuideAlignment.Bottom
                    ),
                    GuideStep(
                        key = "chat_hint",
                        title = "درخواست راهنمایی",
                        description = "اگر در پاسخ دادن به انگلیسی مطمئن نبودید، این دکمه جملات پیشنهادی متناسب با گفتگو را به شما ارائه می‌دهد.",
                        icon = Res.drawable.ic_spell_check,
                        alignment = GuideAlignment.Top
                    )
                )
            )
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        AsyncStageBackground(
            backgroundUrl = stage?.backgroundUrl,
            modifier = modifier,
            enableFrostedGlass = false
        ) {
            val isDark = AppTheme.isDark
            val vignetteBrush = if (isDark) {
                Brush.verticalGradient(
                    0f to Color(0xCC070810),
                    0.2f to Color(0x38090C18),
                    0.55f to Color(0x55090C18),
                    1f to Color(0xF2070810)
                )
            } else {
                Brush.verticalGradient(
                    0f to Color(0x33000000),
                    0.2f to Color(0x0D000000),
                    0.55f to Color(0x1A000000),
                    1f to Color(0x55000000)
                )
            }
            // High-clarity Apple vignette scrims: lets artwork shine through while keeping glass chrome legible
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(vignetteBrush)
            )

            Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
                // Header HUD
                ChatToolbar(
                    stage = stage,
                    userLevel = uiState.userLevel,
                    turnsCount = uiState.turnsCount,
                    isFinishing = uiState.isSubmittingEvaluation,
                    onBackClick = onNavigateBack,
                    onObjectiveClick = { showObjectiveSheet = true },
                    onFinishConversationClick = { viewModel.submitEvaluation() },
                    objectiveModifier = Modifier.guideTarget("chat_objective", guideState)
                )

                // Main Transcript Viewport
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    if (uiState.stageLoadError != null) {
                        StageLoadErrorState(
                            title = if (stage != null) "خطا در شروع گفتگو" else "خطا در باز کردن مرحله",
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

                        // Conversation reads LTR (English)
                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                            LazyColumn(
                                state = listState,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 14.dp),
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
                                                onStopVoice = { viewModel.stopAiVoice(chat.uid) },
                                                onCopied = { toastMessage = "متن پیام در کلیپ‌بورد کپی شد" }
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

                if (uiState.stageLoadError == null && stage != null) {
                    // Floating Hint Suggestion Cue
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
                        onDismiss = { viewModel.dismissHint() },
                        onRetry = { viewModel.requestHint() }
                    )

                    // Floating Liquid Glass Bottom Dock
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
                        onHintClick = { showHintConfirmDialog = true },
                        micModifier = Modifier.guideTarget("chat_mic", guideState),
                        hintModifier = Modifier.guideTarget("chat_hint", guideState)
                    )
                }
            }

            // Objective Bottom Sheet
            ObjectiveSheet(
                visible = showObjectiveSheet,
                stage = stage,
                onDismiss = { showObjectiveSheet = false }
            )

            // Hint Confirmation Modal
            HintConfirmDialog(
                visible = showHintConfirmDialog,
                hintsUsedCount = uiState.hintsUsedCount,
                onConfirm = {
                    showHintConfirmDialog = false
                    viewModel.requestHint()
                },
                onDismiss = { showHintConfirmDialog = false }
            )

            // Microphone Permission Modal
            MicPermissionDialog(
                visible = uiState.showMicPermissionDialog,
                isPermanentlyDenied = uiState.isMicPermissionPermanentlyDenied,
                onGrant = { viewModel.requestMicrophonePermission() },
                onDismiss = { viewModel.dismissMicPermissionDialog() }
            )

            // Evaluation / Stage Complete Modal
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

            // Top Floating Toast for Copied Feedback
            androidx.compose.animation.AnimatedVisibility(
                visible = toastMessage != null,
                enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.slideInVertically { -it },
                exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.slideOutVertically { -it },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(AppleChatTheme.GlassPanelRaised)
                        .border(1.dp, AppleChatTheme.SpecularBorderColor, RoundedCornerShape(50))
                        .padding(horizontal = 18.dp, vertical = 10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        androidx.compose.material3.Icon(
                            painter = painterResource(Res.drawable.ic_done),
                            contentDescription = null,
                            tint = AppleChatTheme.SuccessCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        GameText(
                            text = toastMessage.orEmpty(),
                            size = 13.sp,
                            bold = true,
                            color = Game.TextPrimary
                        )
                    }
                }
            }

            // In-app onboarding & feature spotlight tour
            AppGuideHost(state = guideState)
        }
    }
}

/**
 * Liquid Glass Loading State Pill with Royal Azure Spinner.
 */
@Composable
private fun LoadingState(modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(22.dp)
    Box(
        modifier = modifier
            .clip(shape)
            .background(AppleChatTheme.GlassPanelRaised)
            .border(1.dp, AppleChatTheme.SpecularStrokeBrush, shape)
            .padding(horizontal = 22.dp, vertical = 14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.5.dp,
                color = AppleChatTheme.PrimaryAzure
            )
            GameText(
                text = "در حال آماده‌سازی گفتگو…",
                size = 14.sp,
                color = Game.TextSecondary
            )
        }
    }
}

/**
 * Liquid Glass Error Card with Coral header and Royal Azure retry action.
 */
@Composable
private fun StageLoadErrorState(
    title: String = "خطا در باز کردن مرحله",
    errorMessage: String,
    onRetry: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(24.dp)
    Box(
        modifier = modifier
            .padding(horizontal = 32.dp)
            .clip(shape)
            .background(AppleChatTheme.GlassPanelRaised)
            .border(1.dp, AppleChatTheme.Coral.copy(alpha = 0.5f), shape)
            .padding(22.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            GameText(
                text = title,
                size = 16.sp,
                bold = true,
                color = AppleChatTheme.Coral
            )
            GameText(
                text = errorMessage,
                size = 13.sp,
                lineHeight = 21.sp,
                color = Game.TextSecondary,
                align = androidx.compose.ui.text.style.TextAlign.Center
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                GameChip(
                    text = "تلاش مجدد",
                    accent = AppleChatTheme.PrimaryAzure,
                    border = AppleChatTheme.PrimaryAzure.copy(alpha = 0.5f),
                    active = true,
                    onClick = onRetry
                )
                GameChip(
                    text = "بازگشت",
                    accent = Game.TextSecondary,
                    border = AppleChatTheme.SubtleBorderColor,
                    active = false,
                    onClick = onBack
                )
            }
        }
    }
}

/**
 * Inviting Apple visionOS Floating Glass Card when learner opens the conversation.
 */
@Composable
private fun YourTurnState(
    characterName: String,
    avatarUrl: String?,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(26.dp)
    Box(
        modifier = modifier
            .padding(horizontal = 32.dp)
            .clip(shape)
            .background(AppleChatTheme.GlassPanelRaised)
            .border(1.dp, AppleChatTheme.SpecularStrokeBrush, shape)
            .padding(22.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            GameAvatar(
                name = characterName,
                imageUrl = avatarUrl,
                size = 58.dp,
                ring = AppleChatTheme.PrimaryAzure
            )
            GameText(
                text = "نوبت توست!",
                size = 18.sp,
                bold = true
            )
            GameText(
                text = "$characterName منتظر توست. اولین جمله را به انگلیسی بگو.",
                size = 13.sp,
                lineHeight = 22.sp,
                color = Game.TextSecondary,
                align = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

/**
 * AI Typing Bubble with Siri Iridescent Pulsing Dots.
 */
@Composable
private fun TypingBubble(
    characterName: String,
    avatarUrl: String?,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "typing_bubble")
    val dotColors = listOf(
        AppleChatTheme.SiriViolet,
        AppleChatTheme.SiriCyan,
        AppleChatTheme.SiriAzure
    )

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        GameAvatar(
            name = characterName,
            imageUrl = avatarUrl,
            size = 36.dp,
            ring = AppleChatTheme.SpecularBorderColor
        )
        val shape = RoundedCornerShape(
            topStart = 6.dp,
            topEnd = 20.dp,
            bottomEnd = 20.dp,
            bottomStart = 20.dp
        )
        Row(
            modifier = Modifier
                .clip(shape)
                .background(AppleChatTheme.BubbleAi)
                .border(1.dp, AppleChatTheme.SpecularStrokeBrush, shape)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
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
                    label = "bubble_dot_$index"
                )
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .alpha(alpha)
                        .background(dotColors[index % dotColors.size], CircleShape)
                )
            }
        }
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
