package ir.aispeaking.chat.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.aispeaking.domain.model.stt.SttConnectionStatus
import ir.aispeaking.domain.model.stt.SttEngine
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.ic_clear
import ir.aispeaking.sharedui.ic_edit
import ir.aispeaking.sharedui.ic_keyboard
import ir.aispeaking.sharedui.ic_lamp
import ir.aispeaking.sharedui.ic_microphone
import ir.aispeaking.sharedui.ic_refresh
import ir.aispeaking.sharedui.ic_send
import ir.aispeaking.sharedui.ui.game.Game
import ir.aispeaking.sharedui.ui.game.GameIconButton
import ir.aispeaking.sharedui.ui.game.GameText
import ir.aispeaking.sharedui.ui.them.AppTheme
import org.jetbrains.compose.resources.painterResource

enum class ChatInputMode {
    VOICE,
    TEXT
}

/**
 * 2026 Apple Liquid Glass Floating Dockless Footer for conversation.
 *
 * Implements:
 * - Completely dockless / containerless floating layout: No outer container pill background.
 * - Solid, defined bubble-like glass backgrounds for side buttons (`AppleChatTheme.GlassPanelRaised`).
 * - While AI is speaking, the entire footer hides smoothly with slide-down + fade-out.
 * - In VOICE mode (idle): 3 floating buttons clustered closely in center (`clusterWidth ~202dp`).
 * - In RECORDING mode:
 *     - When preparing: Mic icon is replaced by glowing text "در حال آماده‌سازی…".
 *     - When active: Mic swells to 74dp with rich FILLED liquid blooming waves fading out smoothly.
 *     - Side buttons glide slowly and gracefully to the corners.
 *     - When recording ends: Side buttons smoothly glide back to center and mic returns to 58dp.
 * - In TEXT mode:
 *     - Side buttons slide to corners; STT-styled Input Box expands in center.
 *     - When input is empty, right action button displays Voice (mic); clicking toggles back to voice mode.
 *     - When text is typed, right action button smoothly morphs into Send.
 * - Real-time Voice Transcription Capsule without quotation marks.
 * - Disconnection warning capsule when speech socket drops.
 */
