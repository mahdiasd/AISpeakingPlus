package ir.aispeaking.chat.component

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
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.ic_clear
import ir.aispeaking.sharedui.ic_edit
import ir.aispeaking.sharedui.ic_keyboard
import ir.aispeaking.sharedui.ic_lamp
import ir.aispeaking.sharedui.ic_microphone
import ir.aispeaking.sharedui.ic_send
import ir.aispeaking.sharedui.ui.game.Game
import ir.aispeaking.sharedui.ui.game.GameIconButton
import ir.aispeaking.sharedui.ui.game.GameText
import ir.aispeaking.sharedui.ui.game.fa
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
 * - Floating visionOS translucent glass capsule with specular top light rim.
 * - Tactile Voice Record pill with ambient breathing glow in idle state, switching to
 *   pulsing Apple Voice Memos live indicator while recording.
 * - Sleek Apple text field with Royal Azure focus glow.
 * - Instant toggle between voice and keyboard input modes.
 * - Elevated transcript preview capsule with 1-tap edit / clear actions.
 * - Circular Send & Champagne Gold Hint actions with tactile spring physics.
 */
@Composable
fun ChatBottomBar(
    modifier: Modifier = Modifier,
    inputMode: ChatInputMode,
    text: String,
    isRecording: Boolean,
    hintsUsedCount: Int = 0,
    isRequestingHint: Boolean = false,
    onTextChange: (String) -> Unit,
    onInputModeChange: (ChatInputMode) -> Unit,
    onVoiceToggle: (Boolean) -> Unit,
    onSendClick: () -> Unit,
    onHintClick: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding()
    ) {
        // Floating Transcript Preview Capsule (shown when speech recognition has recognized text)
        AnimatedVisibility(
            visible = text.isNotBlank() && inputMode == ChatInputMode.VOICE,
            enter = fadeIn(tween(140)) + slideInVertically(tween(160)) { it / 2 },
            exit = fadeOut(tween(100)) + slideOutVertically(tween(120)) { it / 2 }
        ) {
            CompactTranscriptCapsule(
                text = text,
                onEdit = { onInputModeChange(ChatInputMode.TEXT) },
                onClear = { onTextChange("") }
            )
        }

        // Main Floating Liquid Glass Capsule Dock
        val dockShape = RoundedCornerShape(28.dp)
        Box(
            modifier = Modifier
                .padding(start = 14.dp, end = 14.dp, bottom = 12.dp, top = 4.dp)
                .fillMaxWidth()
                .clip(dockShape)
                .background(AppleChatTheme.GlassPanel)
                .border(1.dp, AppleChatTheme.SpecularStrokeBrush, dockShape)
                .padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            when (inputMode) {
                ChatInputMode.VOICE -> CompactVoiceRow(
                    text = text,
                    isRecording = isRecording,
                    hintsUsedCount = hintsUsedCount,
                    isRequestingHint = isRequestingHint,
                    onSwitchToText = { onInputModeChange(ChatInputMode.TEXT) },
                    onVoiceToggle = onVoiceToggle,
                    onSendClick = onSendClick,
                    onHintClick = onHintClick
                )

                ChatInputMode.TEXT -> CompactTextRow(
                    text = text,
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

/**
 * Compact voice controls row:
 * [Keyboard toggle] --- [Apple Tactile Record Pill] --- [Send OR Hint]
 */
@Composable
private fun CompactVoiceRow(
    text: String,
    isRecording: Boolean,
    hintsUsedCount: Int,
    isRequestingHint: Boolean,
    onSwitchToText: () -> Unit,
    onVoiceToggle: (Boolean) -> Unit,
    onSendClick: () -> Unit,
    onHintClick: () -> Unit
) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Mode toggle to Keyboard
            GameIconButton(
                icon = Res.drawable.ic_keyboard,
                onClick = onSwitchToText,
                contentDescription = "تایپ متنی",
                size = 42.dp,
                iconSize = 20.dp,
                container = Color(0x1AFFFFFF),
                border = AppleChatTheme.SubtleBorderColor
            )

            // Center: Interactive Recording Pill
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                if (isRecording) {
                    AppleRecordingActivePill(onStop = { onVoiceToggle(false) })
                } else {
                    AppleRecordPill(
                        hasText = text.isNotBlank(),
                        onClick = { onVoiceToggle(true) }
                    )
                }
            }

            // Trailing: Send when text is ready, otherwise Hint button
            if (text.isNotBlank()) {
                AppleSendButton(onClick = onSendClick, size = 42.dp)
            } else {
                AppleHintButton(
                    hintsUsedCount = hintsUsedCount,
                    isRequesting = isRequestingHint,
                    size = 42.dp,
                    onClick = onHintClick
                )
            }
        }
    }
}

