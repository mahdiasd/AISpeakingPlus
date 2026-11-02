package ir.aispeaking.web

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.ic_close
import ir.aispeaking.sharedui.ic_done
import ir.aispeaking.sharedui.ui.game.Game
import ir.aispeaking.sharedui.ui.game.GameButton
import ir.aispeaking.sharedui.ui.game.GameButtonStyle
import ir.aispeaking.sharedui.ui.game.GameModal
import ir.aispeaking.sharedui.ui.game.GameText
import org.jetbrains.compose.resources.painterResource

/**
 * Compact, subtle top banner shown exclusively to iPhone / iOS Safari users who haven't
 * installed the app to Home Screen yet. Encourages A2HS for optimal audio and full-screen experience.
 */
@Composable
fun IosPwaBanner(
    modifier: Modifier = Modifier,
    visible: Boolean = true,
    onOpenGuide: () -> Unit,
    onDismiss: () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut()
    ) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Box(
                modifier = modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF161F33),
                                Color(0xFF0F1524)
                            )
                        )
                    )
                    .border(
                        width = 0.5.dp,
                        color = Color(0x3338BDF8),
                        shape = RoundedCornerShape(0.dp)
                    )
                    .clickable { onOpenGuide() }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Right side in RTL: Icon badge + text message + action hint
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(RoundedCornerShape(5.dp))
                                .background(Color(0xFF0A84FF).copy(alpha = 0.2f))
                                .border(0.5.dp, Color(0xFF0A84FF).copy(alpha = 0.6f), RoundedCornerShape(5.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            IosShareIcon(
                                color = Color(0xFF38BDF8),
                                modifier = Modifier.size(11.dp)
                            )
                        }

                        GameText(
                            text = "برای عملکرد بهتر و روان‌تر، برنامه را به صفحه اصلی اضافه کنید",
                            size = 11.5.sp,
                            bold = true,
                            color = Color(0xFFF1F5F9),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        GameText(
                            text = "راهنما ›",
                            size = 11.5.sp,
                            bold = true,
                            color = Color(0xFF38BDF8)
                        )
                    }

                    // Left side in RTL: Close button
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .clickable(
                                onClick = onDismiss,
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_close),
                            contentDescription = "بستن",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * 2026 Apple Liquid Glass Educational Modal instructing iOS Safari users
 * on the 3-step "Add to Home Screen" process.
 */
@Composable
fun IosPwaTutorialModal(
    visible: Boolean,
    isInAppBrowser: Boolean = false,
    onDismiss: () -> Unit
) {
    GameModal(
        visible = visible,
        onDismiss = onDismiss,
        dismissOnScrimClick = true
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Glowing Badge
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF0A84FF).copy(alpha = 0.15f))
                    .border(1.dp, Color(0xFF0A84FF).copy(alpha = 0.45f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                IosShareIcon(
                    color = Color(0xFF38BDF8),
                    modifier = Modifier.size(28.dp)
                )
            }

            // Title & Subtitle
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                GameText(
                    text = "افزودن به صفحه اصلی آیفون",
                    size = 18.sp,
                    bold = true,
                    align = TextAlign.Center
                )
                GameText(
                    text = "برای تجربه سریع‌تر، دسترسی آسان و عملکرد بدون اختلال میکروفون، وب‌اپلیکیشن را به صفحه اصلی اضافه کنید:",
                    size = 12.5.sp,
                    lineHeight = 19.sp,
                    color = Game.TextSecondary,
                    align = TextAlign.Center
                )
            }

            // In-App browser warning if opened from Telegram/Instagram
            if (isInAppBrowser) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFF9500).copy(alpha = 0.12f))
                        .border(1.dp, Color(0xFFFF9500).copy(alpha = 0.45f), RoundedCornerShape(12.dp))
                        .padding(10.dp)
                ) {
                    GameText(
                        text = "توجه: در صورت باز شدن در تلگرام یا اینستاگرام، ابتدا از منوی سه‌نقطه دکمه «Open in Safari» را لمس کنید.",
                        size = 11.5.sp,
                        color = Color(0xFFFFB340),
                        align = TextAlign.Center
                    )
                }
            }

            // Steps Container
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                IosTutorialStepItem(
                    stepNumber = "۱",
                    title = "لمس دکمه Share",
                    description = "در نوار پایینی مرورگر سافاری، دکمه اشتراک‌گذاری (Share) را لمس کنید.",
                    icon = { IosShareIcon(color = Color(0xFF38BDF8), modifier = Modifier.size(18.dp)) }
                )

                IosTutorialStepItem(
                    stepNumber = "۲",
                    title = "انتخاب Add to Home Screen",
                    description = "منوی باز شده را کمی بالا بکشید و گزینه «Add to Home Screen» (افزودن به صفحه اصلی) را انتخاب کنید.",
                    icon = { IosAddIcon(color = Color(0xFF34C759), modifier = Modifier.size(18.dp)) }
                )

                IosTutorialStepItem(
                    stepNumber = "۳",
                    title = "تایید و افزودن (Add)",
                    description = "در گوشه بالا سمت راست، دکمه «Add» را لمس کنید تا برنامه در صفحه اصلی آیفون نصب شود.",
                    icon = {
                        Icon(
                            painter = painterResource(Res.drawable.ic_done),
                            contentDescription = null,
                            tint = Color(0xFF0A84FF),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
            }

            // Confirm CTA
            GameButton(
                text = "متوجه شدم",
                style = GameButtonStyle.Primary,
                modifier = Modifier.fillMaxWidth(),
                onClick = onDismiss
            )
        }
    }
}

