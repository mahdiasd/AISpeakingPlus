package ir.aispeaking.sharedui.ui.core.divider

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ir.aispeaking.sharedui.ui.core.text.BodyMediumText
import ir.aispeaking.sharedui.ui.them.AppTheme
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun TextDivider(
    modifier: Modifier = Modifier.fillMaxWidth(),
     textRes: StringResource,
) {
    Row(
        modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        CustomSpacer(
            Modifier
                .rotate(180f)
                .weight(1f)
        )

        BodyMediumText(
            modifier = Modifier
                .weight(2f)
                .padding(horizontal = 16.dp)
                .wrapContentWidth(unbounded = true),
            text = stringResource(textRes),
            color = AppTheme.colors.onSurface
        )

        CustomSpacer(Modifier.weight(1f))

    }

}

@Composable
internal fun CustomSpacer(
    modifier: Modifier
) {
    Spacer(
        modifier = modifier
            .fillMaxWidth()
            .height(1.5.dp)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.Black,
                        Color.Black.copy(alpha = 0.5f),
                        Color.Black.copy(alpha = 0.2f),
                        Color.White.copy(alpha = 0.2f),
                        Color.White.copy(alpha = 0.5f),
                        Color.White,
                    )
                ),
                shape = AppTheme.shapes.roundMedium
            )

    )
}