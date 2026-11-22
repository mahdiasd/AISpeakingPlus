package ir.aispeaking.leitner.dialog

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import ir.aispeaking.leitner.component.getBoxAccentColor
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.ic_close
import ir.aispeaking.sharedui.ic_done
import ir.aispeaking.sharedui.ic_speaker
import ir.aispeaking.sharedui.ui.core.text.PhoneticText
import ir.aispeaking.sharedui.ui.game.Game
import ir.aispeaking.sharedui.ui.game.GameButton
import ir.aispeaking.sharedui.ui.game.GameButtonStyle
import ir.aispeaking.sharedui.ui.game.GameIconButton
import ir.aispeaking.sharedui.ui.game.GameText
import ir.aispeaking.sharedui.ui.game.fa
import ir.aispeaking.sharedui.ui.them.AppTheme
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import kotlin.math.absoluteValue

@Composable
fun LeitnerReviewDialog(
    reviewQueue: List<Flashcard>,
    currentIndex: Int,
    promotedCount: Int,
    demotedCount: Int,
    isFinished: Boolean,
    onReviewCard: (Flashcard, Boolean) -> Unit,
    onDismiss: () -> Unit,
    onPlayAudio: (Flashcard) -> Unit,
    isAudioPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    val isDark = AppTheme.isDark
    val scope = rememberCoroutineScope()

    if (reviewQueue.isEmpty()) return

    val pagerState = rememberPagerState(
        initialPage = currentIndex.coerceIn(0, (reviewQueue.size - 1).coerceAtLeast(0)),
        pageCount = { reviewQueue.size }
    )

    // Sync pager position with currentIndex
    LaunchedEffect(currentIndex) {
        if (currentIndex < reviewQueue.size && pagerState.currentPage != currentIndex) {
            pagerState.animateScrollToPage(currentIndex)
        }
    }

    val currentCard = reviewQueue.getOrNull(pagerState.currentPage) ?: reviewQueue.first()
    val ambientColor = getBoxAccentColor(currentCard.boxNumber)

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(if (isDark) Color(0xFF0B0F17) else Color(0xFFF8F9FC))
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Ambient Neon Bottom Glow (Mirroring SubscriptionScreen)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.60f)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                ambientColor.copy(alpha = if (isDark) 0.10f else 0.15f),
                                ambientColor.copy(alpha = if (isDark) 0.28f else 0.40f)
                            )
                        )
                    )
            )

            if (isFinished) {
                // Celebration Completion View
                ReviewCompletionView(
                    promotedCount = promotedCount,
                    demotedCount = demotedCount,
                    totalCount = reviewQueue.size,
                    onDismiss = onDismiss
                )
            } else {
                // Active Review Carousel
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Top Progress & Header Bar
                    ReviewTopBar(
                        currentIndex = pagerState.currentPage,
                        totalCount = reviewQueue.size,
                        currentCard = currentCard,
                        onDismiss = onDismiss
                    )

                    // 3D Coverflow HorizontalPager
                    HorizontalPager(
                        state = pagerState,
                        contentPadding = PaddingValues(horizontal = 48.dp),
                        pageSpacing = 16.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) { page ->
                        val card = reviewQueue[page]

                        // 3D Coverflow graphicsLayer calculations (identical to SubscriptionScreen)
                        val pageOffset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction)
                        val absOffset = pageOffset.absoluteValue.coerceIn(0f, 1.5f)

                        val scale = 1f - (absOffset * 0.08f)
                        val alpha = 1f - (absOffset * 0.18f).coerceIn(0f, 0.28f)
                        val rotationY = pageOffset * 7.5f

                        Review3DFlipCard(
                            card = card,
                            onPlayAudio = { onPlayAudio(card) },
                            isAudioPlaying = isAudioPlaying,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(460.dp)
                                .graphicsLayer {
                                    this.scaleX = scale
                                    this.scaleY = scale
                                    this.alpha = alpha
                                    this.rotationY = rotationY
                                }
                        )
                    }

                    // Bottom Action Buttons: [ ❌ بلد نبودم ] and [ ✅ بلدم ]
                    ReviewActionButtons(
                        onNotRemembered = {
                            onReviewCard(currentCard, false)
                        },
                        onRemembered = {
                            onReviewCard(currentCard, true)
                        },
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ReviewTopBar(
    currentIndex: Int,
    totalCount: Int,
    currentCard: Flashcard,
    onDismiss: () -> Unit
) {
    val boxColor = getBoxAccentColor(currentCard.boxNumber)
    val progressPercent = ((currentIndex + 1).toFloat() / totalCount.toFloat()).coerceIn(0f, 1f)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Close Button
            GameIconButton(
                icon = Res.drawable.ic_close,
                onClick = onDismiss,
                contentDescription = "بستن",
                size = 38.dp,
                iconSize = 16.dp
            )

            // Current Step Pill
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(boxColor.copy(alpha = 0.2f))
                        .border(1.dp, boxColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    GameText(
                        text = "جعبه ${currentCard.boxNumber.fa()}",
                        size = 11.sp,
                        bold = true,
                        color = boxColor
                    )
                }

                GameText(
                    text = "کارت ${(currentIndex + 1).fa()} از ${totalCount.fa()}",
                    size = 13.sp,
                    bold = true,
                    color = Game.TextPrimary
                )
            }

            // Symmetrical Spacer
            Spacer(modifier = Modifier.size(38.dp))
        }

        // Sleek Linear Progress Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(CircleShape)
                .background(if (AppTheme.isDark) Color(0x28FFFFFF) else Color(0x1A000000))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progressPercent)
                    .fillMaxHeight()
                    .clip(CircleShape)
                    .background(boxColor)
            )
        }
    }
}

