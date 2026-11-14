package ir.aispeaking.subscription.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.ic_alert_circle
import ir.aispeaking.sharedui.ic_close
import ir.aispeaking.sharedui.ic_done
import ir.aispeaking.sharedui.ic_gem
import ir.aispeaking.sharedui.ui.game.GameText
import ir.aispeaking.sharedui.ui.them.AppTheme
import org.jetbrains.compose.resources.painterResource

/**
 * Premium Apple Liquid Glass Promo Code banner designed for the top of the subscription page.
 * Seamlessly expands when tapped, keeping the view clean, lightweight, and focused.
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
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (appliedDiscountPercent > 0) {
            // Applied Discount Success Pill
            val successShape = RoundedCornerShape(20.dp)
            Row(
                modifier = Modifier
                    .clip(successShape)
                    .background(
                        Brush.horizontalGradient(
                            if (isDark) listOf(Color(0xFF064E3B).copy(alpha = 0.8f), Color(0xFF047857).copy(alpha = 0.6f))
                            else listOf(Color(0xFFECFDF5), Color(0xFFD1FAE5))
                        )
                    )
                    .border(1.dp, Color(0xFF10B981).copy(alpha = 0.50f), successShape)
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF10B981)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_done),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                }

                GameText(
                    text = "کد تخفیف اعمال شد (${formatToPersian(appliedDiscountPercent.toLong())}٪ کسر گردید)",
                    size = 12.sp,
                    color = if (isDark) Color(0xFF6EE7B7) else Color(0xFF065F46),
                    bold = true
                )

                Spacer(modifier = Modifier.width(4.dp))

                // Small remove/cancel button
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(if (isDark) Color(0x33FFFFFF) else Color(0x22000000))
                        .clickable {
                            onPromoCodeChanged("")
                            onApplyClicked()
                            isExpanded = false
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_close),
                        contentDescription = "حذف تخفیف",
                        tint = if (isDark) Color.White else Color(0xFF065F46),
                        modifier = Modifier.size(11.dp)
                    )
                }
            }
        } else if (!isExpanded) {
            // Sleek Collapsed Pill (Prompt to enter promo code)
            val pillInteraction = remember { MutableInteractionSource() }
            val isPressed by pillInteraction.collectIsPressedAsState()
            val pillShape = RoundedCornerShape(50)

            Row(
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = if (isPressed) 0.96f else 1f
                        scaleY = if (isPressed) 0.96f else 1f
                    }
                    .clip(pillShape)
                    .background(
                        if (isDark) Color(0x26FFFFFF) else Color(0x0F000000)
                    )
                    .border(
                        1.dp,
                        if (isDark) Color(0x28FFFFFF) else Color(0x18000000),
                        pillShape
                    )
                    .clickable(
                        interactionSource = pillInteraction,
                        indication = null,
                        onClick = { isExpanded = true }
                    )
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_gem),
                    contentDescription = null,
                    tint = Color(0xFFFFC83D),
                    modifier = Modifier.size(14.dp)
                )

                GameText(
                    text = "کد تخفیف دارید؟",
                    size = 12.sp,
                    color = if (isDark) Color(0xFFE2E8F0) else Color(0xFF334155),
                    bold = false
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isDark) Color(0x33FFFFFF) else Color(0x18000000))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    GameText(
                        text = "وارد کنید ›",
                        size = 10.5.sp,
                        color = Color(0xFFFFC83D),
                        bold = true
                    )
                }
            }
        }

        // Expanded Glass Card for Promo Code Input
        AnimatedVisibility(
            visible = isExpanded && appliedDiscountPercent == 0,
            enter = fadeIn() + expandVertically(spring(dampingRatio = 0.8f, stiffness = 400f)),
            exit = fadeOut() + shrinkVertically()
        ) {
            val cardShape = RoundedCornerShape(18.dp)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(cardShape)
                    .background(
                        if (isDark) Color(0x381E2638) else Color(0x80FFFFFF)
                    )
                    .border(
                        1.dp,
                        if (isError) Color(0xFFEF4444).copy(alpha = 0.5f)
                        else if (isDark) Color(0x2EFFFFFF) else Color(0x1A000000),
                        cardShape
                    )
                    .padding(horizontal = 10.dp, vertical = 8.dp),
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
                                text = "کد تخفیف...",
                                color = if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8),
                                size = 12.sp
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            focusManager.clearFocus()
                            onApplyClicked()
                        }),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = if (isDark) Color.White else Color(0xFF0F172A),
                            unfocusedTextColor = if (isDark) Color.White else Color(0xFF0F172A),
                            focusedContainerColor = if (isDark) Color(0xFF131924) else Color(0xFFF1F5F9),
                            unfocusedContainerColor = if (isDark) Color(0xFF131924) else Color(0xFFF1F5F9),
                            focusedBorderColor = Color(0xFF22C55E),
                            unfocusedBorderColor = if (isDark) Color(0x26FFFFFF) else Color(0x1A000000),
                            cursorColor = Color(0xFF22C55E)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                    )

                    // Apply Button
                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            onApplyClicked()
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF22C55E),
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 14.dp),
                        modifier = Modifier.height(42.dp)
                    ) {
                        GameText(
                            text = "اعمال",
                            color = Color.White,
                            size = 12.5.sp,
                            bold = true
                        )
                    }

                    // Close/Collapse Button
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(if (isDark) Color(0x22FFFFFF) else Color(0x12000000))
                            .clickable {
                                isExpanded = false
                                focusManager.clearFocus()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_close),
                            contentDescription = "بستن",
                            tint = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                // Error or Info Feedback Message
                feedbackMessage?.let { msg ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        if (isError) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_alert_circle),
                                contentDescription = null,
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(13.dp)
                            )
                        }
                        GameText(
                            text = msg,
                            color = if (isError) Color(0xFFEF4444) else Color(0xFF10B981),
                            size = 11.5.sp,
                            bold = true
                        )
                    }
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
