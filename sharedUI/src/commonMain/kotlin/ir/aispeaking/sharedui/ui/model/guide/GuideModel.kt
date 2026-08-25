package ir.aispeaking.sharedui.ui.model.guide

import ir.aispeaking.sharedui.*
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import org.jetbrains.compose.resources.DrawableResource

sealed class GuideModel( open val images: ImmutableList<DrawableResource>) {
    data class RegisterGuideModel(
         override val images: ImmutableList<DrawableResource> = listOf(
            Res.drawable.guide_register,
        ).toImmutableList(),
    ) : GuideModel(images)

    data class ChallengeGuideModel(
         override val images: ImmutableList<DrawableResource> = listOf(
            Res.drawable.guide_challenge_1,
            Res.drawable.guide_challenge_2,
        ).toImmutableList(),
    ) : GuideModel(images)

    data class LightenerGuideModel(
         override val images: ImmutableList<DrawableResource> = listOf(
            Res.drawable.guide_lightener,
        ).toImmutableList(),
    ) : GuideModel(images)

    data class ScenarioGuideModel(
         override val images: ImmutableList<DrawableResource> = listOf(
            Res.drawable.guide_scenario_1,
            Res.drawable.guide_scenario_2,
            Res.drawable.guide_scenario_3,
        ).toImmutableList(),
    ) : GuideModel(images)

    data class RoadmapGuideModel(
         override val images: ImmutableList<DrawableResource> = listOf(
            Res.drawable.guide_roadmap,
        ).toImmutableList(),
    ) : GuideModel(images)

    data class ChatGuideModel(
         override val images: ImmutableList<DrawableResource> = listOf(
            Res.drawable.guide_chat_1,
            Res.drawable.guide_chat_2,
            Res.drawable.guide_chat_3,
            Res.drawable.guide_chat_4,
            Res.drawable.guide_chat_5,
            Res.drawable.guide_chat_6,
        ).toImmutableList(),
    ) : GuideModel(images)

    data class ProfileGuideModel(
         override val images: ImmutableList<DrawableResource> = listOf(
            Res.drawable.guide_profile_1,
            Res.drawable.guide_profile_2,
        ).toImmutableList(),
    ) : GuideModel(images)

    companion object {
        fun getAll(): ImmutableList<GuideModel> =
            listOf(
                ScenarioGuideModel(),
                ChallengeGuideModel(),
                LightenerGuideModel(),
                RoadmapGuideModel(),
                ChatGuideModel(),
                ProfileGuideModel(),
                RegisterGuideModel(),
            ).toImmutableList()
    }
}

