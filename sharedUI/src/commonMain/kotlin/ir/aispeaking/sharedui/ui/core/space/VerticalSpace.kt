package ir.aispeaking.sharedui.ui.core.space

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun VerticalSpace(
    space: Dp = 16.dp
) {
    Spacer(modifier = Modifier.height(space))
}

@Composable
fun HorizontalSpace(
    space: Dp = 16.dp
) {
    Spacer(modifier = Modifier.width(space))
}