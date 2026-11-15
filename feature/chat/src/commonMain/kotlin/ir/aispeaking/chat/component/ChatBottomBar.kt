package ir.aispeaking.chat.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
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
import androidx.compose.foundation.layout.offset
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
import ir.aispeaking.sharedui.ui.core.loading.AppLoadingIndicator
import ir.aispeaking.sharedui.ui.extension.animatedBorder
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
 * 2026 Apple Liquid Glass Floating Dock for conversation.
 *
 * Implements:
 * - Floating visionOS translucent glass capsule dock with compact height (~52dp).
 * - During active recording: Gentle floating animated rotating border colors while maintaining translucent glass.
 * - Center Glowing Mic button with expanding concentric rings radiating outside container boundaries.
 * - Attached live badge ("در حال شنیدن") floating at the bottom center.
 * - Send button is ALWAYS on the physical RIGHT side.
 * - No text under mic icon in idle state to keep dock clean and aligned with icons.
 * - No quotation marks in displayed transcript.
 * - Brief loading / preparation state before recording pulse begins to guarantee audio pipeline priming.
 * - Clear socket disconnection warning banner and reconnect triggers in dock.
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

        // Main Floating Liquid Glass Capsule Dock
        val dockShape = RoundedCornerShape(26.dp)
        val recordingBorderColors = remember {
            listOf(
                Color(0x8038BDF8), // Siri Cyan
                Color(0x80818CF8), // Electric Indigo
                Color(0x80C084FC), // Lavender Violet
                Color(0x8034D399), // Mint Emerald
                Color(0x8038BDF8)  // Seamless loop
            )
        }

        // Transition for live dot pulse in badge
        val infiniteTransition = rememberInfiniteTransition(label = "dock_live_indicator")
        val liveDotAlpha by infiniteTransition.animateFloat(
            initialValue = 0.35f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(550, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "live_dot_alpha"
        )

        Box(
            modifier = Modifier
                .padding(start = 14.dp, end = 14.dp, bottom = 10.dp, top = 4.dp)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            // Layer A: Dock Capsule Background with Specular / Animated Border (Height: 52.dp, Clipped)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(dockShape)
                    .background(AppleChatTheme.GlassPanel)
                    .then(
                        if (isRecording) {
                            Modifier.animatedBorder(
                                borderColors = recordingBorderColors,
                                shape = dockShape,
                                borderWidth = 1.5.dp,
                                animationDurationInMillis = 3200
                            )
                        } else {
                            Modifier.border(1.dp, AppleChatTheme.SpecularStrokeBrush, dockShape)
                        }
                    )
            )

            // Layer B: Dock Interactive Controls (NOT clipped by dock capsule, so pulse rings overflow)
            AnimatedContent(
                targetState = inputMode,
                transitionSpec = {
                    fadeIn(animationSpec = tween(220, easing = FastOutSlowInEasing)) +
                        scaleIn(initialScale = 0.94f, animationSpec = tween(220, easing = FastOutSlowInEasing)) togetherWith
                    fadeOut(animationSpec = tween(180, easing = FastOutSlowInEasing)) +
                        scaleOut(targetScale = 0.94f, animationSpec = tween(180, easing = FastOutSlowInEasing))
                },
                label = "dock_mode_morph",
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) { mode ->
                when (mode) {
                    ChatInputMode.VOICE -> CompactVoiceRow(
                        text = text,
                        isRecording = isRecording,
                        isRecordingPreparing = isRecordingPreparing,
                        isModelSpeaking = isModelSpeaking,
                        enabled = enabled,
                        hintsUsedCount = hintsUsedCount,
                        isRequestingHint = isRequestingHint,
                        sttEngine = sttEngine,
                        sttConnectionStatus = sttConnectionStatus,
                        onReconnectStt = onReconnectStt,
                        onSwitchToText = { onInputModeChange(ChatInputMode.TEXT) },
                        onVoiceToggle = onVoiceToggle,
                        onSendClick = onSendClick,
                        onHintClick = onHintClick,
                        micModifier = micModifier,
                        hintModifier = hintModifier
                    )

                    ChatInputMode.TEXT -> CompactTextRow(
                        text = text,
                        isModelSpeaking = isModelSpeaking,
                        enabled = enabled,
                        hintsUsedCount = hintsUsedCount,
                        isRequestingHint = isRequestingHint,
                        onTextChange = onTextChange,
                        onSwitchToVoice = { onInputModeChange(ChatInputMode.VOICE) },
                        onSendClick = onSendClick,
                        onHintClick = onHintClick
                    )
                }
            }

            // Layer C: Attached Live Badge "در حال شنیدن" (Floats at bottom center, overflowing dock edge)
            if (isRecording && !isRecordingPreparing) {
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .offset(y = 10.dp)
                        .applePressEffect(targetScale = 0.94f, onClick = { onVoiceToggle(false) })
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xE60D1F17))
                        .border(0.8.dp, Color(0xFF34D399).copy(alpha = 0.65f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .graphicsLayer { this.alpha = liveDotAlpha }
                            .background(Color(0xFF34D399), CircleShape)
                    )
                    GameText(
                        text = "در حال شنیدن",
                        size = 10.5.sp,
                        bold = true,
                        color = Color(0xFF34D399)
                    )
                }
            } else if (isRecording && isRecordingPreparing) {
                // Attached preparation badge
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .offset(y = 10.dp)
                        .applePressEffect(targetScale = 0.94f, onClick = { onVoiceToggle(false) })
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xE61F1A0D))
                        .border(0.8.dp, Color(0xFFFFB236).copy(alpha = 0.65f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .background(Color(0xFFFFB236), CircleShape)
                    )
                    GameText(
                        text = "در حال آماده‌سازی…",
                        size = 10.5.sp,
                        bold = true,
                        color = Color(0xFFFFB236)
                    )
                }
            } else if (sttEngine == SttEngine.APP && (sttConnectionStatus == SttConnectionStatus.DISCONNECTED || sttConnectionStatus == SttConnectionStatus.ERROR)) {
                // Attached disconnected badge
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .offset(y = 10.dp)
                        .applePressEffect(targetScale = 0.94f, onClick = onReconnectStt)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xE6260F0F))
                        .border(0.8.dp, AppleChatTheme.Coral.copy(alpha = 0.65f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .background(AppleChatTheme.Coral, CircleShape)
                    )
                    GameText(
                        text = "قطع ارتباط • لمس مجدد",
                        size = 10.5.sp,
                        bold = true,
                        color = AppleChatTheme.Coral
                    )
                }
            }
        }
    }
}

