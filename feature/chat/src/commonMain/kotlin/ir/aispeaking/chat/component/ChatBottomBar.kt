package ir.aispeaking.chat.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
 * Ultra-compact, elegant Apple-style bottom dock for conversation.
 * Designed to maximize chat transcript visibility while offering instant access to:
 * voice recording, text input, smart hints, transcript preview, and send.
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
        // Floating Compact Transcript Preview Capsule (shown when voice transcript is ready)
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

        // Main Compact Dock (height ~54dp)
        val dockShape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(dockShape)
                .background(Game.Panel)
                .border(1.dp, Game.Stroke, dockShape)
                .padding(horizontal = 10.dp, vertical = 7.dp)
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
 * [Keyboard toggle] --- [Pulsing recording pill OR Apple record button] --- [Send OR Hint]
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
                size = 38.dp,
                iconSize = 19.dp,
                container = Color(0x1AFFFFFF),
                border = Color(0x14FFFFFF)
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
                AppleSendButton(onClick = onSendClick, size = 38.dp)
            } else {
                AppleHintButton(
                    hintsUsedCount = hintsUsedCount,
                    isRequesting = isRequestingHint,
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
                size = 38.dp,
                iconSize = 19.dp,
                container = Color(0x1AFFFFFF),
                border = Color(0x14FFFFFF)
            )

            // Apple Compact Input Pill
            val fieldShape = RoundedCornerShape(20.dp)
            val textStyle = TextStyle(
                color = Game.TextPrimary,
                fontSize = 15.sp,
                lineHeight = 20.sp,
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
                cursorBrush = SolidColor(Game.Blue),
                maxLines = 3,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { if (canSend) onSendClick() }),
                decorationBox = { inner ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(38.dp)
                            .background(Game.PanelRaised, fieldShape)
                            .border(1.dp, if (canSend) Game.Blue.copy(alpha = 0.6f) else Color(0x1AFFFFFF), fieldShape)
                            .padding(horizontal = 14.dp),
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
                AppleSendButton(onClick = onSendClick, size = 38.dp)
            } else {
                AppleHintButton(
                    hintsUsedCount = hintsUsedCount,
                    isRequesting = isRequestingHint,
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
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Game.PanelRaised)
                .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(14.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                GameText(
                    text = text,
                    size = 14.sp,
                    lineHeight = 19.sp,
                    latin = true,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                GameIconButton(
                    icon = Res.drawable.ic_edit,
                    onClick = onEdit,
                    contentDescription = "ویرایش",
                    size = 28.dp,
                    iconSize = 14.dp,
                    tint = Game.Sky,
                    container = Color(0x14FFFFFF),
                    border = Color.Transparent
                )
                GameIconButton(
                    icon = Res.drawable.ic_clear,
                    onClick = onClear,
                    contentDescription = "پاک کردن",
                    size = 28.dp,
                    iconSize = 14.dp,
                    tint = Game.Coral,
                    container = Color(0x14FFFFFF),
                    border = Color.Transparent
                )
            }
        }
    }
}

/** Apple Voice Memos style active recording pill with pulsing wave and stop button */
@Composable
private fun AppleRecordingActivePill(onStop: () -> Unit) {
    val transition = rememberInfiniteTransition(label = "rec_pulse")
    val alpha by transition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(700, easing = LinearEasing), RepeatMode.Reverse),
        label = "rec_dot_alpha"
    )

    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = 0.82f, stiffness = 420f),
        label = "rec_stop_press"
    )

    Row(
        modifier = Modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(20.dp))
            .background(Game.Coral.copy(alpha = 0.16f))
            .border(1.dp, Game.Coral.copy(alpha = 0.45f), RoundedCornerShape(20.dp))
            .clickable(interactionSource = interaction, indication = null, onClick = onStop)
            .padding(horizontal = 14.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(9.dp)
                .graphicsLayer { this.alpha = alpha }
                .background(Game.Coral, CircleShape)
        )
        GameText(
            text = "در حال شنیدن… لمس برای توقف",
            size = 12.sp,
            bold = true,
            color = Game.Coral
        )
        Box(
            modifier = Modifier
                .size(14.dp)
                .background(Game.Coral, RoundedCornerShape(3.dp))
        )
    }
}

/** Apple-style Record button capsule in idle state */
@Composable
private fun AppleRecordPill(
    hasText: Boolean,
    onClick: () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = 0.82f, stiffness = 420f),
        label = "rec_start_press"
    )

    val shape = RoundedCornerShape(20.dp)
    Row(
        modifier = Modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(shape)
            .background(if (hasText) Color(0x22FFFFFF) else Game.Mint.copy(alpha = 0.18f))
            .border(1.dp, if (hasText) Color(0x28FFFFFF) else Game.Mint.copy(alpha = 0.5f), shape)
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            painter = painterResource(Res.drawable.ic_microphone),
            contentDescription = "ضبط صدا",
            tint = if (hasText) Game.TextPrimary else Game.Mint,
            modifier = Modifier.size(17.dp)
        )
        GameText(
            text = if (hasText) "ضبط مجدد صدا" else "برای صحبت لمس کنید",
            size = 13.sp,
            bold = true,
            color = if (hasText) Game.TextPrimary else Game.Mint
        )
    }
}

/** Apple circular vibrant Send button */
@Composable
private fun AppleSendButton(onClick: () -> Unit, size: androidx.compose.ui.unit.Dp) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.92f else 1f,
        animationSpec = spring(dampingRatio = 0.82f, stiffness = 450f),
        label = "apple_send_press"
    )
    Box(
        modifier = Modifier
            .size(size)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(CircleShape)
            .background(Brush.verticalGradient(listOf(Color(0xFF0A84FF), Color(0xFF0071E3))))
            .border(1.dp, Color.White.copy(alpha = 0.25f), CircleShape)
            .clickable(interactionSource = interaction, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(Res.drawable.ic_send),
            contentDescription = "ارسال پیام",
            tint = Color.White,
            modifier = Modifier.size(18.dp)
        )
    }
}

/** Apple compact circular Hint button with smart counter badge */
@Composable
private fun AppleHintButton(
    hintsUsedCount: Int,
    isRequesting: Boolean,
    onClick: () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed && !isRequesting) 0.92f else 1f,
        animationSpec = spring(dampingRatio = 0.82f, stiffness = 420f),
        label = "apple_hint_press"
    )

    Box(
        modifier = Modifier
            .size(38.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(CircleShape)
            .background(if (hintsUsedCount > 0) Game.Gold.copy(alpha = 0.2f) else Color(0x1AFFFFFF))
            .border(
                1.dp,
                if (hintsUsedCount > 0) Game.Gold.copy(alpha = 0.55f) else Color(0x14FFFFFF),
                CircleShape
            )
            .clickable(
                interactionSource = interaction,
                indication = null,
                enabled = !isRequesting,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isRequesting) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp,
                color = Game.Gold
            )
        } else {
            Icon(
                painter = painterResource(Res.drawable.ic_lamp),
                contentDescription = "راهنما",
                tint = if (hintsUsedCount > 0) Game.Gold else Game.TextSecondary,
                modifier = Modifier.size(18.dp)
            )
            if (hintsUsedCount > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(2.dp)
                        .size(8.dp)
                        .background(Game.Gold, CircleShape)
                )
            }
        }
    }
}
