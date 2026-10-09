package ir.aispeaking.profile.component

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.ic_key
import ir.aispeaking.sharedui.ui.game.Game
import ir.aispeaking.sharedui.ui.game.GameButton
import ir.aispeaking.sharedui.ui.game.GameButtonStyle
import ir.aispeaking.sharedui.ui.game.GameText
import org.jetbrains.compose.resources.painterResource

@Composable
fun GuestBannerCard(
    onLoginClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(22.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF142033),
                        Game.PanelSolid
                    )
                )
            )
            .border(1.dp, Color(0x380A84FF), shape)
            .padding(18.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Game.Blue.copy(alpha = 0.16f))
                        .border(1.dp, Game.Blue.copy(alpha = 0.35f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_key),
                        contentDescription = null,
                        tint = Game.Blue,
                        modifier = Modifier.size(19.dp)
                    )
                }

                GameText(
                    text = "ذخیره همیشگی پیشرفت و مراحل",
                    color = Game.TextPrimary,
                    size = 15.sp,
                    bold = true
                )
            }

            GameText(
                text = "اکنون در حالت مهمان هستید. با ثبت‌نام رایگان شماره موبایل، پیشرفت، مراحل و امتیازات شما در فضای ابری ذخیره شده و هیچ‌گاه از دست نمی‌رود.",
                color = Game.TextSecondary,
                size = 12.sp,
                lineHeight = 18.sp
            )

            GameButton(
                text = "ورود یا ثبت‌نام رایگان",
                onClick = onLoginClick,
                style = GameButtonStyle.Primary,
                modifier = Modifier.align(Alignment.End),
                height = 42.dp,
                textSize = 13.sp
            )
        }
    }
}

/* --------------------------------- Previews --------------------------------- */

@androidx.compose.ui.tooling.preview.Preview
@Composable
private fun GuestBannerCardPreview() {
    ir.aispeaking.sharedui.ui.them.AppTheme {
        GuestBannerCard(onLoginClick = {})
    }
}
