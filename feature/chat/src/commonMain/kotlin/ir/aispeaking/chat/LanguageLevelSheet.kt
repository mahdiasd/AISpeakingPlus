package ir.aispeaking.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.aispeaking.chat.component.AppleChatTheme
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.ic_profile
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
 * with prominent, high-contrast, friendly focus on how to adjust it in Profile.
 */
@Composable
fun LanguageLevelSheet(
    visible: Boolean,
    userLevel: String?,
    onDismiss: () -> Unit
) {
    val displayLevel = userLevel?.trim()?.ifEmpty { "A1" } ?: "A1"

    GameSheet(visible = visible, onDismiss = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Luminous Icon Badge
            val iconBadgeShape = CircleShape
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(iconBadgeShape)
                    .background(AppleChatTheme.PrimaryAzure.copy(alpha = 0.16f))
                    .border(1.5.dp, AppleChatTheme.PrimaryAzure.copy(alpha = 0.6f), iconBadgeShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_spell_check),
                    contentDescription = null,
                    tint = AppleChatTheme.PrimaryAzure,
                    modifier = Modifier.size(28.dp)
                )
            }

            // Title with Level Badge
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                GameText(
                    text = "سطح این گفتگو: $displayLevel",
                    size = 19.sp,
                    bold = true,
                    align = TextAlign.Center
                )
                GameText(
                    text = "هوش مصنوعی با مهارت زبانی تو هماهنگ شده",
                    size = 12.5.sp,
                    color = AppleChatTheme.SuccessCyan,
                    bold = true,
                    align = TextAlign.Center
                )
            }

            // Card 1: Warm current level explanation
            val cardShape = RoundedCornerShape(20.dp)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(cardShape)
                    .background(AppleChatTheme.GlassPanelRaised)
                    .border(1.dp, AppleChatTheme.SpecularStrokeBrush, cardShape)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                GameText(
                    text = "من هوای سطحت رو دارم! تمام کلمه‌ها، اصطلاحات و سرعت ادای جملاتم دقیقاً بر اساس سطح «$displayLevel» برات تنظیم شده تا راحت و بدون دغدغه صحبت کنی و قدم‌به‌قدم پیشرفت کنی.",
                    size = 13.5.sp,
                    lineHeight = 22.sp,
                    color = Game.TextPrimary,
                    align = TextAlign.Justify,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Card 2: HERO FOCUS - How to change level (Highlighted, High-Contrast, Apple Liquid Glass)
            val focusCardShape = RoundedCornerShape(20.dp)
            val heroBorderBrush = Brush.horizontalGradient(
                listOf(
                    AppleChatTheme.PrimaryAzure,
                    AppleChatTheme.PrimaryIndigo,
                    AppleChatTheme.PrimaryAzure
                )
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(focusCardShape)
                    .background(AppleChatTheme.PrimaryAzure.copy(alpha = 0.14f))
                    .border(1.5.dp, heroBorderBrush, focusCardShape)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(AppleChatTheme.PrimaryAzure.copy(alpha = 0.22f))
                            .border(1.dp, AppleChatTheme.PrimaryAzure.copy(alpha = 0.6f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_profile),
                            contentDescription = null,
                            tint = AppleChatTheme.PrimaryAzure,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    GameText(
                        text = "می‌خوای سطحت رو تغییر بدی؟",
                        size = 15.sp,
                        bold = true,
                        color = Game.TextPrimary
                    )
                }

                GameText(
                    text = "خیلی راحته! هر وقت احساس کردی گفتگوها برات ساده یا خیلی چالشی شدن، کافیه بری به بخش پروفایل و از اونجا سطح دلخواهت رو انتخاب کنی تا بلافاصله همه‌چیز با سطح جدیدت هماهنگ بشه.",
                    size = 13.5.sp,
                    lineHeight = 22.sp,
                    color = Game.TextPrimary,
                    align = TextAlign.Justify,
                    modifier = Modifier.fillMaxWidth()
                )

                // High-visibility shortcut badge
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(AppleChatTheme.PrimaryAzure.copy(alpha = 0.22f))
                        .border(1.dp, AppleChatTheme.PrimaryAzure.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GameText(
                        text = "راه تغییر:",
                        size = 12.sp,
                        bold = true,
                        color = AppleChatTheme.PrimaryAzure
                    )
                    GameText(
                        text = "صفحه پروفایل ← بخش سطح زبان انگلیسی ← دکمه تغییر",
                        size = 12.sp,
                        bold = true,
                        color = Game.TextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Dismiss CTA
            GameButton(
                text = "متوجه شدم، بزن بریم!",
                onClick = onDismiss,
                style = GameButtonStyle.Primary,
                modifier = Modifier.fillMaxWidth(),
                height = 48.dp
            )
        }
    }
}