/**
 * Compact text input row:
 * [Mic toggle] --- [Apple Input Field] --- [Send OR Hint]
 */
@Composable
private fun CompactTextRow(
    text: String,
    hintsUsedCount: Int,
    isRequestingHint: Boolean,
    onTextChange: (String) -> Unit,
    onSwitchToVoice: () -> Unit,
    onSendClick: () -> Unit,
    onHintClick: () -> Unit
) {
    val canSend = text.isNotBlank()
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        try {
            focusRequester.requestFocus()
        } catch (_: Throwable) {}
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Mode toggle to Voice
            GameIconButton(
                icon = Res.drawable.ic_microphone,
                onClick = onSwitchToVoice,
                contentDescription = "مکالمه صوتی",
                size = 42.dp,
                iconSize = 20.dp,
                container = Color(0x1AFFFFFF),
                border = AppleChatTheme.SubtleBorderColor
            )

            // Apple Compact Input Pill
            val fieldShape = RoundedCornerShape(22.dp)
            val textStyle = TextStyle(
                color = Game.TextPrimary,
                fontSize = 15.sp,
                lineHeight = 22.sp,
                fontFamily = AppTheme.typography.bodyLarge.fontFamily,
                textDirection = TextDirection.Ltr
            )
            BasicTextField(
                value = text,
                onValueChange = onTextChange,
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester)
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
                            .heightIn(min = 42.dp, max = 110.dp)
                            .background(AppleChatTheme.GlassPanelRaised, fieldShape)
                            .border(
                                1.dp,
                                if (canSend) AppleChatTheme.PrimaryAzure.copy(alpha = 0.7f) else Color(0x22FFFFFF),
                                fieldShape
                            )
                            .padding(horizontal = 16.dp, vertical = 9.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (text.isEmpty()) {
                            GameText(
                                text = "Type in English…",
                                size = 14.sp,
                                latin = true,
                                color = Game.TextMuted
                            )
                        }
                        inner()
                    }
                }
            )

            // Trailing: Send when entered, otherwise Hint
            if (canSend) {
                AppleSendButton(onClick = onSendClick, size = 42.dp)
            } else {
                AppleHintButton(
                    hintsUsedCount = hintsUsedCount,
                    isRequesting = isRequestingHint,
                    size = 42.dp,
                    onClick = onHintClick
                )
            }
        }
    }
}

/** Floating pill above the dock showing the voice transcript with 1-tap clear / edit */
@Composable
private fun CompactTranscriptCapsule(
    text: String,
    onEdit: () -> Unit,
    onClear: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 4.dp)
    ) {
        val shape = RoundedCornerShape(16.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(AppleChatTheme.GlassPanelRaised)
                .border(1.dp, AppleChatTheme.SpecularStrokeBrush, shape)
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                GameText(
                    text = text,
                    size = 14.sp,
                    lineHeight = 20.sp,
                    latin = true,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                GameIconButton(
                    icon = Res.drawable.ic_edit,
                    onClick = onEdit,
                    contentDescription = "ویرایش",
                    size = 34.dp,
                    iconSize = 16.dp,
                    tint = AppleChatTheme.SuccessCyan,
                    container = Color(0x18FFFFFF),
                    border = AppleChatTheme.SubtleBorderColor
                )
                GameIconButton(
                    icon = Res.drawable.ic_clear,
                    onClick = onClear,
                    contentDescription = "پاک کردن",
                    size = 34.dp,
                    iconSize = 16.dp,
                    tint = AppleChatTheme.Coral,
                    container = Color(0x18FFFFFF),
                    border = AppleChatTheme.SubtleBorderColor
                )
            }
        }
    }
}

