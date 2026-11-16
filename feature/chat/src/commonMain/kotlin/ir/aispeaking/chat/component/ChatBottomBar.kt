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
import androidx.compose.animation.core.spring
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
 * - In VOICE mode: 3 floating circular buttons grouped in center:
 *     - Left: Suggest / Hint (Lamp icon, 48dp).
 *     - Center: Large Voice button (58dp button with subtle concentric glass rings up to 106dp).
 *     - Right: Action button (48dp): Send if voice transcript is ready, or Keyboard toggle if idle.
 * - In TEXT mode:
 *     - Side icons animate outward to the screen edges with padding.
 *     - Between Suggest and Action button, the STT-styled Input Box expands in the middle.
 *     - When input is empty, right action button displays Voice (mic); clicking toggles back to voice mode.
 *     - When text is typed, right action button smoothly morphs into Send.
 * - Real-time Voice Transcription Capsule (رونویسی همزمان صدا) without quotation marks.
 * - Disconnection warning capsule when speech socket drops.
 * - Fluid Apple spring animation during mode switching.
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

        // Main Floating Controls (No outer container background, fully floating)
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp, top = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            val totalWidth = maxWidth
            // In Voice mode: compact cluster: Left (48dp) + Gap (18dp) + Center (68dp) + Gap (18dp) + Right (48dp) = 202dp
            val clusterWidth = 202.dp
            val voicePadding = ((totalWidth - clusterWidth) / 2).coerceAtLeast(16.dp)
            val textPadding = 16.dp

            val animatedPadding by animateDpAsState(
                targetValue = if (inputMode == ChatInputMode.VOICE) voicePadding else textPadding,
                animationSpec = spring(
                    dampingRatio = 0.82f,
                    stiffness = 380f
                ),
                label = "floating_dock_padding"
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
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
                                        // Large Floating Voice Button & Living Waveform / Rings (overflowing freely)
                                        Box(
                                            modifier = Modifier.size(width = 106.dp, height = 72.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            when {
                                                isModelSpeaking -> {
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
                                        container = if (AppTheme.isDark) Color(0x24182236) else Color(0xB8FFFFFF),
                                        border = AppleChatTheme.SubtleBorderColor,
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
                                        container = if (AppTheme.isDark) Color(0x24182236) else Color(0xB8FFFFFF),
                                        border = AppleChatTheme.SubtleBorderColor,
                                        modifier = Modifier.graphicsLayer { alpha = if (isInteractive) 1f else 0.38f }
                                    )
                                }
                            }
                        }
                    }
                }

                // Attached Live Badges (Floats directly below the center Voice button in Voice mode)
                if (inputMode == ChatInputMode.VOICE) {
                    when {
                        isModelSpeaking -> {
                            Row(
                                modifier = Modifier
                                    .padding(top = 4.dp)
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
                        }

                        isRecording && !isRecordingPreparing -> {
                            Row(
                                modifier = Modifier
                                    .padding(top = 4.dp)
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
                        }

                        isRecording && isRecordingPreparing -> {
                            Row(
                                modifier = Modifier
                                    .padding(top = 4.dp)
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
                        }

                        sttEngine == SttEngine.APP && (sttConnectionStatus == SttConnectionStatus.DISCONNECTED || sttConnectionStatus == SttConnectionStatus.ERROR) -> {
                            Row(
                                modifier = Modifier
                                    .padding(top = 4.dp)
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
 * Radiates soft chromatic aura and dancing equalizer bars in subscription screen color harmonies.
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

    val barColors = remember {
        listOf(
            Color(0xFFFF7A45), // Warm Peach
            Color(0xFF0EA5E9), // Sky Blue
            Color(0xFF818CF8), // Electric Indigo
            Color(0xFF22C55E), // Fresh Mint
            Color(0xFFFFB236)  // Amber Gold
        )
    }

    Box(
        modifier = modifier.size(width = 106.dp, height = 64.dp),
        contentAlignment = Alignment.Center
    ) {
        // Soft Chromatic Radial Aura behind waveform
        Canvas(modifier = Modifier.size(width = 110.dp, height = 70.dp)) {
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
        Canvas(modifier = Modifier.size(width = 68.dp, height = 32.dp)) {
            val barCount = 5
            val barWidthPx = 4.2.dp.toPx()
            val cornerRadius = CornerRadius(barWidthPx / 2f, barWidthPx / 2f)
            val gapPx = (size.width - (barCount * barWidthPx)) / (barCount - 1)
            val centerY = size.height / 2f
            val maxBarHeight = size.height * 0.90f
            val minBarHeight = 8.dp.toPx()

            for (i in 0 until barCount) {
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
 * Features an unconstrained expanding dual-ring harmonic radar shimmer
 * using warm Peach / Amber colors from the subscription screen.
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
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(550, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mic_pulse"
    )

    val orangeColor = Color(0xFFFF7A45) // Warm Peach / Orange
    val amberColor = Color(0xFFFFB236)  // Radiant Gold Amber

    Box(
        modifier = modifier.size(106.dp),
        contentAlignment = Alignment.Center
    ) {
        // Expanding Harmonic Radar Aura
        Canvas(modifier = Modifier.size(106.dp)) {
            val centerOffset = center
            val baseRadius = 26.dp.toPx()
            val maxExpansion = 24.dp.toPx()

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

        // Center Glass Mic Button with warm Amber/Peach glow (58dp)
        Box(
            modifier = Modifier
                .size(58.dp)
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
                            amberColor.copy(alpha = 0.18f),
                            Color(0x1F0B111F)
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
                modifier = Modifier.size(26.dp)
            )
        }
    }
}

/**
 * Active recording button with multi-color concentric expanding acoustic pulse rings
 * radiating outward up to 106dp, tuned with vibrant subscription screen harmonies (Mint, Sky Blue, Warm Peach).
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
        targetValue = 1.07f,
        animationSpec = infiniteRepeatable(
            animation = tween(650, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mic_scale"
    )

    val colorMint = Color(0xFF22C55E)   // Fresh Mint
    val colorSky = Color(0xFF0EA5E9)    // Sky Blue
    val colorPeach = Color(0xFFFF7A45)  // Warm Peach

    Box(
        modifier = modifier.size(106.dp),
        contentAlignment = Alignment.Center
    ) {
        // Multi-Color Concentric Rings expanding outward to 106dp
        Canvas(modifier = Modifier.size(106.dp)) {
            val centerOffset = center
            val baseRadius = 28.dp.toPx()
            val maxExpansion = 24.dp.toPx()

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

        // Center Circular Record Button (Size 58dp) with rich subscription gradient
        Box(
            modifier = Modifier
                .size(58.dp)
                .graphicsLayer {
                    scaleX = micPulseScale
                    scaleY = micPulseScale
                }
                .applePressEffect(targetScale = 0.94f, onClick = onStop)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(
                            Color(0xCC0EA5E9),
                            Color(0x996366F1),
                            Color(0x4D22C55E)
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
                modifier = Modifier.size(26.dp)
            )
        }
    }
}

/**
 * Center microphone button in Idle Floating State.
 * Displays prominent 58dp circular mic button with subtle concentric glass rings
 * fading into background, exactly as seen in reference design.
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
            .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape),
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
 * Features warm Champagne Gold / Amber gradient styling matching the subscription screen.
 * When requesting hint, the lightbulb breathes with an ambient radial golden halo.
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
                    peachColor.copy(alpha = 0.22f * requestingGlow + 0.10f)
                } else if (hintsUsedCount > 0) {
                    goldColor.copy(alpha = 0.22f)
                } else if (isDark) {
                    Color(0x24182236)
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
