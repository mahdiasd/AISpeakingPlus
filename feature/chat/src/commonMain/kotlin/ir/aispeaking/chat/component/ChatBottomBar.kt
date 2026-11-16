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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
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
        // Shifting color palette inspired by the subscription screen (Sky Blue, Mint, Warm Peach, Indigo)
        val recordingBorderColors = remember {
            listOf(
                Color(0x990EA5E9), // 6-Month Sky Blue
                Color(0x9922C55E), // 3-Month Fresh Mint
                Color(0x99FF7A45), // 1-Month Warm Peach
                Color(0x99818CF8), // Lavender / Electric Indigo
                Color(0x990EA5E9)  // Seamless loop
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

            // Layer C: Attached Live Badges (Floats at bottom center, overflowing dock edge)
            if (isModelSpeaking) {
                // Attached AI Speaking Live Badge (Sky Blue from subscription screen)
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .offset(y = 10.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xE60A1726))
                        .border(0.8.dp, Color(0xFF0EA5E9).copy(alpha = 0.65f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .graphicsLayer { this.alpha = liveDotAlpha }
                            .background(Color(0xFF0EA5E9), CircleShape)
                    )
                    GameText(
                        text = "دستیار در حال صحبت…",
                        size = 10.5.sp,
                        bold = true,
                        color = Color(0xFF0EA5E9)
                    )
                }
            } else if (isRecording && !isRecordingPreparing) {
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .offset(y = 10.dp)
                        .applePressEffect(targetScale = 0.94f, onClick = { onVoiceToggle(false) })
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xE60D1F17))
                        .border(0.8.dp, Color(0xFF22C55E).copy(alpha = 0.70f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .graphicsLayer { this.alpha = liveDotAlpha }
                            .background(Color(0xFF22C55E), CircleShape)
                    )
                    GameText(
                        text = "در حال شنیدن",
                        size = 10.5.sp,
                        bold = true,
                        color = Color(0xFF22C55E)
                    )
                }
            } else if (isRecording && isRecordingPreparing) {
                // Attached preparation badge (Warm Peach / Amber from subscription screen)
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .offset(y = 10.dp)
                        .applePressEffect(targetScale = 0.94f, onClick = { onVoiceToggle(false) })
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xE626150D))
                        .border(0.8.dp, Color(0xFFFF7A45).copy(alpha = 0.70f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .background(Color(0xFFFF7A45), CircleShape)
                    )
                    GameText(
                        text = "آماده‌سازی میکروفون…",
                        size = 10.5.sp,
                        bold = true,
                        color = Color(0xFFFF7A45)
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

            // 2. Physically CENTER: Voice Recording Button & Modern Living Waveform
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                contentAlignment = Alignment.Center
            ) {
                when {
                    isModelSpeaking -> {
                        // Modern Siri / Apple Intelligence Voice Waveform with Subscription Color Accents
                        AppleAiSpeakingWaveform()
                    }

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
 * Modern Siri / Apple Intelligence Voice Waveform displayed in center slot while AI is speaking.
 * Unconstrained by any circle container, radiating soft chromatic aura and dancing equalizer bars
 * in the subscription screen color harmonies (Warm Peach, Sky Blue, Electric Indigo, Mint, Amber Gold).
 */
@Composable
private fun AppleAiSpeakingWaveform(
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "ai_speaking_wave")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28318f, // 2 * PI
        animationSpec = infiniteRepeatable(
            animation = tween(1350, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_phase"
    )

    val auraPulse by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(850, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "aura_pulse"
    )

    // Palette from Subscription screen:
    // Bar 1: Warm Peach (Color(0xFFFF7A45) - 1 Month)
    // Bar 2: Sky Blue (Color(0xFF0EA5E9) - 6 Month)
    // Bar 3: Electric Indigo (Color(0xFF818CF8))
    // Bar 4: Fresh Mint (Color(0xFF22C55E) - 3 Month)
    // Bar 5: Amber Gold (Color(0xFFFFB236) - VIP)
    val barColors = remember {
        listOf(
            Color(0xFFFF7A45),
            Color(0xFF0EA5E9),
            Color(0xFF818CF8),
            Color(0xFF22C55E),
            Color(0xFFFFB236)
        )
    }

    Box(
        modifier = modifier
            .size(width = 110.dp, height = 52.dp),
        contentAlignment = Alignment.Center
    ) {
        // Unconstrained Soft Chromatic Radial Aura behind waveform (overflows dock vertically!)
        Canvas(modifier = Modifier.size(width = 120.dp, height = 76.dp)) {
            val centerOffset = center
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF0EA5E9).copy(alpha = 0.40f * auraPulse),
                        Color(0xFF818CF8).copy(alpha = 0.22f * auraPulse),
                        Color.Transparent
                    ),
                    center = centerOffset,
                    radius = 38.dp.toPx()
                ),
                radius = 38.dp.toPx(),
                center = centerOffset
            )
        }

        // 5 Dynamic Equalizer Soundwave Bars
        Canvas(modifier = Modifier.size(width = 72.dp, height = 34.dp)) {
            val barCount = 5
            val barWidthPx = 4.2.dp.toPx()
            val cornerRadius = CornerRadius(barWidthPx / 2f, barWidthPx / 2f)
            val gapPx = (size.width - (barCount * barWidthPx)) / (barCount - 1)
            val centerY = size.height / 2f
            val maxBarHeight = size.height * 0.90f
            val minBarHeight = 8.dp.toPx()

            for (i in 0 until barCount) {
                // Organic height oscillation using sine wave with phase offset
                val barPhase = phase + (i * 0.95f)
                val normalizedHeight = ((kotlin.math.sin(barPhase) + 1.0) / 2.0).toFloat()
                val currentHeight = minBarHeight + normalizedHeight * (maxBarHeight - minBarHeight)
                val left = i * (barWidthPx + gapPx)
                val top = centerY - currentHeight / 2f

                drawRoundRect(
                    color = barColors[i],
                    topLeft = Offset(left, top),
                    size = Size(barWidthPx, currentHeight),
                    cornerRadius = cornerRadius
                )
            }
        }
    }
}

