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
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
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

    val isDark = AppTheme.isDark
    val resolvedColor = when {
        color == Game.TextPrimary || color == Color.Unspecified -> if (isDark) Game.TextPrimary else Color(0xFF111318)
        color == Game.TextSecondary -> if (isDark) Game.TextSecondary else Color(0xFF6B7280)
        color == Game.TextMuted -> if (isDark) Game.TextMuted else Color(0xFF9CA3AF)
        else -> color
    }

    Text(
        text = text,
        modifier = modifier,
        color = resolvedColor,
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
    Primary(Color(0xFF0A84FF), Color(0xFF0071E3), Color.Transparent, Color.White),
    Gold(Color(0xFFFFD60A), Color(0xFFFFB800), Color.Transparent, Color(0xFF1E1700)),
    Violet(Color(0xFF6366F1), Color(0xFF4F46E5), Color.Transparent, Color.White),
    Danger(Color(0xFFFF453A), Color(0xFFD70015), Color.Transparent, Color.White),
    Glass(Color(0x28FFFFFF), Color(0x18FFFFFF), Color.Transparent, Color.White),
}

/**
 * Apple-style interactive button: smooth squircle, responsive spring scale on press (Emil Kowalski principle),
 * and subtle hairline reflection border.
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
    height: Dp = 50.dp,
    textSize: TextUnit = 15.sp,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val active = enabled && !loading
    val scale by animateFloatAsState(
        targetValue = if (pressed && active) 0.96f else 1f,
        animationSpec = spring(dampingRatio = 0.82f, stiffness = Spring.StiffnessMediumLow),
        label = "apple_button_press",
    )
    val shape = RoundedCornerShape(16.dp)
    val isDark = AppTheme.isDark
    val (top, bottom, content) = when {
        !enabled -> Triple(
            if (isDark) Color(0xFF2C2C2E) else Color(0xFFE5E7EB),
            if (isDark) Color(0xFF232326) else Color(0xFFD1D5DB),
            Game.TextMuted
        )
        style == GameButtonStyle.Glass && !isDark -> Triple(
            Color(0xCCFFFFFF),
            Color(0xB3FFFFFF),
            Color(0xFF111318)
        )
        style == GameButtonStyle.Primary && !isDark -> Triple(
            Color(0xFF007AFF),
            Color(0xFF0066CC),
            Color.White
        )
        else -> Triple(style.top, style.bottom, style.content)
    }

    val borderColor = if (isDark) {
        if (style == GameButtonStyle.Glass) Color.White.copy(alpha = if (enabled) 0.22f else 0.08f)
        else Color.White.copy(alpha = if (enabled) 0.16f else 0.06f)
    } else {
        if (style == GameButtonStyle.Glass) Color.Black.copy(alpha = if (enabled) 0.18f else 0.08f)
        else Color.Black.copy(alpha = if (enabled) 0.08f else 0.03f)
    }

    Box(
        modifier = modifier
            .height(height)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(shape)
            .background(Brush.verticalGradient(listOf(top, bottom)))
            .border(1.dp, borderColor, shape)
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
            modifier = Modifier.padding(horizontal = 16.dp),
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
                    modifier = Modifier.size(19.dp),
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

/** Round Apple-style icon button with spring press feedback. */
@Composable
fun GameIconButton(
    icon: DrawableResource,
    onClick: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    iconSize: Dp = 20.dp,
    tint: Color = Game.TextPrimary,
    container: Color = Color.Unspecified,
    border: Color = Game.Stroke,
) {
    val isDark = AppTheme.isDark
    val resolvedContainer = if (container != Color.Unspecified) container else if (isDark) Color(0x26FFFFFF) else Color(0xB8FFFFFF)
    val resolvedBorder = if (border != Game.Stroke) border else if (isDark) Color(0x30FFFFFF) else Color(0x26000000)
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.92f else 1f,
        animationSpec = spring(dampingRatio = 0.82f, stiffness = 420f),
        label = "apple_icon_press",
    )
    Box(
        modifier = modifier
            .size(size)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(CircleShape)
            .background(resolvedContainer)
            .border(1.dp, resolvedBorder, CircleShape)
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
    container: Color = Color.Unspecified,
    border: Color = Color.Unspecified,
    active: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    val isDark = AppTheme.isDark
    val resolvedContainer = if (container != Color.Unspecified) container else if (isDark) Color(0xEB181A22) else Color(0xCCFFFFFF)
    val resolvedBorder = if (border != Color.Unspecified) border else if (isDark) Color(0x33FFFFFF) else Color(0x1F000000)
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = 0.82f, stiffness = 450f),
        label = "apple_chip_press",
    )
    val shape = RoundedCornerShape(50)
    Row(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(shape)
            .background(if (active) accent.copy(alpha = if (isDark) 0.28f else 0.14f) else resolvedContainer)
            .border(1.dp, if (active) accent.copy(alpha = 0.75f) else resolvedBorder, shape)
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
                tint = if (active) accent else (if (accent == Game.TextSecondary) Game.TextPrimary else accent),
                modifier = Modifier.size(15.dp),
            )
        }
        GameText(
            text = text,
            size = 12.sp,
            bold = true,
            color = if (active) accent else Game.TextPrimary,
            maxLines = 1
        )
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
            .defaultMinSize(minHeight = 44.dp)
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
    color: Color = Game.Blue,
    track: Color = Color.Unspecified,
) {
    val resolvedTrack = if (track != Color.Unspecified) track else if (AppTheme.isDark) Color(0x33FFFFFF) else Color(0x1A000000)
    val animated by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(350),
        label = "game_progress",
    )
    Box(
        modifier = modifier
            .height(height)
            .clip(CircleShape)
            .background(resolvedTrack)
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
    emptyTint: Color = Color.Unspecified,
) {
    val resolvedEmptyTint = if (emptyTint != Color.Unspecified) emptyTint else if (AppTheme.isDark) Color(0x55FFFFFF) else Color(0x28000000)
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
                tint = if (earned) earnedTint else resolvedEmptyTint,
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
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(visible) {
        if (visible) {
            try { focusRequester.requestFocus() } catch (_: Throwable) {}
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .focusRequester(focusRequester)
            .focusable()
            .onPreviewKeyEvent { event ->
                if (visible && event.type == KeyEventType.KeyDown && event.key == Key.Escape) {
                    onDismiss()
                    true
                } else false
            }
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(160)),
            exit = fadeOut(tween(130)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Game.ModalScrim)
                    .semantics {
                        contentDescription = "بستن کادر"
                        onClick(label = "بستن") {
                            onDismiss()
                            true
                        }
                    }
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
            enter = fadeIn(tween(160)) + scaleIn(initialScale = 0.94f, animationSpec = spring(dampingRatio = 0.84f, stiffness = 400f)),
            exit = fadeOut(tween(120)) + scaleOut(targetScale = 0.96f, animationSpec = tween(120)),
        ) {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                val isDark = AppTheme.isDark
                val modalShape = RoundedCornerShape(26.dp)
                Column(
                    modifier = Modifier
                        .padding(horizontal = 20.dp, vertical = 24.dp)
                        .widthIn(max = 390.dp)
                        .fillMaxWidth()
                        .heightIn(max = 660.dp)
                        .clip(modalShape)
                        .background(Game.PanelSolid)
                        .border(1.dp, Game.StrokeStrong, modalShape)
                        .pointerInput(Unit) {
                            detectTapGestures { /* consume tap so it does not reach scrim */ }
                        },
                    content = content,
                )
            }
        }
    }
}

