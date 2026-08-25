package ir.aispeaking.chat.component

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import ir.aispeaking.chat.OnAction
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.ui.core.text.BodyMediumText
import ir.aispeaking.sharedui.ui.them.AppTheme
import ir.aispeaking.sharedui.you_should_start_conversation
import org.jetbrains.compose.resources.stringResource

@Composable
fun UserStarterContent(onAction: OnAction) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp, alignment = Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier

    ) {
        BodyMediumText(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
                .border(1.dp, color = AppTheme.colors.outlineVariant, shape = AppTheme.shapes.roundMedium)
                .padding(16.dp),
            text = stringResource(Res.string.you_should_start_conversation),
            color = AppTheme.colors.outline,
            textAlign = TextAlign.Center
        )

        // TODO LottieLoader
//        LottieLoader(
//            Modifier
//                .size(80.dp),
//            anim = R.raw.lt_arrow_anim,
//            color = AppTheme.colors.primary
//        )

    }
}

@PreviewLightDark
@Composable
private fun ChatPreview() {
    AppTheme {
        UserStarterContent(
            onAction = {},
        )
    }
}