/** Apple Voice Memos style active recording pill with pulsating waveform and stop button */
@Composable
private fun AppleRecordingActivePill(onStop: () -> Unit) {
    val transition = rememberInfiniteTransition(label = "rec_pulse")
    val alpha by transition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(650, easing = LinearEasing), RepeatMode.Reverse),
        label = "rec_dot_alpha"
    )

    val bar1 by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(420, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "wave_bar1"
    )
    val bar2 by transition.animateFloat(
        initialValue = 0.9f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(tween(360, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "wave_bar2"
    )
    val bar3 by transition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(tween(480, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "wave_bar3"
    )

    val shape = RoundedCornerShape(22.dp)

    Row(
        modifier = Modifier
            .applePressEffect(targetScale = 0.95f, onClick = onStop)
            .clip(shape)
            .background(AppleChatTheme.Coral.copy(alpha = 0.16f))
            .border(1.dp, AppleChatTheme.Coral.copy(alpha = 0.55f), shape)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Pulsing red recording dot
        Box(
            modifier = Modifier
                .size(9.dp)
                .graphicsLayer { this.alpha = alpha }
                .background(AppleChatTheme.Coral, CircleShape)
        )

        // Mini audio wave
        Row(
            modifier = Modifier.height(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            listOf(bar1, bar2, bar3).forEach { barVal ->
                Box(
                    modifier = Modifier
                        .width(2.5.dp)
                        .height((4 + 10 * barVal).dp)
                        .background(AppleChatTheme.Coral, RoundedCornerShape(1.dp))
                )
            }
        }

        GameText(
            text = "در حال شنیدن… لمس برای توقف",
            size = 12.sp,
            bold = true,
            color = AppleChatTheme.Coral
        )

        // Stop square icon
        Box(
            modifier = Modifier
                .size(13.dp)
                .background(AppleChatTheme.Coral, RoundedCornerShape(3.dp))
        )
    }
}

/**
 * 2026 Apple Record pill in idle state.
 * Features subtle ambient breathing light in Royal Azure / Indigo (replacing green).
 */
@Composable
private fun AppleRecordPill(
    hasText: Boolean,
    onClick: () -> Unit
) {
    val transition = rememberInfiniteTransition(label = "idle_breathing")
    val breathGlow by transition.animateFloat(
        initialValue = 0.18f,
        targetValue = 0.34f,
        animationSpec = infiniteRepeatable(tween(1400, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "breath_glow"
    )

    val shape = RoundedCornerShape(22.dp)
    val pillBg = if (hasText) {
        Color(0x22FFFFFF)
    } else {
        AppleChatTheme.PrimaryAzure.copy(alpha = breathGlow)
    }
    val pillBorder = if (hasText) {
        Color(0x28FFFFFF)
    } else {
        AppleChatTheme.PrimaryAzure.copy(alpha = 0.55f)
    }
    val accentColor = if (hasText) Game.TextPrimary else AppleChatTheme.SuccessCyan

    Row(
        modifier = Modifier
            .applePressEffect(targetScale = 0.95f, onClick = onClick)
            .clip(shape)
            .background(pillBg)
            .border(1.dp, pillBorder, shape)
            .padding(horizontal = 18.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            painter = painterResource(Res.drawable.ic_microphone),
            contentDescription = "ضبط صدا",
            tint = accentColor,
            modifier = Modifier.size(18.dp)
        )
        GameText(
            text = if (hasText) "ضبط مجدد صدا" else "برای صحبت لمس کنید",
            size = 13.sp,
            bold = true,
            color = accentColor
        )
    }
}

/** Apple circular vibrant Send button with Royal Azure gradient */
@Composable
private fun AppleSendButton(onClick: () -> Unit, size: androidx.compose.ui.unit.Dp) {
    Box(
        modifier = Modifier
            .size(size)
            .applePressEffect(targetScale = 0.92f, onClick = onClick)
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
    size: androidx.compose.ui.unit.Dp = 42.dp,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(size)
            .applePressEffect(enabled = !isRequesting, targetScale = 0.92f, onClick = onClick)
            .clip(CircleShape)
            .background(
                if (hintsUsedCount > 0) AppleChatTheme.Gold.copy(alpha = 0.2f) else Color(0x1AFFFFFF)
            )
            .border(
                1.dp,
                if (hintsUsedCount > 0) AppleChatTheme.Gold.copy(alpha = 0.55f) else Color(0x14FFFFFF),
                CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isRequesting) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp,
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
