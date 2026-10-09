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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
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
 * The learner's own message. The bubble stays clean; grammar feedback is shown right underneath
 * in plain Persian so it is never hidden behind a tap or a timer.
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
            val shape = RoundedCornerShape(topStart = 20.dp, topEnd = 6.dp, bottomEnd = 20.dp, bottomStart = 20.dp)
            Column(
                modifier = Modifier
                    .widthIn(max = 300.dp)
                    .background(
                        Brush.verticalGradient(listOf(Game.BubbleUserTop, Game.BubbleUserBottom)),
                        shape
                    )
                    .border(1.dp, androidx.compose.ui.graphics.Color(0x33FFFFFF), shape)
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                GameText(
                    text = chat.message,
                    size = 16.sp,
                    lineHeight = 24.sp,
                    latin = true,
                    color = Game.TextPrimary
                )
            }

            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                when (val status = chat.status) {
                    is ChatStatus.Sending -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(12.dp),
                                strokeWidth = 1.5.dp,
                                color = Game.TextSecondary
                            )
                            GameText(text = "در حال ارسال…", size = 11.sp, color = Game.TextSecondary)
                        }
                    }

                    is ChatStatus.Failed -> {
                        GameChip(
                            text = "ارسال نشد؛ دوباره تلاش کن",
                            icon = Res.drawable.ic_refresh,
                            accent = Game.Coral,
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

@Composable
private fun GrammarOkTag() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            painter = painterResource(Res.drawable.ic_done),
            contentDescription = null,
            tint = Game.Mint,
            modifier = Modifier.size(14.dp)
        )
        GameText(text = "گرامر درست بود", size = 11.sp, bold = true, color = Game.Mint)
    }
}

@Composable
private fun GrammarTipCard(text: String) {
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier = Modifier
            .widthIn(max = 300.dp)
            .background(Game.PanelSolid, shape)
            .border(1.dp, Game.Gold.copy(alpha = 0.6f), shape)
            .padding(horizontal = 12.dp, vertical = 9.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_spell_check),
                contentDescription = null,
                tint = Game.Gold,
                modifier = Modifier.size(15.dp)
            )
            GameText(text = "نکته گرامری", size = 12.sp, bold = true, color = Game.Gold)
        }
        GameText(text = text, size = 13.sp, lineHeight = 21.sp, color = Game.TextPrimary)
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