@Composable
fun ChatBottomBar(
    modifier: Modifier = Modifier,
    inputMode: ChatInputMode,
    text: String,
    isRecording: Boolean,
    isRecordingPreparing: Boolean = false,
    isModelSpeaking: Boolean = false,
    enabled: Boolean = true,
    hintsUsedCount: Int = 0,
    isRequestingHint: Boolean = false,
    sttEngine: SttEngine = SttEngine.APP,
    sttConnectionStatus: SttConnectionStatus = SttConnectionStatus.CONNECTED,
    onReconnectStt: () -> Unit = {},
    onTextChange: (String) -> Unit,
    onInputModeChange: (ChatInputMode) -> Unit,
    onVoiceToggle: (Boolean) -> Unit,
    onSendClick: () -> Unit,
    onHintClick: () -> Unit = {},
    micModifier: Modifier = Modifier,
    hintModifier: Modifier = Modifier
) {
    val isInteractive = enabled && !isModelSpeaking

    Column(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(
                WindowInsets.navigationBars.union(WindowInsets.ime).only(WindowInsetsSides.Bottom)
            )
    ) {
        // Warning banner when speech service connection is lost (voice mode)
        AnimatedVisibility(
            visible = inputMode == ChatInputMode.VOICE &&
                !isModelSpeaking &&
                sttEngine == SttEngine.APP &&
                (sttConnectionStatus == SttConnectionStatus.DISCONNECTED || sttConnectionStatus == SttConnectionStatus.ERROR),
            enter = fadeIn(tween(160)) + slideInVertically(tween(180)) { it / 2 },
            exit = fadeOut(tween(120)) + slideOutVertically(tween(140)) { it / 2 }
        ) {
            CompactConnectionWarningCapsule(
                onReconnect = onReconnectStt
            )
        }

        // Floating Transcript Preview Capsule (shown when speech recognition has recognized text and AI not speaking)
        AnimatedVisibility(
            visible = text.isNotBlank() && inputMode == ChatInputMode.VOICE && !isModelSpeaking,
            enter = fadeIn(tween(160)) + slideInVertically(tween(180)) { it / 2 },
            exit = fadeOut(tween(120)) + slideOutVertically(tween(140)) { it / 2 }
        ) {
            CompactTranscriptCapsule(
                text = text,
                enabled = isInteractive,
                onEdit = { onInputModeChange(ChatInputMode.TEXT) },
                onClear = { onTextChange("") }
            )
        }

        // Main Floating Controls: Hidden with fluid slide-down & fade-out while AI is responding
        AnimatedVisibility(
            visible = !isModelSpeaking,
            enter = fadeIn(tween(300, easing = FastOutSlowInEasing)) +
                slideInVertically(tween(340, easing = FastOutSlowInEasing)) { it },
            exit = fadeOut(tween(240, easing = FastOutSlowInEasing)) +
                slideOutVertically(tween(280, easing = FastOutSlowInEasing)) { it }
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp, top = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                val totalWidth = maxWidth
                // In idle Voice mode: compact cluster ~202dp.
                // In Recording or Text mode: side buttons glide smoothly to the edges!
                val clusterWidth = 202.dp
                val voicePadding = ((totalWidth - clusterWidth) / 2).coerceAtLeast(16.dp)

                val isClustered = inputMode == ChatInputMode.VOICE && !isRecording
                val targetPadding = if (isClustered) voicePadding else 16.dp

                val animatedPadding by animateDpAsState(
                    targetValue = targetPadding,
                    animationSpec = tween(
                        durationMillis = 650,
                        easing = FastOutSlowInEasing
                    ),
                    label = "floating_dock_padding"
                )

                // Physical LTR arrangement guarantees: Left = Suggest, Center = Voice/Input, Right = Action
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = animatedPadding),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 1. Physically LEFT: Suggest / Hint Lamp Button
                        AppleHintButton(
                            hintsUsedCount = hintsUsedCount,
                            isRequesting = isRequestingHint,
                            size = 48.dp,
                            iconSize = 22.dp,
                            enabled = isInteractive,
                            onClick = { if (isInteractive) onHintClick() },
                            modifier = hintModifier
                        )

                        // 2. Physically CENTER: Voice Cluster OR STT-styled Input Box
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = if (inputMode == ChatInputMode.TEXT) 8.dp else 0.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            AnimatedContent(
                                targetState = inputMode,
                                transitionSpec = {
                                    (fadeIn(animationSpec = tween(220, easing = FastOutSlowInEasing)) +
                                        scaleIn(initialScale = 0.90f, animationSpec = tween(220, easing = FastOutSlowInEasing))) togetherWith
                                        (fadeOut(animationSpec = tween(170, easing = FastOutSlowInEasing)) +
                                            scaleOut(targetScale = 0.90f, animationSpec = tween(170, easing = FastOutSlowInEasing)))
                                },
                                label = "center_control_morph",
                                contentAlignment = Alignment.Center
                            ) { mode ->
                                when (mode) {
                                    ChatInputMode.VOICE -> {
                                        // Voice Button / Preparation Capsule / Active Filled Waves
                                        Box(
                                            modifier = Modifier.size(
                                                width = if (isRecording && isRecordingPreparing) 170.dp else 130.dp,
                                                height = if (isRecording && !isRecordingPreparing) 84.dp else 72.dp
                                            ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            when {
                                                isRecording -> {
                                                    if (isRecordingPreparing) {
                                                        AppleRecordingPreparingButton(
                                                            onStop = { onVoiceToggle(false) },
                                                            modifier = micModifier
                                                        )
                                                    } else {
                                                        AppleRecordingActiveButton(
                                                            onStop = { onVoiceToggle(false) },
                                                            modifier = micModifier
                                                        )
                                                    }
                                                }

                                                sttEngine == SttEngine.APP && (sttConnectionStatus == SttConnectionStatus.DISCONNECTED || sttConnectionStatus == SttConnectionStatus.ERROR) -> {
                                                    AppleDisconnectedButton(
                                                        onReconnect = onReconnectStt,
                                                        size = 58.dp,
                                                        modifier = micModifier
                                                    )
                                                }

                                                sttEngine == SttEngine.APP && sttConnectionStatus == SttConnectionStatus.CONNECTING -> {
                                                    AppleConnectingButton(
                                                        size = 64.dp,
                                                        modifier = micModifier
                                                    )
                                                }

                                                else -> {
                                                    AppleRecordIdleFloatingButton(
                                                        hasText = text.isNotBlank(),
                                                        enabled = enabled,
                                                        onClick = { if (isInteractive) onVoiceToggle(true) },
                                                        modifier = micModifier
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    ChatInputMode.TEXT -> {
                                        // STT-styled Input Box loading between Suggest and Send/Voice
                                        AppleCompactInputField(
                                            text = text,
                                            isInteractive = isInteractive,
                                            isModelSpeaking = isModelSpeaking,
                                            onTextChange = onTextChange,
                                            onSendClick = onSendClick
                                        )
                                    }
                                }
                            }
                        }

                        // 3. Physically RIGHT: Action Button (Send / Keyboard / Voice)
                        // In Voice Mode: Send (if text ready) or Keyboard toggle
                        // In Text Mode: Send (if text entered) or Voice toggle (if empty)
                        val actionTarget = if (inputMode == ChatInputMode.VOICE) {
                            if (text.isNotBlank()) "SEND" else "KEYBOARD"
                        } else {
                            if (text.isNotBlank()) "SEND" else "VOICE"
                        }

                        AnimatedContent(
                            targetState = actionTarget,
                            transitionSpec = {
                                (fadeIn(animationSpec = tween(180)) + scaleIn(initialScale = 0.85f)) togetherWith
                                    (fadeOut(animationSpec = tween(150)) + scaleOut(targetScale = 0.85f))
                            },
                            label = "action_btn_morph"
                        ) { target ->
                            when (target) {
                                "SEND" -> {
                                    AppleSendButton(
                                        onClick = { if (isInteractive && text.isNotBlank()) onSendClick() },
                                        size = 48.dp,
                                        enabled = isInteractive && text.isNotBlank()
                                    )
                                }

                                "KEYBOARD" -> {
                                    GameIconButton(
                                        icon = Res.drawable.ic_keyboard,
                                        onClick = { if (isInteractive) onInputModeChange(ChatInputMode.TEXT) },
                                        contentDescription = "تایپ متنی",
                                        size = 48.dp,
                                        iconSize = 22.dp,
                                        container = if (AppTheme.isDark) AppleChatTheme.GlassPanelRaised else Color(0xF0FFFFFF),
                                        border = if (AppTheme.isDark) Color(0x38FFFFFF) else Color(0x28000000),
                                        modifier = Modifier.graphicsLayer { alpha = if (isInteractive) 1f else 0.38f }
                                    )
                                }

                                "VOICE" -> {
                                    GameIconButton(
                                        icon = Res.drawable.ic_microphone,
                                        onClick = { if (isInteractive) onInputModeChange(ChatInputMode.VOICE) },
                                        contentDescription = "مکالمه صوتی",
                                        size = 48.dp,
                                        iconSize = 22.dp,
                                        tint = Color(0xFF38BDF8),
                                        container = if (AppTheme.isDark) AppleChatTheme.GlassPanelRaised else Color(0xF0FFFFFF),
                                        border = if (AppTheme.isDark) Color(0x38FFFFFF) else Color(0x28000000),
                                        modifier = Modifier.graphicsLayer { alpha = if (isInteractive) 1f else 0.38f }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * STT-styled compact input box that smoothly loads between Suggest and Send/Voice buttons in Text mode.
 * Styled matching the STT transcript capsule (`AppleChatTheme.GlassPanelRaised`, specular border, 22dp shape).
 */
@Composable
private fun AppleCompactInputField(
    text: String,
    isInteractive: Boolean,
    isModelSpeaking: Boolean,
    onTextChange: (String) -> Unit,
    onSendClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val canSend = text.isNotBlank() && isInteractive
    val focusRequester = remember { FocusRequester() }
    val fieldShape = RoundedCornerShape(22.dp)
    val isDark = AppTheme.isDark

    val textStyle = TextStyle(
        color = if (isInteractive) Game.TextPrimary else Game.TextMuted,
        fontSize = 14.5.sp,
        lineHeight = 22.sp,
        fontFamily = AppTheme.typography.bodyLarge.fontFamily,
        textDirection = TextDirection.Ltr,
        textAlign = TextAlign.Start
    )

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Box(
            modifier = modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            BasicTextField(
                value = text,
                onValueChange = { if (isInteractive) onTextChange(it) },
                enabled = isInteractive,
                readOnly = !isInteractive,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
                    .semantics {
                        contentDescription = "پیام انگلیسی شما"
                    }
                    .onPreviewKeyEvent { event ->
                        if (event.type == KeyEventType.KeyDown && event.key == Key.Enter && !event.isShiftPressed) {
                            if (canSend) onSendClick()
                            true
                        } else false
                    },
                textStyle = textStyle,
                cursorBrush = SolidColor(AppleChatTheme.PrimaryAzure),
                maxLines = 4,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { if (canSend) onSendClick() }),
                decorationBox = { inner ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp, max = 110.dp)
                            .background(
                                if (isInteractive) AppleChatTheme.GlassPanelRaised else (if (isDark) Color(0x14FFFFFF) else Color(0x40FFFFFF)),
                                fieldShape
                            )
                            .border(
                                1.dp,
                                if (canSend) SolidColor(AppleChatTheme.PrimaryAzure.copy(alpha = 0.7f)) else AppleChatTheme.SpecularStrokeBrush,
                                fieldShape
                            )
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (text.isEmpty()) {
                            GameText(
                                text = if (isModelSpeaking) "Waiting for response…" else "Type in English…",
                                size = 13.5.sp,
                                latin = true,
                                color = Game.TextMuted
                            )
                        }
                        inner()
                    }
                }
            )
        }
    }
}

/**
 * Floating warning capsule above dock when speech service connection is dropped
 */
@Composable
private fun CompactConnectionWarningCapsule(
    onReconnect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 4.dp)
    ) {
        val shape = RoundedCornerShape(16.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(AppleChatTheme.Coral.copy(alpha = 0.14f))
                .border(1.dp, AppleChatTheme.Coral.copy(alpha = 0.5f), shape)
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(AppleChatTheme.Coral, CircleShape)
            )

            GameText(
                text = "ارتباط با سرور صوتی قطع است.",
                size = 12.sp,
                bold = true,
                color = AppleChatTheme.Coral,
                modifier = Modifier.weight(1f)
            )

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(AppleChatTheme.Coral.copy(alpha = 0.18f))
                    .border(0.8.dp, AppleChatTheme.Coral.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                    .applePressEffect(targetScale = 0.94f, onClick = onReconnect)
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_refresh),
                    contentDescription = "تلاش مجدد",
                    tint = AppleChatTheme.Coral,
                    modifier = Modifier.size(13.dp)
                )
                GameText(
                    text = "تلاش مجدد",
                    size = 11.sp,
                    bold = true,
                    color = AppleChatTheme.Coral
                )
            }
        }
    }
}

/**
 * Floating Real-Time Voice Transcription Preview Capsule (رونویسی همزمان صدا).
 * Plain English transcribed text (WITHOUT quotation marks) + pulsing cyan vertical cursor indicator.
 */
@Composable
private fun CompactTranscriptCapsule(
    text: String,
    enabled: Boolean = true,
    onEdit: () -> Unit,
    onClear: () -> Unit
) {
    val cursorTransition = rememberInfiniteTransition(label = "cursor_pulse")
    val cursorAlpha by cursorTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(650, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cursor_alpha"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 4.dp)
    ) {
        val shape = RoundedCornerShape(20.dp)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(AppleChatTheme.GlassPanelRaised)
                .border(1.dp, AppleChatTheme.SpecularStrokeBrush, shape)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header Row (RTL: Right = Status Title, Left = Action Buttons)
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // RTL Start (Right): Green Live Dot + "رونویسی همزمان صدا"
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .background(Color(0xFF34D399), CircleShape)
                        )
                        GameText(
                            text = "رونویسی همزمان صدا",
                            size = 12.sp,
                            bold = true,
                            color = Color(0xFF34D399)
                        )
                    }

                    // RTL End (Left): Action Pills ("ویرایش متن", "پاک کردن")
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Button 1: Edit Text Pill
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(AppleChatTheme.PrimaryAzure.copy(alpha = 0.12f))
                                .border(0.8.dp, AppleChatTheme.PrimaryAzure.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
                                .applePressEffect(enabled = enabled, targetScale = 0.94f, onClick = { if (enabled) onEdit() })
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_edit),
                                contentDescription = "ویرایش متن",
                                tint = AppleChatTheme.SiriCyan,
                                modifier = Modifier.size(12.dp)
                            )
                            GameText(
                                text = "ویرایش متن",
                                size = 11.sp,
                                bold = true,
                                color = AppleChatTheme.SiriCyan
                            )
                        }

                        // Button 2: Clear Pill
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(AppleChatTheme.Coral.copy(alpha = 0.12f))
                                .border(0.8.dp, AppleChatTheme.Coral.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
                                .applePressEffect(enabled = enabled, targetScale = 0.94f, onClick = { if (enabled) onClear() })
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_clear),
                                contentDescription = "پاک کردن",
                                tint = AppleChatTheme.Coral,
                                modifier = Modifier.size(12.dp)
                            )
                            GameText(
                                text = "پاک کردن",
                                size = 11.sp,
                                bold = true,
                                color = AppleChatTheme.Coral
                            )
                        }
                    }
                }
            }

            // Body Row: English transcribed text (WITHOUT quotation marks) + Glowing Cyan Pulse Cursor
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    GameText(
                        text = text,
                        size = 14.5.sp,
                        lineHeight = 21.sp,
                        latin = true,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        color = Game.TextPrimary
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    // Glowing vertical cursor
                    Box(
                        modifier = Modifier
                            .width(2.5.dp)
                            .height(16.dp)
                            .graphicsLayer { this.alpha = cursorAlpha }
                            .clip(RoundedCornerShape(1.5.dp))
                            .background(AppleChatTheme.SiriCyan)
                    )
                }
            }
        }
    }
}

