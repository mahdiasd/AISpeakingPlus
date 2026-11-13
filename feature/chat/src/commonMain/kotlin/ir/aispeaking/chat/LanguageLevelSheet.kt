package ir.aispeaking.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.aispeaking.chat.component.AppleChatTheme
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.ic_spell_check
import ir.aispeaking.sharedui.ui.game.Game
import ir.aispeaking.sharedui.ui.game.GameButton
import ir.aispeaking.sharedui.ui.game.GameButtonStyle
import ir.aispeaking.sharedui.ui.game.GameSheet
import ir.aispeaking.sharedui.ui.game.GameText
import org.jetbrains.compose.resources.painterResource

/**
 * 2026 Apple Liquid Glass Language Level Explanation Bottom Sheet.
 *
 * Appears when tapping the chat header capsule to explain that the conversation
 * and AI persona responses are tailored to the learner's chosen CEFR level,
 * and can be adjusted anytime in their profile.
 */
@Composable
fun LanguageLevelSheet(
    visible: Boolean,
    userLevel: String?,
    onDismiss: () -> Unit
) {
    GameSheet(visible = visible, onDismiss = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Icon Badge
            val iconBadgeShape = CircleShape
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(iconBadgeShape)
                    .background(AppleChatTheme.PrimaryAzure.copy(alpha = 0.14f))
                    .border(1.2.dp, AppleChatTheme.PrimaryAzure.copy(alpha = 0.45f), iconBadgeShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_spell_check),
                    contentDescription = null,
                    tint = AppleChatTheme.PrimaryAzure,
                    modifier = Modifier.size(26.dp)
                )
            }

            // Title with Level Badge
            val displayLevel = userLevel?.trim()?.ifEmpty { "A1" } ?: "A1"
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                GameText(
                    text = "سطح این مکالمه: $displayLevel",
                    size = 18.sp,
                    bold = true,
                    align = TextAlign.Center
                )
                GameText(
                    text = "تنظیم هوشمند لغات و گرامر",
                    size = 12.sp,
                    color = Game.TextSecondary,
                    align = TextAlign.Center
                )
            }

            // Informational Glass Card
            val cardShape = RoundedCornerShape(20.dp)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(cardShape)
                    .background(AppleChatTheme.GlassPanelRaised)
                    .border(1.dp, AppleChatTheme.SpecularStrokeBrush, cardShape)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                GameText(
                    text = "پاسخ‌ها و لحن هوش مصنوعی در این گفتگو بر اساس سطح «$displayLevel» تنظیم شده‌اند تا دایره واژگان، سرعت ادای جملات و ساختار گرامری با میزان آمادگی شما همخوانی داشته باشند.",
                    size = 13.5.sp,
                    lineHeight = 22.sp,
                    color = Game.TextPrimary,
                    align = TextAlign.Justify,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(AppleChatTheme.PrimaryAzure.copy(alpha = 0.08f))
                        .border(1.dp, AppleChatTheme.PrimaryAzure.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GameText(
                        text = "💡 راهنما:",
                        size = 12.sp,
                        bold = true,
                        color = AppleChatTheme.PrimaryAzure
                    )
                    GameText(
                        text = "برای تغییر سطح مهارت خود، می‌توانید در هر زمان به صفحه پروفایل بروید و سطح زبان دلخواهتان را انتخاب کنید.",
                        size = 12.sp,
                        lineHeight = 18.sp,
                        color = Game.TextSecondary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Dismiss Button
            GameButton(
                text = "متوجه شدم",
                onClick = onDismiss,
                style = GameButtonStyle.Primary,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
