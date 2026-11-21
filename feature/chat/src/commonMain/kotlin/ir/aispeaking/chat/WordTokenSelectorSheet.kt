package ir.aispeaking.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.aispeaking.chat.component.AppleChatTheme
import ir.aispeaking.chat.component.applePressEffect
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.ic_close
import ir.aispeaking.sharedui.ic_spell_check
import ir.aispeaking.sharedui.ui.game.Game
import ir.aispeaking.sharedui.ui.game.GameButton
import ir.aispeaking.sharedui.ui.game.GameSheet
import ir.aispeaking.sharedui.ui.game.GameText
import ir.aispeaking.sharedui.ui.them.AppTheme
import org.jetbrains.compose.resources.painterResource

private data class WordToken(
    val index: Int,
    val raw: String,
    val clean: String
)

/**
 * 2026 Apple Liquid Glass Interactive Word Token Selector Sheet.
 *
 * Breaks AI chat message sentences into interactive, touchable word chips:
 * - Direct single-word or multi-word contiguous phrase selection.
 * - Compact Apple-style layout with tactile spring feedback.
 * - Selection preview box with separate primary action button outside.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WordTokenSelectorSheet(
    visible: Boolean,
    sentence: String,
    onDismiss: () -> Unit,
    onWordSelected: (selectedWord: String, sentenceContext: String) -> Unit
) {
    val tokens = remember(sentence) {
        sentence.trim()
            .split(Regex("\\s+"))
            .filter { it.isNotBlank() }
            .mapIndexed { index, raw ->
                val clean = raw.trim().replace(Regex("^[^a-zA-Z0-9']+|[^a-zA-Z0-9']+$"), "")
                WordToken(index = index, raw = raw, clean = clean)
            }
    }

    var selectedIndices by remember(sentence, visible) { mutableStateOf<Set<Int>>(emptySet()) }

    fun toggleToken(index: Int) {
        if (selectedIndices.isEmpty()) {
            selectedIndices = setOf(index)
            return
        }

        if (selectedIndices == setOf(index)) {
            selectedIndices = emptySet()
            return
        }

        val min = selectedIndices.minOrNull() ?: index
        val max = selectedIndices.maxOrNull() ?: index

        selectedIndices = when {
            // Expand contiguous selection to the right
            index == max + 1 -> (min..index).toSet()
            // Expand contiguous selection to the left
            index == min - 1 -> (index..max).toSet()
            // Shrink from the right edge
            index == max && selectedIndices.size > 1 -> (min until max).toSet()
            // Shrink from the left edge
            index == min && selectedIndices.size > 1 -> ((min + 1)..max).toSet()
            // Clicked inside the range or on a disconnected token -> select tapped token
            else -> setOf(index)
        }
    }

    val selectedTokens = remember(selectedIndices, tokens) {
        tokens.filter { it.index in selectedIndices }.sortedBy { it.index }
    }

    val selectedPhrase = remember(selectedTokens) {
        selectedTokens.joinToString(" ") { it.clean.ifBlank { it.raw } }.trim()
    }

    GameSheet(
        visible = visible,
        onDismiss = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(11.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(AppleChatTheme.PrimaryAzure.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_spell_check),
                            contentDescription = null,
                            tint = AppleChatTheme.PrimaryAzure,
                            modifier = Modifier.size(15.dp)
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                        GameText(
                            text = "انتخاب کلمه یا عبارت",
                            size = 15.sp,
                            bold = true,
                            color = Game.TextPrimary
                        )
                        GameText(
                            text = "برای ترجمه و دیکشنری، کلمات مورد نظر را لمس کنید",
                            size = 11.sp,
                            color = Game.TextSecondary
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .applePressEffect(targetScale = 0.92f, onClick = onDismiss)
                        .clip(CircleShape)
                        .background(AppleChatTheme.GlassPanelRaised)
                        .border(1.dp, AppleChatTheme.SubtleBorderColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_close),
                        contentDescription = "بستن",
                        tint = Game.TextSecondary,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }

            // Word Tokens Flow Container (Rendered LTR for correct English word order)
            val containerShape = RoundedCornerShape(16.dp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(containerShape)
                    .background(AppleChatTheme.GlassPanel)
                    .border(1.dp, AppleChatTheme.SpecularStrokeBrush, containerShape)
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        tokens.forEach { token ->
                            val isSelected = token.index in selectedIndices
                            TokenChip(
                                token = token,
                                isSelected = isSelected,
                                onClick = { toggleToken(token.index) }
                            )
                        }
                    }
                }
            }

            // Confirmation / Action Area
            val actionPanelShape = RoundedCornerShape(14.dp)
            AnimatedVisibility(
                visible = selectedPhrase.isNotBlank(),
                enter = fadeIn(tween(180)),
                exit = fadeOut(tween(140))
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Selected Words Box (Distinct display card WITHOUT button inside)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(actionPanelShape)
                            .background(AppleChatTheme.PrimaryAzure.copy(alpha = 0.08f))
                            .border(1.dp, AppleChatTheme.PrimaryAzure.copy(alpha = 0.35f), actionPanelShape)
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            GameText(
                                text = "عبارت انتخاب‌شده:",
                                size = 11.5.sp,
                                color = Game.TextSecondary
                            )

                            // Deselect button
                            GameText(
                                text = "پاک کردن انتخاب",
                                size = 11.sp,
                                color = AppleChatTheme.PrimaryAzure,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable { selectedIndices = emptySet() }
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                            GameText(
                                text = selectedPhrase,
                                size = 17.sp,
                                bold = true,
                                color = AppleChatTheme.PrimaryAzure,
                                latin = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    // Action Button (Outside the box as requested)
                    GameButton(
                        text = "مشاهده در دیکشنری",
                        icon = Res.drawable.ic_spell_check,
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            if (selectedPhrase.isNotBlank()) {
                                onWordSelected(selectedPhrase, sentence)
                                onDismiss()
                            }
                        }
                    )
                }
            }

            if (selectedPhrase.isBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    GameText(
                        text = "یک کلمه را لمس کنید، یا کلمات متوالی را برای انتخاب عبارت انتخاب نمایید",
                        size = 11.5.sp,
                        color = Game.TextSecondary.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(Modifier.height(4.dp))
        }
    }
}

/**
 * Individual touchable word token chip with compact Apple-style ergonomics.
 */
