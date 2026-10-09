package ir.aispeaking.chat.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
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
import androidx.compose.ui.draw.scale
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
import androidx.compose.ui.unit.IntOffset
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
import ir.aispeaking.sharedui.ui.game.GameChip
import ir.aispeaking.sharedui.ui.game.GameIconButton
import ir.aispeaking.sharedui.ui.game.GameText
import ir.aispeaking.sharedui.ui.game.fa
import ir.aispeaking.sharedui.ui.them.AppTheme
import org.jetbrains.compose.resources.painterResource
import kotlin.math.roundToInt

enum class ChatInputMode {
    VOICE,
    TEXT
}

/**
 * Bottom dock of the conversation. One clear primary action at a time:
 *  - voice mode: a big microphone, with live text appearing above it and "send" lighting up when
 *    there is something to send;
 *  - text mode: a plain text field with send.
 * Every secondary control carries a Persian label so nothing has to be guessed.
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
    Column(modifier = modifier.fillMaxWidth()) {
        // Floating help button, always reachable but clearly separate from the main input
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.Start
        ) {
            GameChip(
                text = when {
                    isRequestingHint -> "در حال دریافت راهنما…"
                    hintsUsedCount > 0 -> "راهنما (${hintsUsedCount.fa()} بار)"
                    else -> "راهنما بگیر"
                },
                icon = Res.drawable.ic_lamp,
                accent = Game.Gold,
                container = Game.Panel,
                active = hintsUsedCount > 0,
                onClick = if (isRequestingHint) null else onHintClick
            )
        }

        val dockShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(dockShape)
                .background(Game.Panel)
                .border(1.dp, Game.Stroke, dockShape)
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 16.dp, vertical = 14.dp)
                .animateContentSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when (inputMode) {
                ChatInputMode.VOICE -> VoiceDock(
                    text = text,
                    isRecording = isRecording,
                    onSwitchToText = { onInputModeChange(ChatInputMode.TEXT) },
                    onVoiceToggle = onVoiceToggle,
                    onSendClick = onSendClick,
                    onClear = { onTextChange("") }
                )

                ChatInputMode.TEXT -> TextDock(
                    text = text,
                    onTextChange = onTextChange,
                    onSwitchToVoice = { onInputModeChange(ChatInputMode.VOICE) },
                    onSendClick = onSendClick
                )
            }
        }
    }
}

@Composable
private fun VoiceDock(
    text: String,
    isRecording: Boolean,
    onSwitchToText: () -> Unit,
    onVoiceToggle: (Boolean) -> Unit,
    onSendClick: () -> Unit,
    onClear: () -> Unit
) {
    val hasText = text.isNotBlank()

    // Live transcript
    AnimatedVisibility(
        visible = hasText,
        enter = fadeIn(tween(160)) + expandVertically(tween(200)),
        exit = fadeOut(tween(120)) + shrinkVertically(tween(160))
    ) {
        val shape = RoundedCornerShape(18.dp)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Game.PanelRaised, shape)
                .border(1.dp, Game.Stroke, shape)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            GameText(
                text = "جمله‌ای که گفتی",
                size = 11.sp,
                color = Game.TextSecondary
            )
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                GameText(
                    text = text,
                    size = 16.sp,
                    lineHeight = 24.sp,
                    latin = true,
                    maxLines = 4,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 110.dp)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GameChip(
                    text = "ویرایش",
                    icon = Res.drawable.ic_edit,
                    accent = Game.Sky,
                    onClick = onSwitchToText
                )
                GameChip(
                    text = "پاک کردن",
                    icon = Res.drawable.ic_clear,
                    accent = Game.Coral,
                    onClick = onClear
                )
            }
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            LabeledAction(label = "تایپ می‌کنم", modifier = Modifier.width(72.dp)) {
                GameIconButton(
                    icon = Res.drawable.ic_keyboard,
                    onClick = onSwitchToText,
                    contentDescription = "تایپ کردن به‌جای صحبت",
                    size = 48.dp,
                    iconSize = 22.dp,
                    container = Game.PanelRaised
                )
            }

            MicButton(isRecording = isRecording, onClick = { onVoiceToggle(!isRecording) })

            LabeledAction(label = "ارسال", modifier = Modifier.width(72.dp)) {
                SendButton(enabled = hasText, onClick = onSendClick, size = 48.dp)
            }
        }
    }

    GameText(
        text = when {
            isRecording -> "در حال شنیدن… وقتی تمام شد دوباره لمس کن"
            hasText -> "درست بود؟ ارسال کن یا دوباره ضبط کن"
            else -> "برای صحبت به انگلیسی، میکروفون را لمس کن"
        },
        size = 12.sp,
        color = if (isRecording) Game.Coral else Game.TextSecondary,
        align = androidx.compose.ui.text.style.TextAlign.Center
    )
}

@Composable
private fun LabeledAction(
    label: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        content()
        GameText(text = label, size = 11.sp, color = Game.TextSecondary, maxLines = 1)
    }
}

@Composable
private fun SendButton(enabled: Boolean, onClick: () -> Unit, size: androidx.compose.ui.unit.Dp) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed && enabled) 0.9f else 1f,
        animationSpec = tween(100),
        label = "send_press"
    )
    Box(
        modifier = Modifier
            .size(size)
            .scale(scale)
            .clip(CircleShape)
            .background(
                if (enabled) Brush.verticalGradient(listOf(Color(0xFF5BF5C4), Game.Mint))
                else Brush.verticalGradient(listOf(Color(0xFF2A3262), Color(0xFF2A3262)))
            )
            .border(1.dp, Color.White.copy(alpha = if (enabled) 0.3f else 0.08f), CircleShape)
            .clickable(
                interactionSource = interaction,
                indication = null,
                enabled = enabled,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(Res.drawable.ic_send),
            contentDescription = "ارسال پیام",
            tint = if (enabled) Color(0xFF04261C) else Game.TextMuted,
            modifier = Modifier.size(22.dp)
        )
    }
}

/** Big record button. While recording it turns coral, shows a stop square and emits soft rings. */
@Composable
private fun MicButton(isRecording: Boolean, onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val depth = 5.dp
    val press by animateFloatAsState(
        targetValue = if (pressed) 1f else 0f,
        animationSpec = tween(90),
        label = "mic_press"
    )
    val transition = rememberInfiniteTransition(label = "mic_rings")
    val ring by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1500, easing = LinearEasing), RepeatMode.Restart),
        label = "mic_ring"
    )

    val top = if (isRecording) Color(0xFFFF8E8E) else Color(0xFF5BF5C4)
    val bottom = if (isRecording) Game.Coral else Game.Mint
    val edge = if (isRecording) Game.CoralDeep else Game.MintDeep

    Box(modifier = Modifier.size(92.dp), contentAlignment = Alignment.Center) {
        if (isRecording) {
            repeat(2) { i ->
                val p = ((ring + i * 0.5f) % 1f)
                Box(
                    modifier = Modifier
                        .size(78.dp)
                        .graphicsLayer {
                            val s = 1f + p * 0.45f
                            scaleX = s
                            scaleY = s
                            alpha = (1f - p) * 0.45f
                        }
                        .border(2.dp, Game.Coral, CircleShape)
                )
            }
        }
        Box(
            modifier = Modifier
                .size(78.dp)
                .offset(y = depth / 2)
                .background(edge, CircleShape)
        )
        Box(
            modifier = Modifier
                .size(78.dp)
                .offset { IntOffset(0, (press * depth.toPx()).roundToInt() - (depth / 2).roundToPx()) }
                .clip(CircleShape)
                .background(Brush.verticalGradient(listOf(top, bottom)))
                .border(1.5.dp, Color.White.copy(alpha = 0.35f), CircleShape)
                .clickable(interactionSource = interaction, indication = null, onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            if (isRecording) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .background(Color.White, RoundedCornerShape(6.dp))
                )
            } else {
                Icon(
                    painter = painterResource(Res.drawable.ic_microphone),
                    contentDescription = "شروع ضبط صدا",
                    tint = Color(0xFF04261C),
                    modifier = Modifier.size(34.dp)
                )
            }
        }
    }
}