/**
 * Microphone button in Preparation / Priming state.
 * Replaces the voice icon with a glowing text: "در حال آماده‌سازی…"
 * as explicitly requested by user.
 */
@Composable
private fun AppleRecordingPreparingButton(
    onStop: () -> Unit,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "preparing_wave")
    val pulseAlpha by transition.animateFloat(
        initialValue = 0.40f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(650, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "preparing_alpha"
    )

    val amberColor = Color(0xFFFFB236)
    val peachColor = Color(0xFFFF7A45)

    Box(
        modifier = modifier
            .height(52.dp)
            .applePressEffect(targetScale = 0.95f, onClick = onStop)
            .clip(RoundedCornerShape(26.dp))
            .background(AppleChatTheme.GlassPanelRaised)
            .border(
                1.4.dp,
                Brush.horizontalGradient(
                    listOf(
                        peachColor.copy(alpha = pulseAlpha),
                        amberColor.copy(alpha = pulseAlpha * 0.8f),
                        peachColor.copy(alpha = pulseAlpha)
                    )
                ),
                RoundedCornerShape(26.dp)
            )
            .padding(horizontal = 18.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .graphicsLayer { alpha = pulseAlpha }
                    .background(amberColor, CircleShape)
            )
            GameText(
                text = "در حال آماده‌سازی…",
                size = 13.sp,
                bold = true,
                color = amberColor
            )
        }
    }
}

