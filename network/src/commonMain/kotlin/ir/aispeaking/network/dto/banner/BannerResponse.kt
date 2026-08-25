package ir.aispeaking.network.dto.banner

import kotlinx.serialization.Serializable

@Serializable
data class BannerResponse(
    val bannerLink: String,
    val clickableLink: String,
    val linkType: String,
)
