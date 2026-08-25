package ir.aispeaking.sharedui.ui.core.app_box

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AppBox(
    modifier: Modifier = Modifier,
    horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(16.dp, alignment = Alignment.Top),
    showBlur: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 420.dp)
                .fillMaxWidth(0.8f)
                .fillMaxHeight()
                .padding(vertical = 16.dp)
//                .then(
//                    if (!showBlur) {
//                        background(AppTheme.colors.onPrimary, shape = AppTheme.shapes.roundExtraLarge)
//                    } else {
//                        Modifier
//                            .clip(shape = AppTheme.shapes.roundExtraLarge)
//                            .paint(painter = painterResource(Res.drawable.ic_close), contentScale = ContentScale.FillBounds)
//                    }
//                )
                .padding(vertical = 16.dp, horizontal = 8.dp),
            horizontalAlignment = horizontalAlignment,
            verticalArrangement = verticalArrangement,
            content = content
        )
    }

}

