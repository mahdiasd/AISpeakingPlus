package ir.aispeaking.sharedui.ui.game

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.sp
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.ic_star
import ir.aispeaking.sharedui.ic_star_outline
import ir.aispeaking.sharedui.ui.them.AppTheme
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import kotlin.math.roundToInt

/* ------------------------------------------------------------------------ */
/* Text                                                                      */
/* ------------------------------------------------------------------------ */

/**
 * Single text primitive for the game look. Persian copy uses Vazir, English (chat) copy uses
 * Poppins via [latin] = true.
 */
@Composable
fun GameText(
    text: String,
    modifier: Modifier = Modifier,
    size: TextUnit = 14.sp,
    bold: Boolean = false,
    color: Color = Game.TextPrimary,
    align: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
    lineHeight: TextUnit = TextUnit.Unspecified,
    latin: Boolean = false,
) {
    val typography = AppTheme.typography
    val family: FontFamily = when {
        latin && bold -> typography.bodyLargeBold.fontFamily
        latin -> typography.bodyLarge.fontFamily
        bold -> typography.persianBold
        else -> typography.persianRegular
    } ?: FontFamily.Default
    Text(
        text = text,
        modifier = modifier,
        color = color,
        fontSize = size,
        fontFamily = family,
        fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
        textAlign = align,
        maxLines = maxLines,
        overflow = overflow,
        lineHeight = if (lineHeight == TextUnit.Unspecified) size * 1.5f else lineHeight,
    )
}

/* ------------------------------------------------------------------------ */
/* Buttons                                                                   */
/* ------------------------------------------------------------------------ */

enum class GameButtonStyle(
    val top: Color,
    val bottom: Color,
    val edge: Color,
    val content: Color,
) {
    Primary(Color(0xFF5BF5C4), Color(0xFF2EE6A8), Color(0xFF12A878), Color(0xFF04261C)),
    Gold(Color(0xFFFFE283), Color(0xFFFFC83D), Color(0xFFD98E00), Color(0xFF3A2600)),
    Violet(Color(0xFFA88FFF), Color(0xFF8C6CFF), Color(0xFF5636D8), Color.White),
    Danger(Color(0xFFFF8E8E), Color(0xFFFF6B6B), Color(0xFFC53B3B), Color.White),
    Glass(Color(0xFF2B3568), Color(0xFF232C5A), Color(0xFF121940), Color.White),
}

private val ButtonDepth = 5.dp

/**
 * Chunky "arcade" button: a colored face sitting on a darker edge. Pressing pushes the face down
 * onto the edge, which gives immediate physical feedback.
 */
@Composable
fun GameButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: GameButtonStyle = GameButtonStyle.Primary,
    icon: DrawableResource? = null,
    enabled: Boolean = true,
    loading: Boolean = false,
    height: Dp = 54.dp,
    textSize: TextUnit = 16.sp,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val active = enabled && !loading
    val press by animateFloatAsState(
        targetValue = if (pressed && active) 1f else 0f,
        animationSpec = tween(durationMillis = 90),
        label = "game_button_press",
    )
    val shape = RoundedCornerShape(18.dp)
    val top = if (enabled) style.top else Color(0xFF3B4268)
    val bottom = if (enabled) style.bottom else Color(0xFF343B60)
    val edge = if (enabled) style.edge else Color(0xFF222844)
    val content = if (enabled) style.content else Game.TextMuted

    Box(modifier = modifier.height(height + ButtonDepth)) {
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(height)
                .background(edge, shape)
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .height(height)
                .offset { IntOffset(0, (press * ButtonDepth.toPx()).roundToInt()) }
                .clip(shape)
                .background(Brush.verticalGradient(listOf(top, bottom)))
                .border(1.dp, Color.White.copy(alpha = if (enabled) 0.28f else 0.08f), shape)
                .clickable(
                    interactionSource = interaction,
                    indication = null,
                    enabled = active,
                    onClick = onClick,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                modifier = Modifier.padding(horizontal = 14.dp),
            ) {
                if (loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = content,
                    )
                } else if (icon != null) {
                    Icon(
                        painter = painterResource(icon),
                        contentDescription = null,
                        tint = content,
                        modifier = Modifier.size(20.dp),
                    )
                }
                GameText(
                    text = text,
                    size = textSize,
                    bold = true,
                    color = content,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

/** Round glass icon button used for back / profile / utility actions. */
@Composable
fun GameIconButton(
    icon: DrawableResource,
    onClick: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    iconSize: Dp = 20.dp,
    tint: Color = Game.TextPrimary,
    container: Color = Game.Panel,
    border: Color = Game.Stroke,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.9f else 1f,
        animationSpec = tween(100),
        label = "game_icon_press",
    )
    Box(
        modifier = modifier
            .size(size)
            .scale(scale)
            .clip(CircleShape)
            .background(container)
            .border(1.dp, border, CircleShape)
            .clickable(interactionSource = interaction, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(iconSize),
        )
    }
}

/** Small tappable label chip: icon + text, used for message actions and HUD info. */
@Composable
fun GameChip(
    text: String,
    modifier: Modifier = Modifier,
    icon: DrawableResource? = null,
    accent: Color = Game.TextSecondary,
    container: Color = Color(0x33FFFFFF),
    active: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.94f else 1f,
        animationSpec = tween(100),
        label = "game_chip_press",
    )
    val shape = RoundedCornerShape(50)
    Row(
        modifier = modifier
            .scale(scale)
            .clip(shape)
            .background(if (active) accent.copy(alpha = 0.22f) else container)
            .border(1.dp, if (active) accent.copy(alpha = 0.7f) else Color.Transparent, shape)
            .then(
                if (onClick != null) {
                    Modifier.clickable(interactionSource = interaction, indication = null, onClick = onClick)
                } else Modifier
            )
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (icon != null) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(15.dp),
            )
        }
        GameText(text = text, size = 12.sp, bold = true, color = if (active) accent else Game.TextPrimary, maxLines = 1)
    }
}