@Composable
private fun Review3DFlipCard(
    card: Flashcard,
    onPlayAudio: () -> Unit,
    isAudioPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    val isDark = AppTheme.isDark
    val density = LocalDensity.current.density
    var isFlipped by remember { mutableStateOf(false) }

    val rotationY by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = spring(
            dampingRatio = 0.82f,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "coverflow_card_flip"
    )

    val boxColor = getBoxAccentColor(card.boxNumber)
    val shape = RoundedCornerShape(26.dp)

    Box(
        modifier = modifier
            .graphicsLayer {
                this.rotationY = rotationY
                this.cameraDistance = 16f * density
            }
            .clip(shape)
            .background(if (isDark) Color(0xF2181B26) else Color(0xF8FFFFFF))
            .border(
                1.5.dp,
                if (isDark) Color.White.copy(alpha = 0.16f) else Color.Black.copy(alpha = 0.09f),
                shape
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { isFlipped = !isFlipped }
            )
    ) {
        if (rotationY <= 90f) {
            // FRONT SIDE
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(26.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Row: Tag + Audio Pronunciation Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(if (isDark) Color(0x24FFFFFF) else Color(0x14000000))
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        GameText(
                            text = "صورت کارت",
                            size = 11.sp,
                            color = Game.TextSecondary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(boxColor.copy(alpha = 0.22f))
                            .border(1.dp, boxColor.copy(alpha = 0.6f), CircleShape)
                            .clickable(onClick = onPlayAudio),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_speaker),
                            contentDescription = "پخش تلفظ",
                            tint = if (isAudioPlaying) Color.White else boxColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // English Word & Phonetics
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GameText(
                        text = card.headword.ifBlank { card.customWord.orEmpty() },
                        size = 30.sp,
                        bold = true,
                        color = Game.TextPrimary,
                        align = TextAlign.Center
                    )

                    val phonetic = card.phonetic
                    if (!phonetic.isNullOrBlank()) {
                        PhoneticText(
                            phonetic = phonetic,
                            size = 16.sp,
                            color = boxColor,
                            align = TextAlign.Center
                        )
                    }

                    val pos = card.partOfSpeech
                    if (!pos.isNullOrBlank()) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(boxColor.copy(alpha = 0.16f))
                                .padding(horizontal = 12.dp, vertical = 3.dp)
                        ) {
                            GameText(
                                text = pos,
                                size = 11.sp,
                                bold = true,
                                color = boxColor
                            )
                        }
                    }
                }

                // Tap Hint
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isDark) Color(0x22FFFFFF) else Color(0x12000000))
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    GameText(
                        text = "برای دیدن پاسخ ضربه بزنید 👆",
                        size = 12.sp,
                        bold = true,
                        color = boxColor
                    )
                }
            }
        } else {
            // BACK SIDE (Mirrored rotation back)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { this.rotationY = 180f }
                    .padding(26.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Audio Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(boxColor.copy(alpha = 0.18f))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        GameText(
                            text = "پاسخ کارت",
                            size = 11.sp,
                            bold = true,
                            color = boxColor
                        )
                    }

                    // Pronunciation Audio Button
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(boxColor.copy(alpha = 0.22f))
                            .border(1.dp, boxColor.copy(alpha = 0.6f), CircleShape)
                            .clickable(onClick = onPlayAudio),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_speaker),
                            contentDescription = "پخش تلفظ",
                            tint = if (isAudioPlaying) Color.White else boxColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Meaning & Definition
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(vertical = 12.dp)
                ) {
                    GameText(
                        text = card.meaningFa.ifBlank { card.customMeaning.orEmpty() },
                        size = 24.sp,
                        bold = true,
                        color = Game.TextPrimary,
                        align = TextAlign.Center
                    )
                }

                // Contextual Example
                val example = card.exampleEn ?: card.customExample
                if (!example.isNullOrBlank()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isDark) Color(0x28000000) else Color(0x14000000))
                            .border(1.dp, Game.Stroke, RoundedCornerShape(14.dp))
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        GameText(
                            text = example,
                            size = 12.sp,
                            bold = true,
                            color = Game.TextPrimary
                        )
                        val exampleFa = card.exampleFa
                        if (!exampleFa.isNullOrBlank()) {
                            GameText(
                                text = exampleFa,
                                size = 11.sp,
                                color = Game.TextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                GameText(
                    text = "ضربه بزنید تا به صورت کارت برگردید 🔄",
                    size = 11.sp,
                    color = Game.TextMuted,
                    align = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun ReviewActionButtons(
    onNotRemembered: () -> Unit,
    onRemembered: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Red / Coral Button: "❌ بلد نبودم" (Step down penalty)
        GameButton(
            text = "❌ بلد نبودم",
            onClick = onNotRemembered,
            style = GameButtonStyle.Danger,
            modifier = Modifier.weight(1f),
            height = 54.dp,
            textSize = 15.sp
        )

        // Green / Mint Button: "✅ بلدم" (Advance to next box)
        GameButton(
            text = "✅ بلدم",
            onClick = onRemembered,
            style = GameButtonStyle.Primary,
            modifier = Modifier.weight(1f),
            height = 54.dp,
            textSize = 15.sp
        )
    }
}

@Composable
private fun ReviewCompletionView(
    promotedCount: Int,
    demotedCount: Int,
    totalCount: Int,
    onDismiss: () -> Unit
) {
    val isDark = AppTheme.isDark

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(32.dp))
                .background(if (isDark) Color(0xF21C1F2E) else Color(0xF8FFFFFF))
                .border(1.5.dp, Color(0xFF22C55E).copy(alpha = 0.4f), RoundedCornerShape(32.dp))
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Trophy / Party Icon
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF22C55E).copy(alpha = 0.2f))
                    .border(1.5.dp, Color(0xFF22C55E), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                GameText(
                    text = "🎉",
                    size = 34.sp
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                GameText(
                    text = "آفرین! مرور امروز تکمیل شد",
                    size = 20.sp,
                    bold = true,
                    color = Game.TextPrimary,
                    align = TextAlign.Center
                )

                GameText(
                    text = "همه ${totalCount.fa()} کارت نوبت امروز رو با موفقیت مرور کردی.",
                    size = 13.sp,
                    color = Game.TextSecondary,
                    align = TextAlign.Center
                )
            }

            // Results Summary Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Promoted Box
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF22C55E).copy(alpha = 0.14f))
                        .border(1.dp, Color(0xFF22C55E).copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    GameText(
                        text = "+${promotedCount.fa()}",
                        size = 22.sp,
                        bold = true,
                        color = Color(0xFF22C55E)
                    )
                    GameText(
                        text = "ارتقا به جعبه بعد",
                        size = 11.sp,
                        color = Game.TextPrimary
                    )
                }

                // Demoted Box
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFFF453A).copy(alpha = 0.14f))
                        .border(1.dp, Color(0xFFFF453A).copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    GameText(
                        text = demotedCount.fa(),
                        size = 22.sp,
                        bold = true,
                        color = Color(0xFFFF453A)
                    )
                    GameText(
                        text = "نیاز به تمرین بیشتر",
                        size = 11.sp,
                        color = Game.TextPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Done Button
            GameButton(
                text = "بازگشت به جعبه لایتنر",
                onClick = onDismiss,
                style = GameButtonStyle.Primary,
                modifier = Modifier.fillMaxWidth(),
                height = 50.dp
            )
        }
    }
}
