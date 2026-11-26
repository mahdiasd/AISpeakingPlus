package ir.aispeaking.leitner.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.aispeaking.domain.model.leitner.Flashcard
import ir.aispeaking.domain.model.leitner.LeitnerBox
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.ic_close
import ir.aispeaking.sharedui.ic_delete
import ir.aispeaking.sharedui.ic_speaker
import ir.aispeaking.sharedui.ui.core.text.PhoneticText
import ir.aispeaking.sharedui.ui.game.Game
import ir.aispeaking.sharedui.ui.game.GameIconButton
import ir.aispeaking.sharedui.ui.game.GameText
import ir.aispeaking.sharedui.ui.game.fa
import ir.aispeaking.sharedui.ui.them.AppTheme
import org.jetbrains.compose.resources.painterResource

@Composable
fun FocusedCardDialog(
    card: Flashcard,
    onDismiss: () -> Unit,
    onPlayAudio: (Flashcard) -> Unit,
    isAudioPlaying: Boolean,
    onDeleteClick: (Flashcard) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isDark = AppTheme.isDark
    val density = LocalDensity.current.density
    var isFlipped by remember { mutableStateOf(false) }

    // 3D rotation animation
    val rotationY by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = spring(
            dampingRatio = 0.82f,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "focused_card_3d_flip"
    )

    val boxColor = getBoxAccentColor(card.boxNumber)
    val leitnerBox = LeitnerBox.fromBoxNumber(card.boxNumber)
    val shape = RoundedCornerShape(28.dp)

    // Fullscreen Scrim Backdrop
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.72f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            ),
        contentAlignment = Alignment.Center
    ) {
        // Top Delete Button (Start anchor)
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(24.dp)
        ) {
            GameIconButton(
                icon = Res.drawable.ic_delete,
                onClick = { onDeleteClick(card) },
                contentDescription = "حذف کلمه",
                container = Color(0x33FF453A),
                border = Color(0x66FF453A),
                tint = Color(0xFFFF6961)
            )
        }

        // Top Close Button
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(24.dp)
        ) {
            GameIconButton(
                icon = Res.drawable.ic_close,
                onClick = onDismiss,
                contentDescription = "بستن",
                container = Color(0x33FFFFFF),
                border = Color(0x4DFFFFFF),
                tint = Color.White
            )
        }

        // Centered 3D Flippable Card
        Box(
            modifier = Modifier
                .padding(horizontal = 28.dp)
                .fillMaxWidth()
                .height(440.dp)
                .graphicsLayer {
                    this.rotationY = rotationY
                    this.cameraDistance = 16f * density
                }
                .clip(shape)
                .background(if (isDark) Color(0xF0181A24) else Color(0xF8FFFFFF))
                .border(
                    1.5.dp,
                    boxColor.copy(alpha = if (isDark) 0.45f else 0.35f),
                    shape
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { isFlipped = !isFlipped }
                )
        ) {
            // Ambient Bottom Glow
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Transparent,
                                boxColor.copy(alpha = if (isDark) 0.12f else 0.16f),
                                boxColor.copy(alpha = if (isDark) 0.35f else 0.45f)
                            )
                        )
                    )
            )

            // Front or Back content depending on rotation angle
            if (rotationY <= 90f) {
                // FRONT SIDE (English prompt)
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Header Box Info
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(boxColor.copy(alpha = 0.2f))
                                .border(1.dp, boxColor.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            GameText(
                                text = leitnerBox.titleFa,
                                size = 12.sp,
                                bold = true,
                                color = boxColor
                            )
                        }

                        // Audio pronunciation button on front side
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(boxColor.copy(alpha = 0.2f))
                                .border(1.dp, boxColor.copy(alpha = 0.6f), CircleShape)
                                .clickable { onPlayAudio(card) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_speaker),
                                contentDescription = "پخش تلفظ",
                                tint = if (isAudioPlaying) Color.White else boxColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        GameText(
                            text = "مرور شده ${card.reviewCount.fa()} بار",
                            size = 12.sp,
                            color = Game.TextSecondary
                        )
                    }

                    // Main Headword & Details
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        GameText(
                            text = card.headword.ifBlank { card.customWord.orEmpty() },
                            size = 28.sp,
                            bold = true,
                            color = Game.TextPrimary,
                            align = TextAlign.Center
                        )

                        val phonetic = card.phonetic
                        if (!phonetic.isNullOrBlank()) {
                            PhoneticText(
                                phonetic = phonetic,
                                size = 16.sp,
                                color = Game.TextSecondary,
                                align = TextAlign.Center
                            )
                        }

                        val pos = card.partOfSpeech
                        if (!pos.isNullOrBlank()) {
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(boxColor.copy(alpha = 0.18f))
                                    .padding(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                GameText(
                                    text = pos,
                                    size = 12.sp,
                                    bold = true,
                                    color = boxColor
                                )
                            }
                        }
                    }

                    // Bottom Flip Hint
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isDark) Color(0x22FFFFFF) else Color(0x14000000))
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        GameText(
                            text = "برای دیدن معنی و مثال لمس کنید 🔄",
                            size = 12.sp,
                            bold = true,
                            color = boxColor
                        )
                    }
                }
            } else {
                // BACK SIDE (Persian Meaning & Context)
                // Note: We reverse rotationY so text does not mirror
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer { this.rotationY = 180f }
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Top Row with Audio Speaker Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        GameText(
                            text = "پاسخ و ترجمه",
                            size = 13.sp,
                            bold = true,
                            color = boxColor
                        )

                        // Audio button
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(boxColor.copy(alpha = 0.2f))
                                .border(1.dp, boxColor.copy(alpha = 0.6f), CircleShape)
                                .clickable { onPlayAudio(card) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_speaker),
                                contentDescription = "پخش تلفظ",
                                tint = if (isAudioPlaying) Color.White else boxColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Persian Meaning Hero
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.padding(vertical = 12.dp)
                    ) {
                        GameText(
                            text = card.meaningFa.ifBlank { card.customMeaning.orEmpty() },
                            size = 22.sp,
                            bold = true,
                            color = Game.TextPrimary,
                            align = TextAlign.Center
                        )

                    }

                    // Example Sentence in Glass Container
                    val exampleEn = card.exampleEn ?: card.customExample
                    if (!exampleEn.isNullOrBlank()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isDark) Color(0x28000000) else Color(0x18000000))
                                .border(1.dp, Game.Stroke, RoundedCornerShape(16.dp))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                GameText(
                                    text = exampleEn,
                                    size = 13.sp,
                                    bold = true,
                                    color = Game.TextPrimary,
                                    latin = true,
                                    align = TextAlign.Left,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            val exampleFa = card.exampleFa
                            if (!exampleFa.isNullOrBlank()) {
                                GameText(
                                    text = exampleFa,
                                    size = 12.sp,
                                    color = Game.TextSecondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Bottom Return Hint
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isDark) Color(0x22FFFFFF) else Color(0x14000000))
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        GameText(
                            text = "لمس کنید تا به لغت برگردید 🔄",
                            size = 12.sp,
                            bold = true,
                            color = boxColor
                        )
                    }
                }
            }
        }
    }
}
