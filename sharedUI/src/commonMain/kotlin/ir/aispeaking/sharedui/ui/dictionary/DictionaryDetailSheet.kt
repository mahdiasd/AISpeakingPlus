package ir.aispeaking.sharedui.ui.dictionary

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.aispeaking.domain.model.dictionary.DictionaryEntry
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.ic_alert_circle
import ir.aispeaking.sharedui.ic_archive
import ir.aispeaking.sharedui.ic_close
import ir.aispeaking.sharedui.ic_done
import ir.aispeaking.sharedui.ic_pause
import ir.aispeaking.sharedui.ic_play
import ir.aispeaking.sharedui.ic_refresh
import ir.aispeaking.sharedui.ic_speaker
import ir.aispeaking.sharedui.ic_spell_check
import ir.aispeaking.sharedui.ui.game.Game
import ir.aispeaking.sharedui.ui.game.GameButton
import ir.aispeaking.sharedui.ui.game.GameButtonStyle
import ir.aispeaking.sharedui.ui.game.GameSheet
import ir.aispeaking.sharedui.ui.game.GameText
import ir.aispeaking.sharedui.ui.them.AppleTheme
import ir.aispeaking.sharedui.ui.them.AppTheme
import ir.aispeaking.sharedui.ui.them.applePressEffect
import org.jetbrains.compose.resources.painterResource

