package ir.aispeaking.sharedui.ui.subscription.component

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
                    Text(
                        text = "کد تخفیف دارید؟",
                        color = Color(0xFF64748B),
                        fontSize = 13.sp
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Color(0xFF1E293B),
                    unfocusedContainerColor = Color(0xFF1E293B),
                    focusedBorderColor = Color(0xFF818CF8),
                    unfocusedBorderColor = Color(0xFF334155),
                    cursorColor = Color(0xFF818CF8)
                ),
                modifier = Modifier.weight(1f)
            )

            Button(
                onClick = onApplyClicked,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF334155)
                ),
                contentPadding = PaddingValues(horizontal = 20.dp),
                modifier = Modifier.height(54.dp)
            ) {
                Text(
                    text = "اعمال",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        feedbackMessage?.let { msg ->
            Text(
                text = msg,
                color = if (isError) Color(0xFFF87171) else Color(0xFF34D399),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }
}
