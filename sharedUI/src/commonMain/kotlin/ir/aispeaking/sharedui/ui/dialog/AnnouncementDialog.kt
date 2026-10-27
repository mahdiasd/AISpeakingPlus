package ir.aispeaking.sharedui.ui.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.aispeaking.domain.model.Announcement
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.ic_alert_circle
import ir.aispeaking.sharedui.ic_checked
import ir.aispeaking.sharedui.ic_close
import ir.aispeaking.sharedui.ic_gem
import ir.aispeaking.sharedui.ic_unchecked
import ir.aispeaking.sharedui.ui.core.image.AppAsyncImage
import ir.aispeaking.sharedui.ui.game.Game
import ir.aispeaking.sharedui.ui.game.GameButton
import ir.aispeaking.sharedui.ui.game.GameButtonStyle
import ir.aispeaking.sharedui.ui.game.GameModal
import ir.aispeaking.sharedui.ui.game.GameText
import org.jetbrains.compose.resources.painterResource

@Composable
fun AnnouncementDialog(
    visible: Boolean,
    announcement: Announcement?,
    onDismiss: () -> Unit,
    onPrimaryClick: () -> Unit,
    onSecondaryClick: () -> Unit = {},
    onSuppressionToggle: (Boolean) -> Unit = {}
) {
    if (announcement == null) return

    var suppressFor24h by remember(announcement.id) { mutableStateOf(false) }

    GameModal(
        visible = visible,
        onDismiss = {
            if (!announcement.isBlocking) {
                if (suppressFor24h) {
                    onSuppressionToggle(true)
                }
                onDismiss()
            }
        },
        dismissOnScrimClick = !announcement.isBlocking
    ) {
        val (accentColor, iconRes) = when (announcement.type.uppercase()) {
            "WARNING" -> Color(0xFFF59E0B) to Res.drawable.ic_alert_circle
            "ERROR" -> Color(0xFFEF4444) to Res.drawable.ic_alert_circle
            "SUCCESS" -> Color(0xFF10B981) to Res.drawable.ic_gem
            "INFO" -> Color(0xFF06B6D4) to Res.drawable.ic_alert_circle
            else -> Color(0xFF06B6D4) to Res.drawable.ic_alert_circle
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar with Close button if not blocking
            if (!announcement.isBlocking) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Game.PanelRaised)
                            .border(1.dp, Game.Stroke, CircleShape)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = {
                                    if (suppressFor24h) {
                                        onSuppressionToggle(true)
                                    }
                                    onDismiss()
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_close),
                            contentDescription = "بستن",
                            tint = Game.TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Banner Image (optional)
            if (!announcement.imageUrl.isNullOrBlank()) {
                AppAsyncImage(
                    data = announcement.imageUrl,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Type Icon
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(accentColor.copy(alpha = 0.15f))
                    .border(1.dp, accentColor.copy(alpha = 0.35f), RoundedCornerShape(18.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(iconRes),
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Title
            GameText(
                text = announcement.title,
                size = 18.sp,
                bold = true,
                color = Game.TextPrimary,
                align = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Message Body (scrollable if lengthy)
            val scrollState = rememberScrollState()
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 200.dp)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 4.dp)
            ) {
                GameText(
                    text = announcement.message,
                    size = 14.sp,
                    bold = false,
                    color = Game.TextSecondary,
                    align = TextAlign.Center,
                    lineHeight = 22.sp,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // "Do not show for 24h" checkbox (only for dismissible announcements)
            if (!announcement.isBlocking) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {
                                val newVal = !suppressFor24h
                                suppressFor24h = newVal
                            }
                        )
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        painter = painterResource(
                            if (suppressFor24h) Res.drawable.ic_checked else Res.drawable.ic_unchecked
                        ),
                        contentDescription = null,
                        tint = if (suppressFor24h) accentColor else Game.TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                    GameText(
                        text = "تا ۲۴ ساعت آینده نشان نده",
                        size = 12.sp,
                        color = if (suppressFor24h) Game.TextPrimary else Game.TextMuted
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Primary Button
            val primaryStyle = if (announcement.type.uppercase() == "ERROR") {
                GameButtonStyle.Danger
            } else {
                GameButtonStyle.Primary
            }

            GameButton(
                text = announcement.primaryButtonText.ifBlank { "فهمیدم" },
                onClick = {
                    if (suppressFor24h) {
                        onSuppressionToggle(true)
                    }
                    onPrimaryClick()
                },
                style = primaryStyle,
                modifier = Modifier.fillMaxWidth(),
                height = 46.dp
            )

            // Secondary Button (optional)
            if (!announcement.secondaryButtonText.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                GameButton(
                    text = announcement.secondaryButtonText ?: "",
                    onClick = {
                        if (suppressFor24h) {
                            onSuppressionToggle(true)
                        }
                        onSecondaryClick()
                    },
                    style = GameButtonStyle.Glass,
                    modifier = Modifier.fillMaxWidth(),
                    height = 44.dp
                )
            }
        }
    }
}
