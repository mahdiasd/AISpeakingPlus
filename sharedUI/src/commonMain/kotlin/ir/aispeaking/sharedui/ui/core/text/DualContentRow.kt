package ir.aispeaking.sharedui.ui.core.text

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ir.aispeaking.sharedui.ui.core.icon.AppIcon
import org.jetbrains.compose.resources.DrawableResource

enum class AppPosition {
    Right,
    Left
}

@Composable
fun DualContentRow(
    modifier: Modifier = Modifier,
    leftContent: @Composable () -> Unit,
    rightContent: @Composable () -> Unit,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(8.dp, alignment = Alignment.CenterHorizontally),
) {
    Row(
        modifier = modifier,
        verticalAlignment = verticalAlignment,
        horizontalArrangement = horizontalArrangement
    ) {
        leftContent()
        rightContent()
    }
}

@Composable
fun IconableText(
    modifier: Modifier = Modifier,
    text: String,
    
    icon: DrawableResource,
    iconSize: Dp = 16.dp,
    iconTint: Color? = null,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(8.dp, alignment = Alignment.CenterHorizontally),
    iconPosition: AppPosition = AppPosition.Left
) {
    Row(
        modifier = modifier,
        verticalAlignment = verticalAlignment,
        horizontalArrangement = horizontalArrangement
    ) {
        when (iconPosition) {
            AppPosition.Right -> {
                DualContentRow(
                    leftContent = { BodyMediumText(text = text) },
                    rightContent = { AppIcon(icon = icon, size = iconSize, tint = iconTint) },
                    verticalAlignment = verticalAlignment,
                    horizontalArrangement = horizontalArrangement
                )
            }

            AppPosition.Left -> {
                DualContentRow(
                    rightContent = { BodyMediumText(text = text) },
                    leftContent = { AppIcon(icon = icon, size = iconSize, tint = iconTint) },
                    verticalAlignment = verticalAlignment,
                    horizontalArrangement = horizontalArrangement
                )
            }
        }

    }
}