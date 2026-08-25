package ir.aispeaking.onboarding.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import ir.aispeaking.onboarding.OnAction
import ir.aispeaking.onboarding.OnboardingPage
import ir.aispeaking.sharedui.ui.core.text.BodyMediumText
import ir.aispeaking.sharedui.ui.core.text.TitleBoldText
import ir.aispeaking.sharedui.ui.them.AppTheme
import ir.aispeaking.sharedui.ui.them.OutlineVariantLight
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun OnboardingItem(
    modifier: Modifier,
    item: OnboardingPage,
    onAction: OnAction
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(
            space = 8.dp,
            alignment = Alignment.CenterVertically
        )
    ) {
        Image(
            modifier = Modifier
//                .background(color = Color(0xff3E4052) , shape = CircleShape)
                .fillMaxWidth()
                .aspectRatio(1f)
                .padding(horizontal = 32.dp),
            contentScale = ContentScale.Fit,
            painter = painterResource(item.image),
            contentDescription = null
        )

        TitleBoldText(text = stringResource(item.title), color = Color.White)

        BodyMediumText(text = stringResource(item.body), color = OutlineVariantLight)

        BodyMediumText(text = stringResource(item.footer), color = OutlineVariantLight)
    }

}


@Composable
private fun Preview() {
    AppTheme {
    }
}