/**
 * Active recording button enlarged to 74dp with FILLED concentric blooming liquid waves
 * that expand and decay smoothly into transparency, tuned with subscription colors.
 */
@Composable
private fun AppleRecordingActiveButton(
    onStop: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "recording_filled_waves")
    val wave1Progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave1"
    )
    val wave2Progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, delayMillis = 500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave2"
    )
    val wave3Progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, delayMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave3"
    )

    val micPulseScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mic_scale"
    )

    val colorMint = Color(0xFF22C55E)   // Fresh Mint
    val colorSky = Color(0xFF0EA5E9)    // Sky Blue
    val colorPeach = Color(0xFFFF7A45)  // Warm Peach

    Box(
        modifier = modifier.size(130.dp),
        contentAlignment = Alignment.Center
    ) {
        // FILLED concentric blooming liquid waves that expand and decay in opacity
        Canvas(modifier = Modifier.size(130.dp)) {
            val centerOffset = center
            val baseRadius = 37.dp.toPx()
            val maxExpansion = 26.dp.toPx() // expands up to 63dp radius (126dp diameter)

            // Wave 1: Mint
            val r1 = baseRadius + wave1Progress * maxExpansion
            val a1 = (1f - wave1Progress).coerceIn(0f, 1f) * 0.45f
            if (a1 > 0f) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            colorMint.copy(alpha = a1),
                            colorMint.copy(alpha = a1 * 0.45f),
                            Color.Transparent
                        ),
                        center = centerOffset,
                        radius = r1
                    ),
                    radius = r1,
                    center = centerOffset
                )
            }

            // Wave 2: Sky Blue
            val r2 = baseRadius + wave2Progress * maxExpansion
            val a2 = (1f - wave2Progress).coerceIn(0f, 1f) * 0.40f
            if (a2 > 0f) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            colorSky.copy(alpha = a2),
                            colorSky.copy(alpha = a2 * 0.40f),
                            Color.Transparent
                        ),
                        center = centerOffset,
                        radius = r2
                    ),
                    radius = r2,
                    center = centerOffset
                )
            }

            // Wave 3: Warm Peach
            val r3 = baseRadius + wave3Progress * maxExpansion
            val a3 = (1f - wave3Progress).coerceIn(0f, 1f) * 0.35f
            if (a3 > 0f) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            colorPeach.copy(alpha = a3),
                            colorPeach.copy(alpha = a3 * 0.35f),
                            Color.Transparent
                        ),
                        center = centerOffset,
                        radius = r3
                    ),
                    radius = r3,
                    center = centerOffset
                )
            }
        }

        // Center Voice Button: Enlarged to 74dp with rich gradient & white mic
        Box(
            modifier = Modifier
                .size(74.dp)
                .graphicsLayer {
                    scaleX = micPulseScale
                    scaleY = micPulseScale
                }
                .applePressEffect(targetScale = 0.94f, onClick = onStop)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(
                            Color(0xFF0EA5E9),
                            Color(0xFF6366F1),
                            Color(0xFF1E1B4B)
                        )
                    )
                )
                .border(2.dp, Color.White.copy(alpha = 0.55f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_microphone),
                contentDescription = "در حال ضبط صدا - لمس برای توقف",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

/**
 * Center microphone button in Idle Floating State (58dp).
 * Displays prominent circular mic button with subtle concentric glass rings
 * fading into background.
 */
@Composable
private fun AppleRecordIdleFloatingButton(
    hasText: Boolean,
    enabled: Boolean = true,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "idle_floating_breathing")
    val breathGlow by transition.animateFloat(
        initialValue = 0.20f,
        targetValue = 0.45f,
        animationSpec = infiniteRepeatable(tween(1600, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "breath_glow"
    )
    val ringPulse by transition.animateFloat(
        initialValue = 0.10f,
        targetValue = 0.22f,
        animationSpec = infiniteRepeatable(tween(2200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "ring_pulse"
    )

    val skyBlue = Color(0xFF0EA5E9)

    Box(
        modifier = modifier.size(106.dp),
        contentAlignment = Alignment.Center
    ) {
        // Concentric Subtle Glass Rings fading into background (matching user reference image)
        Canvas(modifier = Modifier.size(106.dp)) {
            val centerOffset = center
            // Ring 2 (outermost, ~96dp diameter)
            drawCircle(
                color = skyBlue.copy(alpha = ringPulse * 0.5f),
                radius = 48.dp.toPx(),
                center = centerOffset,
                style = Stroke(width = 1.dp.toPx())
            )
            // Ring 1 (inner ring, ~76dp diameter)
            drawCircle(
                color = skyBlue.copy(alpha = ringPulse),
                radius = 38.dp.toPx(),
                center = centerOffset,
                style = Stroke(width = 1.2.dp.toPx())
            )
            // Soft ambient radial aura
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        skyBlue.copy(alpha = breathGlow * 0.25f),
                        Color.Transparent
                    ),
                    center = centerOffset,
                    radius = 38.dp.toPx()
                ),
                radius = 38.dp.toPx(),
                center = centerOffset
            )
        }

        // Center Button (58dp diameter)
        Box(
            modifier = Modifier
                .size(58.dp)
                .applePressEffect(enabled = enabled, targetScale = 0.94f, onClick = onClick)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(
                            skyBlue.copy(alpha = breathGlow * 0.8f + 0.12f),
                            Color(0x33101C2E),
                            Color(0x1F0B111F)
                        )
                    )
                )
                .border(1.4.dp, skyBlue.copy(alpha = breathGlow * 0.7f + 0.35f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_microphone),
                contentDescription = "ضبط صدا",
                tint = if (enabled) Color(0xFF38BDF8) else Game.TextMuted,
                modifier = Modifier.size(26.dp)
            )
        }
    }
}