@Composable
private fun TokenChip(
    token: WordToken,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = AppTheme.isDark
    val shape = RoundedCornerShape(9.dp)

    val scaleAnim by animateFloatAsState(
        targetValue = if (isSelected) 1.04f else 1f,
        animationSpec = spring(dampingRatio = 0.78f, stiffness = 420f),
        label = "token_scale"
    )

    val bgAnim by animateColorAsState(
        targetValue = when {
            isSelected -> AppleChatTheme.PrimaryAzure.copy(alpha = 0.22f)
            isDark -> Color(0xEB232738)
            else -> Color(0xF2F1F5F9)
        },
        animationSpec = tween(180),
        label = "token_bg"
    )

    val strokeAnim by animateColorAsState(
        targetValue = when {
            isSelected -> AppleChatTheme.PrimaryAzure
            else -> AppleChatTheme.SubtleBorderColor
        },
        animationSpec = tween(180),
        label = "token_stroke"
    )

    val textColor = when {
        isSelected -> AppleChatTheme.PrimaryAzure
        else -> Game.TextPrimary
    }

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scaleAnim
                scaleY = scaleAnim
            }
            .applePressEffect(targetScale = 0.94f, onClick = onClick)
            .clip(shape)
            .background(bgAnim)
            .border(if (isSelected) 1.5.dp else 1.dp, strokeAnim, shape)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        GameText(
            text = token.raw,
            size = 13.5.sp,
            bold = isSelected,
            color = textColor,
            latin = true
        )
    }
}
