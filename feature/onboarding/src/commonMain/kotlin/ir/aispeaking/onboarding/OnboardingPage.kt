package ir.aispeaking.onboarding

import androidx.compose.ui.graphics.Color
import ir.aispeaking.sharedui.*
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

sealed class OnboardingPage(
    val containerColor : Color,
    val image: DrawableResource,
    val title: StringResource,
    val body: StringResource,
    val footer: StringResource
) {
    data object First : OnboardingPage(
        containerColor = Color(0xFF2B2D3A),
        image = Res.drawable.vector_onboarding_1,
        title = Res.string.onboarding_title_1,
        body = Res.string.onboarding_body_1,
        footer = Res.string.onboarding_footer_1
    )

    data object Second : OnboardingPage(
        containerColor = Color(0xFF115368),
        image = Res.drawable.vector_onboarding_2,
        title = Res.string.onboarding_title_2,
        body = Res.string.onboarding_body_2,
        footer = Res.string.onboarding_footer_2
    )

    data object Third : OnboardingPage(
        containerColor = Color(0xFF2B5D62),
        image = Res.drawable.vector_onboarding_3,
        title = Res.string.onboarding_title_3,
        body = Res.string.onboarding_body_3,
        footer = Res.string.onboarding_footer_3
    )

    companion object {
        val pages = listOf(First, Second, Third)
    }
}
