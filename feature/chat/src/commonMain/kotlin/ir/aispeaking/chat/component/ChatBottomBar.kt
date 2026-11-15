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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
 * - Floating visionOS translucent glass capsule dock with specular top light rim.
 * - During active recording: Gentle floating animated rotating border colors (light, glassy wave)
 *   while maintaining translucent frosted glass inside.
 * - Center Glowing Mic button with Siri-like expanding concentric rings, breathing pulse,
 *   and attached live badge ("در حال شنیدن").
 * - Brief loading / preparation state before recording pulse begins to guarantee audio pipeline priming.
 * - Real-time Voice Transcription Card (رونویسی همزمان صدا) with explicit Edit / Clear actions.
 * - Smooth animated transition between Voice Mode and Text Mode.
 * - Persistent input mode state across AI responses without automatic keyboard disruption.
 * - Clear socket disconnection warning banner and reconnect triggers.
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
        val dockShape = RoundedCornerShape(30.dp)
        val recordingBorderColors = remember {
            listOf(
                Color(0x8038BDF8), // Siri Cyan
                Color(0x80818CF8), // Electric Indigo
                Color(0x80C084FC), // Lavender Violet
                Color(0x8034D399), // Mint Emerald
                Color(0x8038BDF8)  // Seamless loop
            )
        }

        val dockModifier = Modifier
            .padding(start = 14.dp, end = 14.dp, bottom = 12.dp, top = 4.dp)
            .fillMaxWidth()
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
            .padding(horizontal = 10.dp, vertical = 6.dp)

        Box(
            modifier = dockModifier,
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(
                targetState = inputMode,
                transitionSpec = {
                    fadeIn(animationSpec = tween(220, easing = FastOutSlowInEasing)) +
                        scaleIn(initialScale = 0.94f, animationSpec = tween(220, easing = FastOutSlowInEasing)) togetherWith
                    fadeOut(animationSpec = tween(180, easing = FastOutSlowInEasing)) +
                        scaleOut(targetScale = 0.94f, animationSpec = tween(180, easing = FastOutSlowInEasing))
                },
                label = "dock_mode_morph"
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
        }
    }
}

