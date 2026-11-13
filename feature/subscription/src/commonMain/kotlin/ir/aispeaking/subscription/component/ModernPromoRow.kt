package ir.aispeaking.subscription.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.ic_done
import ir.aispeaking.sharedui.ic_key
import ir.aispeaking.sharedui.ui.game.Game
import ir.aispeaking.sharedui.ui.game.GameText
import ir.aispeaking.sharedui.ui.them.AppTheme
import org.jetbrains.compose.resources.painterResource

/**
 * Minimalist expandable promo code input row to keep the subscription screen clean and uncluttered.
 */
@Composable
fun ModernPromoRow(
    promoCode: String,
    appliedDiscountPercent: Int,
    feedbackMessage: String?,
    isError: Boolean,
    onPromoCodeChanged: (String) -> Unit,
    onApplyClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = AppTheme.isDark
    val focusManager = LocalFocusManager.current
    var isExpanded by remember { mutableStateOf(promoCode.isNotBlank() || feedbackMessage != null) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (appliedDiscountPercent > 0) {
            // Success Chip when discount is applied
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF10B981).copy(alpha = if (isDark) 0.20f else 0.12f))
                    .border(1.dp, Color(0xFF10B981).copy(alpha = 0.40f), RoundedCornerShape(14.dp))
                    .clickable { isExpanded = !isExpanded }
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_done),
                    contentDescription = null,
                    tint = Color(0xFF10B981),
                    modifier = Modifier.size(14.dp)
                )
                GameText(
                    text = "کد تخفیف اعمال شد (${formatToPersian(appliedDiscountPercent.toLong())}٪ کسر گردید)",
                    size = 12.sp,
                    color = Color(0xFF10B981),
                    bold = true
                )
            }
        } else if (!isExpanded) {
            // Compact trigger button to expand
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isDark) Color(0x1AFFFFFF) else Color(0x0D000000))
                    .border(1.dp, if (isDark) Color(0x1FFFFFFF) else Color(0x10000000), RoundedCornerShape(12.dp))
                    .clickable { isExpanded = true }
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_key),
                    contentDescription = null,
                    tint = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                    modifier = Modifier.size(13.dp)
                )
                GameText(
                    text = "کد تخفیف دارید؟",
                    size = 12.sp,
                    color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                    bold = false
                )
            }
        }

        // Expanded text field and apply button
        AnimatedVisibility(
            visible = isExpanded && appliedDiscountPercent == 0,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = promoCode,
                        onValueChange = onPromoCodeChanged,
                        placeholder = {
                            GameText(
                                text = "کد تخفیف را وارد کنید...",
                                color = if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8),
                                size = 12.sp
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            focusManager.clearFocus()
                            onApplyClicked()
                        }),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = if (isDark) Color.White else Color(0xFF0F172A),
                            unfocusedTextColor = if (isDark) Color.White else Color(0xFF0F172A),
                            focusedContainerColor = if (isDark) Color(0xFF1E2638) else Color.White,
                            unfocusedContainerColor = if (isDark) Color(0xFF141A28) else Color(0xFFF8FAFC),
                            focusedBorderColor = Color(0xFF6366F1),
                            unfocusedBorderColor = if (isDark) Color(0x33FFFFFF) else Color(0x1F000000),
                            cursorColor = Color(0xFF6366F1)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    )

                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            onApplyClicked()
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF6366F1),
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        modifier = Modifier.height(48.dp)
                    ) {
                        GameText(
                            text = "اعمال",
                            color = Color.White,
                            size = 13.sp,
                            bold = true
                        )
                    }
                }

                feedbackMessage?.let { msg ->
                    GameText(
                        text = msg,
                        color = if (isError) Color(0xFFEF4444) else Color(0xFF10B981),
                        size = 12.sp,
                        bold = true,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            }
        }
    }
}

private fun formatToPersian(number: Long): String {
    val persianDigits = listOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
    val formatted = number.toString().reversed().chunked(3).joinToString(",").reversed()
    return formatted.map { char ->
        if (char in '0'..'9') persianDigits[char - '0'] else char
    }.joinToString("")
}
