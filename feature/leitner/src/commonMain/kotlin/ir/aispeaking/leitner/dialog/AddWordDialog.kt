package ir.aispeaking.leitner.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.aispeaking.domain.validation.LeitnerWordValidator
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.ic_add
import ir.aispeaking.sharedui.ic_close
import ir.aispeaking.sharedui.ic_spell_check
import ir.aispeaking.sharedui.ui.game.Game
import ir.aispeaking.sharedui.ui.game.GameButton
import ir.aispeaking.sharedui.ui.game.GameButtonStyle
import ir.aispeaking.sharedui.ui.game.GameText
import ir.aispeaking.sharedui.ui.them.AppleTheme
import ir.aispeaking.sharedui.ui.them.AppTheme
import ir.aispeaking.sharedui.ui.them.applePressEffect
import org.jetbrains.compose.resources.painterResource

/**
 * 2026 Apple-designed Add Word Modal Dialog for Leitner Box.
 *
 * Prompts user to type any English word or phrase, then triggers dictionary lookup.
 */
@Composable
fun AddWordDialog(
    onDismiss: () -> Unit,
    onConfirm: (word: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = AppTheme.isDark
    var wordInput by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf<String?>(null) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        // Small delay to ensure smooth dialog entry transition before requesting keyboard focus
        kotlinx.coroutines.delay(120)
        try {
            focusRequester.requestFocus()
        } catch (_: Exception) {}
    }

    val dialogShape = RoundedCornerShape(24.dp)

    // Fullscreen Scrim
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.65f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            ),
        contentAlignment = Alignment.Center
    ) {
        // Modal Card
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(dialogShape)
                .background(AppleTheme.GlassPanelRaised)
                .border(1.dp, AppleTheme.SpecularStrokeBrush, dialogShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {} // Intercept clicks inside card
                )
                .padding(22.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(AppleTheme.PrimaryAzure.copy(alpha = 0.16f))
                                .border(1.dp, AppleTheme.PrimaryAzure.copy(alpha = 0.4f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_add),
                                contentDescription = null,
                                tint = AppleTheme.PrimaryAzure,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            GameText(
                                text = "افزودن کلمه به لایتنر",
                                size = 16.sp,
                                bold = true,
                                color = Game.TextPrimary
                            )
                            GameText(
                                text = "کلمه یا اصطلاح انگلیسی رو وارد کنید",
                                size = 12.sp,
                                color = Game.TextSecondary
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .applePressEffect(targetScale = 0.90f, onClick = onDismiss)
                            .clip(CircleShape)
                            .background(if (isDark) Color(0x33FFFFFF) else Color(0x14000000)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_close),
                            contentDescription = "بستن",
                            tint = Game.TextSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                // Apple-style Input Field (LTR)
                val inputShape = RoundedCornerShape(14.dp)
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(inputShape)
                            .background(if (isDark) Color(0xEB161824) else Color(0xF2F1F5F9))
                            .border(
                                1.5.dp,
                                when {
                                    validationError != null -> AppleTheme.Coral
                                    wordInput.isNotBlank() -> AppleTheme.PrimaryAzure
                                    else -> AppleTheme.SubtleBorderColor
                                },
                                inputShape
                            )
                            .padding(horizontal = 14.dp, vertical = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                if (wordInput.isEmpty()) {
                                    GameText(
                                        text = "e.g. breakthrough, persistent",
                                        size = 15.sp,
                                        color = Game.TextSecondary.copy(alpha = 0.6f),
                                        latin = true
                                    )
                                }

                                BasicTextField(
                                    value = wordInput,
                                    onValueChange = {
                                        wordInput = it
                                        validationError = null
                                    },
                                    singleLine = true,
                                    textStyle = TextStyle(
                                        color = if (isDark) Color.White else Color(0xFF0F172A),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    cursorBrush = SolidColor(AppleTheme.PrimaryAzure),
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                    keyboardActions = KeyboardActions(
                                        onSearch = {
                                            val validation = LeitnerWordValidator.validate(wordInput)
                                            if (validation.isValid) {
                                                onConfirm(wordInput.trim())
                                            } else {
                                                validationError = validation.errorMessage
                                            }
                                        }
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .focusRequester(focusRequester)
                                )
                            }

                            if (wordInput.isNotEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .size(22.dp)
                                        .clip(CircleShape)
                                        .background(if (isDark) Color(0x33FFFFFF) else Color(0x1F000000))
                                        .clickable {
                                            wordInput = ""
                                            validationError = null
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        painter = painterResource(Res.drawable.ic_close),
                                        contentDescription = "پاک کردن",
                                        tint = Game.TextSecondary,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                validationError?.let { errorMsg ->
                    GameText(
                        text = errorMsg,
                        size = 12.sp,
                        color = AppleTheme.Coral,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    GameButton(
                        text = "انصراف",
                        style = GameButtonStyle.Glass,
                        modifier = Modifier.weight(1f),
                        onClick = onDismiss
                    )

                    GameButton(
                        text = "تایید و جستجو",
                        icon = Res.drawable.ic_spell_check,
                        style = GameButtonStyle.Primary,
                        modifier = Modifier.weight(1.5f),
                        onClick = {
                            val validation = LeitnerWordValidator.validate(wordInput)
                            if (validation.isValid) {
                                onConfirm(wordInput.trim())
                            } else {
                                validationError = validation.errorMessage
                            }
                        }
                    )
                }
            }
        }
    }
}