/* ------------------------------------------------------------------------ */
/* Surfaces                                                                  */
/* ------------------------------------------------------------------------ */

@Composable
fun GlassPanel(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(22.dp),
    color: Color = Game.Panel,
    border: Color = Game.Stroke,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(color)
            .border(1.dp, border, shape),
        content = content,
    )
}

/** Compact HUD pill (stars counter, status). */
@Composable
fun GameStatPill(
    text: String,
    modifier: Modifier = Modifier,
    icon: DrawableResource? = null,
    accent: Color = Game.Gold,
    onClick: (() -> Unit)? = null,
) {
    val shape = RoundedCornerShape(50)
    Row(
        modifier = modifier
            .clip(shape)
            .background(Game.Panel)
            .border(1.dp, accent.copy(alpha = 0.55f), shape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (icon != null) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(16.dp),
            )
        }
        GameText(text = text, size = 13.sp, bold = true, color = accent, maxLines = 1)
    }
}

@Composable
fun GameProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    height: Dp = 8.dp,
    color: Color = Game.Mint,
    track: Color = Color(0x33FFFFFF),
) {
    val animated by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(350),
        label = "game_progress",
    )
    Box(
        modifier = modifier
            .height(height)
            .clip(CircleShape)
            .background(track)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(animated)
                .clip(CircleShape)
                .background(Brush.horizontalGradient(listOf(color.copy(alpha = 0.8f), color)))
        )
    }
}

/* ------------------------------------------------------------------------ */
/* Stars                                                                     */
/* ------------------------------------------------------------------------ */

@Composable
fun GameStars(
    stars: Int,
    modifier: Modifier = Modifier,
    total: Int = 3,
    size: Dp = 28.dp,
    spacing: Dp = 4.dp,
    animate: Boolean = false,
    earnedTint: Color = Game.Gold,
    emptyTint: Color = Color(0x55FFFFFF),
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(spacing), verticalAlignment = Alignment.CenterVertically) {
        repeat(total) { index ->
            val earned = index < stars
            val pop = remember { Animatable(if (animate && earned) 0f else 1f) }
            LaunchedEffect(stars, animate) {
                if (animate && earned) {
                    pop.snapTo(0f)
                    delay(250L + index * 220L)
                    pop.animateTo(1f, spring(dampingRatio = 0.45f, stiffness = Spring.StiffnessMedium))
                }
            }
            Icon(
                painter = painterResource(if (earned) Res.drawable.ic_star else Res.drawable.ic_star_outline),
                contentDescription = null,
                tint = if (earned) earnedTint else emptyTint,
                modifier = Modifier
                    .size(size)
                    .graphicsLayer {
                        scaleX = pop.value
                        scaleY = pop.value
                    },
            )
        }
    }
}

/* ------------------------------------------------------------------------ */
/* In-frame overlays                                                         */
/* ------------------------------------------------------------------------ */

/**
 * Centered modal rendered inside the screen's own composition. We deliberately avoid the platform
 * `Dialog` so that on web the modal stays inside the phone frame instead of covering the browser.
 */