/**
 * Compact voice controls row:
 * LEFT (Visually Left): [Hint Button]
 * CENTER: [Overflowing Siri-Style Mic]
 * RIGHT (Visually Right): [Send Button OR Keyboard Toggle] - "دکمه ی ارسال باید سمت راست باشه"
 */
@Composable
private fun CompactVoiceRow(
    text: String,
    isRecording: Boolean,
    isRecordingPreparing: Boolean = false,
    isModelSpeaking: Boolean,
    enabled: Boolean,
    hintsUsedCount: Int,
    isRequestingHint: Boolean,
    sttEngine: SttEngine = SttEngine.APP,
    sttConnectionStatus: SttConnectionStatus = SttConnectionStatus.CONNECTED,
    onReconnectStt: () -> Unit = {},
    onSwitchToText: () -> Unit,
    onVoiceToggle: (Boolean) -> Unit,
    onSendClick: () -> Unit,
    onHintClick: () -> Unit,
    micModifier: Modifier = Modifier,
    hintModifier: Modifier = Modifier
) {
    val isInteractive = enabled && !isModelSpeaking

    // LTR positioning guarantees exact physical placement:
    // First item is physically LEFT, Middle is CENTER, Last item is physically RIGHT
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. Physically LEFT: Hint Button
            AppleHintButton(
                hintsUsedCount = hintsUsedCount,
                isRequesting = isRequestingHint,
                size = 42.dp,
                enabled = isInteractive,
                onClick = { if (isInteractive) onHintClick() },
                modifier = hintModifier
            )

            // 2. Physically CENTER: Voice Recording Button with Expanding Concentric Pulse Rings
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
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
                            modifier = micModifier
                        )
                    }

                    sttEngine == SttEngine.APP && sttConnectionStatus == SttConnectionStatus.CONNECTING -> {
                        AppleConnectingButton(modifier = micModifier)
                    }

                    else -> {
                        AppleRecordIdleButton(
                            hasText = text.isNotBlank(),
                            enabled = enabled,
                            isModelSpeaking = isModelSpeaking,
                            onClick = { if (isInteractive) onVoiceToggle(true) },
                            modifier = micModifier
                        )
                    }
                }
            }

            // 3. Physically RIGHT: Send Button (when text ready) OR Keyboard Toggle
            // "دکمه ی ارسال باید سمت راست باشه."
            if (text.isNotBlank()) {
                AppleSendButton(
                    onClick = { if (isInteractive) onSendClick() },
                    size = 42.dp,
                    enabled = isInteractive
                )
            } else {
                GameIconButton(
                    icon = Res.drawable.ic_keyboard,
                    onClick = { if (isInteractive) onSwitchToText() },
                    contentDescription = "تایپ متنی",
                    size = 42.dp,
                    iconSize = 20.dp,
                    container = if (AppTheme.isDark) Color(0x1AFFFFFF) else Color(0xB8FFFFFF),
                    border = AppleChatTheme.SubtleBorderColor,
                    modifier = Modifier.graphicsLayer { alpha = if (isInteractive) 1f else 0.38f }
                )
            }
        }
    }
}