/**
 * Disconnected state button in center dock with 1-tap reconnect action
 */
@Composable
private fun AppleDisconnectedButton(
    onReconnect: () -> Unit,
    size: Dp = 58.dp,
    modifier: Modifier = Modifier
) {
    val color = AppleChatTheme.Coral

    Box(
        modifier = modifier
            .size(size)
            .applePressEffect(targetScale = 0.94f, onClick = onReconnect)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.16f))
            .border(1.2.dp, color.copy(alpha = 0.6f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(Res.drawable.ic_refresh),
            contentDescription = "اتصال مجدد به سرویس صوتی",
            tint = color,
            modifier = Modifier.size(24.dp)
        )
    }
}

/**
 * Modern orbital dual-gradient ring shimmer for connecting state.
 * Rotating orbital halo around the 58dp mic.
 */
@Composable
private fun AppleConnectingButton(
    size: Dp = 64.dp,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "connecting_orbital")
    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbital_rot"
    )
    val pulseAlpha by transition.animateFloat(
        initialValue = 0.45f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orbital_pulse"
    )

    val skyBlue = Color(0xFF0EA5E9)
    val mintGreen = Color(0xFF22C55E)

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        // Rotating Orbital Shimmer Halo around the button
        Canvas(
            modifier = Modifier
                .size(size)
                .graphicsLayer { rotationZ = rotation }
        ) {
            val sweepBrush = Brush.sweepGradient(
                listOf(
                    skyBlue.copy(alpha = pulseAlpha),
                    mintGreen.copy(alpha = pulseAlpha * 0.7f),
                    Color.Transparent,
                    skyBlue.copy(alpha = pulseAlpha)
                )
            )
            drawCircle(
                brush = sweepBrush,
                radius = (size.toPx() / 2f) - 2.dp.toPx(),
                center = center,
                style = Stroke(width = 1.6.dp.toPx())
            )
        }

        // Center mic button (54dp)
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(Color(0x1A0EA5E9))
                .border(1.dp, skyBlue.copy(alpha = 0.45f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_microphone),
                contentDescription = "در حال اتصال به سرور صوتی",
                tint = skyBlue.copy(alpha = pulseAlpha),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

/** Apple circular vibrant Send button with Royal Azure gradient */
@Composable
private fun AppleSendButton(
    onClick: () -> Unit,
    size: Dp = 48.dp,
    enabled: Boolean = true
) {
    Box(
        modifier = Modifier
            .size(size)
            .graphicsLayer { alpha = if (enabled) 1f else 0.4f }
            .applePressEffect(enabled = enabled, targetScale = 0.92f, onClick = onClick)
            .clip(CircleShape)
            .background(
                Brush.verticalGradient(
                    listOf(AppleChatTheme.PrimaryAzure, AppleChatTheme.PrimaryIndigo)
                )
            )
            .border(1.2.dp, Color.White.copy(alpha = 0.35f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(Res.drawable.ic_send),
            contentDescription = "ارسال پیام",
            tint = Color.White,
            modifier = Modifier.size(22.dp)
        )
    }
}

/**
 * Apple compact circular Hint / Suggest button using `lamp.svg` icon.
 * Features a solid glass background matching chat bubbles (`AppleChatTheme.GlassPanelRaised`).
 */
@Composable
private fun AppleHintButton(
    hintsUsedCount: Int,
    isRequesting: Boolean,
    size: Dp = 48.dp,
    iconSize: Dp = 22.dp,
    enabled: Boolean = true,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = AppTheme.isDark
    val isInteractive = enabled && !isRequesting

    val goldColor = Color(0xFFFFB236)
    val peachColor = Color(0xFFFF7A45)

    val transition = rememberInfiniteTransition(label = "hint_pulse")
    val requestingGlow by transition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(650, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "requesting_glow"
    )

    Box(
        modifier = modifier
            .size(size)
            .graphicsLayer { alpha = if (enabled) 1f else 0.38f }
            .applePressEffect(enabled = isInteractive, targetScale = 0.92f, onClick = onClick)
            .clip(CircleShape)
            .background(
                if (isRequesting) {
                    peachColor.copy(alpha = 0.28f * requestingGlow + 0.15f)
                } else if (hintsUsedCount > 0) {
                    goldColor.copy(alpha = 0.28f)
                } else if (isDark) {
                    AppleChatTheme.GlassPanelRaised
                } else {
                    Color(0xF0FFFFFF)
                }
            )
            .border(
                1.2.dp,
                if (isRequesting) {
                    SolidColor(peachColor.copy(alpha = requestingGlow))
                } else if (hintsUsedCount > 0) {
                    SolidColor(goldColor.copy(alpha = 0.70f))
                } else {
                    AppleChatTheme.SpecularStrokeBrush
                },
                CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        // Lamp icon (new lamp.svg)
        Icon(
            painter = painterResource(Res.drawable.ic_lamp),
            contentDescription = "راهنما",
            tint = if (isRequesting) peachColor else if (hintsUsedCount > 0) goldColor else Game.TextSecondary,
            modifier = Modifier.size(iconSize)
        )

        // Hints count badge
        if (hintsUsedCount > 0 && !isRequesting) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(3.dp)
                    .size(8.dp)
                    .background(goldColor, CircleShape)
            )
        }
    }
}
