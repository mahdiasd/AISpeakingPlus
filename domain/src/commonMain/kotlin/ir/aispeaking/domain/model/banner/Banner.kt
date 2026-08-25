package ir.aispeaking.domain.model.banner

import kotlinx.serialization.Serializable

@Serializable
data class Banner(
    val bannerLink: String,
    val clickableLink: String,
    val linkType: LinkType,
)

@Serializable
enum class LinkType {
    InternalLink,
    ExternalLink,
}