/**
 * Microphone button in Preparation / Priming state.
 * Replaces trapped circular spinner with an unconstrained expanding dual-ring harmonic radar shimmer
 * using the warm Peach / Amber colors from the subscription screen.
 */
@Composable
private fun AppleRecordingPreparingButton(
    onStop: () -> Unit,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "preparing_wave")
    val pulseProgress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "preparing_progress"
    )

    val micScale by transition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(550, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mic_pulse"
    )

    val orangeColor = Color(0xFFFF7A45) // 1-Month Plan Warm Peach / Orange
    val amberColor = Color(0xFFFFB236)  // Radiant Gold Amber

    Box(
        modifier = modifier.size(96.dp),
        contentAlignment = Alignment.Center
    ) {
        // Expanding Harmonic Radar Aura (Overflowing outside 52dp dock!)
        Canvas(modifier = Modifier.size(96.dp)) {
            val centerOffset = center
            val baseRadius = 22.dp.toPx()
            val maxExpansion = 22.dp.toPx()

            val r = baseRadius + pulseProgress * maxExpansion
            val alpha = (1f - pulseProgress).coerceIn(0f, 1f) * 0.70f

            if (alpha > 0f) {
                drawCircle(
                    color = orangeColor.copy(alpha = alpha),
                    radius = r,
                    center = centerOffset,
                    style = Stroke(width = 1.6.dp.toPx())
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            amberColor.copy(alpha = alpha * 0.40f),
                            Color.Transparent
                        ),
                        center = centerOffset,
                        radius = r
                    ),
                    radius = r,
                    center = centerOffset
                )
            }
        }

        // Center Glass Mic Button with warm Amber/Peach glow
        Box(
            modifier = Modifier
                .size(46.dp)
                .graphicsLayer {
                    scaleX = micScale
                    scaleY = micScale
                }
                .applePressEffect(targetScale = 0.94f, onClick = onStop)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(
                            orangeColor.copy(alpha = 0.35f),
                            amberColor.copy(alpha = 0.18f)
                        )
                    )
                )
                .border(1.4.dp, amberColor.copy(alpha = 0.85f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_microphone),
                contentDescription = "در حال آماده‌سازی میکروفون",
                tint = amberColor,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

/**
 * Active recording button with multi-color concentric expanding acoustic pulse rings
 * radiating OUTSIDE the dock container boundaries (overflowing vertically),
 * tuned with the vibrant subscription screen harmonies (Mint, Sky Blue, Warm Peach).
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
            animation = tween(1600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring1_progress"
    )
    val ring2Progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, delayMillis = 420, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring2_progress"
    )
    val ring3Progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, delayMillis = 840, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring3_progress"
    )

    val micPulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(650, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mic_scale"
    )

    // Palette from Subscription screen:
    val colorMint = Color(0xFF22C55E)   // 3-Month Fresh Mint
    val colorSky = Color(0xFF0EA5E9)    // 6-Month Sky Blue
    val colorPeach = Color(0xFFFF7A45)  // 1-Month Warm Peach

    Box(
        modifier = modifier.size(104.dp),
        contentAlignment = Alignment.Center
    ) {
        // Multi-Color Concentric Rings: Size 104.dp, radiating far beyond the 52.dp dock capsule!
        Canvas(modifier = Modifier.size(104.dp)) {
            val centerOffset = center
            val baseRadius = 24.dp.toPx()
            val maxExpansion = 26.dp.toPx() // Expands up to 50.dp radius (100.dp diameter!)

            // Ring 1 (Fresh Mint)
            val r1 = baseRadius + ring1Progress * maxExpansion
            val a1 = (1f - ring1Progress).coerceIn(0f, 1f) * 0.65f
            if (a1 > 0f) {
                drawCircle(
                    color = colorMint.copy(alpha = a1),
                    radius = r1,
                    center = centerOffset,
                    style = Stroke(width = 1.4.dp.toPx())
                )
            }

            // Ring 2 (Sky Blue)
            val r2 = baseRadius + ring2Progress * maxExpansion
            val a2 = (1f - ring2Progress).coerceIn(0f, 1f) * 0.60f
            if (a2 > 0f) {
                drawCircle(
                    color = colorSky.copy(alpha = a2),
                    radius = r2,
                    center = centerOffset,
                    style = Stroke(width = 1.4.dp.toPx())
                )
            }

            // Ring 3 (Warm Peach)
            val r3 = baseRadius + ring3Progress * maxExpansion
            val a3 = (1f - ring3Progress).coerceIn(0f, 1f) * 0.50f
            if (a3 > 0f) {
                drawCircle(
                    color = colorPeach.copy(alpha = a3),
                    radius = r3,
                    center = centerOffset,
                    style = Stroke(width = 1.2.dp.toPx())
                )
            }
        }

        // Center Circular Record Button (Size 46.dp) with rich subscription gradient
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
                            Color(0x990EA5E9),
                            Color(0x666366F1),
                            Color(0x3322C55E)
                        )
                    )
                )
                .border(1.6.dp, colorSky.copy(alpha = 0.95f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_microphone),
                contentDescription = "در حال ضبط صدا - لمس برای توقف",
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

/**
 * Center microphone button in Idle State.
 * Displays glowing Sky Blue / Azure glass circle.
 * Note: Under-text removed per user requirement to keep height compact and match icons.
 */
@Composable
private fun AppleRecordIdleButton(
    hasText: Boolean,
    enabled: Boolean = true,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "idle_breathing")
    val breathGlow by transition.animateFloat(
        initialValue = 0.20f,
        targetValue = 0.45f,
        animationSpec = infiniteRepeatable(tween(1400, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "breath_glow"
    )

    val isDark = AppTheme.isDark
    val btnBg = if (!enabled) {
        if (isDark) Color(0x14FFFFFF) else Color(0x40FFFFFF)
    } else if (hasText) {
        if (isDark) Color(0x22FFFFFF) else Color(0xB8FFFFFF)
    } else {
        // Sky Blue from Subscription
        Color(0xFF0EA5E9).copy(alpha = breathGlow)
    }

    val btnBorder = if (!enabled) {
        AppleChatTheme.SubtleBorderColor.copy(alpha = 0.35f)
    } else if (hasText) {
        AppleChatTheme.SpecularBorderColor
    } else {
        Color(0xFF0EA5E9).copy(alpha = 0.65f)
    }

    val iconTint = if (!enabled) {
        Game.TextMuted
    } else if (hasText) {
        Game.TextPrimary
    } else {
        Color(0xFF38BDF8)
    }

    Box(
        modifier = modifier
            .size(46.dp)
            .applePressEffect(enabled = enabled, targetScale = 0.94f, onClick = onClick)
            .clip(CircleShape)
            .background(btnBg)
            .border(1.2.dp, btnBorder, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(Res.drawable.ic_microphone),
            contentDescription = "ضبط صدا",
            tint = iconTint,
            modifier = Modifier.size(21.dp)
        )
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
 * Modern orbital dual-gradient ring shimmer for connecting state.
 * Replaces the trapped spinner with an unconstrained rotating orbital halo around the mic.
 */
@Composable
private fun AppleConnectingButton(
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
        modifier = modifier.size(56.dp),
        contentAlignment = Alignment.Center
    ) {
        // Rotating Orbital Shimmer Halo around the button (unconstrained outside 46dp)
        Canvas(
            modifier = Modifier
                .size(54.dp)
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
                radius = 25.dp.toPx(),
                center = center,
                style = Stroke(width = 1.6.dp.toPx())
            )
        }

        // Center mic button
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Color(0x1A0EA5E9))
                .border(1.dp, skyBlue.copy(alpha = 0.45f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_microphone),
                contentDescription = "در حال اتصال به سرور صوتی",
                tint = skyBlue.copy(alpha = pulseAlpha),
                modifier = Modifier.size(20.dp)
            )
        }
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

/**
 * Apple compact circular Hint / Suggest button using the new downloaded `lamp.svg` icon.
 * Features warm Champagne Gold / Amber gradient styling matching the subscription screen.
 * When requesting hint, the lightbulb itself glows and breathes with an ambient radial golden halo,
 * completely replacing trapped circular spinners.
 */
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
                    peachColor.copy(alpha = 0.22f * requestingGlow + 0.10f)
                } else if (hintsUsedCount > 0) {
                    goldColor.copy(alpha = 0.22f)
                } else if (isDark) {
                    Color(0x1AFFFFFF)
                } else {
                    Color(0xB8FFFFFF)
                }
            )
            .border(
                1.dp,
                if (isRequesting) {
                    peachColor.copy(alpha = requestingGlow)
                } else if (hintsUsedCount > 0) {
                    goldColor.copy(alpha = 0.65f)
                } else {
                    AppleChatTheme.SubtleBorderColor
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
            modifier = Modifier.size(20.dp)
        )

        // Hints count badge
        if (hintsUsedCount > 0 && !isRequesting) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(2.dp)
                    .size(8.dp)
                    .background(goldColor, CircleShape)
            )
        }
    }
}
