package ir.aispeaking.subscription.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import ir.aispeaking.sharedui.ui.game.Game
import ir.aispeaking.sharedui.ui.game.GameText

@Composable
fun PromoCodeInputRow(
    promoCode: String,
    onPromoCodeChanged: (String) -> Unit,
    onApplyClicked: () -> Unit,
    feedbackMessage: String?,
    isError: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = promoCode,
                onValueChange = onPromoCodeChanged,
                placeholder = {
                    GameText(
                        text = "کد تخفیف دارید؟",
                        color = Game.TextMuted,
                        size = 13.sp
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Game.TextPrimary,
                    unfocusedTextColor = Game.TextPrimary,
                    focusedContainerColor = Game.PanelRaised,
                    unfocusedContainerColor = Game.Panel,
                    focusedBorderColor = Game.Blue,
                    unfocusedBorderColor = Game.Stroke,
                    cursorColor = Game.Blue
                ),
                modifier = Modifier.weight(1f)
            )

            Button(
                onClick = onApplyClicked,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Game.Blue,
                    contentColor = Color.White
                ),
                contentPadding = PaddingValues(horizontal = 20.dp),
                modifier = Modifier.height(54.dp)
            ) {
                GameText(
                    text = "اعمال",
                    color = Color.White,
                    size = 14.sp,
                    bold = true
                )
            }
        }

        feedbackMessage?.let { msg ->
            GameText(
                text = msg,
                color = if (isError) Game.Coral else Game.Blue,
                size = 12.sp,
                bold = true,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }
}

/* --------------------------------- Previews --------------------------------- */

@androidx.compose.ui.tooling.preview.Preview
@Composable
private fun PromoCodeInputRowPreview() {
    ir.aispeaking.sharedui.ui.them.AppTheme {
        PromoCodeInputRow(
            promoCode = "SUMMER2026",
            onPromoCodeChanged = {},
            onApplyClicked = {},
            feedbackMessage = "کد تخفیف ۲۰٪ با موفقیت اعمال شد",
            isError = false
        )
    }
}
