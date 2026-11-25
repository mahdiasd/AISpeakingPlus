package ir.aispeaking.sharedui.ui.core.text

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.noto_sans_regular
import ir.aispeaking.sharedui.ui.game.Game
import org.jetbrains.compose.resources.Font

/**
 * Sanitizes IPA phonetic transcriptions to prevent missing glyphs (tofu boxes '☒')
 * in system and bundled fonts that do not carry specialized phonetic punctuation or characters.
 *
 * Normalizes stress marks, quotation marks, and non-standard IPA glyphs:
 * - Primary stress 'ˈ' (U+02C8) -> '\''
 * - Secondary stress 'ˌ' (U+02CC) -> ','
 * - IPA script g 'ɡ' (U+0261) -> 'g'
 * - Length mark 'ː' (U+02D0) -> ':'
 * - Smart quotation marks -> '\''
 * - Enforces surrounding slashes (/.../)
 */
fun cleanIpaPhonetic(raw: String): String {
    if (raw.isBlank()) return ""
    val trimmed = raw.trim()
    val cleaned = trimmed
        .replace('\u02C8', '\'') // modifier letter vertical line (primary stress)
        .replace('\u02CC', ',')  // modifier letter low vertical line (secondary stress)
        .replace('\u2019', '\'') // right single quotation mark
        .replace('\u2018', '\'') // left single quotation mark
        .replace("ˈ", "'")
        .replace("ˌ", ",")
        .replace('ɡ', 'g')       // IPA script g (\u0261) -> ASCII g
        .replace('\u02D0', ':')  // modifier letter triangular colon -> colon
        .replace("ː", ":")
        .replace(Regex("\\s+"), " ")
        .trim()

    val withLeadingSlash = if (cleaned.startsWith("/")) cleaned else "/$cleaned"
    return if (withLeadingSlash.endsWith("/")) withLeadingSlash else "$withLeadingSlash/"
}

/**
 * Renders an English phonetic transcription with:
 * 1. Guaranteed LTR layout direction (preventing BiDi reversed slash/apostrophe bugs in RTL apps)
 * 2. System Sans-Serif font family (which contains full Unicode IPA support across Android, iOS, Web, and Desktop)
 * 3. Sanitized phonetic characters to eliminate missing glyphs and tofu boxes ('☒')
 */
@Composable
fun PhoneticText(
    phonetic: String,
    modifier: Modifier = Modifier,
    size: TextUnit = 13.sp,
    color: Color = Game.TextSecondary,
    fontWeight: FontWeight = FontWeight.Normal,
    align: TextAlign = TextAlign.Center,
    maxLines: Int = 1,
    overflow: TextOverflow = TextOverflow.Ellipsis
) {
    if (phonetic.isBlank()) return

    val ipaFont = Font(Res.font.noto_sans_regular, FontWeight.Normal)
    val ipaFontFamily = remember(ipaFont) { FontFamily(ipaFont) }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Text(
            text = cleanIpaPhonetic(phonetic),
            modifier = modifier,
            fontSize = size,
            color = color,
            fontFamily = ipaFontFamily,
            fontWeight = fontWeight,
            textAlign = align,
            maxLines = maxLines,
            overflow = overflow
        )
    }
}