/**
 * Top card modal sliding down from the top edge of the screen.
 * Perfect for result/evaluation cards so the latest chat messages and audio playback remain visible below.
 */
@Composable
fun GameTopModal(
    visible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    dismissOnScrimClick: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(visible) {
        if (visible) {
            try { focusRequester.requestFocus() } catch (_: Throwable) {}
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .focusRequester(focusRequester)
            .focusable()
            .onPreviewKeyEvent { event ->
                if (visible && event.type == KeyEventType.KeyDown && event.key == Key.Escape) {
                    onDismiss()
                    true
                } else false
            }
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(160)),
            exit = fadeOut(tween(140)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x66000000))
                    .semantics {
                        contentDescription = "بستن کادر"
                        onClick(label = "بستن") {
                            onDismiss()
                            true
                        }
                    }
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
            modifier = Modifier.align(Alignment.TopCenter),
            enter = slideInVertically(
                initialOffsetY = { -it },
                animationSpec = spring(dampingRatio = 0.82f, stiffness = 380f)
            ) + fadeIn(tween(160)),
            exit = slideOutVertically(
                targetOffsetY = { -it },
                animationSpec = tween(160)
            ) + fadeOut(tween(120)),
        ) {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                val isDark = AppTheme.isDark
                val topModalShape = RoundedCornerShape(26.dp)
                Column(
                    modifier = Modifier
                        .padding(top = 16.dp, start = 14.dp, end = 14.dp, bottom = 20.dp)
                        .widthIn(max = 410.dp)
                        .fillMaxWidth()
                        .heightIn(max = 560.dp)
                        .clip(topModalShape)
                        .background(Game.PanelSolid)
                        .border(1.dp, Game.StrokeStrong, topModalShape)
                        .pointerInput(Unit) {
                            detectTapGestures { /* consume tap so it does not reach scrim */ }
                        },
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
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(visible) {
        if (visible) {
            try { focusRequester.requestFocus() } catch (_: Throwable) {}
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .focusRequester(focusRequester)
            .focusable()
            .onPreviewKeyEvent { event ->
                if (visible && event.type == KeyEventType.KeyDown && event.key == Key.Escape) {
                    onDismiss()
                    true
                } else false
            }
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(160)),
            exit = fadeOut(tween(130)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Game.ModalScrim)
                    .semantics {
                        contentDescription = "بستن برگ"
                        onClick(label = "بستن") {
                            onDismiss()
                            true
                        }
                    }
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
            enter = slideInVertically(spring(dampingRatio = 0.86f, stiffness = 380f)) { it } + fadeIn(tween(140)),
            exit = slideOutVertically(tween(200)) { it } + fadeOut(tween(140)),
        ) {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                val isDark = AppTheme.isDark
                val shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 680.dp)
                        .clip(shape)
                        .background(Game.PanelSolid)
                        .border(1.dp, Game.StrokeStrong, shape)
                        .pointerInput(Unit) {
                            detectTapGestures { /* consume tap so it does not reach scrim */ }
                        }
                        .navigationBarsPadding(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Spacer(Modifier.height(10.dp))
                    Box(
                        Modifier
                            .width(36.dp)
                            .height(5.dp)
                            .clip(CircleShape)
                            .background(if (isDark) Color(0x40FFFFFF) else Color(0x28000000))
                    )
                    Spacer(Modifier.height(8.dp))
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
    accent: Color = Game.Blue,
    leading: (@Composable () -> Unit)? = null,
) {
    val shape = RoundedCornerShape(14.dp)
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
                fontSize = 16.sp,
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
                    focused -> accent.copy(alpha = 0.85f)
                    else -> Game.Stroke
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 50.dp)
                        .background(Game.PanelRaised, shape)
                        .border(if (focused || isError) 1.5.dp else 1.dp, borderColor, shape)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    if (leading != null) leading()
                    Box(modifier = Modifier.weight(1f), contentAlignment = if (centered) Alignment.Center else Alignment.CenterStart) {
                        if (value.isEmpty()) {
                            GameText(
                                text = placeholder,
                                size = 16.sp,
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