@Composable
private fun TextDock(
    text: String,
    onTextChange: (String) -> Unit,
    onSwitchToVoice: () -> Unit,
    onSendClick: () -> Unit
) {
    val canSend = text.isNotBlank()
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        try {
            focusRequester.requestFocus()
        } catch (_: Throwable) {
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            LabeledAction(label = "صحبت می‌کنم", modifier = Modifier.width(72.dp)) {
                GameIconButton(
                    icon = Res.drawable.ic_microphone,
                    onClick = onSwitchToVoice,
                    contentDescription = "صحبت کردن به‌جای تایپ",
                    size = 48.dp,
                    iconSize = 22.dp,
                    container = Game.PanelRaised
                )
            }

            val fieldShape = RoundedCornerShape(22.dp)
            val textStyle = TextStyle(
                color = Game.TextPrimary,
                fontSize = 16.sp,
                lineHeight = 24.sp,
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
                cursorBrush = SolidColor(Game.Mint),
                maxLines = 4,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { if (canSend) onSendClick() }),
                decorationBox = { inner ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .background(Game.PanelRaised, fieldShape)
                            .border(1.dp, if (canSend) Game.Mint.copy(alpha = 0.6f) else Game.StrokeStrong, fieldShape)
                            .padding(horizontal = 16.dp, vertical = 11.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (text.isEmpty()) {
                            GameText(
                                text = "Type your reply in English…",
                                size = 16.sp,
                                latin = true,
                                color = Game.TextMuted
                            )
                        }
                        inner()
                    }
                }
            )

            LabeledAction(label = "ارسال", modifier = Modifier.width(56.dp)) {
                SendButton(enabled = canSend, onClick = onSendClick, size = 48.dp)
            }
        }
    }
}

/* --------------------------------- Previews --------------------------------- */

@androidx.compose.ui.tooling.preview.Preview
@Composable
private fun ChatBottomBarVoicePreview() {
    AppTheme {
        ChatBottomBar(
            inputMode = ChatInputMode.VOICE,
            text = "",
            isRecording = false,
            onTextChange = {},
            onInputModeChange = {},
            onVoiceToggle = {},
            onSendClick = {}
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview
@Composable
private fun ChatBottomBarRecordingPreview() {
    AppTheme {
        ChatBottomBar(
            inputMode = ChatInputMode.VOICE,
            text = "I would like the chicken, please",
            isRecording = true,
            hintsUsedCount = 1,
            onTextChange = {},
            onInputModeChange = {},
            onVoiceToggle = {},
            onSendClick = {}
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview
@Composable
private fun ChatBottomBarTextPreview() {
    AppTheme {
        ChatBottomBar(
            inputMode = ChatInputMode.TEXT,
            text = "Hello there",
            isRecording = false,
            onTextChange = {},
            onInputModeChange = {},
            onVoiceToggle = {},
            onSendClick = {}
        )
    }
}