/**
 * Compact voice controls row (RTL flow: Start/Right = Keyboard, Center = Mic, End/Left = Send/Hint):
 * [Keyboard toggle] --- [Glowing Siri-Style Mic & Status Badge] --- [Send OR Hint]
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

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // RTL Start (Right Side): Mode toggle to Keyboard
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

            // Center: Interactive Circular Microphone Button with Expanding Glow / Rings
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 6.dp),
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

            // RTL End (Left Side): Send when text is ready, otherwise Hint button
            if (text.isNotBlank()) {
                AppleSendButton(
                    onClick = { if (isInteractive) onSendClick() },
                    size = 42.dp,
                    enabled = isInteractive
                )
            } else {
                AppleHintButton(
                    hintsUsedCount = hintsUsedCount,
                    isRequesting = isRequestingHint,
                    size = 42.dp,
                    enabled = isInteractive,
                    onClick = { if (isInteractive) onHintClick() },
                    modifier = hintModifier
                )
            }
        }
    }
}

/**
 * Compact text input row (RTL flow: Start/Right = Mic Toggle, Center = Text Input, End/Left = Send/Hint):
 * [Mic toggle] --- [Apple Input Field] --- [Send OR Hint]
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

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // RTL Start (Right Side): Mode toggle to Voice
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

            // Center: Apple Compact Input Pill
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
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
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
            }

            // RTL End (Left Side): Send when text ready, otherwise Hint
            if (canSend) {
                AppleSendButton(
                    onClick = { if (isInteractive) onSendClick() },
                    size = 42.dp,
                    enabled = isInteractive
                )
            } else {
                AppleHintButton(
                    hintsUsedCount = hintsUsedCount,
                    isRequesting = isRequestingHint,
                    size = 42.dp,
                    enabled = isInteractive,
                    onClick = { if (isInteractive) onHintClick() }
                )
            }
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
                text = "ارتباط با سرویس صوتی قطع است.",
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
                    contentDescription = "اتصال مجدد",
                    tint = AppleChatTheme.Coral,
                    modifier = Modifier.size(13.dp)
                )
                GameText(
                    text = "اتصال مجدد",
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
 *   Quoted English transcribed text + pulsing cyan vertical cursor indicator.
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

            // Body Row: English transcribed text + Glowing Cyan Pulse Cursor
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    GameText(
                        text = "\"$text\"",
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

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Center Circular Loading Button (~48.dp)
        Box(
            modifier = Modifier
                .size(48.dp)
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

        Spacer(modifier = Modifier.height(3.dp))

        // Attached status badge
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0x33000000))
                .border(0.6.dp, amberColor.copy(alpha = 0.45f), RoundedCornerShape(10.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .background(amberColor, CircleShape)
            )
            GameText(
                text = "در حال آماده‌سازی…",
                size = 10.5.sp,
                bold = true,
                color = amberColor
            )
        }
    }
}

/**
 * Active recording button with Siri-like concentric expanding pulse rings & attached "در حال شنیدن" live badge.
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
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring1_progress"
    )
    val ring2Progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, delayMillis = 450, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring2_progress"
    )

    val micPulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mic_scale"
    )

    val liveDotAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(550, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "live_dot_alpha"
    )

    val emeraldColor = Color(0xFF34D399)
    val ringColor = Color(0xFF38BDF8)

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Glowing Siri / Concentric Wave Container
        Box(
            modifier = Modifier.size(52.dp),
            contentAlignment = Alignment.Center
        ) {
            // Concentric expanding glowing rings (Canvas, 0 allocation)
            Canvas(modifier = Modifier.size(52.dp)) {
                val centerOffset = center
                val baseRadius = 22.dp.toPx()
                val maxExtraRadius = 14.dp.toPx()

                // Ring 1
                val r1 = baseRadius + ring1Progress * maxExtraRadius
                val a1 = (1f - ring1Progress).coerceIn(0f, 1f) * 0.45f
                if (a1 > 0f) {
                    drawCircle(
                        color = ringColor.copy(alpha = a1),
                        radius = r1,
                        center = centerOffset,
                        style = Stroke(width = 1.2.dp.toPx())
                    )
                }

                // Ring 2
                val r2 = baseRadius + ring2Progress * maxExtraRadius
                val a2 = (1f - ring2Progress).coerceIn(0f, 1f) * 0.45f
                if (a2 > 0f) {
                    drawCircle(
                        color = ringColor.copy(alpha = a2),
                        radius = r2,
                        center = centerOffset,
                        style = Stroke(width = 1.2.dp.toPx())
                    )
                }
            }

            // Center Circular Record Button
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
                                Color(0x660A84FF),
                                Color(0x330066CC)
                            )
                        )
                    )
                    .border(1.5.dp, AppleChatTheme.SiriCyan.copy(alpha = 0.85f), CircleShape),
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

        Spacer(modifier = Modifier.height(2.dp))

        // Attached live badge: Green dot + "در حال شنیدن" (matching Image 2)
        Row(
            modifier = Modifier
                .applePressEffect(targetScale = 0.94f, onClick = onStop)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xE60D1F17))
                .border(0.8.dp, emeraldColor.copy(alpha = 0.65f), RoundedCornerShape(10.dp))
                .padding(horizontal = 8.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .graphicsLayer { this.alpha = liveDotAlpha }
                    .background(emeraldColor, CircleShape)
            )
            GameText(
                text = "در حال شنیدن",
                size = 11.sp,
                bold = true,
                color = emeraldColor
            )
        }
    }
}

/**
 * Center microphone button in Idle State.
 * Displays glowing Siri cyan / Azure glass circle and clear tactile hint below.
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

    val labelText = when {
        isModelSpeaking -> "در حال پاسخ…"
        !enabled -> "لطفاً صبر کنید…"
        hasText -> "ضبط مجدد صدا"
        else -> "برای صحبت لمس کنید"
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
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

        Spacer(modifier = Modifier.height(2.dp))

        GameText(
            text = labelText,
            size = 10.5.sp,
            bold = true,
            color = if (isInteractive) Game.TextSecondary else Game.TextMuted
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

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
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

        Spacer(modifier = Modifier.height(2.dp))

        Row(
            modifier = Modifier
                .applePressEffect(targetScale = 0.94f, onClick = onReconnect)
                .clip(RoundedCornerShape(10.dp))
                .background(color.copy(alpha = 0.14f))
                .border(0.6.dp, color.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .background(color, CircleShape)
            )
            GameText(
                text = "قطع ارتباط • لمس مجدد",
                size = 10.5.sp,
                bold = true,
                color = color
            )
        }
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

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
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

        Spacer(modifier = Modifier.height(2.dp))

        GameText(
            text = "در حال اتصال…",
            size = 10.5.sp,
            bold = true,
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
