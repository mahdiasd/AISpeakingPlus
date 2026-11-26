package ir.aispeaking.profile.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.aispeaking.sharedui.ui.game.Game
import ir.aispeaking.sharedui.ui.game.GameButton
import ir.aispeaking.sharedui.ui.game.GameButtonStyle
import ir.aispeaking.sharedui.ui.game.GameModal
import ir.aispeaking.sharedui.ui.game.GameText

/**
 * Brief pre-start introduction dialog shown before opening the English placement test.
 */
@Composable
fun PlacementTestIntroDialog(
    visible: Boolean,
    onStart: () -> Unit,
    onDismiss: () -> Unit
) {
    GameModal(visible = visible, onDismiss = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            GameText(
                text = "آزمون تعیین سطح",
                size = 18.sp,
                bold = true
            )

            GameText(
                text = "این آزمون ۲۰ سؤال چهارگزینه‌ای دارد و حدود ۵ دقیقه زمان می‌برد.",
                size = 13.5.sp,
                lineHeight = 22.sp,
                color = Game.TextSecondary
            )

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                GameText(
                    text = "• سطح فعلی زبان انگلیسی‌ات (از A1 تا C1) با دقت مشخص می‌شود.",
                    size = 12.5.sp,
                    lineHeight = 20.sp,
                    color = Game.TextSecondary
                )
                GameText(
                    text = "• هر زمان بخواهی می‌توانی آزمون را زودتر به پایان برسانی.",
                    size = 12.5.sp,
                    lineHeight = 20.sp,
                    color = Game.TextSecondary
                )
                GameText(
                    text = "• نتیجه در پروفایلت ثبت می‌شود تا تمرین‌ها متناسب با سطحت باشند.",
                    size = 12.5.sp,
                    lineHeight = 20.sp,
                    color = Game.TextSecondary
                )
            }

            GameButton(
                text = "شروع آزمون",
                onClick = onStart,
                modifier = Modifier.fillMaxWidth()
            )

            GameButton(
                text = "انصراف",
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                style = GameButtonStyle.Glass,
                height = 46.dp,
                textSize = 14.sp
            )
        }
    }
}
