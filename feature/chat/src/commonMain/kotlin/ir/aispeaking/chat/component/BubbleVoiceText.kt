package ir.aispeaking.chat.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.ic_clear
import ir.aispeaking.sharedui.ic_edit
import ir.aispeaking.sharedui.ui.core.icon.AppIcon
import ir.aispeaking.sharedui.ui.extension.animateClickable
import ir.aispeaking.sharedui.ui.them.AppTheme
import ir.aispeaking.sharedui.utils.shape.TooltipShape

@Composable
fun BubbleVoiceText(
    modifier: Modifier = Modifier,
    text: String,
    onEditClick: () -> Unit,
    onClearClick: () -> Unit
) {
    val shape = remember {
        TooltipShape(
            arrowWidth = 36.dp,
            arrowHeight = 16.dp,
            cornerRadius = 16.dp
        )
    }

    Column(
        modifier = modifier
            .background(
                color = AppTheme.colors.surfaceContainerLow.copy(alpha = 0.95f),
                shape = shape
            )
            .border(
                width = 1.dp,
                color = AppTheme.colors.outlineVariant,
                shape = shape
            )
            .padding(12.dp)
            .padding(bottom = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp, alignment = Alignment.CenterVertically)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Edit pencil button
            AppIcon(
                modifier = Modifier
                    .background(AppTheme.colors.primary.copy(alpha = 0.15f), CircleShape)
                    .padding(6.dp),
                icon = Res.drawable.ic_edit,
                size = 20.dp,
                tint = AppTheme.colors.primary,
                contentDescription = "Edit text",
                onClick = onEditClick
            )

            // Clear button
            AppIcon(
                modifier = Modifier
                    .background(AppTheme.colors.surfaceContainerHighest, CircleShape)
                    .padding(6.dp),
                icon = Res.drawable.ic_clear,
                size = 18.dp,
                tint = AppTheme.colors.onSurface,
                contentDescription = "Clear text",
                onClick = onClearClick
            )
        }

        Text(
            text = text,
            color = AppTheme.colors.onSurface,
            fontSize = 15.sp,
            lineHeight = 22.sp,
            style = AppTheme.typography.bodyMedium.copy(
                textDirection = TextDirection.Ltr
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
                .animateClickable { onEditClick() }
        )
    }
}

/* --------------------------------- Previews --------------------------------- */

@androidx.compose.ui.tooling.preview.Preview
@Composable
private fun BubbleVoiceTextPreview() {
    AppTheme {
        BubbleVoiceText(
            text = "I'm looking for a table for two.",
            onEditClick = {},
            onClearClick = {}
        )
    }
}
