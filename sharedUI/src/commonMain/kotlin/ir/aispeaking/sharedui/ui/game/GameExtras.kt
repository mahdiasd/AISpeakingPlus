package ir.aispeaking.sharedui.ui.game

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage

/** Converts Latin digits to Persian digits for user-facing Persian copy. */
fun String.toFaDigits(): String =
    map { if (it in '0'..'9') '۰' + (it - '0') else it }.joinToString("")

fun Int.fa(): String = toString().toFaDigits()

/**
 * Character / user avatar. Shows an initial on a gradient disc and paints the image on top when
 * one is available, so a missing or slow image never leaves an empty circle.
 */
@Composable
fun GameAvatar(
    name: String,
    imageUrl: String?,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    ring: Color = Game.StrokeStrong,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(Brush.linearGradient(listOf(Game.Violet, Game.VioletDeep)))
            .border(1.5.dp, ring, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        GameText(
            text = name.trim().split(" ").lastOrNull { it.isNotBlank() }?.first()?.uppercase() ?: "A",
            size = (size.value * 0.42f).sp,
            bold = true,
            latin = true,
            color = Color.White,
        )
        if (!imageUrl.isNullOrBlank()) {
            AsyncImage(
                model = imageUrl,
                contentDescription = name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(size),
            )
        }
    }
}
