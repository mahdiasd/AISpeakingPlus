package ir.aispeaking.sharedui.ui.core.button

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.ic_back
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

/**
 * Standard circular top-bar icon button matching the profile container styling.
 */
@Composable
fun AppTopBarIconButton(
    icon: DrawableResource,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    tint: Color = Color.White,
    containerSize: Dp = 38.dp,
    iconSize: Dp = 18.dp,
    backgroundColor: Color = Color(0xCC0F172A),
    borderColor: Color = Color(0x446366F1)
) {
    Box(
        modifier = modifier
            .size(containerSize)
            .clip(CircleShape)
            .background(backgroundColor)
            .border(
                width = 1.5.dp,
                color = borderColor,
                shape = CircleShape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(iconSize)
        )
    }
}

/**
 * Reusable Back Button with uniform container and right-pointing chevron for RTL.
 */
@Composable
fun AppBackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = Color.White,
    containerSize: Dp = 38.dp,
    iconSize: Dp = 18.dp,
    backgroundColor: Color = Color(0xCC0F172A),
    borderColor: Color = Color(0x446366F1)
) {
    AppTopBarIconButton(
        icon = Res.drawable.ic_back,
        onClick = onClick,
        contentDescription = "بازگشت",
        tint = tint,
        containerSize = containerSize,
        iconSize = iconSize,
        backgroundColor = backgroundColor,
        borderColor = borderColor,
        modifier = modifier
    )
}
