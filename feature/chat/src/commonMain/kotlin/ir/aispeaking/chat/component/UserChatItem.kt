package ir.aispeaking.chat.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.aispeaking.domain.model.chat.Chat
import ir.aispeaking.domain.model.chat.ChatStatus
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.ic_done
import ir.aispeaking.sharedui.ic_refresh
import ir.aispeaking.sharedui.ic_spell_check
import ir.aispeaking.sharedui.ui.game.Game
import ir.aispeaking.sharedui.ui.game.GameChip
import ir.aispeaking.sharedui.ui.game.GameText
import ir.aispeaking.sharedui.ui.them.AppTheme
import org.jetbrains.compose.resources.painterResource

/**
 * 2026 Apple Liquid Glass User Chat Message Item.
 *
 * Characteristics:
 * - Rich Royal Azure & Electric Indigo glass gradient with specular top rim.
 * - Hero English copy with high legibility.
 * - Grammar status indicator beneath the bubble:
 *   - Serene Cyan checkmark for correct grammar (strictly NO green).
 *   - Warm Champagne Gold translucent card for grammar coaching tips.
 *   - Apple Coral retry chip for network failures.
 */
@Composable
fun UserChatItem(
    modifier: Modifier = Modifier,
    chat: Chat.User,
    onRetry: () -> Unit = {}
) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val shape = RoundedCornerShape(
                topStart = 20.dp,
                topEnd = 6.dp,
                bottomEnd = 20.dp,
                bottomStart = 20.dp
            )

            // Rich Royal Azure & Indigo Glass Bubble
            Column(
                modifier = Modifier
                    .widthIn(max = 310.dp)
                    .clip(shape)
                    .background(AppleChatTheme.UserBubbleBrush)
                    .border(1.dp, AppleChatTheme.SpecularStrokeBrush, shape)
                    .padding(horizontal = 15.dp, vertical = 11.dp)
            ) {
                GameText(
                    text = chat.message,
                    size = 16.sp,
                    lineHeight = 24.sp,
                    latin = true,
                    color = Color.White
                )
            }

            // Status & Feedback HUD underneath
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                when (val status = chat.status) {
                    is ChatStatus.Sending -> Unit
                    is ChatStatus.Failed -> {
                        GameChip(
                            text = "ارسال نشد؛ دوباره تلاش کن",
                            icon = Res.drawable.ic_refresh,
                            accent = AppleChatTheme.Coral,
                            border = AppleChatTheme.Coral.copy(alpha = 0.5f),
                            active = true,
                            onClick = onRetry
                        )
                    }

                    is ChatStatus.Answered -> {
                        if (status.grammar.isBlank()) {
                            GrammarOkTag()
                        } else {
                            GrammarTipCard(text = status.grammar)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Grammar OK status tag in Serene Cyan (strictly NO green).
 */
@Composable
private fun GrammarOkTag() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        Icon(
            painter = painterResource(Res.drawable.ic_done),
            contentDescription = null,
            tint = AppleChatTheme.SuccessCyan,
            modifier = Modifier.size(14.dp)
        )
        GameText(
            text = "گرامر درست بود",
            size = 11.sp,
            bold = true,
            color = AppleChatTheme.SuccessCyan
        )
    }
}

/**
 * Champagne Gold Frosted Glass Grammar Tip Card.
 */
@Composable
private fun GrammarTipCard(text: String) {
    val isDark = AppTheme.isDark
    val shape = RoundedCornerShape(16.dp)
    val accent = if (isDark) AppleChatTheme.Gold else AppleChatTheme.GoldDeep
    Column(
        modifier = Modifier
            .widthIn(max = 310.dp)
            .clip(shape)
            .background(if (isDark) Color(0xD91E1C1A) else Color(0xFFFFFBEB))
            .border(1.dp, accent.copy(alpha = if (isDark) 0.5f else 0.45f), shape)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_spell_check),
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(15.dp)
            )
            GameText(
                text = "نکته گرامری",
                size = 12.sp,
                bold = true,
                color = accent
            )
        }
        GameText(
            text = text,
            size = 13.sp,
            lineHeight = 21.sp,
            color = Game.TextPrimary
        )
    }
}

/* --------------------------------- Previews --------------------------------- */

@androidx.compose.ui.tooling.preview.Preview
@Composable
private fun UserChatItemPreview() {
    AppTheme {
        UserChatItem(
            chat = Chat.User(
                uid = "1",
                message = "I want to have a coffee.",
                status = ChatStatus.Answered(grammar = "")
            )
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview
@Composable
private fun UserChatItemGrammarPreview() {
    AppTheme {
        UserChatItem(
            chat = Chat.User(
                uid = "2",
                message = "I wants a coffee.",
                status = ChatStatus.Answered(grammar = "بهتر است بگویی «I want a coffee» چون فاعل I است.")
            )
        )
    }
}