@Composable
fun GameModal(
    visible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    dismissOnScrimClick: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(modifier = modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(180)),
            exit = fadeOut(tween(140)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Game.ModalScrim)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        enabled = dismissOnScrimClick,
                        onClick = onDismiss,
                    )
            )
        }
        AnimatedVisibility(
            visible = visible,
            modifier = Modifier.align(Alignment.Center),
            enter = fadeIn(tween(180)) + scaleIn(initialScale = 0.9f, animationSpec = tween(220)),
            exit = fadeOut(tween(120)) + scaleOut(targetScale = 0.95f, animationSpec = tween(120)),
        ) {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 20.dp, vertical = 24.dp)
                        .widthIn(max = 380.dp)
                        .fillMaxWidth()
                        .heightIn(max = 640.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(Game.PanelSolid)
                        .border(1.dp, Game.StrokeStrong, RoundedCornerShape(28.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {},
                        ),
                    content = content,
                )
            }
        }
    }
}

/** Bottom sheet rendered inside the screen, same reasoning as [GameModal]. */
@Composable
fun GameSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(modifier = modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(180)),
            exit = fadeOut(tween(140)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Game.ModalScrim)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onDismiss,
                    )
            )
        }
        AnimatedVisibility(
            visible = visible,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = slideInVertically(tween(260)) { it } + fadeIn(tween(160)),
            exit = slideOutVertically(tween(200)) { it } + fadeOut(tween(160)),
        ) {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                val shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 680.dp)
                        .clip(shape)
                        .background(Game.PanelSolid)
                        .border(1.dp, Game.StrokeStrong, shape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {},
                        )
                        .navigationBarsPadding(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Spacer(Modifier.height(10.dp))
                    Box(
                        Modifier
                            .width(40.dp)
                            .height(4.dp)
                            .clip(CircleShape)
                            .background(Color(0x55FFFFFF))
                    )
                    Spacer(Modifier.height(6.dp))
                    content()
                }
            }
        }
    }
}

/** Soft multiplier helper so disabled content can be dimmed consistently. */
fun Modifier.dimmed(enabled: Boolean): Modifier = if (enabled) this else this.alpha(0.5f)

/* ------------------------------------------------------------------------ */
/* Inputs                                                                    */
/* ------------------------------------------------------------------------ */

/**
 * Single-purpose text field with a Persian label above it. Set [ltr] for phone numbers, codes and
 * English text so digits and Latin letters stay left-to-right.
 */
@Composable
fun GameTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String = "",
    keyboardType: androidx.compose.ui.text.input.KeyboardType = androidx.compose.ui.text.input.KeyboardType.Text,
    imeAction: androidx.compose.ui.text.input.ImeAction = androidx.compose.ui.text.input.ImeAction.Done,
    onImeAction: () -> Unit = {},
    ltr: Boolean = false,
    isError: Boolean = false,
    singleLine: Boolean = true,
    centered: Boolean = false,
    accent: Color = Game.Mint,
    leading: (@Composable () -> Unit)? = null,
) {
    val shape = RoundedCornerShape(18.dp)
    var focused by remember { mutableStateOf(false) }
    val direction = if (ltr) androidx.compose.ui.text.style.TextDirection.Ltr else androidx.compose.ui.text.style.TextDirection.Rtl
    val fontFamily = AppTheme.typography.persianRegular
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        if (label != null) {
            GameText(text = label, size = 13.sp, bold = true, color = Game.TextSecondary)
        }
        androidx.compose.foundation.text.BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = singleLine,
            textStyle = androidx.compose.ui.text.TextStyle(
                color = Game.TextPrimary,
                fontSize = 17.sp,
                fontFamily = fontFamily,
                textDirection = direction,
                textAlign = if (centered) TextAlign.Center else TextAlign.Start,
            ),
            cursorBrush = androidx.compose.ui.graphics.SolidColor(accent),
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                keyboardType = keyboardType,
                imeAction = imeAction,
            ),
            keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                onDone = { onImeAction() },
                onSend = { onImeAction() },
                onGo = { onImeAction() },
            ),
            modifier = Modifier
                .fillMaxWidth()
                .trackFocus { focused = it },
            decorationBox = { inner ->
                val borderColor = when {
                    isError -> Game.Coral
                    focused -> accent
                    else -> Game.StrokeStrong
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 54.dp)
                        .background(Game.PanelRaised, shape)
                        .border(if (focused || isError) 2.dp else 1.dp, borderColor, shape)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    if (leading != null) leading()
                    Box(modifier = Modifier.weight(1f), contentAlignment = if (centered) Alignment.Center else Alignment.CenterStart) {
                        if (value.isEmpty()) {
                            GameText(
                                text = placeholder,
                                size = 17.sp,
                                color = Game.TextMuted,
                                align = if (centered) TextAlign.Center else null,
                            )
                        }
                        inner()
                    }
                }
            },
        )
    }
}

private fun Modifier.trackFocus(onChange: (Boolean) -> Unit): Modifier =
    this.then(Modifier.onFocusChanged { onChange(it.isFocused) })
