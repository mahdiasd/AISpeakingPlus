package ir.aispeaking.onboarding.component

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ir.aispeaking.onboarding.OnAction
import ir.aispeaking.onboarding.OnboardingPage
import ir.aispeaking.onboarding.OnboardingUiEvent
import ir.aispeaking.sharedui.ui.core.icon.AppIcon
import ir.aispeaking.sharedui.ui.them.AppTheme
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import ir.aispeaking.onboarding.Res
import ir.aispeaking.onboarding.ic_back

@Composable
fun OnboardingBottomBar(
    modifier: Modifier,
    onboardingPages: ImmutableList<OnboardingPage>,
    selectedPage: OnboardingPage,
    onAction: OnAction,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {

        AppIcon(
            modifier = Modifier
                .border(width = 1.dp, color = Color.White, shape = CircleShape)
                .padding(top = 12.dp , bottom = 12.dp , start = 8.dp , end = 12.dp),
            size = 48.dp,
            icon = Res.drawable.ic_back,
            tint = Color.White,
            onClick = {
                val currentPage = onboardingPages.indexOf(selectedPage)
                if (currentPage > 0) {
                    onAction(OnboardingUiEvent.OnChangePage(onboardingPages[currentPage - 1]))
                }
            }
        )

        AppIcon(
            modifier = Modifier
                .rotate(180f)
                .border(width = 1.dp, color = Color.White, shape = CircleShape)
                .padding(top = 12.dp , bottom = 12.dp , start = 8.dp , end = 12.dp),
            size = 48.dp,
            icon = Res.drawable.ic_back,
            tint = Color.White,
            onClick = {
                val currentPage = onboardingPages.indexOf(selectedPage)
                if (currentPage < onboardingPages.size - 1) {
                    onAction(OnboardingUiEvent.OnChangePage(onboardingPages[currentPage + 1]))
                } else {
                    onAction(OnboardingUiEvent.OnNavigateToMain)
                }
            }
        )
    }
}