/**
 * Compact text input row:
 * LEFT (Visually Left): [Mic Toggle Button]
 * CENTER: [Apple Input Field ("Type in English...")]
 * RIGHT (Visually Right): [Send Button] - "دکمه ی ارسال باید سمت راست باشه"
 */
@Composable
private fun CompactTextRow(
    text: String,
    isModelSpeaking: Boolean,
    enabled: Boolean,
    hintsUsedCount: Int,
    isRequestingHint: Boolean,
    onTextChange: (String) -> Unit,
    onSwitchToVoice: () -> Unit,
    onSendClick: () -> Unit,
    onHintClick: () -> Unit
) {
    val isInteractive = enabled && !isModelSpeaking
    val canSend = text.isNotBlank() && isInteractive
    val focusRequester = remember { FocusRequester() }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. Physically LEFT: Mode toggle to Voice
            GameIconButton(
                icon = Res.drawable.ic_microphone,
                onClick = { if (isInteractive) onSwitchToVoice() },
                contentDescription = "مکالمه صوتی",
                size = 42.dp,
                iconSize = 20.dp,
                container = if (AppTheme.isDark) Color(0x1AFFFFFF) else Color(0xB8FFFFFF),
                border = AppleChatTheme.SubtleBorderColor,
                modifier = Modifier.graphicsLayer { alpha = if (isInteractive) 1f else 0.38f }
            )

            // 2. Physically CENTER: Apple Compact Input Pill
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

            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
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
                                .heightIn(min = 40.dp, max = 110.dp)
                                .background(
                                    if (isInteractive) AppleChatTheme.GlassPanelRaised else (if (isDark) Color(0x14FFFFFF) else Color(0x40FFFFFF)),
                                    fieldShape
                                )
                                .border(
                                    1.dp,
                                    if (canSend) AppleChatTheme.PrimaryAzure.copy(alpha = 0.7f) else AppleChatTheme.SubtleBorderColor.copy(alpha = if (isInteractive) 1f else 0.4f),
                                    fieldShape
                                )
                                .padding(horizontal = 14.dp, vertical = 8.dp),
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

            // 3. Physically RIGHT: Send Button
            // "دکمه ی ارسال باید سمت راست باشه."
            AppleSendButton(
                onClick = { if (canSend) onSendClick() },
                size = 42.dp,
                enabled = canSend
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
 * Directly matches the reference design:
 * - Header (RTL):
 *   Right: Glowing emerald pulse dot + "رونویسی همزمان صدا"
 *   Left: "ویرایش متن" pill button & "پاک کردن" pill button
 * - Content:
 *   Plain English transcribed text (WITHOUT quotation marks) + pulsing cyan vertical cursor indicator.
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

            // Body Row: English transcribed text (WITHOUT quotation marks per user requirement) + Glowing Cyan Pulse Cursor
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
 * Microphone button in Preparation / Loading state.
 * Prevents user from talking before the audio engine and websocket handshake are primed.
 */
@Composable
private fun AppleRecordingPreparingButton(
    onStop: () -> Unit,
    modifier: Modifier = Modifier
) {
    val amberColor = Color(0xFFFFB236)

    Box(
        modifier = modifier
            .size(46.dp)
            .applePressEffect(targetScale = 0.94f, onClick = onStop)
            .clip(CircleShape)
            .background(amberColor.copy(alpha = 0.16f))
            .border(1.2.dp, amberColor.copy(alpha = 0.6f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        AppLoadingIndicator(
            size = 20.dp,
            color = amberColor
        )
    }
}

/**
 * Active recording button with Siri-like concentric expanding pulse rings radiating OUTSIDE
 * the dock container boundaries (overflowing vertically), matching the design in media_1794770094347_4998c27a.jpg.
 *
 * Performance tuned: Concentric rings are drawn in Canvas with zero runtime object allocations.
 */
@Composable
private fun AppleRecordingActiveButton(
    onStop: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "recording_rings")
    val ring1Progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring1_progress"
    )
    val ring2Progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, delayMillis = 480, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring2_progress"
    )

    val micPulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.07f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mic_scale"
    )

    val ringColor = Color(0xFF38BDF8)

    Box(
        modifier = modifier.size(100.dp),
        contentAlignment = Alignment.Center
    ) {
        // Expanding Concentric Rings: Size 100.dp, radiating far beyond the 52.dp dock capsule!
        Canvas(modifier = Modifier.size(100.dp)) {
            val centerOffset = center
            val baseRadius = 24.dp.toPx()
            val maxExpansion = 24.dp.toPx() // Expands up to 48.dp radius (96.dp diameter!)

            // Ring 1
            val r1 = baseRadius + ring1Progress * maxExpansion
            val a1 = (1f - ring1Progress).coerceIn(0f, 1f) * 0.55f
            if (a1 > 0f) {
                drawCircle(
                    color = ringColor.copy(alpha = a1),
                    radius = r1,
                    center = centerOffset,
                    style = Stroke(width = 1.4.dp.toPx())
                )
            }

            // Ring 2
            val r2 = baseRadius + ring2Progress * maxExpansion
            val a2 = (1f - ring2Progress).coerceIn(0f, 1f) * 0.55f
            if (a2 > 0f) {
                drawCircle(
                    color = ringColor.copy(alpha = a2),
                    radius = r2,
                    center = centerOffset,
                    style = Stroke(width = 1.4.dp.toPx())
                )
            }
        }

        // Center Circular Record Button (Size 46.dp)
        Box(
            modifier = Modifier
                .size(46.dp)
                .graphicsLayer {
                    scaleX = micPulseScale
                    scaleY = micPulseScale
                }
                .applePressEffect(targetScale = 0.94f, onClick = onStop)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(
                            Color(0x800A84FF),
                            Color(0x400066CC)
                        )
                    )
                )
                .border(1.5.dp, AppleChatTheme.SiriCyan.copy(alpha = 0.9f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_microphone),
                contentDescription = "در حال ضبط صدا - لمس برای توقف",
                tint = AppleChatTheme.SiriCyan,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

/**
 * Center microphone button in Idle State.
 * Displays glowing Siri cyan / Azure glass circle.
 * Note: Under-text removed per user requirement to keep height compact and match icons.
 */
@Composable
private fun AppleRecordIdleButton(
    hasText: Boolean,
    enabled: Boolean = true,
    isModelSpeaking: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isInteractive = enabled && !isModelSpeaking

    val transition = rememberInfiniteTransition(label = "idle_breathing")
    val breathGlow by transition.animateFloat(
        initialValue = 0.22f,
        targetValue = 0.44f,
        animationSpec = infiniteRepeatable(tween(1400, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "breath_glow"
    )

    val isDark = AppTheme.isDark
    val btnBg = if (!isInteractive) {
        if (isDark) Color(0x14FFFFFF) else Color(0x40FFFFFF)
    } else if (hasText) {
        if (isDark) Color(0x22FFFFFF) else Color(0xB8FFFFFF)
    } else {
        AppleChatTheme.PrimaryAzure.copy(alpha = breathGlow)
    }

    val btnBorder = if (!isInteractive) {
        AppleChatTheme.SubtleBorderColor.copy(alpha = 0.35f)
    } else if (hasText) {
        AppleChatTheme.SpecularBorderColor
    } else {
        AppleChatTheme.PrimaryAzure.copy(alpha = 0.6f)
    }

    val iconTint = if (!isInteractive) {
        Game.TextMuted
    } else if (hasText) {
        Game.TextPrimary
    } else {
        AppleChatTheme.SiriCyan
    }

    Box(
        modifier = modifier
            .size(46.dp)
            .applePressEffect(enabled = isInteractive, targetScale = 0.94f, onClick = onClick)
            .clip(CircleShape)
            .background(btnBg)
            .border(1.2.dp, btnBorder, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (isModelSpeaking) {
            AppLoadingIndicator(
                size = 18.dp,
                color = AppleChatTheme.PrimaryAzure.copy(alpha = 0.85f)
            )
        } else {
            Icon(
                painter = painterResource(Res.drawable.ic_microphone),
                contentDescription = "ضبط صدا",
                tint = iconTint,
                modifier = Modifier.size(21.dp)
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
    modifier: Modifier = Modifier
) {
    val color = AppleChatTheme.Coral

    Box(
        modifier = modifier
            .size(46.dp)
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
            modifier = Modifier.size(19.dp)
        )
    }
}

/**
 * Connecting state button in center dock
 */
@Composable
private fun AppleConnectingButton(
    modifier: Modifier = Modifier
) {
    val azure = AppleChatTheme.PrimaryAzure

    Box(
        modifier = modifier
            .size(46.dp)
            .clip(CircleShape)
            .background(azure.copy(alpha = 0.14f))
            .border(1.2.dp, azure.copy(alpha = 0.45f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        AppLoadingIndicator(
            size = 18.dp,
            color = azure
        )
    }
}

/** Apple circular vibrant Send button with Royal Azure gradient */
@Composable
private fun AppleSendButton(
    onClick: () -> Unit,
    size: Dp,
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
            .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(Res.drawable.ic_send),
            contentDescription = "ارسال پیام",
            tint = Color.White,
            modifier = Modifier.size(19.dp)
        )
    }
}

/** Apple compact circular Hint button with Champagne Gold badge */
@Composable
private fun AppleHintButton(
    hintsUsedCount: Int,
    isRequesting: Boolean,
    size: Dp = 42.dp,
    enabled: Boolean = true,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = AppTheme.isDark
    val isInteractive = enabled && !isRequesting
    Box(
        modifier = modifier
            .size(size)
            .graphicsLayer { alpha = if (enabled) 1f else 0.38f }
            .applePressEffect(enabled = isInteractive, targetScale = 0.92f, onClick = onClick)
            .clip(CircleShape)
            .background(
                if (hintsUsedCount > 0) AppleChatTheme.Gold.copy(alpha = 0.2f) else if (isDark) Color(0x1AFFFFFF) else Color(0xB8FFFFFF)
            )
            .border(
                1.dp,
                if (hintsUsedCount > 0) AppleChatTheme.Gold.copy(alpha = 0.55f) else AppleChatTheme.SubtleBorderColor,
                CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isRequesting) {
            AppLoadingIndicator(
                size = 16.dp,
                color = AppleChatTheme.Gold
            )
        } else {
            Icon(
                painter = painterResource(Res.drawable.ic_lamp),
                contentDescription = "راهنما",
                tint = if (hintsUsedCount > 0) AppleChatTheme.Gold else Game.TextSecondary,
                modifier = Modifier.size(18.dp)
            )
            if (hintsUsedCount > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(3.dp)
                        .size(8.dp)
                        .background(AppleChatTheme.Gold, CircleShape)
                )
            }
        }
    }
}