/**
 * 2026 Apple Liquid Glass Dictionary Detail Bottom Sheet.
 *
 * Renders structured dictionary lookup results:
 * - English headword, phonetic notation, and part of speech badge.
 * - Kokoro pronunciation audio playback with animated audio waves.
 * - Contextual Persian translation highlighted in Serene Cyan.
 * - Alternative meanings and bilingual example sentences.
 * - One-tap "+ افزودن به لایتنر" card addition with tactile checkmark feedback.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DictionaryDetailSheet(
    visible: Boolean,
    targetWord: String,
    entry: DictionaryEntry? = null,
    isLoading: Boolean = false,
    errorMessage: String? = null,
    isAudioPlaying: Boolean = false,
    isAddedToLeitner: Boolean = false,
    isAddingToLeitner: Boolean = false,
    onDismiss: () -> Unit,
    onRetry: () -> Unit = {},
    onPlayAudio: (url: String) -> Unit = {},
    onStopAudio: () -> Unit = {},
    onAddToLeitner: (entry: DictionaryEntry) -> Unit = {}
) {
    GameSheet(
        visible = visible,
        onDismiss = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
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
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(AppleTheme.PrimaryAzure.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_spell_check),
                            contentDescription = null,
                            tint = AppleTheme.PrimaryAzure,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        GameText(
                            text = "دیکشنری هوشمند",
                            size = 16.sp,
                            bold = true,
                            color = Game.TextPrimary
                        )
                        GameText(
                            text = "تحلیل واژه بر اساس بافت جمله",
                            size = 12.sp,
                            color = Game.TextSecondary
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .applePressEffect(targetScale = 0.92f, onClick = onDismiss)
                        .clip(CircleShape)
                        .background(AppleTheme.GlassPanelRaised)
                        .border(1.dp, AppleTheme.SubtleBorderColor, CircleShape),
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

            // Content Sections: Loading / Error / Loaded
            when {
                isLoading -> {
                    DictionaryLoadingState(targetWord = targetWord)
                }

                errorMessage != null -> {
                    DictionaryErrorState(
                        errorMessage = errorMessage,
                        onRetry = onRetry
                    )
                }

                entry != null -> {
                    DictionaryContentState(
                        entry = entry,
                        targetWord = targetWord,
                        isAudioPlaying = isAudioPlaying,
                        isAddedToLeitner = isAddedToLeitner,
                        isAddingToLeitner = isAddingToLeitner,
                        onPlayAudio = onPlayAudio,
                        onStopAudio = onStopAudio,
                        onAddToLeitner = onAddToLeitner
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun DictionaryLoadingState(targetWord: String) {
    val transition = rememberInfiniteTransition(label = "dict_loading_pulse")
    val alphaAnim by transition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "loading_pulse_alpha"
    )

    val shape = RoundedCornerShape(18.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(AppleTheme.GlassPanel)
            .border(1.dp, AppleTheme.SpecularStrokeBrush, shape)
            .padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(36.dp),
            color = AppleTheme.PrimaryAzure,
            strokeWidth = 3.dp
        )

        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            GameText(
                text = targetWord,
                size = 18.sp,
                bold = true,
                color = AppleTheme.PrimaryAzure,
                latin = true
            )
        }

        GameText(
            text = "در حال تحلیل معنای کلمه در متن توسط هوش مصنوعی...",
            size = 13.sp,
            color = Game.TextSecondary.copy(alpha = alphaAnim),
            align = TextAlign.Center
        )
    }
}

@Composable
private fun DictionaryErrorState(
    errorMessage: String,
    onRetry: () -> Unit
) {
    val shape = RoundedCornerShape(18.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(AppleTheme.Coral.copy(alpha = 0.08f))
            .border(1.dp, AppleTheme.Coral.copy(alpha = 0.4f), shape)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            painter = painterResource(Res.drawable.ic_alert_circle),
            contentDescription = null,
            tint = AppleTheme.Coral,
            modifier = Modifier.size(32.dp)
        )

        GameText(
            text = errorMessage,
            size = 13.sp,
            color = Game.TextPrimary,
            align = TextAlign.Center
        )

        GameButton(
            text = "تلاش مجدد",
            icon = Res.drawable.ic_refresh,
            style = GameButtonStyle.Danger,
            onClick = onRetry
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DictionaryContentState(
    entry: DictionaryEntry,
    targetWord: String = "",
    isAudioPlaying: Boolean,
    isAddedToLeitner: Boolean,
    isAddingToLeitner: Boolean,
    onPlayAudio: (url: String) -> Unit,
    onStopAudio: () -> Unit,
    onAddToLeitner: (entry: DictionaryEntry) -> Unit
) {
    val isDark = AppTheme.isDark
    val panelShape = RoundedCornerShape(18.dp)

    val displayHeadword = when {
        targetWord.isNotBlank() && (targetWord.contains(" ") || entry.headword.isBlank() || (entry.headword.length < targetWord.length && !entry.headword.contains(" "))) -> targetWord
        entry.headword.isNotBlank() -> entry.headword
        else -> targetWord
    }

    // 1. Headword, Phonetic & Pronunciation Audio Hero Card
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(panelShape)
            .background(AppleTheme.GlassPanelRaised)
            .border(1.dp, AppleTheme.SpecularStrokeBrush, panelShape)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            // Headword & Part of Speech (strictly bounded with weight(1f))
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                        Text(
                            text = displayHeadword,
                            fontSize = when {
                                displayHeadword.length > 35 -> 16.sp
                                displayHeadword.length > 20 -> 18.sp
                                else -> 22.sp
                            },
                            fontWeight = FontWeight.Bold,
                            color = Game.TextPrimary,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = if (displayHeadword.length > 20) 24.sp else 28.sp
                        )
                    }

                    val pos = entry.partOfSpeech
                    if (!pos.isNullOrBlank()) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterVertically)
                                .clip(RoundedCornerShape(8.dp))
                                .background(AppleTheme.PrimaryAzure.copy(alpha = 0.15f))
                                .border(0.8.dp, AppleTheme.PrimaryAzure.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 7.dp, vertical = 2.dp)
                        ) {
                            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                Text(
                                    text = pos,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AppleTheme.PrimaryAzure
                                )
                            }
                        }
                    }
                }

                // Phonetic notation with clean IPA sanitization and system sans-serif font
                val phonetic = entry.phonetic
                if (!phonetic.isNullOrBlank()) {
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                        Text(
                            text = cleanIpaPhonetic(phonetic),
                            fontSize = 13.sp,
                            color = Game.TextSecondary,
                            fontFamily = FontFamily.SansSerif
                        )
                    }
                }
            }

            // Audio Playback Button (Kokoro TTS)
            val audioUrl = entry.audioUrl
            if (!audioUrl.isNullOrBlank()) {
                val audioShape = RoundedCornerShape(50)
                Row(
                    modifier = Modifier
                        .applePressEffect(
                            targetScale = 0.94f,
                            onClick = {
                                if (isAudioPlaying) onStopAudio()
                                else onPlayAudio(audioUrl)
                            }
                        )
                        .clip(audioShape)
                        .background(if (isAudioPlaying) AppleTheme.PrimaryAzure.copy(alpha = 0.2f) else AppleTheme.GlassPanel)
                        .border(
                            if (isAudioPlaying) 1.5.dp else 1.dp,
                            if (isAudioPlaying) AppleTheme.PrimaryAzure else AppleTheme.SubtleBorderColor,
                            audioShape
                        )
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(
                        painter = painterResource(if (isAudioPlaying) Res.drawable.ic_pause else Res.drawable.ic_speaker),
                        contentDescription = "پخش تلفظ",
                        tint = AppleTheme.PrimaryAzure,
                        modifier = Modifier.size(15.dp)
                    )

                    if (isAudioPlaying) {
                        PronunciationEqualizerWaves()
                    } else {
                        GameText(
                            text = "تلفظ",
                            size = 12.sp,
                            color = AppleTheme.PrimaryAzure,
                            bold = true
                        )
                    }
                }
            }
        }
    }

    // 2. Contextual Meaning (Hero Highlight Card)
    val contextShape = RoundedCornerShape(16.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(contextShape)
            .background(AppleTheme.PrimaryAzure.copy(alpha = 0.10f))
            .border(1.dp, AppleTheme.PrimaryAzure.copy(alpha = 0.40f), contextShape)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(AppleTheme.SuccessCyan, CircleShape)
            )
            GameText(
                text = "معنی در این جمله:",
                size = 12.sp,
                bold = true,
                color = AppleTheme.SuccessCyan
            )
        }

        GameText(
            text = entry.contextMeaningFa,
            size = 17.sp,
            bold = true,
            color = Game.TextPrimary
        )
    }

    // 3. English Definition (if available)
    val defEn = entry.definitionEn
    if (!defEn.isNullOrBlank()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(panelShape)
                .background(AppleTheme.GlassPanel)
                .border(1.dp, AppleTheme.SubtleBorderColor, panelShape)
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            GameText(
                text = "تعریف انگلیسی:",
                size = 12.sp,
                color = Game.TextSecondary
            )
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                GameText(
                    text = defEn,
                    size = 13.sp,
                    lineHeight = 19.sp,
                    color = Game.TextPrimary,
                    latin = true
                )
            }
        }
    }

    // 4. Other Meanings (if available)
    if (entry.otherMeanings.isNotEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(panelShape)
                .background(AppleTheme.GlassPanel)
                .border(1.dp, AppleTheme.SubtleBorderColor, panelShape)
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            GameText(
                text = "سایر معانی:",
                size = 12.sp,
                color = Game.TextSecondary
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                entry.otherMeanings.forEach { meaning ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isDark) Color(0xEB232738) else Color(0xF2F1F5F9))
                            .border(0.8.dp, AppleTheme.SubtleBorderColor, RoundedCornerShape(8.dp))
                            .padding(horizontal = 9.dp, vertical = 4.dp)
                    ) {
                        GameText(
                            text = meaning,
                            size = 12.sp,
                            color = Game.TextPrimary
                        )
                    }
                }
            }
        }
    }

    // 5. Bilingual Example Sentence (if available)
    val exEn = entry.sampleSentenceEn
    val exFa = entry.sampleSentenceFa
    if (!exEn.isNullOrBlank()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(panelShape)
                .background(AppleTheme.GlassPanel)
                .border(1.dp, AppleTheme.SubtleBorderColor, panelShape)
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            GameText(
                text = "مثال کاربردی:",
                size = 12.sp,
                color = Game.TextSecondary
            )

            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                GameText(
                    text = exEn,
                    size = 13.sp,
                    lineHeight = 19.sp,
                    color = Game.TextPrimary,
                    bold = true,
                    latin = true
                )
            }

            if (!exFa.isNullOrBlank()) {
                GameText(
                    text = exFa,
                    size = 12.sp,
                    lineHeight = 18.sp,
                    color = Game.TextSecondary
                )
            }
        }
    }

    // 6. Action: Add to Leitner Button
    Box(modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
        if (isAddedToLeitner) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(AppleTheme.SuccessCyan.copy(alpha = 0.15f))
                    .border(1.dp, AppleTheme.SuccessCyan.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_done),
                    contentDescription = null,
                    tint = AppleTheme.SuccessCyan,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                GameText(
                    text = "به جعبه لایتنر افزوده شد",
                    size = 14.sp,
                    bold = true,
                    color = AppleTheme.SuccessCyan
                )
            }
        } else {
            GameButton(
                text = if (isAddingToLeitner) "در حال افزودن..." else "+ افزودن به لایتنر",
                icon = if (isAddingToLeitner) null else Res.drawable.ic_archive,
                loading = isAddingToLeitner,
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    if (!isAddingToLeitner) {
                        val effectiveEntry = if (displayHeadword.isNotBlank() && displayHeadword != entry.headword) {
                            entry.copy(headword = displayHeadword)
                        } else {
                            entry
                        }
                        onAddToLeitner(effectiveEntry)
                    }
                }
            )
        }
    }
}

/**
 * Sanitizes IPA phonetic transcriptions to prevent missing glyphs (tofu boxes '☒')
 * caused by fonts lacking modifier letters (like U+02C8 primary stress 'ˈ').
 */
internal fun cleanIpaPhonetic(raw: String): String {
    return raw
        .replace('\u02C8', '\'') // modifier letter vertical line (primary stress) -> standard apostrophe
        .replace('\u02CC', ',')  // modifier letter low vertical line (secondary stress) -> comma
        .replace('\u2019', '\'') // right single quotation mark
        .replace('\u2018', '\'') // left single quotation mark
        .replace("ˈ", "'")
        .replace("ˌ", ",")
        .trim()
}

@Composable
private fun PronunciationEqualizerWaves() {
    val transition = rememberInfiniteTransition(label = "dict_audio_eq")
    val bar1 by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(380, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dict_b1"
    )
    val bar2 by transition.animateFloat(
        initialValue = 0.85f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(320, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dict_b2"
    )
    val bar3 by transition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(420, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dict_b3"
    )

    Row(
        modifier = Modifier.height(13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        listOf(bar1, bar2, bar3, bar1).forEach { bVal ->
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height((3 + 10 * bVal).dp)
                    .background(
                        AppleTheme.PrimaryAzure,
                        RoundedCornerShape(1.dp)
                    )
            )
        }
    }
}