@Composable
private fun IosTutorialStepItem(
    stepNumber: String,
    title: String,
    description: String,
    icon: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF161C2C))
            .border(1.dp, Color(0x1FFFFFFF), RoundedCornerShape(14.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Step Number Badge
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF0A84FF).copy(alpha = 0.2f))
                    .border(1.dp, Color(0xFF0A84FF).copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                GameText(
                    text = stepNumber,
                    size = 13.sp,
                    bold = true,
                    color = Color(0xFF38BDF8)
                )
            }

            // Step Content
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                GameText(
                    text = title,
                    size = 13.5.sp,
                    bold = true,
                    color = Color(0xFFF8FAFC)
                )
                GameText(
                    text = description,
                    size = 11.5.sp,
                    color = Game.TextSecondary,
                    lineHeight = 16.sp
                )
            }

            // Step Graphic Icon
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0x14FFFFFF)),
                contentAlignment = Alignment.Center
            ) {
                icon()
            }
        }
    }
}

/**
 * Authentic Apple Safari Share Icon (`⎋`): open-top box with upward arrow.
 */
@Composable
fun IosShareIcon(
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val strokeWidth = (w * 0.1f).coerceAtLeast(1.5f)

        val boxTop = h * 0.45f
        val boxBottom = h * 0.95f
        val boxLeft = w * 0.15f
        val boxRight = w * 0.85f

        val boxPath = Path().apply {
            moveTo(boxLeft, boxTop)
            lineTo(boxLeft, boxBottom)
            lineTo(boxRight, boxBottom)
            lineTo(boxRight, boxTop)
        }
        drawPath(
            path = boxPath,
            color = color,
            style = Stroke(
                width = strokeWidth,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )

        val arrowTop = h * 0.05f
        val arrowBottom = h * 0.65f
        val centerX = w * 0.5f

        drawLine(
            color = color,
            start = Offset(centerX, arrowBottom),
            end = Offset(centerX, arrowTop),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )

        val headWingW = w * 0.22f
        val headWingH = h * 0.22f
        drawLine(
            color = color,
            start = Offset(centerX - headWingW, arrowTop + headWingH),
            end = Offset(centerX, arrowTop),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )

        drawLine(
            color = color,
            start = Offset(centerX + headWingW, arrowTop + headWingH),
            end = Offset(centerX, arrowTop),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
    }
}

/**
 * Authentic Add-to-Home-Screen icon: rounded square with plus symbol inside.
 */
@Composable
fun IosAddIcon(
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val strokeWidth = (w * 0.1f).coerceAtLeast(1.5f)
        val inset = w * 0.12f

        drawRoundRect(
            color = color.copy(alpha = 0.45f),
            topLeft = Offset(inset, inset),
            size = Size(w - 2 * inset, h - 2 * inset),
            cornerRadius = CornerRadius(w * 0.2f, h * 0.2f),
            style = Stroke(width = strokeWidth)
        )

        val cx = w * 0.5f
        val cy = h * 0.5f
        val plusArm = w * 0.18f

        drawLine(
            color = color,
            start = Offset(cx - plusArm, cy),
            end = Offset(cx + plusArm, cy),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )

        drawLine(
            color = color,
            start = Offset(cx, cy - plusArm),
            end = Offset(cx, cy + plusArm),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
    }
}
